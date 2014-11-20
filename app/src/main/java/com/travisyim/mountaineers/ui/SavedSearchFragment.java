package com.travisyim.mountaineers.ui;

import android.app.ActionBar;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.travisyim.mountaineers.MountaineersApp;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.adapters.SavedSearchAdapter;
import com.travisyim.mountaineers.objects.FilterOptions;
import com.travisyim.mountaineers.objects.Mountaineer;
import com.travisyim.mountaineers.objects.SavedSearch;
import com.travisyim.mountaineers.utils.OnParseTaskCompleted;
import com.travisyim.mountaineers.utils.ParseConstants;
import com.travisyim.mountaineers.utils.SavedSearchComparator;
import com.travisyim.mountaineers.utils.SavedSearchLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SavedSearchFragment extends ListFragment implements OnParseTaskCompleted {
    private Fragment mActivitySearchFragment;
    private Mountaineer mMember;
    private FilterOptions mFilterOptions;
    private Menu mMenu;
    private List<SavedSearch> mSavedSearchList = new ArrayList<SavedSearch>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mIsCanceled = false;
    private boolean mIsEditMode = false;

    private final String TAG = SavedSearchFragment.class.getSimpleName() + ":";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SAVED_SEARCH_NAME = "savedSearchName";
    private static final String ARG_FILTER_OPTIONS = "filterOptions";
    private static final String ARG_QUERY_TEXT = "queryText";
    private static final String ARG_LAST_VIEWED = "lastViewed";
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

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorScheme(R.color.swipe_refresh1, R.color.swipe_refresh2,
                R.color.swipe_refresh3, R.color.swipe_refresh4);

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

        // Make sure the user is logged in before updating saved searches
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

        mMenu = menu;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (!mIsEditMode) {  // Ensure the fragment is not in edit mode
            // Check to see if the Saved Search fragment is still updating results
            if (!mSwipeRefreshLayout.isRefreshing()) {
                // Google Analytics tracking code - Edit user profile
                Tracker t = ((MountaineersApp) getActivity().getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName(getString(R.string.title_browse) + " (" + getString(R.string.title_saved_searches) +")");
                t.send(new HitBuilders.AppViewBuilder().build());

            /* Tell Parse backend that user is now viewing this saved search so go ahead and update
             * the last viewed timestamp and reset the update counter to 0 */
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(ParseConstants.KEY_OBJECT_ID, mMember.getSavedSearchList().get(position).getObjectID());

                // Run cloud code to reset counter and timestamp for selected saved search
                ParseCloud.callFunctionInBackground("resetUpdateCounter", params, new FunctionCallback<Object>() {
                    @Override
                    public void done(Object o, ParseException e) {
                        if (e != null) {  // An error occurred running the cloud function
                            Toast.makeText(getActivity(), getActivity().getString(R.string.toast_error_reset),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

                // Create new Filter Options if it does not already exist
                if (mFilterOptions == null) {
                    mFilterOptions = new FilterOptions();
                }

                // Load all filter options for the selected saved search into FilterOptions object
                loadFilterOptions(mMember.getSavedSearchList().get(position));

                // Launch ActivitySearch fragment to show search results
                if (mActivitySearchFragment == null) {
                    // TODO: Fix up section numbering scheme
                    // 2 represents (position + 1) drawer index for Activity Search fragment
                    mActivitySearchFragment = ActivitySearchFragment.newInstance
                            (2, getActivity().getActionBar().getTitle().toString());
                }

                Bundle args = mActivitySearchFragment.getArguments();

                // Pass the following in a bundle because the data changes with each click
                // Saved search name
                args.putString(ARG_SAVED_SEARCH_NAME, mSavedSearchList.get(position).getSearchName());

                // Filter options
                args.putSerializable(ARG_FILTER_OPTIONS, mFilterOptions);

                // Search query text
                args.putString(ARG_QUERY_TEXT, mSavedSearchList.get(position).getQueryText());

                // Last Viewed Date (in long format)
                args.putLong(ARG_LAST_VIEWED, mSavedSearchList.get(position).getLastAccessDateDate().getTime());

                // Load activity search fragment with slide animations
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left,
                        R.animator.slide_in_right, R.animator.slide_out_right);

                transaction.replace(R.id.container, mActivitySearchFragment,
                        mSavedSearchList.get(position).getSearchName()).addToBackStack(null).commit();
            } else {  // Still updating so prevent user from moving to the activity search page
                Toast.makeText(getActivity(), getActivity().getString(R.string.toast_activity_search_wait),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:  // Edit
                // Hide Edit and show Done menu buttons
                item.setVisible(false);
                mMenu.findItem(R.id.action_done).setVisible(true);

                mMenu.findItem(R.id.action_logOut).setVisible(false);  // Hide log out menu item

                enterEditState();

                return true;
            case R.id.action_done:  // Done
                // Hide Done and show Edit menu buttons
                item.setVisible(false);
                mMenu.findItem(R.id.action_edit).setVisible(true);

                mMenu.findItem(R.id.action_logOut).setVisible(true);  // Show log out menu item

                exitEditState();

                return true;
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
            // This method is called when the task of retrieving the full set of saved searches is complete
            mSavedSearchList.clear();

            if (e == null) {
                // Load saved search results and assign to the current Mountaineer object
                mMember.setSavedSearchList(SavedSearchLoader.load(resultList));

                // Reorganize the saved searches with updates by name
                reorganizeList(mMember.getSavedSearchList());

                // Add all of these SavedSearches to the ListView
                mSavedSearchList.addAll(mMember.getSavedSearchList());

                // Update search results
                refreshSavedSearches();
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

    private void exitEditState() {
        ActionBar actionBar = getActivity().getActionBar();

        actionBar.setHomeButtonEnabled(true); // disable the button
        actionBar.setDisplayHomeAsUpEnabled(true); // remove the left caret
        actionBar.setDisplayShowHomeEnabled(true); // remove the icon

        mSwipeRefreshLayout.setEnabled(true);  // Enable swiperefresh

        mIsEditMode = false;

        ((SavedSearchAdapter) getListAdapter()).changeState(false);  // Exit edit mode
    }

    private void enterEditState() {
        ActionBar actionBar = getActivity().getActionBar();

        actionBar.setHomeButtonEnabled(false); // disable the button
        actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
        actionBar.setDisplayShowHomeEnabled(false); // remove the icon

        mSwipeRefreshLayout.setEnabled(false);  // Disable swiperefresh

        mIsEditMode = true;

        ((SavedSearchAdapter) getListAdapter()).changeState(true);  // Enter edit mode
    }

    private void reorganizeList(List<SavedSearch> list) {
        /* This method takes in a list that is sorted first by descending update counter and then
        ascending name and reorganizes so that the saved searches with updates are sorted by
        ascending name (update counter does not matter except those with 0 should all be at the
        bottom) */
        int endSubList = 0;

        if (list.size() > 1) {  // Make sure there is at least two entries (i.e. saved searches)
            // Make sure the first entry has updates or the list does not need to be reorganized
            if (list.get(0).getUpdateCounter() > 0) {  // First entry has updates
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getUpdateCounter() == 0) {
                        // Get the index for the end of the saved searches with updates
                        endSubList = i;
                        break;
                    }
                }

                // Create a sub-list of the saved searches with counter results and sort it based on name
                Collections.sort(list.subList(0, endSubList), new SavedSearchComparator());

                // Do this again for all remaining 0-counter results (helps reorg by ignoring case)
                Collections.sort(list.subList(endSubList, list.size()), new SavedSearchComparator());
            }
            else {  // None of the saved searches has updates
                // Reorganize based on case-insensitive alphabetic order
                Collections.sort(list, new SavedSearchComparator());
            }
        }
    }

    private void getSavedSearchList() {
        ParseQuery<ParseObject> query;

        // This method is called to retrieve all of the saved searches from the backend
        // Intermediate check to ensure all processes have not been canceled
        if (!mIsCanceled) {
            query = ParseQuery.getQuery(ParseConstants.CLASS_SAVED_SEARCH);
            query.whereEqualTo(ParseConstants.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
            query.orderByDescending(ParseConstants.KEY_UPDATE_COUNT);
            query.addAscendingOrder(ParseConstants.KEY_SAVE_NAME);
            // Limit to 500 results max (hopefully user never has more than this)
            query.setLimit(ParseConstants.QUERY_LIMIT);

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

    private void loadFilterOptions(SavedSearch savedSearch) {
        // This method loads all of the selected saved search filter options to the FilterOptions object
        // Start and end dates
        mFilterOptions.setStartDate(savedSearch.getActivityStartDate());
        mFilterOptions.setEndDate(savedSearch.getActivityEndDate());

        // Audience filters
        mFilterOptions.setAudience2030Somethings(savedSearch.isAudience2030Somethings());
        mFilterOptions.setAudienceAdults(savedSearch.isAudienceAdults());
        mFilterOptions.setAudienceFamilies(savedSearch.isAudienceFamilies());
        mFilterOptions.setAudienceRetiredRovers(savedSearch.isAudienceRetiredRovers());
        mFilterOptions.setAudienceSingles(savedSearch.isAudienceSingles());
        mFilterOptions.setAudienceYouth(savedSearch.isAudienceYouth());

        mFilterOptions.setBranchTheMountaineers(savedSearch.isBranchTheMountaineers());
        mFilterOptions.setBranchBellingham(savedSearch.isBranchBellingham());
        mFilterOptions.setBranchEverett(savedSearch.isBranchEverett());
        mFilterOptions.setBranchFoothills(savedSearch.isBranchFoothills());
        mFilterOptions.setBranchKitsap(savedSearch.isBranchKitsap());
        mFilterOptions.setBranchOlympia(savedSearch.isBranchOlympia());
        mFilterOptions.setBranchOutdoorCenters(savedSearch.isBranchOutdoorCenters());
        mFilterOptions.setBranchSeattle(savedSearch.isBranchSeattle());
        mFilterOptions.setBranchTacoma(savedSearch.isBranchTacoma());

        mFilterOptions.setClimbingBasicAlpine(savedSearch.isClimbingBasicAlpine());
        mFilterOptions.setClimbingIntermediateAlpine(savedSearch.isClimbingIntermediateAlpine());
        mFilterOptions.setClimbingAidClimb(savedSearch.isClimbingAidClimb());
        mFilterOptions.setClimbingRockClimb(savedSearch.isClimbingRockClimb());

        mFilterOptions.setRatingForBeginners(savedSearch.isRatingForBeginners());
        mFilterOptions.setRatingEasy(savedSearch.isRatingEasy());
        mFilterOptions.setRatingModerate(savedSearch.isRatingModerate());
        mFilterOptions.setRatingChallenging(savedSearch.isRatingChallenging());

        mFilterOptions.setSkiingCrossCountry(savedSearch.isSkiingCrossCountry());
        mFilterOptions.setSkiingBackcountry(savedSearch.isSkiingBackcountry());
        mFilterOptions.setSkiingGlacier(savedSearch.isSkiingGlacier());

        mFilterOptions.setSnowshoeingBeginner(savedSearch.isSnowshoeingBeginner());
        mFilterOptions.setSnowshoeingBasic(savedSearch.isSnowshoeingBasic());
        mFilterOptions.setSnowshoeingIntermediate(savedSearch.isSnowshoeingIntermediate());

        mFilterOptions.setTypeAdventureClub(savedSearch.isTypeAdventureClub());
        mFilterOptions.setTypeBackpacking(savedSearch.isTypeBackpacking());
        mFilterOptions.setTypeClimbing(savedSearch.isTypeClimbing());
        mFilterOptions.setTypeDayHiking(savedSearch.isTypeDayHiking());
        mFilterOptions.setTypeExplorers(savedSearch.isTypeExplorers());
        mFilterOptions.setTypeExploringNature(savedSearch.isTypeExploringNature());
        mFilterOptions.setTypeGlobalAdventures(savedSearch.isTypeGlobalAdventures());
        mFilterOptions.setTypeNavigation(savedSearch.isTypeNavigation());
        mFilterOptions.setTypePhotography(savedSearch.isTypePhotography());
        mFilterOptions.setTypeSailing(savedSearch.isTypeSailing());
        mFilterOptions.setTypeScrambling(savedSearch.isTypeScrambling());
        mFilterOptions.setTypeSeaKayaking(savedSearch.isTypeSeaKayaking());
        mFilterOptions.setTypeSkiingSnowboarding(savedSearch.isTypeSkiingSnowboarding());
        mFilterOptions.setTypeSnowshoeing(savedSearch.isTypeSnowshoeing());
        mFilterOptions.setTypeStewardship(savedSearch.isTypeStewardship());
        mFilterOptions.setTypeTrailRunning(savedSearch.isTypeTrailRunning());
        mFilterOptions.setTypeUrbanAdventure(savedSearch.isTypeUrbanAdventure());
        mFilterOptions.setTypeYouth(savedSearch.isTypeYouth());
    }

    private void refreshSavedSearches() {
        // Ensure all processes have not been canceled
        if (!mIsCanceled) {
            // Pass the list to the adapter
            if (getListView().getAdapter() == null) {  // First time using the list adapter
                SavedSearchAdapter adapter = new SavedSearchAdapter(getListView().getContext(), mSavedSearchList);
                setListAdapter(adapter);
            } else {  // Results already shown so update the list
                ((SavedSearchAdapter) getListAdapter()).notifyDataSetChanged();
            }

            if (mSavedSearchList.size() == 0) {
                Toast toast = Toast.makeText(getActivity(), getActivity().getString
                        (R.string.toast_empty_saved_searches), Toast.LENGTH_LONG);
                ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                        .setGravity(Gravity.CENTER_HORIZONTAL);
                toast.show();
            }

            getListView().setEmptyView(getActivity().findViewById(R.id.textViewEmpty));

            // Update the navigation drawer to show saved search updates
            ((MainActivity) getActivity()).updateNavigationDrawerContents();

            // Flag as updating activities
            ((MainActivity) getActivity()).setLoadingActivities(false);
            mSwipeRefreshLayout.setRefreshing(false);  // Turn off update indicator
        }
        else {
            mIsCanceled = false;
        }
    }

    /* This method resets the Saved Search fragment in the event the user logs out and logs
     * right back in */
    public void resetFragment() {
        mIsCanceled = false;
        mFilterOptions = null;  // Create new filter options
    }
}