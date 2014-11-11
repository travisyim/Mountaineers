package com.travisyim.mountaineers.objects;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.utils.OnTaskCompleted;
import com.travisyim.mountaineers.utils.ParseConstants;
import com.travisyim.mountaineers.utils.SavedSearchLoader;
import com.travisyim.mountaineers.utils.SimpleCrypto;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public final class Mountaineer implements Serializable {
    private String mUsername;
    private String mPassword;
    private String[][] mBadgeInfo;
    private String[][] mCurrentActivity;
    private String[][] mPastActivity;
    private String mBranch;
    private String mProfileImageUrl;
    private String mMemberUrl;
    private String mName;
    private String mRegDate;
    private String mLoginUrl;
    private String mWebPage;
    private List<String> mCookies;
    private List<SavedSearch> mSavedSearches = new ArrayList<SavedSearch>();

    public static final int STAGE_GET_LOGIN_WEB_PAGE = 1;
    public static final int STAGE_LOGIN = 2;
    public static final int STAGE_SAVE_MEMBER = 3;
    public static final int STAGE_GET_SAVED_SEARCHES = 4;
    public static final int STAGE_GET_MEMBER_DATA = 5;
    public static final int STAGE_GET_MEMBER_HISTORY = 6;

    public Mountaineer(final String loginURL, final String username, final String password,
                       final String memberUrl) {
        mLoginUrl = loginURL;
        mUsername = username;
        mPassword = password;
        mMemberUrl = memberUrl;
    }

    public Mountaineer(final String loginURL) {
        mLoginUrl = loginURL;
    }

    public final String[][] getBadgeInfo() {
        return mBadgeInfo;
    }

    public final void setBadgeInfo(final String[][] badgeInfo) {
        mBadgeInfo = badgeInfo;
    }

    public final String getBranch() {
        return mBranch;
    }

    public final void setBranch(final String branch) {
        mBranch = branch;
    }

    public final List<String> getCookies() {
        return mCookies;
    }

    public final void setCookies(final List<String> cookies) {
        mCookies = cookies;
    }

    public final String[][] getCurrentActivity() {
        return mCurrentActivity;
    }

    public final void setCurrentActivity(final String[][] currentActivity) {
        mCurrentActivity = currentActivity;
    }

    public final String getLoginUrl() {
        return mLoginUrl;
    }

    public final String getMemberUrl() {
        return mMemberUrl;
    }

    public final void setMemberUrl(final String memberUrl) {
        mMemberUrl = memberUrl;
    }

    public final String getName() {
        return mName;
    }

    public final void setName(final String name) {
        mName = name;
    }

    public final String getPassword() {
        return mPassword;
    }

    public final void setPassword(final String password) {
        mPassword = password;
    }

    public final String[][] getPastActivity() {
        return mPastActivity;
    }

    public final void setPastActivity(final String[][] pastActivity) {
        mPastActivity = pastActivity;
    }

    public final String getProfileImageUrl() {
        return mProfileImageUrl;
    }

    public final void setProfileImageUrl(final String profileImageUrl) {
        mProfileImageUrl = profileImageUrl;
    }

    public final String getRegDate() {
        return mRegDate;
    }

    public final void setRegDate(final String regDate) {
        mRegDate = regDate;
    }

    public final List<SavedSearch> getSavedSearchList() {
        return mSavedSearches;
    }

    public final void setSavedSearchList(final List<SavedSearch> savedSearches) {
        mSavedSearches = savedSearches;
    }

    public final String getUsername() {
        return mUsername;
    }

    public final void setUsername(final String username) {
        mUsername = username;
    }

    public final String getWebPage() {
        return mWebPage;
    }

    public final void setWebPage(final String webPage) {
        mWebPage = webPage;
    }


    public final void getLoginWebPage(final OnTaskCompleted listener) {
        // This method launches the first task in the authentication process
        new LoginWebPage(this, listener).execute();
    }

    public final void login(final OnTaskCompleted listener) {
        /* This method picks up where getLoginWebPage() left off and logs into the Mountaineers
         * website to authentication the user */
        new Login(this, listener).execute();
    }

    public final void saveParseUser(final Activity activity) {
        new SaveMember((Context) activity, this, (OnTaskCompleted) activity).execute();
    }

    public final void getSavedSearches(final OnTaskCompleted listener) {
        new SavedSearches(this, listener).execute();
    }

    public final void getMemberData(final OnTaskCompleted listener) {
        new MemberData(this, listener).execute();
    }

    public final void getMemberHistory(final OnTaskCompleted listener) {
        new MemberHistory(this, listener).execute();
    }

    /* The following class contains the code to complete the first task in the authentication
     * process.  The login webpage must be downloaded and the login and password fields identified. */
    private static final class LoginWebPage extends AsyncTask<Object, Void, AsyncTaskResult<Boolean>> {
        private final Mountaineer mMember;
        private final OnTaskCompleted mListener;
        private static final String TAG = LoginWebPage.class.getSimpleName() + ":";

        private LoginWebPage(final Mountaineer member, final OnTaskCompleted listener) {
            mMember = member;
            mListener = listener;
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Boolean> result) {
            super.onPostExecute(result);

            // Return result to onTaskCompleted method of calling class
            mListener.onTaskCompleted(STAGE_GET_LOGIN_WEB_PAGE, result);
        }

        @Override
        protected final AsyncTaskResult<Boolean> doInBackground(Object[] objects) {
            WebPageResult webPageResult;

            // Make sure cookies are turned on
            CookieHandler.setDefault(new CookieManager());

            // Send a "GET" request to obtain login page's form data
            try {
                webPageResult = GetPageContent(mMember.getLoginUrl(), true);

                // Check for successful webpage download
                if (webPageResult.getResponseCode() == 200) {  // Success!
                    // Save web page HTML
                    mMember.setWebPage(webPageResult.getWebPageHTML());

                    // Return to UI thread
                    return new AsyncTaskResult<Boolean>(true);
                }
                else {  // Error
                    // Return to UI thread
                    return new AsyncTaskResult<Boolean>
                            (new Exception("Member login web page unreachable"));
                }
            } catch (Exception e) {  // Return any errors for processing by UI thread
                return new AsyncTaskResult<Boolean>
                        (new Exception("Member login web page unreachable"));
            }
        }

        private WebPageResult GetPageContent(String url, boolean overwriteCookies) throws Exception {
            WebPageResult webPageResult = new WebPageResult();
            URL obj = new URL(url);

            // Open GET connection to login page
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);

            // Act like a browser
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            if (mMember.getCookies() != null) {
                for (String cookie : mMember.getCookies()) {
                    conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
                }
            }

            int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            // Overwrite cookies if allowed
            if (overwriteCookies) {
                // Get the response cookies
                mMember.setCookies(conn.getHeaderFields().get("Set-Cookie"));
            }

            // Populate result object and return it to originating method
            webPageResult.setResponseCode(responseCode);
            webPageResult.setWebPageHTML(response.toString());
            return webPageResult;
        }
    }

    private static final class Login extends AsyncTask<Object, Void, AsyncTaskResult<Boolean>> {
        private final Mountaineer mMember;
        private final OnTaskCompleted mListener;
        private static final String TAG = Login.class.getSimpleName() + ":";

        private Login(final Mountaineer member, final OnTaskCompleted listener) {
            mMember = member;
            mListener = listener;
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Boolean> result) {
            super.onPostExecute(result);

            // Return result to onTaskCompleted method of calling class
            mListener.onTaskCompleted(STAGE_LOGIN, result);
        }

        @Override
        protected final AsyncTaskResult<Boolean> doInBackground(Object[] objects) {
            WebPageResult webPageResult;

            // Construct login content and then send a POST request for authentication
            try {
                // Generate a parameter string for POST request
                webPageResult = GetFormParams(mMember.getWebPage(), mMember.getUsername(),
                        mMember.getPassword());

                // Check to see if the parameter list generation was successful
                if (webPageResult.getSuccess()) {  // Success
                    // Send POST request
                    webPageResult = SendPost(mMember.getLoginUrl(), webPageResult.getParams());

                    // Check for successful webpage login
                    if (webPageResult.getResponseCode() == 200 &&
                            !webPageResult.getWebPageHTML().toLowerCase().contains("log in")) {
                        // Save web page HTML
                        mMember.setWebPage(webPageResult.getWebPageHTML());

                        /* Get member's profile webpage address - only do this if we don't already
                         * know the member's profile webpage URL */
                        if (mMember.getMemberUrl() == null) {
                            webPageResult = GetMemberPage(mMember.getWebPage());

                            // Check to see if we get a valid user profile URL
                            if (webPageResult.getError() == null &&
                                    webPageResult.getWebPageURL().toLowerCase().contains
                                            ("https://www.mountaineers.org/members/")) {
                                mMember.setMemberUrl(webPageResult.getWebPageURL());

                                // Return to UI thread
                                return new AsyncTaskResult<Boolean>(true);
                            }
                            else if (webPageResult.getError() != null) {
                                /* This error is the result of not being able to find the expected
                                 * location of the member profile URL most likely due to the website
                                 * changing format or layout. Expecting NullPointException*/

                                // Return to UI thread
                                return new AsyncTaskResult<Boolean>(new Exception
                                        ("Member profile web page unreachable at this time"));
                            }
                            else {  // No error but URL returned is not valid (unknown error)
                                // Return to UI thread
                                return new AsyncTaskResult<Boolean>(new Exception
                                        ("Member profile web page unreachable at this time"));
                            }
                        }
                        else {  // User profile URL already known - no need to find it in the webpage
                            // Return to UI thread
                            return new AsyncTaskResult<Boolean>(true);
                        }
                    }
                    else {  // Error sending POST request
                        // Return to UI thread
                        return new AsyncTaskResult<Boolean>(new Exception
                                ("The username and password provided are not valid"));
                    }
                }
                else {  // Error generating parameter list
                    /* This error is most likely due to the login webpage changing formats as the
                     * expected username and/or password fields cannot be found */

                    // Return to UI thread
                    return new AsyncTaskResult<Boolean>(new Exception
                            ("Unable to log in at this time"));
                }
            } catch (Exception e) {  // Return any errors for processing by UI thread
                return new AsyncTaskResult<Boolean>(new Exception
                        ("Unable to log in at this time"));
            }
        }

        private WebPageResult GetFormParams(String html, String username, String password) throws
                UnsupportedEncodingException {
            WebPageResult webPageResult = new WebPageResult();
            boolean foundUsername = false;
            boolean foundPassword = false;

            Document doc = Jsoup.parse(html);

            // Mountaineers form id
            Element loginForm = doc.getElementById("login_form");
            Elements inputElements = loginForm.getElementsByTag("input");
            List<String> paramList = new ArrayList<String>();

            for (Element inputElement : inputElements) {
                String key = inputElement.attr("name");
                String value = inputElement.attr("value");

                if (key.equals("__ac_name")) {
                    value = username;
                    foundUsername = true;
                } else if (key.equals("__ac_password")) {
                    value = password;
                    foundPassword = true;
                }

                paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
            }

            // Build parameters list
            StringBuilder result = new StringBuilder();
            for (String param : paramList) {
                if (result.length() == 0) {
                    result.append(param);
                } else {
                    result.append("&").append(param);
                }
            }

            // Populate result object and return it to originating method
            if (foundUsername && foundPassword) {  // Both fields were found
                webPageResult.setSuccess(true);
            }
            else {  // Error with finding username and/or password fields
                webPageResult.setSuccess(false);
            }

            webPageResult.setParams(result.toString());  // Return POST parameters
            return webPageResult;
        }

        private WebPageResult GetMemberPage(String html) {
            /* This function returns the URL of the user's "My Profile" webpage.  It must be
             * dynamically obtained because the URL contains the member's name */
            WebPageResult webPageResult = new WebPageResult();
            final Document doc = Jsoup.parse(html);

            /* Return the member website in the <a href=> tag under the <li id=personaltools-profile>
             * element node */
            try {
                // Save user profile URL
                webPageResult.setWebPageURL(doc.select("li#personaltools-profile").first()
                        .select("a").first().attr("href"));
            }
            catch (Exception e) {  // Error occurred
                webPageResult.setError(e);
            }

            return webPageResult;
        }

        private WebPageResult SendPost(String url, String postParams) throws Exception {
            WebPageResult webPageResult = new WebPageResult();
            BufferedReader in;

            URL obj = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();

            // Act like a browser
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Host", "www.mountaineers.org");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            for (String cookie : mMember.getCookies()) {
                conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }

            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Referer", url);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // Send post request
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            // Get the response cookies
            mMember.setCookies(conn.getHeaderFields().get("Set-Cookie"));

            // Populate result object and return it to originating method
            webPageResult.setResponseCode(responseCode);
            webPageResult.setWebPageHTML(response.toString());
            return webPageResult;
        }
    }

    private static final class SaveMember extends AsyncTask<Object, Void, AsyncTaskResult<Boolean>> {
        private final Context mContext;
        private final Mountaineer mMember;
        private final OnTaskCompleted mListener;
        private AsyncTaskResult<Boolean> mResult;

        private static final String TAG = SaveMember.class.getSimpleName() + ":";

        private SaveMember
                (final Context context, final Mountaineer member, final OnTaskCompleted listener) {
            mContext = context;
            mMember = member;
            mListener = listener;
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Boolean> result) {
            super.onPostExecute(result);

            mListener.onTaskCompleted(STAGE_SAVE_MEMBER, result);
        }

        @Override
        protected final AsyncTaskResult<Boolean> doInBackground(Object[] objects) {
            // Save to Parse if this user is logging in (from Login Activity).
            ParseUser user = null;
            boolean userExists = false;
            final String username = mMember.getUsername();
            final String password = mMember.getPassword();
            final String memberUrl = mMember.getMemberUrl();

            // Search for existing user in the Parse User class
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(ParseConstants.KEY_USERNAME, username);

            try {
                List<ParseUser> results = query.find();

                // Check to see if user exists
                if (results.size() > 0) {  // Exists
                    userExists = true;
                    user = results.get(0);
                } else {  // Does not exist
                    userExists = false;
                    user = new ParseUser();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.put(ParseConstants.KEY_MEMBER_URL, memberUrl);
                    user.put(ParseConstants.KEY_PASSWORD,
                            SimpleCrypto.encrypt(mContext.getString(R.string.key), password));
                }

                // Sign up user if he/she did not exist previously
                if (!userExists && user != null) {  // New Parse user sign up
                    user.signUp();
                }
                else {  // Existing Parse user login
                    /* Log into Parse using existing password (not the password provided in Login
                     * Activity) */
                    ParseUser.logIn(username, SimpleCrypto.decrypt(mContext.getString(R.string.key),
                            user.get(ParseConstants.KEY_PASSWORD).toString()));

                    /* Update user's password and member URL even if nothing has changed.  The
                     * reason being that it takes as many or more calls to Parse to retrieve
                     * password and then save if necessary.  Just go ahead and overwrite password
                     * each time. Extra side note - user must be  signed in to change/update
                     * password (that's why the user is signed in first) */
                    user.setPassword(password);
                    user.put(ParseConstants.KEY_MEMBER_URL, memberUrl);
                    user.put(ParseConstants.KEY_PASSWORD, SimpleCrypto.encrypt
                            (mContext.getString(R.string.key), password));

                    user.save();
                }

                // Saving of crendtials successful
                mResult = new AsyncTaskResult<Boolean>(true);
            } catch (ParseException e) {
                // Something went wrong
                mResult = new AsyncTaskResult<Boolean>
                        (new Exception("Error saving login information"));
            }

            return mResult;
        }
    }

    private static final class SavedSearches extends AsyncTask<Object, Void, AsyncTaskResult<Boolean>> {
        private final Mountaineer mMember;
        private final OnTaskCompleted mListener;

        private static final String TAG = SaveMember.class.getSimpleName() + ":";

        private SavedSearches(final Mountaineer member, final OnTaskCompleted listener) {
            mMember = member;
            mListener = listener;
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Boolean> result) {
            super.onPostExecute(result);

            mListener.onTaskCompleted(STAGE_GET_SAVED_SEARCHES, result);
        }

        @Override
        protected final AsyncTaskResult<Boolean> doInBackground(Object[] objects) {
            ParseQuery<ParseObject> query;
            List<ParseObject> results;

            query = ParseQuery.getQuery(ParseConstants.CLASS_SAVED_SEARCH);
            query.whereEqualTo(ParseConstants.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
            query.orderByAscending(ParseConstants.KEY_SAVE_NAME);
            query.setLimit(1000); // limit to 1000 results max

            try {
                // Get results from Parse (no need to find in background as this is running in AsyncTask)
                results = query.find();
            }
            catch (ParseException e) {
                // Something went wrong
                return new AsyncTaskResult<Boolean>
                        (new Exception("Error getting saved searches"));
            }

            // Load saved search results and assign to the current Mountaineer object
            mMember.setSavedSearchList(SavedSearchLoader.load(results));

            // Saving of credentials successful
            return new AsyncTaskResult<Boolean>(true);
        }
    }

    private static final class MemberData extends AsyncTask<Object, Void, AsyncTaskResult<Boolean>> {
        private final Mountaineer mMember;
        private final OnTaskCompleted mListener;
        private static final String TAG = MemberData.class.getSimpleName() + ":";

        private MemberData(final Mountaineer member, final OnTaskCompleted listener) {
            mMember = member;
            mListener = listener;
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Boolean> result) {
            super.onPostExecute(result);

            mListener.onTaskCompleted(STAGE_GET_MEMBER_DATA, result);
        }

        @Override
        protected final AsyncTaskResult<Boolean> doInBackground(Object[] objects) {
            WebPageResult webPageResult;

            // Extract user data from website
            try {
                // Scrape user profile data from member webpage
                webPageResult = GetPageContent(mMember.getMemberUrl(), false);

                // Check for successful webpage download
                if (webPageResult.getResponseCode() == 200 &&
                        webPageResult.getWebPageHTML().toLowerCase().contains("my profile")) {
                    // Success! Save web page HTML
                    mMember.setWebPage(webPageResult.getWebPageHTML());

                    // Scrape user profile data
                    webPageResult = ScrapeUserData(mMember.getWebPage());

                    if (webPageResult.getSuccess()) {  // Success!
                        // Return to UI thread
                        return new AsyncTaskResult<Boolean>(true);
                    }
                    else {  // Return to UI thread
                        /* This error is the result of not being able to find the expected location
                         * of the member profile data most likely due to the website changing format
                         * or layout. Expecting NullPointException */

                        // Return to UI thread
                        return new AsyncTaskResult<Boolean>(new Exception
                                ("Member profile web page unreachable at this time"));
                    }
                }
                else {  // Error
                    /* If the response code = 200, then the page being returned does not match what
                     * is expected.  It is possible that either the format of the member profile web
                     * page has changed or the cookies have expired. */

                    // Return to UI thread
                    return new AsyncTaskResult<Boolean>
                            (new Exception("Member profile web page unreachable at this time"));
                }
            } catch (Exception e) {  // Return any errors for processing by UI thread
                return new AsyncTaskResult<Boolean>
                        (new Exception("Member profile web page unreachable at this time"));
            }
        }

        private WebPageResult ScrapeUserData(String html) {
            WebPageResult webPageResult = new WebPageResult();
            final Document doc = Jsoup.parse(html);
            final Elements badges;
            final String[][] badgeInfo;
            Element badgeImg;
            String tempString;
            int i = 0;

            // USER PERSONAL INFO
            try {
                // Name
                mMember.setName(doc.select("div.profile-left").first().getElementsByTag("h1")
                        .first().text());
                // Registration date
                tempString = doc.select("ul.details").first().getElementsByTag("li").first().text();
                mMember.setRegDate(tempString.substring(tempString.indexOf(":") + 2));
                // Branch
                tempString = doc.select("ul.details").first().getElementsByTag("li").last().text();
                mMember.setBranch(tempString.substring(tempString.indexOf(":") + 2));
                // Profile image
                mMember.setProfileImageUrl(doc.select("div.profile-image").first()
                        .getElementsByTag("img").first().absUrl("src"));

                // User may not have any badges, so if we got to this point this is a success
                webPageResult.setSuccess(true);

                // TODO: When implementing badges in the PROFILE fragment, make sure to account for either have null for mBadges or a 2 x 0 length array
                // BADGES
                badges = doc.select("li.badge");  // Get badge parent elements

                // Initialize badge array sizes
                badgeInfo = new String[2][badges.size()];

                // Loop through each badge parent element node and get "img" child node
                for (Element badge : badges) {
                    /* There's only one "img" child node per badge parent element, so go directly to
                     * the first */
                    badgeImg = badge.getElementsByTag("img").first();

                    // Assign badge name and URL
                    badgeInfo[0][i] = badgeImg.attr("title");
                    badgeInfo[1][i] = badgeImg.absUrl("src");

                    i++;  // Increment array index counter
                }

                // Array of badge names and their respective image URLs
                mMember.setBadgeInfo(badgeInfo);
            }
            catch (Exception e) {
                webPageResult.setError(e);
            }

            return webPageResult;
        }

        private WebPageResult GetPageContent(String url, boolean overwriteCookies) throws Exception {
            WebPageResult webPageResult = new WebPageResult();
            URL obj = new URL(url);

            // Open GET connection to login page
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);

            // Act like a browser
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            if (mMember.getCookies() != null) {
                for (String cookie : mMember.getCookies()) {
                    conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
                }
            }

            int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            // Overwrite cookies if allowed
            if (overwriteCookies) {
                // Get the response cookies
                mMember.setCookies(conn.getHeaderFields().get("Set-Cookie"));
            }

            // Populate result object and return it to originating method
            webPageResult.setResponseCode(responseCode);
            webPageResult.setWebPageHTML(response.toString());
            return webPageResult;
        }
    }

    private static final class MemberHistory extends AsyncTask<Object, Void, AsyncTaskResult<Boolean>> {
        private final Mountaineer mMember;
        private final OnTaskCompleted mListener;
        private static final String TAG = MemberHistory.class.getSimpleName() + ":";

        private MemberHistory(final Mountaineer member, final OnTaskCompleted listener) {
            mMember = member;
            mListener = listener;
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Boolean> result) {
            super.onPostExecute(result);

            mListener.onTaskCompleted(STAGE_GET_MEMBER_HISTORY, result);
        }

        @Override
        protected final AsyncTaskResult<Boolean> doInBackground(Object[] objects) {
            WebPageResult webPageResult;

            // Extract user activities from website
            try {
                // Scrape registered activities and activity history
                webPageResult = GetPageContent(mMember.getMemberUrl() + "/member-activities", false);

                // Check for successful webpage download
                if (webPageResult.getResponseCode() == 200 &&
                        webPageResult.getWebPageHTML().toLowerCase()
                                .contains("my activities")) {  // Success!
                    // Save web page HTML
                    mMember.setWebPage(webPageResult.getWebPageHTML());

                    webPageResult = ScrapeUserHistory(mMember.getWebPage());

                    if (webPageResult.getError() == null && webPageResult.getSuccess()) {
                        // Return to UI thread
                        return new AsyncTaskResult<Boolean>(true);
                    }
                    else {  // Error - Return to UI thread
                        /* This error is the result of not being able to find the expected
                         * location of the activity tables most likely due to the website
                         * changing format or layout. Expecting NullPointException*/

                        return new AsyncTaskResult<Boolean>(new Exception
                                ("Member activities web page unreachable at this time"));
                    }
                }
                else {  // Return to UI thread
                    /* If the response code = 200, then the page being returned does not
                     * match what is expected.  It is possible that either the format of the
                     * member profile web page has changed or the cookies have expired. */
                    return new AsyncTaskResult<Boolean>(new Exception
                            ("Member activities web page unreachable at this time"));
                }
            } catch (Exception e) {  // Return any errors for processing by UI thread
                return new AsyncTaskResult<Boolean>
                        (new Exception("Member activities web page unreachable at this time"));
            }
        }

        private WebPageResult ScrapeUserHistory(String html) {
            WebPageResult webPageResult = new WebPageResult();
            final Document doc = Jsoup.parse(html);
            final Elements tables;
            String[][] curActivity = null;
            String[][] pastActivity = null;
            Elements trs;
            Elements td;
            int i;

            // Scrape the activity data
            try {
                // Get both table elements
                tables = doc.select("table.listing");  // Get badge parent elements

                // For each table, size the array according to contents
                for (Element table : tables) {
                    i = 0;
                    trs = table.select("tr.activity-listing");

                    if (table == tables.first()) {  // Current activity table
                        curActivity = new String[6][trs.size()];

                        // Go through each <tr> tag and extract pertinent activity data
                        for (Element tr : trs) {
                            // <td> tags contain the "Date", "Activity", "Leader", "Role" and "Status" fields
                            td = tr.getElementsByTag("td");

                            curActivity[0][i] = td.first().text();  // Date
                            curActivity[1][i] = td.get(1).text();  // Activity name
                            // Activity webpage
                            curActivity[2][i] = td.get(1).select("a").first().attr("href");
                            curActivity[3][i] = td.get(2).text();  // Leader name
                            curActivity[4][i] = td.get(3).text();  // Role
                            curActivity[5][i] = td.last().text();  // Status

                            i++;
                        }

                        mMember.setCurrentActivity(curActivity);
                    }
                    else {  // Past activity table
                        pastActivity = new String[6][trs.size()];

                        // Go through each <tr> tag and extract pertinent activity data
                        for (Element tr : trs) {
                            // <td> tags contain the "Date", "Activity", "Leader", "Role" and "Status" fields
                            td = tr.getElementsByTag("td");

                            pastActivity[0][i] = td.first().text();  // Date
                            pastActivity[1][i] = td.get(1).text();  // Activity name
                            // Activity webpage
                            pastActivity[2][i] = td.get(1).select("a").first().attr("href");
                            pastActivity[3][i] = td.get(2).text();  // Leader name
                            pastActivity[4][i] = td.get(3).text();  // Role
                            pastActivity[5][i] = td.last().text();  // Status

                            i++;
                        }

                        // TODO: When implementing badges in the activities history / signed up fragments, make sure to account for either have null for mActivities or a 6 x 0 length array
                        mMember.setPastActivity(pastActivity);
                    }
                }

                // Activity data scraping successful
                webPageResult.setSuccess(true);
            } catch (Exception e) {  // Error while scraping activity data
                webPageResult.setError(e);
            }

            // Check to see if there was only one table
            if (pastActivity == null) {  // Only one table exists
                // This table represents past history
                if (html.toLowerCase().contains("my activities history")) {
                    // Initialize past history array
                    pastActivity = new String[6][curActivity[0].length];

                    // Copy the array over
                    for (i = 0; i <= 5; i++) {
                        System.arraycopy(curActivity[i], 0, pastActivity[i], 0, curActivity[i].length);
                    }

                    // Clear current activity list
                    curActivity = null;

                    // Update activities
                    mMember.setCurrentActivity(curActivity);
                    mMember.setPastActivity(pastActivity);
                }
            }

            return webPageResult;
        }

        private WebPageResult GetPageContent(String url, boolean overwriteCookies) throws Exception {
            WebPageResult webPageResult = new WebPageResult();
            URL obj = new URL(url);

            // Open GET connection to login page
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);

            // Act like a browser
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            if (mMember.getCookies() != null) {
                for (String cookie : mMember.getCookies()) {
                    conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
                }
            }

            int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            // Overwrite cookies if allowed
            if (overwriteCookies) {
                // Get the response cookies
                mMember.setCookies(conn.getHeaderFields().get("Set-Cookie"));
            }

            // Populate result object and return it to originating method
            webPageResult.setResponseCode(responseCode);
            webPageResult.setWebPageHTML(response.toString());
            return webPageResult;
        }
    }
}