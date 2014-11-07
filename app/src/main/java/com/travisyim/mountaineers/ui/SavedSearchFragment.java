package com.travisyim.mountaineers.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.adapters.ActivityAdapter;
import com.travisyim.mountaineers.objects.Mountaineer;
import com.travisyim.mountaineers.objects.MountaineerActivity;
import com.travisyim.mountaineers.utils.ActivityLoader;
import com.travisyim.mountaineers.utils.OnParseTaskCompleted;
import com.travisyim.mountaineers.utils.ParseConstants;
import com.travisyim.mountaineers.utils.SavedSearchLoader;

import java.util.ArrayList;
import java.util.List;

public class SavedSearchFragment extends ListFragment implements OnParseTaskCompleted {
    private Fragment mActivityDetailsFragment;
    private Mountaineer mMember;
    private FilterFragment mFilterFragment;
    private List<MountaineerActivity> mActivityList = new ArrayList<MountaineerActivity>();
    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mQueryText;
    private int activityPosition;
    private boolean newSearch;
    private boolean mAlreadyLoaded = false;
    private boolean mIsCollapsed = false;
    private boolean mHasSearchLostFocus = false;
    private boolean mIsCanceled = false;

    private final String TAG = SavedSearchFragment.class.getSimpleName() + ":";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_ACTIVITY_NAME = "activityName";
    private static final String ARG_ACTIVITY_URL = "activityURL";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_FAVORITE = "isFavorite";
    private static final String ARG_ACTIVITY_START_DATE = "startDate";
    private static final String ARG_ACTIVITY_END_DATE = "endDate";
    private static final String ARG_ACTIVITY_REG_OPEN_DATE = "regOpenDate";
    private static final String ARG_ACTIVITY_REG_CLOSE_DATE = "regCloseDate";
    private static final String ARG_MEMBER = "member";

    // Returns a new instance of this fragment for the given section number
    public static SavedSearchFragment newInstance(int sectionNumber) {
        SavedSearchFragment fragment = new SavedSearchFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SavedSearchFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));

        // Get Mountaineer member from bundle
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
        View rootView = inflater.inflate(R.layout.fragment_saved_search, container, false);

        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorScheme(R.color.swipe_refresh1, R.color.swipe_refresh2,
                R.color.swipe_refresh3, R.color.swipe_refresh4);

        // Load saved search list if this is the first time creating this fragment
        if (savedInstanceState == null && !mAlreadyLoaded) {
            mAlreadyLoaded = true;

            // Flag as updating saved search update count
            ((MainActivity) getActivity()).setLoadingActivities(true);
            mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator
            getSavedSearchList();  // Get saved search update count from Parse data
        }

        // Setup SwipeRefresh behavior
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Flag as updating saved search list
                ((MainActivity) getActivity()).setLoadingActivities(true);
                getSavedSearchList();  // Get saved search update count from Parse data
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure the user is logged in before completing the following activities
        if (ParseUser.getCurrentUser() != null) {
            // Flag as updating saved search update count
            ((MainActivity) getActivity()).setLoadingActivities(true);
            mSwipeRefreshLayout.setRefreshing(true);  // Turn on update indicator
            getSavedSearchList();  // Get saved search update count from Parse data
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.saved_search, menu);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Fragment fragment;

        super.onListItemClick(l, v, position, id);

        // Check to see if the Favorite Activity fragment is still updating results
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mHasSearchLostFocus = true;  // Flag this as clicked
            // Set the keyboard state as hidden
            ((ActivityAdapter) getListAdapter()).setKeyboardState(false);

            activityPosition = position;  // Capture position of listitem clicked

            // Launch ActivityDetails fragment to show activity's webpage
            if (mActivityDetailsFragment == null) {
                // TODO: Fix up section numbering scheme
                fragment = ActivityDetailsFragment.newInstance
                        (this, (float) (this.getArguments().getInt(ARG_SECTION_NUMBER) + 0.1),
                                getActivity().getActionBar().getTitle().toString());
                mActivityDetailsFragment = fragment;
            }
            else {
                fragment = mActivityDetailsFragment;
            }

            Bundle args = fragment.getArguments();

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
            getFragmentManager().beginTransaction().replace(R.id.container, fragment)
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onParseTaskCompleted(List<ParseObject> resultList, ParseException e) {
        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            // This method is called when the task of retrieving the full set of activities is complete
            mActivityList.clear();

            if (e == null) {
                mActivityList.addAll(ActivityLoader.load(resultList, true));

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

    private void getSavedSearchList() {
        ParseQuery<ParseObject> query;
        List<ParseObject> results = null;

        // This method is called to retrieve all of the saved searches from the backend
        // Intermediate check to ensure all processes have not been canceled
        if (!mIsCanceled) {
            query = ParseQuery.getQuery(ParseConstants.CLASS_SAVED_SEARCH);
            query.whereEqualTo(ParseConstants.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
            query.orderByAscending(ParseConstants.KEY_SAVE_NAME);
            query.setLimit(1000); // limit to 1000 results max

            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> results, ParseException e) {
                    if (e == null) {  // No error



                    }
                    else {  // No matches?
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
                }
            });

            // Load saved search results and assign to the current Mountaineer object
            mMember.setSavedSearches(SavedSearchLoader.load(results));

        }
        else {
            mIsCanceled = false;
        }

//            if (favoritesList != null) {
//                query = ParseQuery.getQuery(ParseConstants.CLASS_ACTIVITY);
//                query.whereContainedIn(ParseConstants.KEY_OBJECT_ID, favoritesList);
//
//                query.findInBackground(new FindCallback<ParseObject>() {
//                    public void done(List<ParseObject> resultList, ParseException e) {
//                        // Forward the results and error variable to the custom OnParseTaskCompleted method
//                        onParseTaskCompleted(resultList, e);
//                    }
//                });
//            } else {
//
//            }

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

                // Apply user defined filters to the latest list of activities
                ((ActivityAdapter) getListAdapter()).applyTextFilter(mQueryText);
//                ((ActivityAdapter) getListAdapter()).applyFilterOptions(mFilterOptions);
            }

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
        mAlreadyLoaded = false;
        mIsCanceled = false;
        mQueryText = "";  // Reset search text
        mFilterFragment = null;
//        mFilterOptions = new FilterOptions();  // Create new filter options
    }
}