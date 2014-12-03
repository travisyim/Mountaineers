package com.travisyim.mountaineers.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.travisyim.mountaineers.utils.ListUtil;
import com.travisyim.mountaineers.utils.OnParseTaskCompleted;
import com.travisyim.mountaineers.utils.ParseConstants;
import com.travisyim.mountaineers.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ActivitySearchFragment extends ListFragment implements OnParseTaskCompleted,
        FilterFragment.OnFiltersSelectedListener, ActivityDetailsFragment.OnFavoriteSelectedListener {
    private Fragment mActivityDetailsFragment;
    private FilterFragment mFilterFragment;
    private FilterOptions mFilterOptions;
    private List<MountaineerActivity> mActivityList = new ArrayList<MountaineerActivity>();
    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<String> mFavoritesList;
    private List<String> mTempSavedSearchList = new ArrayList<String>();
    private Date mPreviousSearchStartDate;
    private Date mPreviousSearchEndDate;
    private String mParentFragmentTitle;
    private String mSavedSearchName;
    private String mQueryText;
    private long mLastViewed;
    private int mActivityPosition;
    private boolean mAlreadyLoaded;
    private boolean mReturnFromFilter = false;
    private boolean mReturnFromDetails = false;
    private boolean mIsSubstantialUpdate = false;
    private boolean mIsCanceled = false;
    private boolean mIsSavedSearch = false;
    private boolean mIsUpdatingSavedSearch = false;
    private boolean mIsMaxLimitReached = false;

    private final String TAG = ActivitySearchFragment.class.getSimpleName() + ":";
    private static final String ARG_PARENT_TITLE = "parentFragmentTitle";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SAVED_SEARCH_NAME = "savedSearchName";
    private static final String ARG_FILTER_OPTIONS = "filterOptions";
    private static final String ARG_QUERY_TEXT = "queryText";
    private static final String ARG_LAST_VIEWED = "lastViewed";
    private static final String ARG_ACTIVITY_NAME = "activityName";
    private static final String ARG_LEADER_NAMES = "leaderNames";
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
         * fragment */
        // Get the reference to the filter options
        if (getArguments().getString(ARG_PARENT_TITLE) != null) {
            mIsSavedSearch = true;

            // Get the parent fragment's title
            mParentFragmentTitle = getArguments().getString(ARG_PARENT_TITLE);
            mSavedSearchName = getArguments().getString(ARG_SAVED_SEARCH_NAME);
            mFilterOptions = (FilterOptions) getArguments().getSerializable(ARG_FILTER_OPTIONS);
            mQueryText = getArguments().getString(ARG_QUERY_TEXT);  // Query text
            mLastViewed = getArguments().getLong(ARG_LAST_VIEWED);

            // Update ActionBar title to show saved search name
            getActivity().getActionBar().setTitle(mSavedSearchName);

            /* Check to see if this is a different search being opened in this fragment (applies
             * only to saved searches) */
            if (mAlreadyLoaded) {  // Previous saved search results already loaded
                mIsUpdatingSavedSearch = true;

                // Run filter update code to determine activity list should be treated
                FilterUpdates();

                // Ensure the desired post filter update behavior (just like if filters were just applied)
                mReturnFromFilter = true;
            }

            // Google Analytics tracking code - Saved search load
            Tracker t = ((MountaineersApp) getActivity().getApplication()).getTracker
                    (MountaineersApp.TrackerName.APP_TRACKER);
            t.setScreenName(getString(R.string.title_browse) + " (" + getString(R.string.title_saved_searches) +")");
            t.send(new HitBuilders.AppViewBuilder().build());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (!mAlreadyLoaded) {
            /* If the FilterOptions object was not passed in, then create it.  The FilterOptions
             * object is only passed in when opening a Saved Search */
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

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorScheme(R.color.swipe_refresh1, R.color.swipe_refresh2,
                R.color.swipe_refresh3, R.color.swipe_refresh4);

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

        // Ensure there is a user logged in
        if (ParseUser.getCurrentUser() != null) {
            /* Get full activity list from the latest Parse data if this is the first time creating
             * this fragment */
            if (!mAlreadyLoaded) {
                mAlreadyLoaded = true;

                // Flag as updating activities
                ((MainActivity) getActivity()).setLoadingActivities(true);
                mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator

                getActivityList();  // Get the activity list for the defined filter and query options
            }
            // User returning from filter options or if it is a saved search then it could be entering activity search
            else if (mReturnFromFilter) {
                mReturnFromFilter = false;  // Reset this flag

                // Being returned from filter application that requires a new Parse query (significant date change)
                if (mIsSubstantialUpdate) {
                    mIsSubstantialUpdate = false;  // Reset this flag

                    // Flag as updating activities
                    ((MainActivity) getActivity()).setLoadingActivities(true);
                    mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator
                }
            }
            /* Refresh favorite activity status if coming from another fragment other than the filter
             * or activity detail fragments */
            else if (!mReturnFromDetails && !mReturnFromFilter) {
                /* Refresh favorites (e.g. user has deselected a favorite Completed activity and
                 * that favorite status needs to the same activity in this list */
                mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator

                // Clear temporary saved search name array
                mTempSavedSearchList.clear();

                /* Update activity list to reflect latest set of Favorite activities.  This is
                 * typically completed in the ActivityLoader but we are loading any activities -
                 * just updating the showing activities */
                refreshFavorites();
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

        if (mIsSavedSearch && !mIsCanceled) {  // Only applies when returning to saved search fragment
            // Reset the title back to that of the parent fragment
            getActivity().getActionBar().setTitle(mParentFragmentTitle);

            // Google Analytics tracking code - User Profile
            Tracker t = ((MountaineersApp) getActivity().getApplication()).getTracker
                    (MountaineersApp.TrackerName.APP_TRACKER);
            t.setScreenName(getString(R.string.title_saved_searches));
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

        // Check to see if the Activity Search fragment is still updating results
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
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        // Not updating so load filter fragment
                        if (mFilterFragment == null) {
                            // TODO: Fix up section numbering scheme
                            mFilterFragment = FilterFragment.newInstance
                                    ((float) (this.getArguments().getFloat(ARG_SECTION_NUMBER) + 0.1),
                                    mFilterOptions, getActivity().getActionBar().getTitle().toString());
                        }
                        else {  // Update the filter fragment's parent fragment title
                            Bundle args = mFilterFragment.getArguments();

                            /* Pass the following in a bundle because the original title is created
                             * through newInstance.  In this case, we are reusing an existing
                             * FilterFragment. */
                            args.putString(ARG_PARENT_TITLE,
                                    getActivity().getActionBar().getTitle().toString());
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
            case R.id.action_save:
                showSaveDialog();

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
         * Activity Search fragment */
        mFilterOptions = filterOptions;

        FilterUpdates();  // Apply any filter updates to the list
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
                mActivityList.addAll(ActivityLoader.load(resultList, mFavoritesList));

                // TODO: Can this go at the beginning?  It's behaviour differs from Favorites fragment, for example
                // Set the default empty textview corresponding to the listview
                getListView().setEmptyView(getActivity().findViewById(R.id.textViewEmpty));

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

    private void FilterUpdates() {
        /* The logic in this method controls when to make new queries to the Parse backend.  Since
         * requests are limited, the program should be most efficient at determining when new
         * requests should be made */
        final ActivityAdapter listAdapter = (ActivityAdapter) getListAdapter();
        mIsMaxLimitReached = false;

        // If this is a saved search then update the search's last view date before updating list
        if (mIsSavedSearch) {
            listAdapter.updateLastViewed(mLastViewed);
        }

        /* Either submit a new query or apply filters depending on how the start/end date selection
         * has changed (if it has at all) */
        // Previous start and end dates were both undefined
        if (mPreviousSearchStartDate == null && mPreviousSearchEndDate == null) {
            // Current start and end dates are both undefined
            if (mFilterOptions.getStartDate() == null && mFilterOptions.getEndDate() == null) {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    mIsMaxLimitReached = true;
                    showMaxLimitToast();
                }

                // Apply both text and filter options to the activity list
                listAdapter.applyTextFilter(mQueryText);
                listAdapter.applyFilterOptions(mFilterOptions);
            }
            // Current start date is defined and end date is undefined
            else if (mFilterOptions.getStartDate() != null && mFilterOptions.getEndDate() == null) {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    // Current start date is equal to or prior to previous min start date
                    if (mFilterOptions.getStartDate().getTime() <= listAdapter.getMinStartDate().getTime()) {
                        mIsMaxLimitReached = true;
                        showMaxLimitToast();
                    }
                }

                // Apply both text and filter options to the activity list
                listAdapter.applyTextFilter(mQueryText);
                listAdapter.applyFilterOptions(mFilterOptions);
            }
            // Current start date is undefined and end date is defined
            else if (mFilterOptions.getStartDate() == null && mFilterOptions.getEndDate() != null) {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    // Current end date is equal to or past previous max start date
                    if (mFilterOptions.getEndDate().getTime() >= listAdapter.getMaxStartDate().getTime()) {
                        mIsMaxLimitReached = true;
                        showMaxLimitToast();

                        // Apply both text and filter options to the activity list
                        listAdapter.applyTextFilter(mQueryText);
                        listAdapter.applyFilterOptions(mFilterOptions);
                    } else {  // Current end date is prior to previous max start date
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                }
                // Previous results generated less than the maximum number of results allowed
                else {
                    // Apply both text and filter options to the activity list
                    listAdapter.applyTextFilter(mQueryText);
                    listAdapter.applyFilterOptions(mFilterOptions);
                }
            }
            // Current start and end dates are both defined
            else {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    // Current end date is equal to or past previous max start date
                    if (mFilterOptions.getEndDate().getTime() >= listAdapter.getMaxStartDate().getTime()) {
                        // Current start date is equal to or prior to previous min start date
                        if (mFilterOptions.getStartDate().getTime() <= listAdapter.getMinStartDate().getTime()) {
                            mIsMaxLimitReached = true;
                            showMaxLimitToast();
                        }

                        // Apply both text and filter options to the activity list
                        listAdapter.applyTextFilter(mQueryText);
                        listAdapter.applyFilterOptions(mFilterOptions);
                    }
                    // Current end date is past previous min start date
                    else if (mFilterOptions.getEndDate().getTime() > listAdapter.getMinStartDate().getTime()) {
                        // Current start date is past previous min start date
                        if (mFilterOptions.getStartDate().getTime() > listAdapter.getMinStartDate().getTime()) {
                            // Apply both text and filter options to the activity list
                            listAdapter.applyTextFilter(mQueryText);
                            listAdapter.applyFilterOptions(mFilterOptions);
                        } else {  // Current start date is equal to or prior to previous min start date
                            /* Flag that there's a Parse update that needs to occur so we can start the swipe
                             * refreshing (does not work if we call the refreshing command directly here) */
                            mIsSubstantialUpdate = true;

                            // Search again
                            getActivityList();
                        }
                    }
                    // Current end date is equal to or prior to previous min start date
                    else {
                            /* Flag that there's a Parse update that needs to occur so we can start the swipe
                             * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                }
                // Previous results generated less than the maximum number of results allowed
                else {
                    // Apply both text and filter options to the activity list
                    listAdapter.applyTextFilter(mQueryText);
                    listAdapter.applyFilterOptions(mFilterOptions);
                }
            }
        }

        // Previous start date was defined and end date was undefined
        else if (mPreviousSearchStartDate != null && mPreviousSearchEndDate == null) {
            // Current start and end dates are both undefined
            if (mFilterOptions.getStartDate() == null && mFilterOptions.getEndDate() == null) {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    mIsMaxLimitReached = true;
                    showMaxLimitToast();

                    // Apply both text and filter options to the activity list
                    listAdapter.applyTextFilter(mQueryText);
                    listAdapter.applyFilterOptions(mFilterOptions);
                }
                // Previous results generated less than the maximum number of results allowed
                else {
                    /* Flag that there's a Parse update that needs to occur so we can start the swipe
                     * refreshing (does not work if we call the refreshing command directly here) */
                    mIsSubstantialUpdate = true;

                    // Search again
                    getActivityList();
                }
            }
            // Current start date is defined and end date is undefined
            else if (mFilterOptions.getStartDate() != null && mFilterOptions.getEndDate() == null) {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    // Current start date is equal to or prior to previous min start date
                    if (mFilterOptions.getStartDate().getTime() <= listAdapter.getMinStartDate().getTime()) {
                        mIsMaxLimitReached = true;
                        showMaxLimitToast();
                    }

                    // Apply both text and filter options to the activity list
                    listAdapter.applyTextFilter(mQueryText);
                    listAdapter.applyFilterOptions(mFilterOptions);
                }
                // Previous results generated less than the maximum number of results allowed
                else {
                    // Current start date is equal to or past previous start date
                    if (mFilterOptions.getStartDate().getTime() >= mPreviousSearchStartDate.getTime()) {
                        // Apply both text and filter options to the activity list
                        listAdapter.applyTextFilter(mQueryText);
                        listAdapter.applyFilterOptions(mFilterOptions);
                    } else {  // Current start date is prior to previous start date
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                }
            }
            // Current start date is undefined and end date is defined
            else if (mFilterOptions.getStartDate() == null && mFilterOptions.getEndDate() != null) {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    // Current end date is equal to or past previous max start date
                    if (mFilterOptions.getEndDate().getTime() >= listAdapter.getMaxStartDate().getTime()) {
                        mIsMaxLimitReached = true;
                        showMaxLimitToast();

                        // Apply both text and filter options to the activity list
                        listAdapter.applyTextFilter(mQueryText);
                        listAdapter.applyFilterOptions(mFilterOptions);
                    } else {  // Current end date is prior to previous max start date
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                }
                // Previous results generated less than the maximum number of results allowed
                else {
                    /* Flag that there's a Parse update that needs to occur so we can start the swipe
                     * refreshing (does not work if we call the refreshing command directly here) */
                    mIsSubstantialUpdate = true;

                    // Search again
                    getActivityList();
                }
            }
            // Current start and end dates are both defined
            else {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    // Current end date is equal to or past previous max start date
                    if (mFilterOptions.getEndDate().getTime() >= listAdapter.getMaxStartDate().getTime()) {
                        // Current start date is equal to or prior to previous min start date
                        if (mFilterOptions.getStartDate().getTime() <= listAdapter.getMinStartDate().getTime()) {
                            mIsMaxLimitReached = true;
                            showMaxLimitToast();
                        }

                        // Apply both text and filter options to the activity list
                        listAdapter.applyTextFilter(mQueryText);
                        listAdapter.applyFilterOptions(mFilterOptions);
                    }
                    // Current end date is past previous min start date
                    else if (mFilterOptions.getEndDate().getTime() > listAdapter.getMinStartDate().getTime()) {
                        // Current start date is past previous min start date
                        if (mFilterOptions.getStartDate().getTime() > listAdapter.getMinStartDate().getTime()) {
                            // Apply both text and filter options to the activity list
                            listAdapter.applyTextFilter(mQueryText);
                            listAdapter.applyFilterOptions(mFilterOptions);
                        }
                        // Current start date is equal to or prior to previous min start date
                        else {
                            /* Flag that there's a Parse update that needs to occur so we can start the swipe
                             * refreshing (does not work if we call the refreshing command directly here) */
                            mIsSubstantialUpdate = true;

                            // Search again
                            getActivityList();
                        }
                    } else {  // Current end date is equal to or prior to previous min start date
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                }
                // Previous results generated less than the maximum number of results allowed
                else {
                    if (mFilterOptions.getStartDate().getTime() < mPreviousSearchStartDate.getTime()) {
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    } else {
                        // Apply both text and filter options to the activity list
                        listAdapter.applyTextFilter(mQueryText);
                        listAdapter.applyFilterOptions(mFilterOptions);
                    }
                }
            }
        }

        // Previous start date was undefined and end date was defined
        else if (mPreviousSearchStartDate == null && mPreviousSearchEndDate != null) {
            // Current end date is undefined
            if (mFilterOptions.getEndDate() == null) {
                /* Flag that there's a Parse update that needs to occur so we can start the swipe
                 * refreshing (does not work if we call the refreshing command directly here) */
                mIsSubstantialUpdate = true;

                // Search again
                getActivityList();
            }
            // Current start date is undefined and end date is defined
            else if (mFilterOptions.getStartDate() == null && mFilterOptions.getEndDate() != null) {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    // Current end date is equal to the previous end date
                    if (mFilterOptions.getEndDate().getTime() == mPreviousSearchEndDate.getTime()) {
                        mIsMaxLimitReached = true;
                        showMaxLimitToast();

                        // Apply both text and filter options to the activity list
                        listAdapter.applyTextFilter(mQueryText);
                        listAdapter.applyFilterOptions(mFilterOptions);
                    } else {  // Current end date has changed from previous end date
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                }
                // Previous results generated less than the maximum number of results allowed
                else {
                    // Current end date is past the previous end date
                    if (mFilterOptions.getEndDate().getTime() > mPreviousSearchEndDate.getTime()) {
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                    // Current end date is equal to or prior to the previous end date
                    else {
                        // Apply both text and filter options to the activity list
                        listAdapter.applyTextFilter(mQueryText);
                        listAdapter.applyFilterOptions(mFilterOptions);
                    }
                }
            }
            // Current start and end dates are both defined
            else {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    // Current end date is past the previous end date
                    if (mFilterOptions.getEndDate().getTime() > mPreviousSearchEndDate.getTime()) {
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                    // Current end date is past the previous min start date
                    else if (mFilterOptions.getEndDate().getTime() > listAdapter.getMinStartDate().getTime()) {
                        // Current end date is past the previous min start date
                        if (mFilterOptions.getStartDate().getTime() > listAdapter.getMinStartDate().getTime()) {
                            // Apply both text and filter options to the activity list
                            listAdapter.applyTextFilter(mQueryText);
                            listAdapter.applyFilterOptions(mFilterOptions);
                        }
                        // Current end date is equal to or prior to the previous min start date
                        else {
                            /* Flag that there's a Parse update that needs to occur so we can start the swipe
                             * refreshing (does not work if we call the refreshing command directly here) */
                            mIsSubstantialUpdate = true;

                            // Search again
                            getActivityList();
                        }
                    } else {  // Current end date is equal to or prior to the previous min start date
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();

                    }
                }
                // Previous results generated less than the maximum number of results allowed
                else {
                    // Current end date is past the previous end date
                    if (mFilterOptions.getEndDate().getTime() > mPreviousSearchEndDate.getTime()) {
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                    // Current end date is equal to or prior to the previous end date
                    else {
                        // Apply both text and filter options to the activity list
                        listAdapter.applyTextFilter(mQueryText);
                        listAdapter.applyFilterOptions(mFilterOptions);
                    }
                }
            }
        }

        // Previous start and end dates were both defined
        else {
            // Current end date is both undefined
            if (mFilterOptions.getEndDate() == null) {
                /* Flag that there's a Parse update that needs to occur so we can start the swipe
                 * refreshing (does not work if we call the refreshing command directly here) */
                mIsSubstantialUpdate = true;

                // Search again
                getActivityList();
            }
            // Current start date is undefined and end date is defined
            else if (mFilterOptions.getStartDate() == null && mFilterOptions.getEndDate() != null) {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    // Current end date is equal to previous end date
                    if (mFilterOptions.getEndDate().getTime() == mPreviousSearchEndDate.getTime()) {
                        mIsMaxLimitReached = true;
                        showMaxLimitToast();

                        // Apply both text and filter options to the activity list
                        listAdapter.applyTextFilter(mQueryText);
                        listAdapter.applyFilterOptions(mFilterOptions);
                    }
                    // Current end date is not equal to previous end date
                    else {
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                }
                // Previous results generated less than the maximum number of results allowed
                else {
                    /* Flag that there's a Parse update that needs to occur so we can start the swipe
                     * refreshing (does not work if we call the refreshing command directly here) */
                    mIsSubstantialUpdate = true;

                    // Search again
                    getActivityList();
                }
            }
            // Current start and end dates are both defined
            else {
                // Previous results generated maximum number of results allowed
                if (listAdapter.getMasterActivityListCount() == ParseConstants.QUERY_LIMIT) {
                    // Current end date is past the previous end date
                    if (mFilterOptions.getEndDate().getTime() > mPreviousSearchEndDate.getTime()) {
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                    // Current end date is past the previous min start date
                    else if (mFilterOptions.getEndDate().getTime() > listAdapter.getMinStartDate().getTime()) {
                        // Current start date is past the previous min start date
                        if (mFilterOptions.getStartDate().getTime() > listAdapter.getMinStartDate().getTime()) {
                            // Apply both text and filter options to the activity list
                            listAdapter.applyTextFilter(mQueryText);
                            listAdapter.applyFilterOptions(mFilterOptions);
                        }
                        // Current start date is equal to or prior to the previous min start date
                        else {
                            /* Flag that there's a Parse update that needs to occur so we can start the swipe
                             * refreshing (does not work if we call the refreshing command directly here) */
                            mIsSubstantialUpdate = true;

                            // Search again
                            getActivityList();
                        }
                    }
                    else {
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                }
                // Previous results generated less than the maximum number of results allowed
                else {
                    if (mFilterOptions.getEndDate().getTime() > mPreviousSearchEndDate.getTime()) {
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                    else if (mFilterOptions.getEndDate().getTime() >= mPreviousSearchStartDate.getTime()) {
                        if (mFilterOptions.getStartDate().getTime() >= mPreviousSearchStartDate.getTime()) {
                            // Apply both text and filter options to the activity list
                            listAdapter.applyTextFilter(mQueryText);
                            listAdapter.applyFilterOptions(mFilterOptions);
                        }
                        else {
                            /* Flag that there's a Parse update that needs to occur so we can start the swipe
                             * refreshing (does not work if we call the refreshing command directly here) */
                            mIsSubstantialUpdate = true;

                            // Search again
                            getActivityList();
                        }
                    }
                    else {
                        /* Flag that there's a Parse update that needs to occur so we can start the swipe
                         * refreshing (does not work if we call the refreshing command directly here) */
                        mIsSubstantialUpdate = true;

                        // Search again
                        getActivityList();
                    }
                }
            }
        }

        /* If filter updates are being applied, refresh the favorites as well (it is
         * possible that there were favorite changes in the previous saved search) */
        if (mIsUpdatingSavedSearch) {
            mIsUpdatingSavedSearch = false;  // Saved search updating logic is done

            if (!mIsSubstantialUpdate) {
                refreshFavorites();
            }
        }
    }

    private void getActivityList() {
        // This method is called to retrieve activities from the backend
        ParseQuery<ParseObject> query;

        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            // Get list of favorite activities
            List<String> tempFavorites = ParseUser.getCurrentUser().getList(ParseConstants.KEY_FAVORITES);
            mFavoritesList = ListUtil.copy(tempFavorites);

            query = ParseQuery.getQuery(ParseConstants.CLASS_ACTIVITY);

            // Track start and end date filter values
            mPreviousSearchStartDate = mFilterOptions.getStartDate();
            mPreviousSearchEndDate = mFilterOptions.getEndDate();

            // Apply start date filter only if it is defined (i.e. not null)
            if (mFilterOptions.getStartDate() != null) {  // Start date defined
                query.whereGreaterThanOrEqualTo(ParseConstants.KEY_ACTIVITY_START_DATE,
                        DateUtil.convertToUNC(mFilterOptions.getStartDate()));
            }

            // Apply end date filter only if it is defined (i.e. not null)
            if (mFilterOptions.getEndDate() != null) {  // End date defined
                query.whereLessThanOrEqualTo(ParseConstants.KEY_ACTIVITY_START_DATE,
                        DateUtil.convertToUNC(mFilterOptions.getEndDate()));
            }

        /* A query constraint is not set on the end date.  Instead, up to 500 activities will be
         * returned (max for Parse is 1000) and if the end date is captured, then limit the
         * results shown.  However, if the end date is not reached within the 500 result limit, the
         * user will be shown an error message to reduce the date range. */

            // Sort in reverse order to ensure we get the latest start date activities
            query.orderByDescending(ParseConstants.KEY_ACTIVITY_START_DATE);
            query.addDescendingOrder(ParseConstants.KEY_ACTIVITY_TITLE);
            query.setLimit(ParseConstants.QUERY_LIMIT); // limit to 500 results max

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
                ActivityAdapter adapter;

                if (!mIsSavedSearch) {
                    adapter = new ActivityAdapter(getListView().getContext(), mActivityList, null);
                }
                else {
                    adapter = new ActivityAdapter(getListView().getContext(), mActivityList,
                            new Date(mLastViewed));
                }

                setListAdapter(adapter);
            }
            /* Results already shown so update the list (assume no changes and user is still looking
             * at a saved search).  lastViewed time is kept the same (this can e updated in the
             * future). */
            else {
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
                        List<String> tempFavorites = ParseUser.getCurrentUser().getList(ParseConstants.KEY_FAVORITES);
                        mFavoritesList = ListUtil.copy(tempFavorites);

                        for (MountaineerActivity activity : mActivityList) {
                            if (mFavoritesList != null) {  // Favorite activities in the list
                                favoriteObjectIdPosition = mFavoritesList.indexOf(activity.getObjectID());

                                if (favoriteObjectIdPosition >= 0) {  // This activity is a favorite
                                    activity.setFavorite(true);  // Set favorite flag to true

                                    // Remove the favorite ObjectId from the list (for efficiency)
                                    mFavoritesList.remove(favoriteObjectIdPosition);
                                } else {  // Activity object ID not found in favorites list
                                    activity.setFavorite(false);
                                }
                            } else {  // No favorite activities in the list
                                activity.setFavorite(false);
                            }
                        }

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
                // Add all unique keywords
                savedSearch.addAllUnique(ParseConstants.KEY_KEYWORDS,
                        Arrays.asList(StringUtil.getKeywords(mQueryText)));
            }

            // Start Date
            if (mFilterOptions.getStartDate() != null) {  // Defined
                savedSearch.put(ParseConstants.KEY_ACTIVITY_START_DATE,
                        DateUtil.convertToUNC(mFilterOptions.getStartDate()));
            }
            else {  // Undefined
                savedSearch.put(ParseConstants.KEY_ACTIVITY_START_DATE, JSONObject.NULL);
            }

            // End Date
            if (mFilterOptions.getEndDate() != null) {  // Defined
                savedSearch.put(ParseConstants.KEY_ACTIVITY_END_DATE,
                        DateUtil.convertToUNC(mFilterOptions.getEndDate()));
            }
            else {  // Undefined
                savedSearch.put(ParseConstants.KEY_ACTIVITY_START_DATE, JSONObject.NULL);
            }

            // Filter Options
            savedSearch.put(ParseConstants.KEY_TYPE_ADVENTURE_CLUB, mFilterOptions.isTypeAdventureClub());
            savedSearch.put(ParseConstants.KEY_TYPE_BACKPACKING, mFilterOptions.isTypeBackpacking());
            savedSearch.put(ParseConstants.KEY_TYPE_CLIMBING, mFilterOptions.isTypeClimbing());
            savedSearch.put(ParseConstants.KEY_TYPE_DAY_HIKING, mFilterOptions.isTypeDayHiking());
            savedSearch.put(ParseConstants.KEY_TYPE_EXPLORERS, mFilterOptions.isTypeExplorers());
            savedSearch.put(ParseConstants.KEY_TYPE_EXPLORING_NATURE, mFilterOptions.isTypeExploringNature());
            savedSearch.put(ParseConstants.KEY_TYPE_GLOBAL_ADVENTURES, mFilterOptions.isTypeGlobalAdventures());
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
                    String name = input.getText().toString().trim();

                    if (!name.isEmpty()) {
                        // Check for existing Saved Search name from this user
                        if (!compareSavedSearchNames(name,
                                ((MainActivity) getActivity()).getSavedSearchList()) &&
                                !compareSavedSearchNames(name, mTempSavedSearchList)) {
                            // Save the search with the provided name
                            saveSearch(name);
                            mTempSavedSearchList.add(name);  // Add save search name to temp list
                        }
                        else {
                            showSaveDialog();  // Show dialog again
                        }
                    } else {  // Invalid name
                        showSaveDialog();  // Show dialog again
                    }
                }

                private boolean compareSavedSearchNames(final String name, final List<String> nameList) {
                    Toast toast;

                    for (String str : nameList) {
                        if (name.equals(str)) {
                            // Show toast about having an identically named saved search
                            toast = Toast.makeText(getActivity(), getActivity().getString
                                    (R.string.toast_error_duplicate), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();

                            return true;
                        }
                    }

                    return false;
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

    private void showMaxLimitToast() {
        Toast toast = Toast.makeText(getActivity(), getActivity().getString
                (R.string.toast_max_activities), Toast.LENGTH_LONG);
        ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                .setGravity(Gravity.CENTER_HORIZONTAL);
        toast.show();
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
}