package com.travisyim.mountaineers.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.adapters.ActivityAdapter;
import com.travisyim.mountaineers.objects.AsyncTaskResult;
import com.travisyim.mountaineers.objects.FilterOptions;
import com.travisyim.mountaineers.objects.Mountaineer;
import com.travisyim.mountaineers.objects.MountaineerActivity;
import com.travisyim.mountaineers.utils.ActivityLoader;
import com.travisyim.mountaineers.utils.ListUtil;
import com.travisyim.mountaineers.utils.OnParseTaskCompleted;
import com.travisyim.mountaineers.utils.OnTaskCompleted;
import com.travisyim.mountaineers.utils.ParseConstants;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserActivityFragment extends ListFragment implements OnTaskCompleted,
        OnParseTaskCompleted, FilterFragment.OnFiltersSelectedListener,
        ActivityDetailsFragment.OnFavoriteSelectedListener {
    private Fragment mActivityDetailsFragment;
    private FilterFragment mFilterFragment;
    private FilterOptions mFilterOptions = new FilterOptions();
    private List<MountaineerActivity> mActivityList = new ArrayList<MountaineerActivity>();
    private Mountaineer mMember;
    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    List<String> mActivityURLs = new ArrayList<String>();
    private List<String> mFavoritesList;
    private String mQueryText;
    private int mActivityPosition;
    private boolean mReturnFromFilter = false;
    private boolean mReturnFromDetails = false;
    private boolean mAlreadyLoaded = false;
    private boolean mIsCanceled = false;
    private boolean mIsMaxLimitReached = false;
    private boolean mIsSignedUpActivity;

    private final String TAG = UserActivityFragment.class.getSimpleName() + ":";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_MEMBER = "member";
    private static final String ARG_ACTIVITY_NAME = "activityName";
    private static final String ARG_LEADER_NAMES = "leaderNames";
    private static final String ARG_ACTIVITY_URL = "activityURL";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_FAVORITE = "isFavorite";
    private static final String ARG_ACTIVITY_START_DATE = "startDate";
    private static final String ARG_ACTIVITY_END_DATE = "endDate";
    private static final String ARG_ACTIVITY_REG_OPEN_DATE = "regOpenDate";
    private static final String ARG_ACTIVITY_REG_CLOSE_DATE = "regCloseDate";
    private static final String ARG_USER_ACTIVITY_IS_SIGNED_UP = "isSignedUp";

    public UserActivityFragment() {
    }

    // Returns a new instance of this fragment for the given section number
    public static UserActivityFragment newInstance(int sectionNumber, ActivityLoader.ActivityType type) {
        UserActivityFragment fragment = new UserActivityFragment();
        Bundle args = new Bundle();

        /* Save the arguments to be accessed later in setArguments (must wait because member
         * variables are not yet accessible */
        // TODO: Clean up number formats
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);

        // Determine the type of user activity this fragment is to represent
        if (type == ActivityLoader.ActivityType.SIGNED_UP) {  // Signed Up activities
            args.putBoolean(ARG_USER_ACTIVITY_IS_SIGNED_UP, true);
        }
        else {  // Completed activities
            args.putBoolean(ARG_USER_ACTIVITY_IS_SIGNED_UP, false);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set additional arguments passed in after creating new fragment instance
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));

        // Get user activity type
        mIsSignedUpActivity = getArguments().getBoolean(ARG_USER_ACTIVITY_IS_SIGNED_UP);

        // Get the member object
        mMember = (Mountaineer) getArguments().getSerializable(ARG_MEMBER);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_activity, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorScheme(R.color.swipe_refresh1, R.color.swipe_refresh2,
                R.color.swipe_refresh3, R.color.swipe_refresh4);

        // Setup SwipeRefresh behavior
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Flag as updating activities
                ((MainActivity) getActivity()).setLoadingActivities(true);
                getUserActivityData();  // Get user activity list from website & Parse data
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        /* Get full member activity history list from the latest Parse data if this is the first
         * time creating this fragment */

        // Make sure the user is logged in before completing the following activities
        if (ParseUser.getCurrentUser() != null) {
            // Only run this on the first time the fragment is shown
            if (!mAlreadyLoaded) {
                mAlreadyLoaded = true;

                // Flag as updating activities
                ((MainActivity) getActivity()).setLoadingActivities(true);
                mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator

                getUserActivityData();  // Get user activity list from website & Parse data
            }
            else if (!mReturnFromDetails && !mReturnFromFilter) {
                // Flag as updating activities
                ((MainActivity) getActivity()).setLoadingActivities(true);
                mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator

                /* Refresh favorites (important in case the user has deselected a favorite user
                 * activity from the Favorites fragment */
                refreshFavorites();

                // Apply user defined filters to the latest list of activities
                ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
            }
            else if (mReturnFromFilter) {  // User returning from filter options
                mReturnFromFilter = false;  // Reset this flag
            }
            else if (mReturnFromDetails) {  // User returning from activity details
                mReturnFromDetails = false;  // Reset flag
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.user_activity, menu);

        // Setup listeners for the SearchView
        mSearchMenuItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) mSearchMenuItem.getActionView();
        mSearchView.setQueryHint(getActivity().getString(R.string.hint_searchview));
        mSearchView.setSubmitButtonEnabled(true);

        // Get the search submit button image view
        int submitButtonId = mSearchView.getContext().getResources()
                .getIdentifier("android:id/search_go_btn", null, null);
        View submitButton = mSearchView.findViewById(submitButtonId);

        // Set on click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the max limit message if applicable
                if (mIsMaxLimitReached) {
                    showMaxLimitToast();
                }

                // This is triggered when the submit button on the search menu is clicked
                mQueryText = String.valueOf(mSearchView.getQuery());  // Save query text

                mSearchMenuItem.collapseActionView();  // Collapse search view

                // Update results
                ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Show the max limit message if applicable
                if (mIsMaxLimitReached) {
                    showMaxLimitToast();
                }

                // This is triggered when the enter button on the keyboard is pressed
                mQueryText = query;  // Save query text

                mSearchMenuItem.collapseActionView();  // Collapse search view

                // Update results
                ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // This is triggered when the query string changes but is not currently used
                return false;
            }
        });

        mSearchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Run the old search text (must be done in a runnable or will not work)
                mSearchView.post(new Runnable() {
                    @Override
                    public void run() {
                        mSearchView.setQuery(mQueryText, false);

                        // The following allows the user to input text after the previous query text
                        mSearchView.setIconified(false);
                    }
                });

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Check to see if the User Activity fragment is still updating results
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mActivityPosition = position;  // Capture position of listitem clicked
            mReturnFromDetails = true;  // User is launching an activity details fragment

            // Launch Activity Details fragment to show activity's webpage
            if (mActivityDetailsFragment == null) {
                // TODO: Fix up section numbering scheme
                mActivityDetailsFragment = ActivityDetailsFragment.newInstance
                        ((float) (this.getArguments().getInt(ARG_SECTION_NUMBER) + 0.1),
                                getActivity().getActionBar().getTitle().toString());
            }

            Bundle args = mActivityDetailsFragment.getArguments();

            // Pass the following in a bundle because the data changes with each click
            args.putString(ARG_ACTIVITY_NAME, mActivityList.get(position).getTitle());
            args.putString(ARG_ACTIVITY_URL, mActivityList.get(position).getActivityUrl());
            args.putBoolean(ARG_FAVORITE, mActivityList.get(position).isFavorite());
            args.putLong(ARG_ACTIVITY_START_DATE,
                    mActivityList.get(position).getActivityStartDate().getTime());
            args.putLong(ARG_ACTIVITY_END_DATE,
                    mActivityList.get(position).getActivityEndDate().getTime());

            if (mActivityList.get(position).getRegistrationOpenTime() != null) {
                args.putLong(ARG_ACTIVITY_REG_OPEN_DATE,
                        mActivityList.get(position).getRegistrationOpenTime().getTime());
            }

            if (mActivityList.get(position).getRegistrationCloseTime() != null) {
                args.putLong(ARG_ACTIVITY_REG_CLOSE_DATE,
                        mActivityList.get(position).getRegistrationCloseTime().getTime());
            }

            if (mActivityList.get(position).getStartLatitude() != -999 &&
                    mActivityList.get(position).getStartLongitude() != -999) {
                args.putString(ARG_LOCATION, mActivityList.get(position).getStartLatitude() + ", " +
                        mActivityList.get(position).getStartLongitude());
            }
            else if (mActivityList.get(position).getEndLatitude() != -999 &&
                    mActivityList.get(position).getEndLongitude() != -999) {
                args.putString(ARG_LOCATION, mActivityList.get(position).getEndLatitude() + ", " +
                        mActivityList.get(position).getEndLongitude());
            }
            else {
                args.putString(ARG_LOCATION, "");
            }

            // Pass in Leader Name(s)
            try {  // Join leader names together and remove quotes (due to being stored in JSON array)
                args.putString(ARG_LEADER_NAMES, mActivityList.get(position).getLeaderName()
                        .join(", ").replace("\"", ""));
            }
            catch (JSONException e) {
                // Error with leader names so pass in empty String
                args.putString(ARG_LEADER_NAMES, "");
            }

            // Update ActionBar title to show name
            getActivity().getActionBar().setTitle(getString(R.string.title_activity_details));

            // Load activity details fragment with fade and slide animations
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                    R.animator.slide_in_right, R.animator.slide_out_right);

            transaction.replace(R.id.container, mActivityDetailsFragment).addToBackStack(null).commit();
        }
        else {  // Still updating so prevent user from moving to the Activity Details page
            Toast.makeText(getActivity(), getActivity().getString(R.string.toast_filter_wait),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logOut:  // Log Out
                mIsCanceled = true;  // Alerts processes to cancel
                ParseUser.getCurrentUser().logOut();
                ((MainActivity) getActivity()).showLoginScreen();
                return true;
            case R.id.action_filter:  // Filter
                /* Check to see if the User Activity fragment is still updating results or if the
                 * drawer is open */
                if (!((MainActivity) getActivity()).isDrawerOpen()) {
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        // Not updating so load filter fragment
                        if (mFilterFragment == null) {
                            // TODO: Fix up section numbering scheme
                            mFilterFragment = FilterFragment.newInstance
                                    ((float) (this.getArguments().getFloat(ARG_SECTION_NUMBER) + 0.1),
                                    mFilterOptions, getActivity().getActionBar().getTitle().toString());
                        }

                        // Set this flag to produce null behavior when hitting back in filter fragment
                        mReturnFromFilter = true;

                        // Update ActionBar title to show name
                        getActivity().getActionBar().setTitle(getString(R.string.title_activity_filters));

                        // Load filter fragment with fade and slide animations
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                                R.animator.slide_in_right, R.animator.slide_out_right);

                        transaction.replace(R.id.container, mFilterFragment).addToBackStack(null).commit();
                    } else {  // Still updating so prevent user from moving to the filter page
                        Toast.makeText(getActivity(), getActivity().getString(R.string.toast_filter_wait),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFavoriteSelected(boolean isFavorite) {
        // Favorite value has changed so update it
        mActivityList.get(mActivityPosition).setFavorite(isFavorite);  // Local update

        // Parse backend update
        if (isFavorite) {  // Add entry to user's favorites array list
            ParseUser.getCurrentUser().add(ParseConstants.KEY_FAVORITES,
                    mActivityList.get(mActivityPosition).getObjectID());
        }
        else {  // Delete entry to user's favorites array list
            List<String> objectId = new ArrayList<String>();
            objectId.add(mActivityList.get(mActivityPosition).getObjectID());
            ParseUser.getCurrentUser().removeAll(ParseConstants.KEY_FAVORITES, objectId);
        }

        try {
            ParseUser.getCurrentUser().saveInBackground();
        }
        catch (Exception e) { /* Intentionally left blank */ }
    }

    @Override
    public void onFiltersSelected(FilterOptions filterOptions) {
        /* This method is called when the user has defined filter options and has returned to the
         * User Activity fragment */
        mFilterOptions = filterOptions;

        // Apply both text and filter options to the activity list
        ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
        ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
    }

    @Override
    public void onParseTaskCompleted(List<ParseObject> resultList, ParseException e) {
        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            /* This method is called when the task of retrieving the full set of User activities
             * is complete */
            mActivityList.clear();

            if (e == null) {
                // Reverse the list to get oldest start date activities first
                Collections.reverse(resultList);

                // Load activity list with results
                mActivityList.addAll(ActivityLoader.load(resultList, mFavoritesList, mMember,
                        mIsSignedUpActivity ? ActivityLoader.ActivityType.SIGNED_UP :
                                ActivityLoader.ActivityType.COMPLETED));

                // See if the max limit is reached
                if (mActivityList.size() == ParseConstants.QUERY_LIMIT) {
                    mIsMaxLimitReached = true;
                    showMaxLimitToast();
                }

                // Update search results
                refreshActivities();
            } else {
                Toast toast = Toast.makeText(getActivity(), getActivity().getString
                        (R.string.toast_parse_error), Toast.LENGTH_LONG);
                ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                        .setGravity(Gravity.CENTER_HORIZONTAL);
                toast.show();

                mSwipeRefreshLayout.setRefreshing(false);  // Turn on update indicator
                // Flag as finished updating activities
                ((MainActivity) getActivity()).setLoadingActivities(false);
            }
        }
        else {
            mIsCanceled = false;
        }
    }

    @Override
    public void onTaskCompleted(int stage, AsyncTaskResult<Boolean> result) {
        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            mActivityURLs.clear();  // Clear all previous URLs

            // Check for success in scraping user data
            if (result.getError() != null || !result.getResult()) {  // Error!
                /* This check ensures member profile and activity history web pages are in the format
                 * that we expect and that the app could properly scrape this data */
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.error_title)
                        .setMessage(result.getError().getMessage())
                        .setPositiveButton(android.R.string.ok, null);

                AlertDialog alert = builder.create();
                alert.show();

                // Let the user decide if he/she wants to try refreshing again.  Otherwise, leave blank.
            }
            else {  // If successful scraping user activities, find these items on the backend
                // Get a string array of all activity URLs
                if (mIsSignedUpActivity) {  // Signed up activities
                    if (mMember.getCurrentActivity() != null) {
                        for (int i = 0; i < mMember.getCurrentActivity()[0].length; i++) {
                            mActivityURLs.add(mMember.getCurrentActivity()[2][i]);  // Activity URL
                        }
                    }
                }
                else {  // Completed activities
                    // Get a string array of all activity URLs
                    if (mMember.getPastActivity() != null) {
                        for (int i = 0; i < mMember.getPastActivity()[0].length; i++) {
                            mActivityURLs.add(mMember.getPastActivity()[2][i]);  // Activity URL
                        }
                    }
                }

                // Intermediate check to ensure all processes have not been canceled
                if (!mIsCanceled) {
                    // Re-fetch current user data - ensures favorites list is up-to-date
                    ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            ParseQuery<ParseObject> query;

                            // Intermediate check to ensure all processes have not been canceled
                            if (!mIsCanceled) {  // Get list of favorite activities
                                List<String> tempFavorites = ParseUser.getCurrentUser().getList(ParseConstants.KEY_FAVORITES);
                                mFavoritesList = ListUtil.copy(tempFavorites);

                                query = ParseQuery.getQuery(ParseConstants.CLASS_ACTIVITY);
                                query.whereContainedIn(ParseConstants.KEY_ACTIVITY_URL, mActivityURLs);
                                query.orderByDescending(ParseConstants.KEY_ACTIVITY_START_DATE);
                                query.addDescendingOrder(ParseConstants.KEY_ACTIVITY_TITLE);
                                query.setLimit(ParseConstants.QUERY_LIMIT); // Limit to 500 results max

                                query.findInBackground(new FindCallback<ParseObject>() {
                                    public void done(List<ParseObject> resultList, ParseException e) {
                                        // Forward the results and error variable to the custom OnParseTaskCompleted method
                                        onParseTaskCompleted(resultList, e);
                                    }
                                });
                            }
                            else {
                                mIsCanceled = false;
                            }
                        }
                    });
                }
                else {
                    mIsCanceled = false;
                }
            }
        }
        else {
            mIsCanceled = false;
        }
    }

    private void getUserActivityData() {
        // Scrape member activity history data from website
        mMember.getMemberHistory(this);
    }

    private void refreshActivities() {
        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            // Pass the list to the adapter
            if (getListView().getAdapter() == null) {  // First time using the list adapter
                ActivityAdapter adapter = new ActivityAdapter(getListView().getContext(), mActivityList, null);
                setListAdapter(adapter);
            }
            else {  // Results already shown so update the list
                ((ActivityAdapter) getListAdapter()).setMasterActivityList(mActivityList);
            }

            // Apply user defined filters to the latest list of activities
            ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
            ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);

            if (mActivityList.size() == 0) {
                Toast toast = Toast.makeText(getActivity(), getActivity().getString
                        (mIsSignedUpActivity ? R.string.toast_sign_up :
                                R.string.toast_complete), Toast.LENGTH_LONG);
                ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                        .setGravity(Gravity.CENTER_HORIZONTAL);
                toast.show();
            }

            getListView().setEmptyView(getActivity().findViewById(R.id.textViewEmpty));

            // Flag as finished updating activities
            ((MainActivity) getActivity()).setLoadingActivities(false);
            mSwipeRefreshLayout.setRefreshing(false);  // Turn off update indicator
        }
        else {
            mIsCanceled = false;
        }
    }

    private void refreshFavorites() {
        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            // Re-fetch current user data - ensures favorites list is up-to-date
            ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    int favoriteObjectIdPosition;

                    // Intermediate check to ensure all processes have not been canceled
                    if (!mIsCanceled) {
                        // Get list of favorite activities
                        List<String> tempFavorites = ParseUser.getCurrentUser().getList(ParseConstants.KEY_FAVORITES);
                        mFavoritesList = ListUtil.copy(tempFavorites);

                        for (MountaineerActivity activity : mActivityList) {
                            if (mFavoritesList != null) {  // Favorite activities defined
                                favoriteObjectIdPosition = mFavoritesList.indexOf(activity.getObjectID());

                                if (favoriteObjectIdPosition >= 0) {  // This activity is a favorite
                                    activity.setFavorite(true);  // Set favorite flag to true

                                    // Remove the favorite ObjectId from the list (for efficiency)
                                    mFavoritesList.remove(favoriteObjectIdPosition);
                                } else {  // Activity object ID not found in favorites list
                                    activity.setFavorite(false);
                                }
                            } else {  // No favorite activities defined
                                activity.setFavorite(false);
                            }
                        }

                        getListView().setEmptyView(getActivity().findViewById(R.id.textViewEmpty));

                        // Flag as updating activities
                        ((MainActivity) getActivity()).setLoadingActivities(false);
                        mSwipeRefreshLayout.setRefreshing(false);  // Turn off update indicator
                    }
                    else {
                        mIsCanceled = false;
                    }
                }
            });
        }
        else {
            mIsCanceled = false;
        }
    }

    private void showMaxLimitToast() {
        Toast toast = Toast.makeText(getActivity(), getActivity().getString
                (R.string.toast_max_activities), Toast.LENGTH_LONG);
        ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                .setGravity(Gravity.CENTER_HORIZONTAL);
        toast.show();
    }

    /* This method resets the User Activity fragment in the event the user logs out and logs
     * right back in */
    public void resetFragment() {
        mAlreadyLoaded = false;
        mIsCanceled = false;
        mQueryText = "";  // Reset search text
        mFilterFragment = null;
        mFilterOptions = new FilterOptions();  // Create new filter options
    }
}