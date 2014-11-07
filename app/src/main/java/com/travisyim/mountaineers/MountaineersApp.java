package com.travisyim.mountaineers;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.PushService;
import com.travisyim.mountaineers.ui.MainActivity;

import java.util.HashMap;

public class MountaineersApp extends Application {
    private static final String PROPERTY_ID = "UA-54000902-1";
    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    @Override
    public void onCreate() {
        super.onCreate();

        // Set Parse application id and client key
        Parse.initialize(this, "GizL3YSQIa9Py8qPbcX0O4j2Jb9MF4FGQ4rYt883",
                "qLYu9qIppftQB902VTmQ1TsLuoI2bfhbN3damTzf");

        // Specify default activity to handle push notifications
        PushService.setDefaultPushCallback(this, MainActivity.class);
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.enableAutoActivityReports(this);

            Tracker t = analytics.newTracker(PROPERTY_ID);

            mTrackers.put(trackerId, t);
        }

        return mTrackers.get(trackerId);
    }
}