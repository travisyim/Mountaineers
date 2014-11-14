package com.travisyim.mountaineers.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
    public static List<String> copy(List<String> src) {
        List<String> dest = new ArrayList<String>(src.size());

        // Add all Strings from src to dest
        for (String str : src) {
            dest.add(str);
        }

        return dest;
    }
}