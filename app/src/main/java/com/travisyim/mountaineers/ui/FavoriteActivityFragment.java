package com.travisyim.mountaineers.ui;

import android.app.Activity;
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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.adapters.ActivityAdapter;
import com.travisyim.mountaineers.objects.FilterOptions;
import com.travisyim.mountaineers.objects.MountaineerActivity;
import com.travisyim.mountaineers.utils.ActivityLoader;
import com.travisyim.mountaineers.utils.OnParseTaskCompleted;
import com.travisyim.mountaineers.utils.ParseConstants;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoriteActivityFragment extends ListFragment implements OnParseTaskCompleted,
        FilterFragment.OnFiltersSelectedListener, ActivityDetailsFragment.OnFavoriteSelectedListener {
    private Fragment mActivityDetailsFragment;
    private FilterFragment mFilterFragment;
    private FilterOptions mFilterOptions = new FilterOptions();
    private List<MountaineerActivity> mActivityList = new ArrayList<MountaineerActivity>();
    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mQueryText;
    private int mActivityPosition;
    private boolean mIsCanceled = false;
    private boolean mIsMaxLimitReached = false;
    private boolean mReturnFromFilter = false;
    private boolean mReturnFromDetails = false;

    private final String TAG = FavoriteActivityFragment.class.getSimpleName() + ":";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_ACTIVITY_NAME = "activityName";
    private static final String ARG_LEADER_NAMES = "leaderNames";
    private static final String ARG_ACTIVITY_URL = "activityURL";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_FAVORITE = "isFavorite";
    private static final String ARG_ACTIVITY_START_DATE = "startDate";
    private static final String ARG_ACTIVITY_END_DATE = "endDate";
    private static final String ARG_ACTIVITY_REG_OPEN_DATE = "regOpenDate";
    private static final String ARG_ACTIVITY_REG_CLOSE_DATE = "regCloseDate";

    public FavoriteActivityFragment() {
    }

    // Returns a new instance of this fragment for the given section number
    public static FavoriteActivityFragment newInstance(int sectionNumber) {
        FavoriteActivityFragment fragment = new FavoriteActivityFragment();
        Bundle args = new Bundle();

        /* Save the arguments to be accessed later in setArguments (must wait because member
         * variables are not yet accessible */
        // TODO: Clean up number formats
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set additional arguments passed in after creating new fragment instance
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite_activity, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorScheme(R.color.swipe_refresh1, R.color.swipe_refresh2,
                R.color.swipe_refresh3, R.color.swipe_refresh4);

        // Setup SwipeRefresh behavior
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Flag as updating activities
                ((MainActivity) getActivity()).setLoadingActivities(true);
                getFavoriteActivityList();  // Get favorite activity list from Parse data
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure the user is logged in before completing the following activities
        if (ParseUser.getCurrentUser() != null) {
            // Check if this is returning from activity details or the filter fragment
            if (!mReturnFromDetails && !mReturnFromFilter) {  // Not returning from either
                // Flag as updating activities
                ((MainActivity) getActivity()).setLoadingActivities(true);
                mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator

                // Update favorite activities list
                getFavoriteActivityList();
            }
            else if (mReturnFromFilter) {  // User returning from filter options
                mReturnFromFilter = false;  // Reset this flag
            }
            else if (mReturnFromDetails) {  // User returning from activity details
                mReturnFromDetails = false;  // Reset this flag
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

        // Check to see if the Favorite Activity fragment is still updating results
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mActivityPosition = position;  // Capture position of listitem clicked
            mReturnFromDetails = true;  // User is launching an activity details fragment

            // Launch ActivityDetails fragment to show activity's webpage
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
        else {  // Still updating so prevent user from moving to the activity details page
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
                /* Check to see if the Activity Search fragment is still updating results or if the
                 * drawer is open */
                if (!((MainActivity) getActivity()).isDrawerOpen()) {
                    // Check to see if the Favorite Activity fragment is still updating results
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

//                        // Update ActionBar title to show name
//                        getActivity().getActionBar().setTitle(getString(R.string.title_activity_filters));

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
        // Flag as updating activities
        ((MainActivity) getActivity()).setLoadingActivities(true);
        mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator

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
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        getFavoriteActivityList();
                    }

                    // Flag as finished updating activities
                    ((MainActivity) getActivity()).setLoadingActivities(false);
                    mSwipeRefreshLayout.setRefreshing(false);  // Turn off update indicator
                }
            });
        }
        catch (Exception e) {
            // Flag as finished updating activities
            ((MainActivity) getActivity()).setLoadingActivities(false);
            mSwipeRefreshLayout.setRefreshing(false);  // Turn off update indicator
        }
    }

    @Override
    public void onFiltersSelected(FilterOptions filterOptions) {
        /* This method is called when the user has defined filter options and has returned to the
         * Favorite Activity fragment */
        mFilterOptions = filterOptions;

        // Apply user defined filters to the latest list of activities
        ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
        ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
    }

    @Override
    public void onParseTaskCompleted(List<ParseObject> resultList, ParseException e) {
        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            // This method is called when the task of retrieving the full set of activities is complete
            mActivityList.clear();

            if (e == null) {
                // Reverse the list to get oldest start date activities first
                Collections.reverse(resultList);

                // Load activities into the list view
                mActivityList.addAll(ActivityLoader.load(resultList, true));

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

    private void getFavoriteActivityList() {
        // This method is called to retrieve all of the favorite activities from the backend
        ParseQuery<ParseObject> query;

        // Intermediate check to ensure all processes have not been canceled
        if (!mIsCanceled) {
            // Get list of favorite activities
            List<String> favoritesList = ParseUser.getCurrentUser()
                    .getList(ParseConstants.KEY_FAVORITES);

            if (favoritesList != null) {
                query = ParseQuery.getQuery(ParseConstants.CLASS_ACTIVITY);
                query.whereContainedIn(ParseConstants.KEY_OBJECT_ID, favoritesList);
                query.orderByDescending(ParseConstants.KEY_ACTIVITY_START_DATE);
                query.addDescendingOrder(ParseConstants.KEY_ACTIVITY_TITLE);
                query.setLimit(ParseConstants.QUERY_LIMIT); // Limit to 500 results max

                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> resultList, ParseException e) {
                        // Forward the results and error variable to the custom OnParseTaskCompleted method
                        onParseTaskCompleted(resultList, e);
                    }
                });
            } else {
                Toast toast = Toast.makeText(getActivity(), getActivity().getString
                        (R.string.toast_empty_favorites), Toast.LENGTH_LONG);
                ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                        .setGravity(Gravity.CENTER_HORIZONTAL);
                toast.show();

                getListView().setEmptyView(getActivity().findViewById(R.id.textViewEmpty));

                // Flag as updating activities
                ((MainActivity) getActivity()).setLoadingActivities(false);
                mSwipeRefreshLayout.setRefreshing(false);  // Turn off update indicator
            }
        } else {
            mIsCanceled = false;
        }
    }

    private void refreshActivities() {
        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            // Pass the list to the adapter
            if (getListView().getAdapter() == null) {  // First time using the list adapter
                ActivityAdapter adapter = new ActivityAdapter(getListView().getContext(), mActivityList, null);
                setListAdapter(adapter);
            } else {  // Results already shown so update the list
                ((ActivityAdapter) getListAdapter()).setMasterActivityList(mActivityList);
            }

            // Apply user defined filters to the latest list of activities
            ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
            ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);

            if (mActivityList.size() == 0) {
                Toast toast = Toast.makeText(getActivity(), getActivity().getString
                        (R.string.toast_empty_favorites), Toast.LENGTH_LONG);
                ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                        .setGravity(Gravity.CENTER_HORIZONTAL);
                toast.show();
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

    /* This method resets the Favorite Activity fragment in the event the user logs out and logs
     * right back in */
    public void resetFragment() {
        mIsCanceled = false;
        mQueryText = "";  // Reset search text
        mFilterFragment = null;
        mFilterOptions = new FilterOptions();  // Create new filter options
    }

    private void showMaxLimitToast() {
        Toast toast = Toast.makeText(getActivity(), getActivity().getString
                (R.string.toast_max_activities), Toast.LENGTH_LONG);
        ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                .setGravity(Gravity.CENTER_HORIZONTAL);
        toast.show();
    }
}