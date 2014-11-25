package com.travisyim.mountaineers.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.travisyim.mountaineers.MountaineersApp;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.objects.AsyncTaskResult;
import com.travisyim.mountaineers.objects.Mountaineer;
import com.travisyim.mountaineers.objects.SavedSearch;
import com.travisyim.mountaineers.utils.OnTaskCompleted;
import com.travisyim.mountaineers.utils.ParseConstants;
import com.travisyim.mountaineers.utils.SimpleCrypto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnTaskCompleted {
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Fragment mUserProfileFragment;
    private Fragment mActivitySearchFragment;
    private Fragment mCompletedActivityFragment;
    private Fragment mSignedUpActivityFragment;
    private Fragment mFavoriteActivityFragment;
    private Fragment mSavedSearchFragment;
    private Mountaineer mMember;
    private CharSequence mTitle;
    private boolean mFirstResume = true;
    private boolean mFinishedUserData = false;
    private boolean mLoadingActivities = false;
    private boolean mIsDrawerOpen = false;
    protected String mCookie;

    private static final String TAG = MainActivity.class.getSimpleName() + ":";
    private static final String ARG_MEMBER = "member";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Determine startup behavior of app
        ParseUser currentUser = ParseUser.getCurrentUser();

        // Either send user to login screen or grab latest user data from website
        if (currentUser == null) {  // No user logged in so start login screen
            showLoginScreen();
        }
        else {  // User is already logged in so grab latest user data
            setProgressBarIndeterminateVisibility(true);

            // Re-fetch current user data - ensures all data is uptodate
            ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {  // No error
                        // Define a new Mountaineers member object and log in with user provided credentials
                        mMember = new Mountaineer(getString(R.string.mountaineers_login_url),
                                ParseUser.getCurrentUser().getUsername(),
                                SimpleCrypto.decrypt(getString(R.string.key),
                                        ParseUser.getCurrentUser().get(ParseConstants.KEY_PASSWORD).toString()),
                                ParseUser.getCurrentUser().get(ParseConstants.KEY_MEMBER_URL).toString());

                        // Start the login process
                        mMember.getLoginWebPage(MainActivity.this);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* onResume is called once before the log in activity is shown.  Do not launch the activity
         * again in this case. */
        if (mFirstResume) {  // First time through
            mFirstResume = false;
        }
        /* In the case that this was sent back from the login screen after clicking the Sign Up
         * button, send the user back to the login screen */
        else {
            if (ParseUser.getCurrentUser() == null) {
                showLoginScreen();
            }
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        if (!isLoadingActivities()) {  // If loading something do not process back button press
            super.onBackPressed();
        }
        else {
            Toast.makeText(this, getString(R.string.toast_filter_wait), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        Bundle args;
        Tracker t;

        switch(position) {
            case 0:  // User Profile
                // Google Analytics tracking code - User Profile
                t = ((MountaineersApp) getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName(getString(R.string.title_profile));
                t.send(new HitBuilders.AppViewBuilder().build());

                mTitle = getString(R.string.title_profile);

                if (mUserProfileFragment == null) {
                    fragment = UserProfileFragment.newInstance(position + 1);

                    args = fragment.getArguments();
                    args.putSerializable(ARG_MEMBER, (Serializable) mMember);
                    mUserProfileFragment = fragment;
                }
                else {
                    fragment = mUserProfileFragment;

                    // Restore ActionBar title if user clicks on same item in Navigation Drawer
                    if (mUserProfileFragment.isVisible()) {
                        restoreActionBar();
                    }
                }

                // Show the requested fragment
                fragmentManager.beginTransaction().replace(R.id.container, fragment, mTitle.toString()).commit();

                break;
            case 2:  // Activity Search
                // Google Analytics tracking code - Activity Search
                t = ((MountaineersApp) getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName(getString(R.string.title_browse));
                t.send(new HitBuilders.AppViewBuilder().build());

                mTitle = getString(R.string.title_browse);

                if (mActivitySearchFragment == null) {
                    fragment = ActivitySearchFragment.newInstance(position + 1, null);
                    mActivitySearchFragment = fragment;
                }
                else {
                    fragment = mActivitySearchFragment;

                    // Restore ActionBar title if user clicks on same item in Navigation Drawer
                    if (mActivitySearchFragment.isVisible()) {
                        restoreActionBar();
                    }
                }

                // Show the requested fragment
                fragmentManager.beginTransaction().replace(R.id.container, fragment, mTitle.toString()).commit();

                break;
            case 3:  // Saved Searches
                // Google Analytics tracking code - Favorite Activities
                t = ((MountaineersApp) getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName(getString(R.string.title_saved_searches));
                t.send(new HitBuilders.AppViewBuilder().build());

                mTitle = getString(R.string.title_saved_searches);

                if (mSavedSearchFragment == null) {
                    fragment = SavedSearchFragment.newInstance(position + 1);

                    args = fragment.getArguments();
                    args.putSerializable(ARG_MEMBER, (Serializable) mMember);
                    mSavedSearchFragment = fragment;
                }
                else {
                    fragment = mSavedSearchFragment;

                    // Restore ActionBar title if user clicks on same item in Navigation Drawer
                    if (mSavedSearchFragment.isVisible()) {
                        restoreActionBar();
                    }
                }

                // Show the requested fragment
                fragmentManager.beginTransaction().replace(R.id.container, fragment, mTitle.toString()).commit();

                break;
            case 5:  // Completed Activities
                // Google Analytics tracking code - Completed Activities
                t = ((MountaineersApp) getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName(getString(R.string.title_completed));
                t.send(new HitBuilders.AppViewBuilder().build());

                mTitle = getString(R.string.title_completed);

                if (mCompletedActivityFragment == null) {
                    fragment = CompletedActivityFragment.newInstance(position + 1);

                    args = fragment.getArguments();
                    args.putSerializable(ARG_MEMBER, (Serializable) mMember);
                    mCompletedActivityFragment = fragment;
                }
                else {
                    fragment = mCompletedActivityFragment;

                    // Restore ActionBar title if user clicks on same item in Navigation Drawer
                    if (mCompletedActivityFragment.isVisible()) {
                        restoreActionBar();
                    }
                }

                // Show the requested fragment
                fragmentManager.beginTransaction().replace(R.id.container, fragment, mTitle.toString()).commit();

                break;
            case 6:  // Signed Up Activities
                // Google Analytics tracking code - Signed Up Activities
                t = ((MountaineersApp) getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName(getString(R.string.title_signed_up));
                t.send(new HitBuilders.AppViewBuilder().build());

                mTitle = getString(R.string.title_signed_up);

                if (mSignedUpActivityFragment == null) {
                    fragment = SignedUpActivityFragment.newInstance(position + 1);

                    args = fragment.getArguments();
                    args.putSerializable(ARG_MEMBER, (Serializable) mMember);
                    mSignedUpActivityFragment = fragment;
                }
                else {
                    fragment = mSignedUpActivityFragment;

                    // Restore ActionBar title if user clicks on same item in Navigation Drawer
                    if (mSignedUpActivityFragment.isVisible()) {
                        restoreActionBar();
                    }
                }

                // Show the requested fragment
                fragmentManager.beginTransaction().replace(R.id.container, fragment, mTitle.toString()).commit();

                break;
            case 7:  // Favorite Activities
                // Google Analytics tracking code - Favorite Activities
                t = ((MountaineersApp) getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName(getString(R.string.title_bookmarked));
                t.send(new HitBuilders.AppViewBuilder().build());

                mTitle = getString(R.string.title_bookmarked);

                if (mFavoriteActivityFragment == null) {
                    fragment = FavoriteActivityFragment.newInstance(position + 1);
                    mFavoriteActivityFragment = fragment;
                }
                else {
                    fragment = mFavoriteActivityFragment;

                    // Restore ActionBar title if user clicks on same item in Navigation Drawer
                    if (mFavoriteActivityFragment.isVisible()) {
                        restoreActionBar();
                    }
                }

                // Show the requested fragment
                fragmentManager.beginTransaction().replace(R.id.container, fragment, mTitle.toString()).commit();

                break;
            case 9:  // Send comments
                // Google Analytics tracking code - Signed Up Activities
                t = ((MountaineersApp) getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName(getString(R.string.title_comments));
                t.send(new HitBuilders.AppViewBuilder().build());

                Intent email = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("mountaineers.org@gmail.com") +
                        "?subject=" + Uri.encode("Comment from " + mMember.getName());
                Uri uri = Uri.parse(uriText);

                email.setData(uri);

                startActivity(Intent.createChooser(email, "Choose an E-mail client"));

                break;
            case 10:  // Favorite Activities
                // Google Analytics tracking code - Favorite Activities
                t = ((MountaineersApp) getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName(getString(R.string.title_rate));
                t.send(new HitBuilders.AppViewBuilder().build());

                Intent intent = new Intent(Intent.ACTION_VIEW);

                try {
                    intent.setData(Uri.parse("market://details?id=com.travisyim.mountaineers"));
                    startActivity(intent);
                }
                catch (Exception e) {  // Issue accessing Google Play store app
                    // Go to website instead
                    intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=com.travisyim.mountaineers"));
                    startActivity(intent);
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* To improve login time, the login process is handed back to Main activity after the user
         * has logged in and user account data has been saved to Parse.  At this point the main
         * activity is shown and the user can interact with the actual app.  However, the scraping
         * of user data (member profile) will still be completed in the background at this point. */

        // Check to see if this is the result from Login Activity
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Reset the Activity Search fragment
            ((ActivitySearchFragment) mActivitySearchFragment).resetFragment();

            // Check if the other activity fragments exists and if so, reset them
            if (mCompletedActivityFragment != null) {  // Completed
                ((CompletedActivityFragment) mCompletedActivityFragment).resetFragment();
            }
            if (mSignedUpActivityFragment != null) {  // Signed Up
                ((SignedUpActivityFragment) mSignedUpActivityFragment).resetFragment();
            }
            if (mFavoriteActivityFragment != null) {  // Favorite
                ((FavoriteActivityFragment) mFavoriteActivityFragment).resetFragment();
            }
            if (mSavedSearchFragment != null) {  // Saved Searches
                ((SavedSearchFragment) mSavedSearchFragment).resetFragment();
            }

            mNavigationDrawerFragment.resetDrawer();  // Show the Activities Search fragment

            // Demo the drawer demo if necessary
            mNavigationDrawerFragment.demoDrawer();

            // Get the member object that was passed back to this activity
            mMember = (Mountaineer) data.getSerializableExtra(ARG_MEMBER);

            // Save the cookie!
            mCookie = mMember.getCookies().get(0);

            setProgressBarIndeterminateVisibility(true);  // Show progress circle

            // Scrape member profile data from website
            mMember.getMemberData(this);
        }
        // 99 represents pressing back when the user is on the Login screen
        else if (requestCode == 1 && resultCode == 99) {
            finish();
        }
    }

    @Override
     public void onTaskCompleted(final int stage, final AsyncTaskResult<Boolean> result) {
         /* This method is called when a task that is part of the login process completes.  The first
          * step is getting the member login webpage and that is called in the onCreate() method. The
          * other necessary steps are further captured and launched from this method as they happen
          * sequentially using AsyncTask */

         switch (stage) {
             case Mountaineer.STAGE_GET_LOGIN_WEB_PAGE:
                 // Check for success in downloading the member login web page
                 if (result.getError() == null && result.getResult()) {  // Success!
                     // Demo the drawer demo if necessary
                     mNavigationDrawerFragment.demoDrawer();

                     // Login as the user using their previously entered credentials
                     mMember.login(this);
                 }
                 else {  // Error!
                     /* This check only ensures that the member login web page url was downloaded
                      * properly.  It does not check to see if it is in the format that we expect -
                      * this check will happen as the result of the next step */
                     showError(result.getError().getMessage());

                     setProgressBarIndeterminateVisibility(false);  // Hide progress circle
                     showLoginScreen();  // Send user to login screen
                 }

                 break;
             case Mountaineer.STAGE_LOGIN:
                 // Check for success in logging in as user and getting user profile URL
                 if (result.getError() == null && result.getResult()) {  // Success!
                     /* The login process for a user who is returning as the Parse Current User
                      * differs from a user who has just logged in via the Login Activity.  At this
                      * point in the process, the paths diverge.  For a returning current user, jump
                      * straight into getting the member data.  Unlike the new log in, there is no
                      * need to save user credentials to Parse since they successfully logged into
                      * the Mountaineers site with their existing credentials. */

                     mMember.getSavedSearches(this);
                 }
                 else {  // Error!
                     /* This check only ensures that the login web page url is in the format that we
                      * expect, that the app could properly log in as the user with the provided
                      * credentials and that the user profile URL was successfully accessed */
                     showError(result.getError().getMessage());

                     setProgressBarIndeterminateVisibility(false);  // Hide progress circle
                     showLoginScreen();  // Send user to login screen
                 }

                 break;
             case Mountaineer.STAGE_GET_SAVED_SEARCHES:
                 // Check for success in logging in as user and getting user profile URL
                 if (result.getError() == null && result.getResult()) {  // Success!
                     /* The login process for a user who is returning as the Parse Current User
                      * differs from a user who has just logged in via the Login Activity.  At this
                      * point in the process, the paths diverge.  For a returning current user, jump
                      * straight into getting the member data.  Unlike the new log in, there is no
                      * need to save user credentials to Parse since they successfully logged into
                      * the Mountaineers site with their existing credentials. */

                     // Save the cookie!
                     mCookie = mMember.getCookies().get(0);

                     // Update the navigation drawer to show saved search updates
                     updateNavigationDrawerCounters();

                     // Scrape member profile data from website
                     mMember.getMemberData(this);
                 }
                 else {  // Error!
                     /* This check only ensures that the login web page url is in the format that we
                      * expect, that the app could properly log in as the user with the provided
                      * credentials and that the user profile URL was successfully accessed */
                     showError(result.getError().getMessage());

                     setProgressBarIndeterminateVisibility(false);  // Hide progress circle
                     showLoginScreen();  // Send user to login screen
                 }

                 break;
             case Mountaineer.STAGE_GET_MEMBER_DATA:
                 // Add user object ID to current installation
                 if (ParseUser.getCurrentUser() != null) {
                     try {
                         ParseInstallation.getCurrentInstallation().put
                                 (ParseConstants.KEY_USER_OBJECT_ID,
                                         ParseUser.getCurrentUser().getObjectId());
                         ParseInstallation.getCurrentInstallation().saveInBackground();
                     }
                     catch (Exception e) { /* Intentionally left blank */ }
                 }

                 // Check for success in scraping user data
                 if (result.getError() != null || !result.getResult()) {  // Error!
                     /* This check ensures that the member profile web page is in the format that we
                      * expect and that the app could properly scrape this data */
                     showError(result.getError().getMessage());

                     setProgressBarIndeterminateVisibility(false);  // Hide progress circle
                     showLoginScreen();  // Send user to login screen
                 }

                 // Update the navigation drawer to show user profile image
                 mNavigationDrawerFragment.updateProfileImage(mMember.getProfileImageUrl());

                 setProgressBarIndeterminateVisibility(false);  // Hide progress circle
                 mFinishedUserData = true;  // Flag as finished getting user profile date

                 break;
         }
     }

    // TODO: Standardize the method of assigning titles to the fragments
    // The lower level fragments are currently being assigned in the higher-level fragment
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_profile);
                break;
            case 3:
                mTitle = getString(R.string.title_browse);
                break;
            case 4:
                mTitle = getString(R.string.title_saved_searches);
                break;
            case 6:
                mTitle = getString(R.string.title_completed);
                break;
            case 7:
                mTitle = getString(R.string.title_signed_up);
                break;
            case 8:
                mTitle = getString(R.string.title_bookmarked);
                break;
        }

        restoreActionBar();
    }

    public void showLoginScreen() {
        // Google Analytics tracking code - Log Out
        Tracker t = ((MountaineersApp) getApplication()).getTracker
                (MountaineersApp.TrackerName.APP_TRACKER);
        t.setScreenName("ACTION: Log out");
        t.send(new HitBuilders.AppViewBuilder().build());

        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private final void showError(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.error_title)
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public final boolean finishedGettingUserData() {
        return mFinishedUserData;
    }

    public final void setLoadingActivities(final boolean isLoading) {
        mLoadingActivities = isLoading;
    }

    public final boolean isLoadingActivities() {
        return mLoadingActivities;
    }

    // Alerts fragments to the state of the drawer
    public boolean isDrawerOpen() {
        return mIsDrawerOpen;
    }

    // The following method is only used in Activity Search where there is currently no access to the member variable
    public List<String> getSavedSearchList() {
        List<String> savedSearchList = new ArrayList<String>();

        // Go through all saved search objects and save the names
        for (SavedSearch ss : mMember.getSavedSearchList()) {
            savedSearchList.add(ss.getSearchName());
        }

        return savedSearchList;
    }

    // Sets the state of the drawer
    public void setDrawerOpen(final boolean isDrawerOpen) {
        mIsDrawerOpen = isDrawerOpen;
    }

    public void updateNavigationDrawerCounters() {
        int navDrawerCounter = 0;

        // Count saved searches with updates and update the counter in the notification drawer
        for (SavedSearch ss : mMember.getSavedSearchList()) {
            if (ss.getUpdateCounter() > 0) {
                navDrawerCounter++;
            }
        }

        // Update the navigation drawer to show saved search updates
        mNavigationDrawerFragment.updateSavedSearchCounter(navDrawerCounter);
    }
}