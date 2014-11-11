package com.travisyim.mountaineers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.objects.SavedSearch;

import java.util.Date;
import java.util.List;

public class SavedSearchAdapter extends ArrayAdapter<SavedSearch> {
    private Context mContext;
    private List<SavedSearch> mSavedSearches;  // Contains saved searches attached to the adapter

    public SavedSearchAdapter(Context context, List<SavedSearch> savedSearches) {
        super(context, R.layout.saved_search_item, savedSearches);

        mContext = context;
        mSavedSearches = savedSearches;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        SavedSearch savedSearch;

        // Check to see if view for item exists
        if (convertView == null) { // No
            convertView = LayoutInflater.from(mContext).inflate(R.layout.saved_search_item, null);
            holder = new ViewHolder();
            holder.textViewName = (TextView) convertView.findViewById(R.id.textViewName);
            holder.textViewAccessDate = (TextView) convertView.findViewById(R.id.textViewAccessDate);
            holder.textViewUpdateCounter = (TextView) convertView.findViewById(R.id.textViewUpdateCounter);
            convertView.setTag(holder);
        }
        else { // Yes
            holder = (ViewHolder) convertView.getTag();
        }

        savedSearch = mSavedSearches.get(position);

        // Assign the properties to this holder item
        holder.textViewName.setText(savedSearch.getSearchName());  // Saved search title

        // Last access date
        holder.textViewAccessDate.setText(timeSinceLastView(savedSearch.getLastAccessDateDate()));

        // Update counter
        if (savedSearch.getUpdateCounter() <= 50) {
            holder.textViewUpdateCounter.setText(Integer.toString(savedSearch.getUpdateCounter()));
        }
        else {  // More than 100 updates so shorten the text
            holder.textViewUpdateCounter.setText("50+");
        }

        // Show/Hide update counter based on the number of updates
        if (savedSearch.getUpdateCounter() == 0) {
            holder.textViewUpdateCounter.setVisibility(View.GONE);  // Hide counter visibility
        }
        else {
            holder.textViewUpdateCounter.setVisibility(View.VISIBLE);  // Hide counter visibility
        }

        return convertView;
    }

    private class ViewHolder {
        TextView textViewName;
        TextView textViewAccessDate;
        TextView textViewUpdateCounter;
    }

    private String timeSinceLastView(Date lastView) {
        StringBuffer str = new StringBuffer();
        long delta;

        // Calculate time since last viewing
        delta = new Date().getTime() - lastView.getTime();

        str.append(mContext.getString(R.string.saved_search_last_view));

        // Determine when the saved search was last viewed
        if (delta / 1000 < 10) {
            // Less than 10 seconds
            str.append("A moment ago");
        }
        else if (delta / 1000 < 60) {
            // Less than a minute ago
            str.append("Less than a minute ago");
        }
        else if (delta / (1000 * 60) < 60) {
            // X minute(s) ago
            str.append(((int) delta / (1000 * 60)) + " minute(s) ago");
        }
        else if (delta / (1000 * 60 * 60) < 24) {
            // X hours(s) ago
            str.append(((int) delta / (1000 * 60 * 60)) + " hours(s) ago");
        }
        else {
            // X day(s) ago
            str.append(((int) delta / (1000 * 60 * 60 * 24)) + " day(s) ago");
        }

        return str.toString();
    }
}