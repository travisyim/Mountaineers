package com.travisyim.mountaineers.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.travisyim.mountaineers.MountaineersApp;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.objects.AsyncTaskResult;
import com.travisyim.mountaineers.objects.Mountaineer;
import com.travisyim.mountaineers.utils.OnTaskCompleted;

public class LoginActivity extends Activity implements OnTaskCompleted {
    private Mountaineer mMember;
    private EditText mUsername;
    private EditText mPassword;
    private ProgressBar mProgress;
    private boolean preLoginComplete = false;
    private boolean failedToLogIn = false;

    private static final String TAG = LoginActivity.class.getSimpleName() + ":";
    private static final String ARG_MEMBER = "member";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Google Analytics tracking code - Sign Up activity
        Tracker t = ((MountaineersApp) getApplication()).getTracker
                (MountaineersApp.TrackerName.APP_TRACKER);
        t.setScreenName("Sign In");
        t.send(new HitBuilders.AppViewBuilder().build());

        // Get references to pertinent views
        mUsername = (EditText) findViewById(R.id.editTextUsername);
        mPassword = (EditText) findViewById(R.id.editTextPassword);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);

        // Start the prelogin process of getting the login webpage
        getLoginWebPage();
    }

    @Override
    public void onBackPressed() {
        /* Inhibit the back button key press.  There is no way to clear the back stack whilst being
         * able to return the Mountaineer object for the member to the MainActivity */
        // User presses back so lets close the app
        Intent intent = new Intent();  //this, MainActivity.class

        // Pass the CANCEL value back to MainActivity
        setResult(99);
        finish();  // End this intent
    }

    @Override
    public void onTaskCompleted(final int stage, final AsyncTaskResult<Boolean> result) {
        /* This method is called when a task that is part of the login process completes.  The first
         * step is getting the member login webpage and that is called in the onCreate() method.
         * The next step of logging in is launched from the "Sign In" button's onClick listener
         * method signIn().  The result of this task is captured in this method.  All scraping tasks
         * are then handed off to Main activity to cut down on the initial start-up time. */
        switch (stage) {
            case Mountaineer.STAGE_GET_LOGIN_WEB_PAGE:
                // Check for success in downloading the member login web page
                if (result.getError() == null && result.getResult()) {  // Success!
                    preLoginComplete = true;  // Allow the user to log in now

                    /* User has clicked on "SIGN IN" button after having a failed attempt to log in.
                    However, this time they are successful in completing the prelogin task. Show the
                    user a toast message to sign in again. */
                    if (failedToLogIn) {
                        Toast.makeText(this, getString(R.string.toast_sign_in),
                                Toast.LENGTH_SHORT).show();

                        failedToLogIn = false;
                    }
                }
                else {  // Error!
                    /* This check only ensures that the member login web page url was downloaded
                     * properly.  It does not check to see if it is in the format that we expect -
                     * this check will happen as the result of the next step */
                    failedToLogIn = true;
                    showError(result.getError().getMessage());
                }

                mProgress.setVisibility(View.INVISIBLE);  // Hide progress circle

                break;
            case Mountaineer.STAGE_LOGIN:
                // Check for success in logging in as user and getting user profile URL
                if (result.getError() == null && result.getResult()) {  // Success!
                    /* The login process for a user who is returning as the Parse Current User
                     * differs from a user who has just logged in via the Login Activity.  At this
                     * point in the process, the paths diverge.  For a new logged in user, save the
                     * member data to Parse.  This is to ensure that their working Mountaineers
                     * credentials are saved.  This is done because of the scenario where a user had
                     * logged in before with their credentials then logged out.  These credentials
                     * would be saved to Parse.  But imagine the user going to the Mountaineers
                     * website to change their password.  Now the new password would not match that
                     * on Parse.  Therefore, we need to save credentials each time the user logs
                     * into the service from the Login Activity.  There is also a check placed in
                     * the other path in case the Parse credentials do not work for logging into the
                     * Mountaineers website (and should therefore kick into the Login Activity for
                     * the user to log in with their current credentials */

                    // Save user data to Parse
                    mMember.saveParseUser(this);
                }
                else {  // Error!
                    /* This check only ensures that the login web page url is in the format that we
                     * expect, that the app could properly log in as the user with the provided
                     * credentials and that the user profile URL was successfully accessed */
                    showError(result.getError().getMessage());

                    // The user will remain on this Activity until they successfully log in
                    mProgress.setVisibility(View.INVISIBLE);  // Hide progress circle
                }

                break;
            case Mountaineer.STAGE_SAVE_MEMBER:
                // Check for success in saving user to Parse backend
                if (result.getError() == null && result.getResult()) {  // Success!
                    // Google Analytics tracking code - successful login
                    Tracker t = ((MountaineersApp) getApplication()).getTracker
                            (MountaineersApp.TrackerName.APP_TRACKER);
                    t.setScreenName("Sign in success");
                    t.send(new HitBuilders.AppViewBuilder().build());

                    // Success - Return an intent with the Mountaineer member variable
                    Intent intent = new Intent();  //this, MainActivity.class

                    // Pass the Mountaineer member variable and result back to Main activity
                    intent.putExtra(ARG_MEMBER, mMember);
                    setResult(RESULT_OK, intent);
                    finish();  // End this intent
                }
                else {  // Error!
                    /* There is an error saving to Parse backend.  Since the user's favorites and
                     * saved searched resides on Parse, do not allow user to continue. */
                    showError(result.getError().getMessage());

                    /* The user will remain on this Activity until they successfully log in and save
                     * user information on Parse */
                }

                mProgress.setVisibility(View.INVISIBLE);  // Hide progress circle
                break;
        }
    }

    private void getLoginWebPage() {
        // This method saves some time by downloading the login webpage in preparation to login
        mProgress.setVisibility(View.VISIBLE);  // Show progress circle

        // Define a new Mountaineers member object and download login webpage
        mMember = new Mountaineer(getString(R.string.mountaineers_login_url));
        mMember.getLoginWebPage(this);
    }

    private final void showError(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_title)
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void signIn(View v) {
        // onClick event routine for clicking Sign In button
        if (preLoginComplete) {
            final String username = mUsername.getText().toString().trim();
            final String password = mPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {  // Error with user inputted fields
                showError(getString(R.string.error_message_login));
            }
            else {  // User inputted fields are valid
                // Google Analytics tracking code - attempted login
                Tracker t = ((MountaineersApp) getApplication()).getTracker
                        (MountaineersApp.TrackerName.APP_TRACKER);
                t.setScreenName("Sign in attempt");
                t.send(new HitBuilders.AppViewBuilder().build());

                // This method logs the member in to validate his/her identity
                mProgress.setVisibility(View.VISIBLE);  // Show progress circle

                // Log in with user provided credentials
                mMember.setUsername(username);
                mMember.setPassword(password);
                mMember.login(this);
            }
        }
        else {  // Still downloading the login web page so prevent user from submitting credentials
            if (failedToLogIn) {
                // Start the prelogin process of getting the login webpage
                getLoginWebPage();
            }
            else {
                Toast.makeText(this, getString(R.string.toast_prelogin_wait),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void signUp(View v) {
        // Google Analytics tracking code - redirect to Sign Up webpage
        Tracker t = ((MountaineersApp) getApplication()).getTracker
                (MountaineersApp.TrackerName.APP_TRACKER);
        t.setScreenName("Sign Up");
        t.send(new HitBuilders.AppViewBuilder().build());

        // onClick event routine for clicking Sign Up text
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(R.string.mountaineers_sign_up_url)));
        // Prevent the next activity to come back to the login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}