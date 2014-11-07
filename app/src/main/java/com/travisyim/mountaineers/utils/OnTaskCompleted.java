package com.travisyim.mountaineers.utils;

import com.travisyim.mountaineers.objects.AsyncTaskResult;

public interface OnTaskCompleted {
    void onTaskCompleted(final int stage, final AsyncTaskResult<Boolean> result);
}