package com.travisyim.mountaineers.ui;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
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

public class ProfileEditFragment extends Fragment {
    private WebView mWebView;
    private String mParentFragmentTitle;
    private String mProfileEditURL;
    private String mCookie;
    private boolean mLogOut = false;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_PARENT_TITLE = "parentFragmentTitle";
    private static final String ARG_EDIT_PROFILE_URL = "editProfileURL";

    // Returns a new instance of this fragment for the given section number
    public static ProfileEditFragment newInstance(final float sectionNumber,
                                                      final String parentFragmentTitle,
                                                      final String editProfileURL) {
        ProfileEditFragment fragment = new ProfileEditFragment();
        Bundle args = new Bundle();

        /* Save the arguments to be accessed later in setArguments (must wait because member
         * variables are not yet accessible */
        // TODO: Make sure the section number is in the format of 2.1.1
        args.putFloat(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_PARENT_TITLE, parentFragmentTitle);
        args.putString(ARG_EDIT_PROFILE_URL, editProfileURL);

        fragment.setArguments(args);

        return fragment;
    }

    public ProfileEditFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set arguments
        // Get the parent fragment's title
        mParentFragmentTitle = getArguments().getString(ARG_PARENT_TITLE);
        // User profile edit webpage URL
        mProfileEditURL = getArguments().getString(ARG_EDIT_PROFILE_URL);

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
        View rootView = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        // Sync cookie from initial login phase with the WebView so that the user is logged in
        CookieSyncManager.createInstance(getActivity());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie("www.mountaineers.org", mCookie);
        CookieSyncManager.getInstance().sync();

        // Load WebView
        mWebView = (WebView) rootView.findViewById(R.id.webView);

        // Load activity webpage
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        // Setup WevViewClient to handle webpage events
        mWebView.setWebViewClient(new WebViewClient() {
            /**
             * Notify the host application that a page has started loading. This method
             * is called once for each main frame load so a page with iframes or
             * framesets will call onPageStarted one time for the main frame. This also
             * means that onPageStarted will not be called when the contents of an
             * embedded frame changes, i.e. clicking a link whose target is an iframe.
             *
             * @param view    The WebView that is initiating the callback.
             * @param url     The url to be loaded.
             * @param favicon The favicon for this page if it already exists in the
             */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                // Show the progress circle
                getActivity().setProgressBarIndeterminateVisibility(true);
            }

            public void onPageFinished(WebView view, String url) {
                // Stop the progress circle
                try {
                    // Hide unnecessary areas of the webpage (i.e. headers, etc)
                    view.loadUrl("javascript:" +
                            "(function() {" +
                            "if (document.getElementById('abs')) {document.getElementById('abs').style.display='none';}" +
                            "if (document.getElementById('nabs')) {document.getElementById('nabs').style.display='none';}" +
                            "if (document.getElementById('header')) {document.getElementById('header').style.display='none';}" +
                            "if (document.getElementById('navigation')) {document.getElementById('navigation').style.display='none';}" +
                            "if (document.getElementById('edit-bar')) {document.getElementById('edit-bar').style.display='none';}" +
                            "if (document.getElementById('breadcrumbs')) {document.getElementById('breadcrumbs').style.display='none';}" +
                            "if (document.getElementById('main').getElementsByClassName('wrapper')[0].getElementsByClassName('column grid-3 leftportlets')[0]) {document.getElementById('main').getElementsByClassName('wrapper')[0].getElementsByClassName('column grid-3 leftportlets')[0].style.display='none';}" +
                            "if (document.getElementById('viewlet-below-content')) {document.getElementById('viewlet-below-content').style.display='none';}" +
                            "if (document.getElementById('footer')) {document.getElementById('footer').style.display='none';}" +
                            "if (document.getElementsByClassName('uv-icon uv-bottom-right')[0]) {document.getElementsByClassName('uv-icon uv-bottom-right')[0].style.display='none';}" +
                            "})()");

                    getActivity().setProgressBarIndeterminateVisibility(false);
                }
                catch (NullPointerException e) {
                /* Intentionally left blank - error caused by clicking back before webpage finished
                 * loading */
                }
            }
        });

        // Start loading profile web page
        mWebView.loadUrl(mProfileEditURL);

        return rootView;
    }

    @Override
    public void onDestroy() {
        // Stop loading the webpage and hide the progress circle
        mWebView.stopLoading();
        getActivity().setProgressBarIndeterminateVisibility(false);

        if (!mLogOut) {
            // Google Analytics tracking code - User Profile
            Tracker t = ((MountaineersApp) getActivity().getApplication()).getTracker
                    (MountaineersApp.TrackerName.APP_TRACKER);
            t.setScreenName(getString(R.string.title_profile));
            t.send(new HitBuilders.AppViewBuilder().build());

            // Reset the title back to that of the parent fragment
            getActivity().getActionBar().setTitle(mParentFragmentTitle);
        }

        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.profile_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logOut:  // Log Out
                mLogOut = true;
                ParseUser.getCurrentUser().logOut();
                ((MainActivity) getActivity()).showLoginScreen();
                getFragmentManager().popBackStackImmediate();  // Go back to parent fragment
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}