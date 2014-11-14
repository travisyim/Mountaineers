package com.travisyim.mountaineers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.objects.SavedSearch;
import com.travisyim.mountaineers.utils.ParseConstants;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SavedSearchAdapter extends ArrayAdapter<SavedSearch> {
    private Context mContext;
    private List<SavedSearch> mSavedSearches;  // Contains saved searches attached to the adapter
    private boolean mIsDeleteState = false;

    private final String TAG = SavedSearchAdapter.class.getSimpleName() + ":";

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
            holder.imageViewNext = (ImageView) convertView.findViewById(R.id.imageViewNext);
            holder.imageViewDelete = (ImageView) convertView.findViewById(R.id.imageViewDelete);

            holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = (Integer) v.getTag();

                    /* Tell Parse backend that user is now viewing this saved search so go ahead and update
                     * the last viewed timestamp and reset the update counter to 0 */
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(ParseConstants.KEY_OBJECT_ID, mSavedSearches.get(position).getObjectID());

                    // Run cloud code to delete selected saved search
                    ParseCloud.callFunctionInBackground("deleteSavedSearch", params,
                            new FunctionCallback<Object>() {
                                @Override
                                public void done(Object o, ParseException e) {
                                    if (e != null) {  // An error occured running the cloud function
                                        Toast.makeText(mContext, mContext.getString(R.string.toast_error_delete),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                    // Delete item and update listView
                    mSavedSearches.remove(position);
                    notifyDataSetChanged();
                }
            });

            convertView.setTag(holder);
        }
        else { // Yes
            holder = (ViewHolder) convertView.getTag();
        }

        savedSearch = mSavedSearches.get(position);
        holder.imageViewDelete.setTag(position);

        // Assign the properties to this holder item
        holder.textViewName.setText(savedSearch.getSearchName());  // Saved search title

        // Last access date
        holder.textViewAccessDate.setText(timeSinceLastView(savedSearch.getLastAccessDateDate()));

        // Update counter
        if (savedSearch.getUpdateCounter() <= 50) {
            holder.textViewUpdateCounter.setText(Integer.toString(savedSearch.getUpdateCounter()));
        }
        else {  // More than 50 updates so shorten the text
            holder.textViewUpdateCounter.setText("50+");
        }

        // Show/Hide update counter based on the number of updates
        if (savedSearch.getUpdateCounter() == 0) {
            holder.textViewUpdateCounter.setVisibility(View.GONE);  // Hide counter visibility
        }
        else {
            holder.textViewUpdateCounter.setVisibility(View.VISIBLE);  // Hide counter visibility
        }

        // Show / hide Delete and Next imageViews based on Edit state of Saved Search Fragment
        if (mIsDeleteState) {  // In delete state
            holder.imageViewDelete.setVisibility(View.VISIBLE);
            holder.imageViewNext.setVisibility(View.INVISIBLE);
        }
        else {
            holder.imageViewDelete.setVisibility(View.GONE);
            holder.imageViewNext.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private class ViewHolder {
        TextView textViewName;
        TextView textViewAccessDate;
        TextView textViewUpdateCounter;
        ImageView imageViewNext;
        ImageView imageViewDelete;
    }

    public void changeState(boolean isDeleteState) {
        mIsDeleteState = isDeleteState;
        notifyDataSetChanged();
    }

    private String timeSinceLastView(Date lastView) {
        // This method calculates the time since the saved search was last viewed
        StringBuffer str = new StringBuffer();
        long delta;

        // Calculate time since last viewing
        delta = new Date().getTime() - lastView.getTime();

        str.append(mContext.getString(R.string.saved_search_last_view));

        // Determine when the saved search was last viewed
        if (delta / 1000 < 10) {
            // Less than 10 seconds ago
            str.append(mContext.getString(R.string.lastViewMoment));
        }
        else if (delta / 1000 < 60) {
            // X seconds ago
            str.append(mContext.getResources().getQuantityString(R.plurals.lastViewedSeconds,
                    (int) (delta / 1000), (int) (delta / 1000)));
        }
        else if (delta / (1000 * 60) < 60) {
            // X minute(s) ago
            str.append(mContext.getResources().getQuantityString(R.plurals.lastViewedMinutes,
                    (int) (delta / (1000 * 60)), (int) (delta / (1000 * 60))));
        }
        else if (delta / (1000 * 60 * 60) < 24) {
            // X hours(s) ago
            str.append(mContext.getResources().getQuantityString(R.plurals.lastViewedHours,
                    (int) (delta / (1000 * 60 * 60)), (int) (delta / (1000 * 60 * 60))));
        }
        else {
            // X day(s) ago
            str.append(mContext.getResources().getQuantityString(R.plurals.lastViewedDays,
                    (int) (delta / (1000 * 60 * 60 * 24)), (int) (delta / (1000 * 60 * 60 * 24))));
        }

        return str.toString();
    }
}