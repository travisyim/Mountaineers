package com.travisyim.mountaineers.utils;

import com.travisyim.mountaineers.objects.SavedSearch;

import java.util.Comparator;

// This class helps compare the names of SavedSearch objects and return the order
public class SavedSearchComparator implements Comparator<SavedSearch> {
    @Override
    public int compare(SavedSearch ss1, SavedSearch ss2) {
        return ss1.getSearchName().toLowerCase().compareTo(ss2.getSearchName().toLowerCase());
    }
}