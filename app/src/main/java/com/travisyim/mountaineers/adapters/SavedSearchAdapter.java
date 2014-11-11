package com.travisyim.mountaineers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.objects.SavedSearch;
import com.travisyim.mountaineers.utils.DateUtil;

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
        holder.textViewAccessDate.setText("Last viewed: " + DateUtil.convertToString(
                savedSearch.getLastAccessDateDate(),DateUtil.TYPE_ACTIVITY_DATE_WITH_YEAR));

        // Update counter
        holder.textViewUpdateCounter.setText(Integer.toString(savedSearch.getUpdateCounter()));

        return convertView;
    }

    private class ViewHolder {
        TextView textViewName;
        TextView textViewAccessDate;
        TextView textViewUpdateCounter;
    }
}