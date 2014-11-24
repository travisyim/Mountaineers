package com.travisyim.mountaineers.objects;

public class FragmentListItem {
    private String mProfileImage = null;
    private String mTitle;
    private int mIcon;
    private int mUpdateCount;

    public FragmentListItem(final String title, final int icon, final int updateCount) {
        mTitle = title;
        mIcon = icon;
        mUpdateCount = updateCount;
    }

    public String getProfileImage() {
        return mProfileImage;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getIcon() {
        return mIcon;
    }

    public int getUpdateCount() {
        return mUpdateCount;
    }

    public void setCounter(final int updateCount) {
        mUpdateCount = updateCount;
    }

    public void setProfileImage(final String profileImage) {
        mProfileImage = profileImage;
    }
}
