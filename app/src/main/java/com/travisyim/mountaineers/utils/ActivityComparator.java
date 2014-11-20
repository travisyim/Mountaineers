package com.travisyim.mountaineers.utils;

import com.travisyim.mountaineers.objects.MountaineerActivity;

import java.util.Comparator;

// This class helps compare the names of Activity objects and return the order
public class ActivityComparator implements Comparator<MountaineerActivity> {
    @Override
    public int compare(MountaineerActivity act1, MountaineerActivity act2) {
        return act1.getTitle().toLowerCase().compareTo(act2.getTitle().toLowerCase());
    }
}