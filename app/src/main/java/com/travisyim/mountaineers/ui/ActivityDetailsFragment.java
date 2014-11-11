package com.travisyim.mountaineers.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseUser;
import com.travisyim.mountaineers.MountaineersApp;
import com.travisyim.mountaineers.R;

import java.util.Date;

public class ActivityDetailsFragment extends Fragment {
    private MenuItem mFavorite;
    private WebView mWebView;
    private Date mActivityStartDate;
    private Date mActivityEndDate;
    private Date mRegOpenDate;
    private Date mRegCloseDate;
    private String mCookie;
    private String mParentFragmentTitle;
    private String mActivityName;
    private String mActivityURL;
    private String mLocation;
    private boolean mIsFavorite;
    private boolean mLogOut;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_PARENT_TITLE = "parentFragmentTitle";
    private static final String ARG_ACTIVITY_NAME = "activityName";
    private static final String ARG_ACTIVITY_URL = "activityURL";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_FAVORITE = "isFavorite";
    private static final String ARG_ACTIVITY_START_DATE = "startDate";
    private static final String ARG_ACTIVITY_END_DATE = "endDate";
    private static final String ARG_ACTIVITY_REG_OPEN_DATE = "regOpenDate";
    private static final String ARG_ACTIVITY_REG_CLOSE_DATE = "regCloseDate";

    // Returns a new instance of this fragment for the given section number
    public static ActivityDetailsFragment newInstance(final float sectionNumber,
                                                      final String parentFragmentTitle) {
        ActivityDetailsFragment fragment = new ActivityDetailsFragment();
        Bundle args = new Bundle();

        /* Save the arguments to be accessed later in setArguments (must wait because member
         * variables are not yet accessible */
        // TODO: Make sure the section number is in the format of 2.1.1
        args.putFloat(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_PARENT_TITLE, parentFragmentTitle);

        fragment.setArguments(args);
        return fragment;
    }

    public ActivityDetailsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set additional arguments passed in after creating new fragment instance
        // Get the parent fragment's title
        mParentFragmentTitle = getArguments().getString(ARG_PARENT_TITLE);

        mIsFavorite = getArguments().getBoolean(ARG_FAVORITE);  // Get favorite status
        mActivityName = getArguments().getString(ARG_ACTIVITY_NAME);  // Activity name
        mActivityURL = getArguments().getString(ARG_ACTIVITY_URL);  // Activity URL
        mLocation = getArguments().getString(ARG_LOCATION);  // GPS Location
        // Activity start date
        mActivityStartDate = new Date(getArguments().getLong(ARG_ACTIVITY_START_DATE));
        // Activity end date
        mActivityEndDate = new Date(getArguments().getLong(ARG_ACTIVITY_END_DATE));
        // Registration open date
        mRegOpenDate = new Date(getArguments().getLong(ARG_ACTIVITY_REG_OPEN_DATE));
        // Registration close date
        mRegCloseDate = new Date(getArguments().getLong(ARG_ACTIVITY_REG_CLOSE_DATE));

        mCookie = ((MainActivity) activity).mCookie;  // Get cookie - YUM!
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mLogOut = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity_details, container, false);

        // Sync cookie from initial login phase with the WebView so that the user is logged in
        CookieSyncManager.createInstance(getActivity());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie("www.mountaineers.org", mCookie);
        CookieSyncManager.getInstance().sync();

        // Load activity webpage
        mWebView = (WebView) rootView.findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        // Setup WevViewClient to handle webpage events
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // Stop the progress circle
                try {
                    getActivity().setProgressBarIndeterminateVisibility(false);
                }
                catch (NullPointerException e) {
                /* Intentionally left blank - error caused by clicking back before webpage finished
                 * loading */
                }
            }
        });

        // Start loading webpage and show the progress circle
        getActivity().setProgressBarIndeterminateVisibility(true);
        mWebView.loadUrl(mActivityURL);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop loading the webpage and hide the progress circle
        mWebView.stopLoading();
        getActivity().setProgressBarIndeterminateVisibility(false);

        if (!mLogOut) {
            // Reset the title back to that of the parent fragment
            getActivity().getActionBar().setTitle(mParentFragmentTitle);

            // Google Analytics tracking code - User Profile
            Tracker t = ((MountaineersApp) getActivity().getApplication()).getTracker
                    (MountaineersApp.TrackerName.APP_TRACKER);

            // Send the favorite status back to the previous fragment for implementation
            // Check which fragment this filter was launched from
            if (mParentFragmentTitle == getString(R.string.title_section2)) {  // Activity Search
                t.setScreenName("Activity Search");

                ((ActivitySearchFragment) getFragmentManager().findFragmentByTag(mParentFragmentTitle))
                        .onFavoriteSelected(mIsFavorite);
            }
            else if (mParentFragmentTitle == getString(R.string.title_section3)) {  // Completed Activity
                t.setScreenName("Completed Activities");

                ((CompletedActivityFragment) getFragmentManager().findFragmentByTag(mParentFragmentTitle))
                        .onFavoriteSelected(mIsFavorite);
            }
            else if (mParentFragmentTitle == getString(R.string.title_section4)) {  // Signed Up Activity
                t.setScreenName("Signed Up Activities");

                ((SignedUpActivityFragment) getFragmentManager().findFragmentByTag(mParentFragmentTitle))
                        .onFavoriteSelected(mIsFavorite);
            }
            else if (mParentFragmentTitle == getString(R.string.title_section5)) {  // Favorite Activity
                t.setScreenName("Favorite Activities");

                ((FavoriteActivityFragment) getFragmentManager().findFragmentByTag(mParentFragmentTitle))
                        .onFavoriteSelected(mIsFavorite);
            }
            else {  // Saved search activity search
                t.setScreenName("Activity Search (Saved Search)");
                ((ActivitySearchFragment) getFragmentManager().findFragmentByTag(mParentFragmentTitle))
                        .onFavoriteSelected(mIsFavorite);
            }

            t.send(new HitBuilders.AppViewBuilder().build());
        }
    }

    public interface OnFavoriteSelectedListener {
        void onFavoriteSelected(final boolean isFavorite);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.activity_details, menu);

        mFavorite = menu.findItem(R.id.action_favorite);

        // Set the status of the favorites icon
        if (mIsFavorite) {  // This is a favorite activity
            mFavorite.setIcon(R.drawable.ic_action_important);
            mFavorite.setTitle(R.string.favorites_remove);
        }
        else {  // Not a favorite activity
            mFavorite.setIcon(R.drawable.ic_action_not_important);
            mFavorite.setTitle(R.string.favorites_add);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Tracker t;

        switch (item.getItemId()) {
            case R.id.action_logOut:  // Log Out
                mLogOut = true;
                ParseUser.getCurrentUser().logOut();
                ((MainActivity) getActivity()).showLoginScreen();
                getFragmentManager().popBackStackImmediate();  // Go back to parent fragment
                return true;
            case R.id.action_share:  // Share activity
                // Google Analytics tracking code - Share activity
                t = ((MountaineersApp) getActivity().getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName("ACTION: Share activity");
                t.send(new HitBuilders.AppViewBuilder().build());

                intent = new Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, "Check out this trip:\n" + mActivityURL)
                        .putExtra(android.content.Intent.EXTRA_SUBJECT, mActivityName);

                startActivity(Intent.createChooser(intent, getActivity().getString(R.string.chooser_share)));

                return true;
            case R.id.action_event:  // Add activity reminder
                // Google Analytics tracking code - Add activity reminder
                t = ((MountaineersApp) getActivity().getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName("ACTION: Add activity reminder");
                t.send(new HitBuilders.AppViewBuilder().build());

                intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, mActivityStartDate
                                .getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, mActivityEndDate.getTime()
                                + (24 * 60 * 60 * 1000) - 1)
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                        .putExtra(CalendarContract.Events.TITLE, mActivityName)
                        .putExtra(CalendarContract.Events.EVENT_LOCATION, mLocation)
                        .putExtra(CalendarContract.Events.AVAILABILITY,
                                CalendarContract.Events.AVAILABILITY_BUSY);

                startActivity(Intent.createChooser(intent, getActivity().getString(R.string.chooser_reminder)));

                return true;
            case R.id.action_favorite:  // Add to / Remove from Favorites
                // Google Analytics tracking code - Add to / Remove from Favorites
                t = ((MountaineersApp) getActivity().getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName("ACTION: Add to / remove from favorites");
                t.send(new HitBuilders.AppViewBuilder().build());

                // Swap icons and text
                if (mFavorite.getTitle().equals(getString(R.string.favorites_add))) {
                    mFavorite.setIcon(R.drawable.ic_action_important);
                    mFavorite.setTitle(R.string.favorites_remove);
                }
                else {
                    mFavorite.setIcon(R.drawable.ic_action_not_important);
                    mFavorite.setTitle(R.string.favorites_add);
                }

                mIsFavorite = !mIsFavorite;  // Change the value

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}