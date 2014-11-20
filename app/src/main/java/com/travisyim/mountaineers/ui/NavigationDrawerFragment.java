package com.travisyim.mountaineers.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.adapters.DrawerAdapter;
import com.travisyim.mountaineers.objects.FragmentListItem;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
    private Object[] mDrawerItems;
    private String mTitle;

    private int mCurrentSelectedPosition = 2;  // Initial start index of drawer (shows Activities)
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    // android.R.id.home as defined by public API in v11
    private static final int ID_HOME = 0x0102002c;

    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            setActionBarArrowDependingOnFragmentsBackStack();
        }
    };

    public NavigationDrawerFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Check if it is a valid time to change selection
                // The initial user data is still being pulled
                if (!((MainActivity) getActivity()).finishedGettingUserData()) {
                    Toast toast = Toast.makeText(getActivity(), getActivity().getString
                            (R.string.toast_user_data_wait), Toast.LENGTH_SHORT);
                    ((TextView)((LinearLayout)toast.getView()).getChildAt(0))
                            .setGravity(Gravity.CENTER_HORIZONTAL);
                    toast.show();

                    mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
                }
                // The activities are still being updated
                else if (((MainActivity) getActivity()).isLoadingActivities()) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.toast_filter_wait),
                            Toast.LENGTH_SHORT).show();

                    mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
                }
                else {  // Valid time to change item
                    selectItem(position);
                }
            }
        });

        // Load drawer items to list
        mDrawerItems = new Object[]{
                new FragmentListItem(getString(R.string.title_profile), R.drawable.ic_action_person, 0),
                "Activity Search",
                new FragmentListItem(getString(R.string.title_browse), R.drawable.ic_action_search, 0),
                new FragmentListItem(getString(R.string.title_saved_searches), R.drawable.ic_action_save, 0),
                "My Activities",
                new FragmentListItem(getString(R.string.title_completed), R.drawable.ic_action_accept, 0),
                new FragmentListItem(getString(R.string.title_signed_up), R.drawable.ic_action_view_as_list, 0),
                new FragmentListItem(getString(R.string.title_favorites), R.drawable.ic_action_important, 0),
        };

        // New navigation drawer adapter in work
        DrawerAdapter drawerAdapter = new DrawerAdapter
                (getActivity().getActionBar().getThemedContext(), mDrawerItems);

        mDrawerListView.setAdapter(drawerAdapter);

        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        getFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.isDrawerIndicatorEnabled() && mDrawerToggle.onOptionsItemSelected(item)) {
            return false;
        }
        else if (item.getItemId() == android.R.id.home) {
            // The activities are still being updated
            if (((MainActivity) getActivity()).isLoadingActivities()) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.toast_filter_wait),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                getFragmentManager().popBackStackImmediate();
            }

            return false;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Clear the existing ActionBar options menu when the drawer opens
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView)) {
            menu.clear();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;

        if (mDrawerListView != null) {  // Check / select item in drawer
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {  // Close the drawer
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {  // Load the appropriate fragment
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            /**
             * {@link android.support.v4.widget.DrawerLayout.DrawerListener} callback method. If you do not use your
             * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
             * through to this method from your own listener object.
             *
             * @param newState The new drawer motion state
             */
            @Override
            public void onDrawerStateChanged(int newState) {
                // Check to see if the drawer is opening
                if (newState == 2) {  // Drawer is opening or open
                    ((MainActivity) getActivity()).setDrawerOpen(true);
                }
                else if (newState == 0) {  // Drawer is closed
                    ((MainActivity) getActivity()).setDrawerOpen(false);
                }

                super.onDrawerStateChanged(newState);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                setActionBarArrowDependingOnFragmentsBackStack();

                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()

                /* Check to see if the user clicks on the drawer icon or on the empty space to the
                 * right of the drawer when the drawer is open.  This can be determined from the
                 * action bar title (if equals to app name) */
                if (getActivity().getActionBar().getTitle() == getString(R.string.app_name)) {
                    getActivity().getActionBar().setTitle(mTitle);  // Show previous fragment title
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                // Change the title of the ActionBar
                showGlobalContextActionBar();

                mDrawerToggle.setDrawerIndicatorEnabled(true);

                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Setup back stack listener for the FragmentManager to determine when to switch the top left icon
        getFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
    }

    private void setActionBarArrowDependingOnFragmentsBackStack() {
        // If there is are back stack entries, this fragment is not top-level, so switch to UP caret symbol
        int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
        mDrawerToggle.setDrawerIndicatorEnabled(backStackEntryCount == 0);
    }

    /* Per the navigation drawer design guidelines, update the action bar to show the global app
     * 'context', rather than just what's in the current screen */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mTitle = actionBar.getTitle().toString();  // Save previous fragment title
        actionBar.setTitle(R.string.app_name);  // Show app title
    }

    public void resetDrawer() {
        // Reset the drawer selection to Activity Search
        selectItem(1);
    }

    public void demoDrawer() {
        /* If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
         * per the navigation drawer design guidelines */
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
            getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
        }
    }

    public void updateSavedSearchCounter(int counter) {
        // This method updates the saved search counter in the navigation drawer
        ((FragmentListItem) mDrawerItems[3]).setCounter(counter);
        ((DrawerAdapter) mDrawerListView.getAdapter()).notifyDataSetChanged();
    }
}