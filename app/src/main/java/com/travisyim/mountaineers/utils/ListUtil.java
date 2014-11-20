package com.travisyim.mountaineers.utils;

import java.util.ArrayList;
import java.util.List;

/* This creates a copy of a list obtain from a Parse object.  A copy is needed because if the
 * original is modified, it updates the Parse object. */
public class ListUtil {
    public static List<String> copy(List<String> src) {
        if (src == null) {  // Check for empty list
            return null;
        }

        List<String> dest = new ArrayList<String>(src.size());

        // Add all Strings from src to dest
        for (String str : src) {
            dest.add(str);
        }

        return dest;
    }
}