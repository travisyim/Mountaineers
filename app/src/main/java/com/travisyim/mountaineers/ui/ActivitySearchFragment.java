package com.travisyim.mountaineers.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.travisyim.mountaineers.MountaineersApp;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.adapters.ActivityAdapter;
import com.travisyim.mountaineers.objects.FilterOptions;
import com.travisyim.mountaineers.objects.MountaineerActivity;
import com.travisyim.mountaineers.utils.ActivityLoader;
import com.travisyim.mountaineers.utils.DateUtil;
import com.travisyim.mountaineers.utils.OnParseTaskCompleted;
import com.travisyim.mountaineers.utils.ParseConstants;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ActivitySearchFragment extends ListFragment implements OnParseTaskCompleted,
        FilterFragment.OnFiltersSelectedListener, ActivityDetailsFragment.OnFavoriteSelectedListener {
    private Fragment mActivityDetailsFragment;
    private FilterFragment mFilterFragment;
    private FilterOptions mFilterOptions;
    private List<MountaineerActivity> mActivityList = new ArrayList<MountaineerActivity>();
    private DrawerLayout mDrawerLayout;
    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<String> mFavoritesList;
    private Date mPreviousSearchStartDate;
    private String mParentFragmentTitle;
    private String mSavedSearchName;
    private String mQueryText;
    private String mPreviousSearch;
    private int mActivityPosition;
    private boolean mIsNewSearch;
    private boolean mAlreadyLoaded;
    private boolean mReturnFromFilter = false;
    private boolean mReturnFromDetails = false;
    private boolean mIsCollapsed = false;
    private boolean mHasSearchLostFocus = false;
    private boolean mIsCanceled = false;
    private boolean mIsSubmitted = false;
    private boolean mIsSavedSearch = false;

    private final String TAG = ActivitySearchFragment.class.getSimpleName() + ":";
    private static final String ARG_PARENT_TITLE = "parentFragmentTitle";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SAVED_SEARCH_NAME = "savedSearchName";
    private static final String ARG_FILTER_OPTIONS = "filterOptions";
    private static final String ARG_QUERY_TEXT = "queryText";
    private static final String ARG_ACTIVITY_NAME = "activityName";
    private static final String ARG_ACTIVITY_URL = "activityURL";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_FAVORITE = "isFavorite";
    private static final String ARG_ACTIVITY_START_DATE = "startDate";
    private static final String ARG_ACTIVITY_END_DATE = "endDate";
    private static final String ARG_ACTIVITY_REG_OPEN_DATE = "regOpenDate";
    private static final String ARG_ACTIVITY_REG_CLOSE_DATE = "regCloseDate";

    public ActivitySearchFragment() {
    }

    // Returns a new instance of this fragment
    public static ActivitySearchFragment newInstance(final int sectionNumber,
                                                     final String parentFragmentTitle) {
        ActivitySearchFragment fragment = new ActivitySearchFragment();
        Bundle args = new Bundle();

        /* Save the arguments to be accessed later in setArguments (must wait because member
         * variables are not yet accessible */
        // TODO: Clean up number formats
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);

        // There will only be a parent fragment title if this is created from the saved search fragment
        args.putString(ARG_PARENT_TITLE, parentFragmentTitle);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set additional arguments passed in after creating new fragment instance
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));

        /* If any of the data below is provided that means this was created from the Saved Search
         * fragment.  Otherwise, this exists because we are looking at the default Activity Search
         * fragment in which the app starts in (after the user logs in) */
        // Get the reference to the filter options
        if (getArguments().getString(ARG_PARENT_TITLE) != null) {
            mIsSavedSearch = true;

            // Get the parent fragment's title
            mParentFragmentTitle = getArguments().getString(ARG_PARENT_TITLE);
            mSavedSearchName = getArguments().getString(ARG_SAVED_SEARCH_NAME);
            mFilterOptions = (FilterOptions) getArguments().getSerializable(ARG_FILTER_OPTIONS);
            mQueryText = getArguments().getString(ARG_QUERY_TEXT);  // Query text

            // Update ActionBar title to show saved search name
            getActivity().getActionBar().setTitle(mSavedSearchName);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (!mAlreadyLoaded) {
            // Google Analytics tracking code - Activity Search first load
            Tracker t = ((MountaineersApp) getActivity().getApplication()).getTracker
                    (MountaineersApp.TrackerName.APP_TRACKER);
            t.setScreenName("Activity Search");
            t.send(new HitBuilders.AppViewBuilder().build());

            // If the FilterOptions object was not passed in, then create it
            // The FilterOptions object is only passed in when opening a Saved Search
            if (mFilterOptions == null) {
                mFilterOptions = new FilterOptions();  // Create new filter options

                // Set the default date range of the results shown starting from the current date
                mFilterOptions.setStartDate(DateUtil.convertToDate(DateUtil.convertToString
                        (new Date(), DateUtil.TYPE_BUTTON_DATE), DateUtil.TYPE_BUTTON_DATE));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity_search, container, false);

        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorScheme(R.color.swipe_refresh1, R.color.swipe_refresh2,
                R.color.swipe_refresh3, R.color.swipe_refresh4);

        /* Check to make sure the user is logged in before loading activities (the user's favorites
         * are required for this process). The code below does not run for a user who has not logged
         * in yet.  Once the user logs in, the same code will be run in the onResume() method */
        if (ParseUser.getCurrentUser() != null) {
            /* Get full activity list from the latest Parse data if this is the first time creating
             * this fragment */
            if (savedInstanceState == null && !mAlreadyLoaded) {
                mAlreadyLoaded = true;
                mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator
                // Flag as updating activities
                ((MainActivity) getActivity()).setLoadingActivities(true);
                getActivityList();
            }
        }

        // Setup SwipeRefresh behavior
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Flag as updating activities
                ((MainActivity) getActivity()).setLoadingActivities(true);
                getActivityList();  // Get full activity list from Parse data
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        /* The intent of the code below is to download the latest activity data when the user has
         * just logged in.  This code would be ignored if the user was logged in from a previous
         * session (i.e. returning Current User). */
        if (ParseUser.getCurrentUser() != null) {
            if (mReturnFromFilter) {
                mReturnFromFilter = false;
                mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator
                // Flag as updating activities
                ((MainActivity) getActivity()).setLoadingActivities(true);
            }
            // Only run if activities had been previously populated in the listview
            else if (!mReturnFromDetails && mActivityList.size() != 0) {
                /* Refresh favorites (important in case the user has deselected a favorite Completed
                 * activity from the Favorites fragment */
                mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator
                refreshFavorites();

                // Apply user defined filters to the latest list of activities
                ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
            }
            // User returning from activity details
            else if (mReturnFromDetails) {
                mReturnFromDetails = false;  // Reset flag
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mIsSavedSearch && !mIsCanceled) {
            // Reset the title back to that of the parent fragment
            getActivity().getActionBar().setTitle(mParentFragmentTitle);

            // Google Analytics tracking code - User Profile
            Tracker t = ((MountaineersApp) getActivity().getApplication()).getTracker
                    (MountaineersApp.TrackerName.APP_TRACKER);
            t.setScreenName("Saved Search");
            t.send(new HitBuilders.AppViewBuilder().build());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.activity_search, menu);

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
                // This is triggered when the submit button on the search menu is clicked
                mQueryText = String.valueOf(mSearchView.getQuery());  // Save query text
                mIsSubmitted = true;
                mSearchMenuItem.collapseActionView();  // Collapse search view

                // Update results
                ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // This is triggered when the enter button on the keyboard is pressed
                mQueryText = query;  // Save query text
                mIsSubmitted = true;
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
                mIsNewSearch = true;  // Mark this as a new search
                mIsCollapsed = false;  // Set this collapsed flag

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
                // Check to see if query was submitted or if back button / up was pressed
                if (!mIsSubmitted) {  // User canceled query
                    // Reload previous search results
                    mQueryText = mPreviousSearch;
                    ((ActivityAdapter) getListAdapter()).applyTextFilter(mPreviousSearch);
                    ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
                }
                else {  // User submitted new query
                    mIsSubmitted = false;
                    mPreviousSearch = mQueryText;  // Save previous search
                }

                mIsCollapsed = true;  // Set this collapsed flag

                return true;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Check to see if the Activity Search fragment is still updating results
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mHasSearchLostFocus = true;  // Flag this as clicked

            mActivityPosition = position;  // Capture position of listitem clicked

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

            // Update ActionBar title to show name
            getActivity().getActionBar().setTitle(getString(R.string.title_activity_details));

            // Load activity details fragment
            getFragmentManager().beginTransaction().replace(R.id.container, mActivityDetailsFragment)
                    .addToBackStack(null).commit();
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
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        mHasSearchLostFocus = true;  // Flag this as clicked

                        // Not updating so load filter fragment
                        if (mFilterFragment == null) {
                            mFilterFragment = FilterFragment.newInstance
                                    ((float) (this.getArguments().getFloat(ARG_SECTION_NUMBER) + 0.1),
                                    mFilterOptions, getActivity().getActionBar().getTitle().toString());
                        }

                        // Update ActionBar title to show name
                        getActivity().getActionBar().setTitle(getString(R.string.title_activity_filters));

                        // Launch Filter fragment
                        getFragmentManager().beginTransaction().replace(R.id.container, mFilterFragment)
                                .addToBackStack(null).commit();
                    } else {  // Still updating so prevent user from moving to the filter page
                        Toast.makeText(getActivity(), getActivity().getString(R.string.toast_filter_wait),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            case R.id.action_save:
                showSaveDialog();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFavoriteSelected(boolean isFavorite) {
        mReturnFromDetails = true;

        // Update activity with the favorite state if it has changed
        if (mActivityList.get(mActivityPosition).isFavorite() != isFavorite) {
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
    }

    @Override
    public void onFiltersSelected(FilterOptions filterOptions) {
        /* This method is called when the user has defined filter options and has returned to the
         * Activity Search fragment */
        mFilterOptions = filterOptions;

        /* Either submit a new query or apply filters depending on how the start date selection has
         * changed (if it has at all) */
        if (mFilterOptions.getStartDate() == null) {  // No start date defined
            if (mPreviousSearchStartDate == null) {  // No start date defined previously
                // No start date change - apply both text and filter options to the activity list
                ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
            }
            else {  // Previous query used a defined start date
                /* Flag that we're returning from the filter so we can start the swipe refreshing
                 * (does not work if we call the refreshing command directly) */
                mReturnFromFilter = true;

                // Search again since the start date has been opened up
                getActivityList();
            }
        }
        else {  // Defined start date
            if (mPreviousSearchStartDate == null) {  // No start date defined previously
                // Check for the number of activities
                if (((ActivityAdapter) getListAdapter()).getMasterActivityListCount() == 1000) {
                    // More than 1000 results (Parse will only return 1000 results at a time
                    if (mFilterOptions.getStartDate().getTime() <
                            ((ActivityAdapter) getListAdapter()).getMinStartDate().getTime()) {
                        /* Start date is defined prior to any activities on Parse - do not search
                         * again - just update filtered results since we have the range covered */
                        ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                        ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
                    }
                    else if (mFilterOptions.getEndDate() == null ||
                            mFilterOptions.getEndDate().getTime() >=
                                    ((ActivityAdapter) getListAdapter()).getMaxStartDate().getTime()) {
                        /* Flag that we're returning from the filter so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly) */
                        mReturnFromFilter = true;

                        // Search again
                        getActivityList();
                    }
                    else {  // No need to update results from Parse
                        // Do not query Parse - just update filtered results
                        ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                        ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
                    }
                }
                else {  // Less than 1000 total activities
                    // Do not query Parse - just update filtered results
                    ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                    ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
                }
            }
            else {  // Start date was defined for the previous search
                if (mFilterOptions.getStartDate().getTime() < mPreviousSearchStartDate.getTime()) {
                    /* Flag that we're returning from the filter so we can start the swipe refreshing
                     * (does not work if we call the refreshing command directly) */
                    mReturnFromFilter = true;

                    // Search again since the start date has been opened up
                    getActivityList();
                }
                else if (mFilterOptions.getStartDate().getTime() == mPreviousSearchStartDate.getTime()) {
                    // Do not query Parse - just update filtered results
                    ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                    ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
                }
                else {  // Current start date is greater than the previous search's start date
                    // Check for the number of activities
                    if (((ActivityAdapter) getListAdapter()).getMasterActivityListCount() == 1000) {
                        // More than 1000 results (Parse will only return 1000 results at a time
                        if (mFilterOptions.getEndDate() == null ||
                                mFilterOptions.getEndDate().getTime() >=
                                        ((ActivityAdapter) getListAdapter()).getMaxStartDate().getTime()) {
                            /* Flag that we're returning from the filter so we can start the swipe
                             * refreshing (does not work if we call the refreshing command directly) */
                            mReturnFromFilter = true;

                            // Search again
                            getActivityList();
                        }
                        else {  // No need to update results from Parse
                            // Do not query Parse - just update filtered results
                            ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                            ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
                        }
                    }
                    else {  // Less than 1000 total activities
                        // Do not query Parse - just update filtered results
                        ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
                        ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
                    }
                }
            }
        }
    }

    @Override
    public void onParseTaskCompleted(List<ParseObject> resultList, ParseException e) {
        Toast toast;

        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            // This method is called when the task of retrieving the full set of activities is complete
            mActivityList.clear();

            if (e == null) {
                mActivityList.addAll(ActivityLoader.load(resultList, mFavoritesList));

                // See if the results encapsulate the end of our data range
                if (mFilterOptions.getEndDate() == null) {  // No end date defined
                    if (mActivityList.size() == 1000) {  // Max limit reached
                        toast = Toast.makeText(getActivity(), getActivity().getString
                                (R.string.toast_max_activities), Toast.LENGTH_LONG);
                        ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                                .setGravity(Gravity.CENTER_HORIZONTAL);
                        toast.show();
                    }
                } else {  // End date defined
                    if (mActivityList.size() == 1000) {  // Max limit reached
                        // See if the end date is captured in the 1000 results
                        if (mFilterOptions.getEndDate().getTime() >= mActivityList
                                .get(mActivityList.size() - 1).getActivityEndDate().getTime()) {
                        /* The defined end date either falls outside or on the day of the last
                         * result's start date. */
                            toast = Toast.makeText(getActivity(), getActivity().getString
                                    (R.string.toast_max_activities), Toast.LENGTH_LONG);
                            ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                                    .setGravity(Gravity.CENTER_HORIZONTAL);
                            toast.show();
                        }
                    }
                }

                // Update search results
                refreshActivities();
            } else {
                toast = Toast.makeText(getActivity(), getActivity().getString
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

    private void getActivityList() {
        // This method is called to retrieve activities from the backend
        ParseQuery<ParseObject> query;

        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            // Get list of favorite activities
            mFavoritesList = ParseUser.getCurrentUser().getList(ParseConstants.KEY_FAVORITES);

            query = ParseQuery.getQuery(ParseConstants.CLASS_ACTIVITY);

            // Set query constraint based on the start date
            mPreviousSearchStartDate = mFilterOptions.getStartDate();

            // Apply start date filter only if it is defined (i.e. not null)
            if (mFilterOptions.getStartDate() != null) {  // Start date defined
                query.whereGreaterThanOrEqualTo(ParseConstants.KEY_ACTIVITY_START_DATE,
                        DateUtil.convertToUNC(mFilterOptions.getStartDate()));
            }

        /* A query constraint is not set on the end date.  Instead, up to 1000 activities will be
         * returned (max of Parse query items) and if the end date is captured, then limit the
         * results shown.  However, if the end date is not reached within the 1000 result limit, the
         * user will be shown an error message to reduce the date range. */

            query.orderByAscending(ParseConstants.KEY_ACTIVITY_START_DATE);
            query.addAscendingOrder(ParseConstants.KEY_ACTIVITY_TITLE);
            query.setLimit(1000); // limit to 1000 results max

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

    private void refreshActivities() {
        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            // Pass the list to the adapter
            if (getListView().getAdapter() == null) {  // First time using the list adapter
                ActivityAdapter adapter = new ActivityAdapter(getListView().getContext(), mActivityList);
                setListAdapter(adapter);
            } else {  // Results already shown so update the list
                ((ActivityAdapter) getListAdapter()).setMasterActivityList(mActivityList);
            }

            // Apply user defined filters to the latest list of activities
            ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
            ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);

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
                        mFavoritesList = ParseUser.getCurrentUser().getList(ParseConstants.KEY_FAVORITES);

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

                        // Flag as finished updating activities
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

    /* This method resets the Activity Search fragment in the event the user logs out and logs right
     * back in */
    public void resetFragment() {
        mAlreadyLoaded = false;
        mIsCanceled = false;
        mQueryText = "";  // Reset search text
        mFilterFragment = null;
        mFilterOptions = new FilterOptions();  // Create new filter options

        // Set the default date range of the results shown starting from the current date
        mFilterOptions.setStartDate(DateUtil.convertToDate(DateUtil.convertToString(new Date(),
                DateUtil.TYPE_BUTTON_DATE), DateUtil.TYPE_BUTTON_DATE));
    }

    // This method saves the current search
    private void saveSearch(String name) {
        ParseObject savedSearch = null;
        boolean errorEncountered = false;

        // Check if name is already defined (i.e. is already a saved search)
        if (mIsSavedSearch) {  // Yes
            ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_SAVED_SEARCH);
            query.whereEqualTo(ParseConstants.KEY_SAVE_NAME, name);

            try {
                savedSearch = query.getFirst();
            }
            catch (ParseException e) {
                errorEncountered = true;
            }
        }
        else {  // No
            // Create new savedSearch object
            savedSearch = new ParseObject(ParseConstants.CLASS_SAVED_SEARCH);
        }

        // If no error was encountered while retrieving saved search object from Parse (if applicable)
        if (!errorEncountered) {
            // Save search criteria to Search table
            savedSearch.put(ParseConstants.KEY_SAVE_NAME, name);  // Name of saved search

            // Save user and search date
            savedSearch.put(ParseConstants.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
            // Save this in the equivalent UNC time (automatically happens when sending to Parse)
            savedSearch.put(ParseConstants.KEY_LAST_ACCESS, new Date());

            // Reset update counter
            savedSearch.put(ParseConstants.KEY_UPDATE_COUNT, 0);

            // Check if query string is defined
            if (mQueryText != null && mQueryText.length() > 0) {
                // Keywords array
                savedSearch.put(ParseConstants.KEY_KEYWORDS, new JSONArray());  // Clear previous keywords
                savedSearch.addAllUnique(ParseConstants.KEY_KEYWORDS,
                        Arrays.asList(mQueryText.trim().toLowerCase().split("\\s+")));
            }

            // Start Date
            if (mFilterOptions.getStartDate() != null) {
                savedSearch.put(ParseConstants.KEY_ACTIVITY_START_DATE,
                        DateUtil.convertToUNC(mFilterOptions.getStartDate()));
            }

            // End Date
            if (mFilterOptions.getEndDate() != null) {
                savedSearch.put(ParseConstants.KEY_ACTIVITY_END_DATE,
                        DateUtil.convertToUNC(mFilterOptions.getEndDate()));
            }

            // Filter Options
            savedSearch.put(ParseConstants.KEY_TYPE_ADVENTURE_CLUB, mFilterOptions.isTypeAdventureClub());
            savedSearch.put(ParseConstants.KEY_TYPE_BACKPACKING, mFilterOptions.isTypeBackpacking());
            savedSearch.put(ParseConstants.KEY_TYPE_CLIMBING, mFilterOptions.isTypeClimbing());
            savedSearch.put(ParseConstants.KEY_TYPE_DAY_HIKING, mFilterOptions.isTypeDayHiking());
            savedSearch.put(ParseConstants.KEY_TYPE_EXPLORERS, mFilterOptions.isTypeExplorers());
            savedSearch.put(ParseConstants.KEY_TYPE_EXPLORING_NATURE, mFilterOptions.isTypeExploringNature());
            savedSearch.put(ParseConstants.KEY_TYPE_GLOBAL_ADVENTURES, mFilterOptions.isTypeGlobalAdventures());
            savedSearch.put(ParseConstants.KEY_TYPE_MOUNTAIN_WORKSHOP, mFilterOptions.isTypeMountainWorkshop());
            savedSearch.put(ParseConstants.KEY_TYPE_NAVIGATION, mFilterOptions.isTypeNavigation());
            savedSearch.put(ParseConstants.KEY_TYPE_PHOTOGRAPHY, mFilterOptions.isTypePhotography());
            savedSearch.put(ParseConstants.KEY_TYPE_SAILING, mFilterOptions.isTypeSailing());
            savedSearch.put(ParseConstants.KEY_TYPE_SCRAMBLING, mFilterOptions.isTypeScrambling());
            savedSearch.put(ParseConstants.KEY_TYPE_SEA_KAYAKING, mFilterOptions.isTypeSeaKayaking());
            savedSearch.put(ParseConstants.KEY_TYPE_SKIING_SNOWBOARDING, mFilterOptions.isTypeSkiingSnowboarding());
            savedSearch.put(ParseConstants.KEY_TYPE_SNOWSHOEING, mFilterOptions.isTypeSnowshoeing());
            savedSearch.put(ParseConstants.KEY_TYPE_STEWARDSHIP, mFilterOptions.isTypeStewardship());
            savedSearch.put(ParseConstants.KEY_TYPE_TRAIL_RUNNING, mFilterOptions.isTypeTrailRunning());
            savedSearch.put(ParseConstants.KEY_TYPE_URBAN_ADVENTURE, mFilterOptions.isTypeUrbanAdventure());
            savedSearch.put(ParseConstants.KEY_TYPE_YOUTH, mFilterOptions.isTypeYouth());
            savedSearch.put(ParseConstants.KEY_RATING_FOR_BEGINNERS, mFilterOptions.isRatingForBeginners());
            savedSearch.put(ParseConstants.KEY_RATING_EASY, mFilterOptions.isRatingEasy());
            savedSearch.put(ParseConstants.KEY_RATING_MODERATE, mFilterOptions.isRatingModerate());
            savedSearch.put(ParseConstants.KEY_RATING_CHALLENGING, mFilterOptions.isRatingChallenging());
            savedSearch.put(ParseConstants.KEY_AUDIENCE_ADULTS, mFilterOptions.isAudienceAdults());
            savedSearch.put(ParseConstants.KEY_AUDIENCE_FAMILIES, mFilterOptions.isAudienceFamilies());
            savedSearch.put(ParseConstants.KEY_AUDIENCE_RETIRED_ROVERS, mFilterOptions.isAudienceRetiredRovers());
            savedSearch.put(ParseConstants.KEY_AUDIENCE_SINGLES, mFilterOptions.isAudienceSingles());
            savedSearch.put(ParseConstants.KEY_AUDIENCE_20_30_SOMETHINGS, mFilterOptions.isAudience2030Somethings());
            savedSearch.put(ParseConstants.KEY_AUDIENCE_YOUTH, mFilterOptions.isAudienceYouth());
            savedSearch.put(ParseConstants.KEY_BRANCH_THE_MOUNTAINEERS, mFilterOptions.isBranchTheMountaineers());
            savedSearch.put(ParseConstants.KEY_BRANCH_BELLINGHAM, mFilterOptions.isBranchBellingham());
            savedSearch.put(ParseConstants.KEY_BRANCH_EVERETT, mFilterOptions.isBranchEverett());
            savedSearch.put(ParseConstants.KEY_BRANCH_FOOTHILLS, mFilterOptions.isBranchFoothills());
            savedSearch.put(ParseConstants.KEY_BRANCH_KITSAP, mFilterOptions.isBranchKitsap());
            savedSearch.put(ParseConstants.KEY_BRANCH_OLYMPIA, mFilterOptions.isBranchOlympia());
            savedSearch.put(ParseConstants.KEY_BRANCH_OUTDOOR_CENTERS, mFilterOptions.isBranchOutdoorCenters());
            savedSearch.put(ParseConstants.KEY_BRANCH_SEATTLE, mFilterOptions.isBranchSeattle());
            savedSearch.put(ParseConstants.KEY_BRANCH_TACOMA, mFilterOptions.isBranchTacoma());
            savedSearch.put(ParseConstants.KEY_CLIMBING_BASIC_ALPINE, mFilterOptions.isClimbingBasicAlpine());
            savedSearch.put(ParseConstants.KEY_CLIMBING_INTERMEDIATE_ALPINE, mFilterOptions.isClimbingIntermediateAlpine());
            savedSearch.put(ParseConstants.KEY_CLIMBING_AID_CLIMB, mFilterOptions.isClimbingAidClimb());
            savedSearch.put(ParseConstants.KEY_CLIMBING_ROCK_CLIMB, mFilterOptions.isClimbingRockClimb());
            savedSearch.put(ParseConstants.KEY_SKIING_CROSS_COUNTRY, mFilterOptions.isSkiingCrossCountry());
            savedSearch.put(ParseConstants.KEY_SKIING_BACKCOUNTRY, mFilterOptions.isSkiingBackcountry());
            savedSearch.put(ParseConstants.KEY_SKIING_GLACIER, mFilterOptions.isSkiingGlacier());
            savedSearch.put(ParseConstants.KEY_SNOWSHOEING_BEGINNER, mFilterOptions.isSnowshoeingBeginner());
            savedSearch.put(ParseConstants.KEY_SNOWSHOEING_BASIC, mFilterOptions.isSnowshoeingBasic());
            savedSearch.put(ParseConstants.KEY_SNOWSHOEING_INTERMEDIATE, mFilterOptions.isSnowshoeingIntermediate());

            // Save search to Parse backend
            savedSearch.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {  // Error saving search
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle(getActivity().getString(R.string.error_title));
                        alert.setMessage(e.getMessage());
                        alert.show();
                    } else {  // Saving successful
                        // Determine the message to be shown to the user
                        if (mIsSavedSearch) {  // Saved existing search
                            Toast.makeText(getActivity(), getActivity().getString
                                    (R.string.toast_save_existing), Toast.LENGTH_SHORT).show();
                        }
                        else {  // Saved new search
                            Toast.makeText(getActivity(), getActivity().getString
                                    (R.string.toast_save_new), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        else {  // Error encountered trying to get existing saved search object from Parse
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(getActivity().getString(R.string.error_title));
            alert.setMessage(getActivity().getString(R.string.error_message_saved_search));
            alert.show();
        }
    }

    private void showSaveDialog() {
        // Check if the save is coming from saved search results
        if (mIsSavedSearch) {  // Yes
            // Save using existing name
            saveSearch(mSavedSearchName);
        } else {  // No - get a name from the user
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(getActivity().getString(R.string.dialog_save_title));
            alert.setMessage(getActivity().getString(R.string.dialog_save_message));

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setSingleLine();
            alert.setView(input);

            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (!input.getText().toString().trim().isEmpty()) {
                        // Check for existing Saved Search name from this user


                        // Save the search with the provided name
                        saveSearch(input.getText().toString().trim());
                    } else {  // Invalid name
                        showSaveDialog();  // Show dialog again
                    }
                }
            });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled - intentionally left blank
                }
            });

            alert.show();
        }
    }
}