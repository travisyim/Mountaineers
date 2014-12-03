package com.travisyim.mountaineers.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.ui.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class Receiver extends ParsePushBroadcastReceiver {
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String NOTIFICATION_MSG_COUNT = "notification_message_count";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        String alert = null;
        int count = 0;
        int previousCount;

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            if (json.has("alert")) {
                alert = json.getString("alert");
            }

            if (json.has("count")) {
                count = json.getInt("count");
            }
        }
        catch (JSONException e) { /* Intentionally left blank */ }

        /* Get the previous saved search update count shown in last push notification.  This will be
         * 0 if user clicked (i.e. viewed) on push notification. */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        previousCount = sp.getInt(NOTIFICATION_MSG_COUNT, 0);

        // count = 0 represents an alert being sent (not count update)
        if (count == 0 || count != previousCount) {
            // Save latest count and generate notification
            sp.edit().putInt(NOTIFICATION_MSG_COUNT, count).apply();
            generateNotification(context, alert, count);  // Generate notification
        }
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Class<? extends Activity> cls = getActivity(context, intent);
        Intent activityIntent;

        ParseAnalytics.trackAppOpened(intent);

        activityIntent = new Intent(context, MainActivity.class);

        /* Trigger the navigation drawer by setting it to unlearned state (this will show user the #
         * of updates) */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, false).apply();

        // Save the clicked (i.e. viewed) state of the push notification
        sp.edit().putInt(NOTIFICATION_MSG_COUNT, 0).apply();

        if (Build.VERSION.SDK_INT >= 16) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(cls);
            stackBuilder.addNextIntent(activityIntent);
            stackBuilder.startActivities();
        } else {
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(activityIntent);
        }
    }

    private void generateNotification(final Context context, final String alert, final int count) {
        String message;
        int notificationID = 1;  // Notification ID number that identifies this message

        // Check if this is a custom message or regular saved search count update
        if (alert == null) {  // Generate counter message
            message = count + " saved searches have new activities!";
        }
        else {  // Show custom message
            message = alert;
        }

        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        Intent intent = new Intent("com.parse.push.intent.OPEN");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_logomark)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL);

        // Because the ID remains unchanged, the existing notification is updated
        notificationManager.notify(notificationID, builder.build());
    }
}