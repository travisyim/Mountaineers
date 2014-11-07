package com.travisyim.mountaineers.objects;

public class AsyncTaskResult<V> {
    private V mResult;
    private Exception mError;

    public V getResult() {
        return mResult;
    }

    public Exception getError() {
        return mError;
    }

    public AsyncTaskResult(V result) {
        mResult = result;
    }

    public AsyncTaskResult(Exception error) {
        mError = error;
    }
}