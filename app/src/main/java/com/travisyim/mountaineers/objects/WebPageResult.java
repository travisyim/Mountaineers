package com.travisyim.mountaineers.objects;

public class WebPageResult {
    private Exception mError;
    private String mParams;
    private String mWebPageHTML;
    private String mWebPageURL;
    private int mResponseCode;
    private boolean mSuccess;

    public Exception getError() {
        return mError;
    }

    public void setError(Exception error) {
        mError = error;
    }

    public String getParams() {
        return mParams;
    }

    public void setParams(String params) {
        mParams = params;
    }

    public String getWebPageHTML() {
        return mWebPageHTML;
    }

    public void setWebPageHTML(String webPageHTML) {
        mWebPageHTML = webPageHTML;
    }

    public String getWebPageURL() {
        return mWebPageURL;
    }

    public void setWebPageURL(String webPageURL) {
        mWebPageURL = webPageURL;
    }

    public int getResponseCode() {
        return mResponseCode;
    }

    public void setResponseCode(int responseCode) {
        mResponseCode = responseCode;
    }

    public boolean getSuccess() {
        return mSuccess;
    }

    public void setSuccess(boolean success) {
        mSuccess = success;
    }
}