package com.travisyim.mountaineers.objects;

public class FragmentListItem {
    public String mTitle;
    public int mIcon;
    public int mUpdateCount;

    public FragmentListItem(final String title, final int icon, final int updateCount) {
        mTitle = title;
        mIcon = icon;
        mUpdateCount = updateCount;
    }

    public void setCounter(final int updateCount) {
        mUpdateCount = updateCount;
    }
}
