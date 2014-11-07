package com.travisyim.mountaineers.utils;

import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.List;

public interface OnParseTaskCompleted {
    void onParseTaskCompleted(List<ParseObject> resultList, ParseException e);
}