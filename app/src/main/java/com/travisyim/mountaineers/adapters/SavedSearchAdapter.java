package com.travisyim.mountaineers.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
            holder.imageViewRename = (ImageView) convertView.findViewById(R.id.imageViewRename);

            holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = (Integer) v.getTag();

                    deleteSavedSearch(position);
                }
            });

            holder.imageViewRename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = (Integer) v.getTag();

                    showRenameDialog(position);
                }
            });

            convertView.setTag(holder);
        }
        else { // Yes
            holder = (ViewHolder) convertView.getTag();
        }

        savedSearch = mSavedSearches.get(position);

        // Save position in icon's tag for use in setOnClickListeners
        holder.imageViewDelete.setTag(position);
        holder.imageViewRename.setTag(position);

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
            holder.imageViewRename.setVisibility(View.VISIBLE);
            holder.imageViewNext.setVisibility(View.INVISIBLE);
        }
        else {
            holder.imageViewDelete.setVisibility(View.GONE);
            holder.imageViewRename.setVisibility(View.GONE);
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
        ImageView imageViewRename;
    }

    public void changeState(final boolean isDeleteState) {
        mIsDeleteState = isDeleteState;
        notifyDataSetChanged();
    }

    private void deleteSavedSearch(final int position) {
        // Dialog that is shown to user to confirm delete
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(mContext.getString(R.string.dialog_delete_title));
        alert.setMessage(mContext.getString(R.string.dialog_delete_message) + " \""
                + mSavedSearches.get(position).getSearchName() + "\"?");

        alert.setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Create params to be provided to the cloud function
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
                                } else {
                                    // Delete item and update listView
                                    mSavedSearches.remove(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(mContext, mContext.getString(R.string.toast_success_delete),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        alert.setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled - intentionally left blank
            }
        });

        alert.show();
    }

    private void renameSavedSearch(final String newName, final int position) {
        // Create params to be provided to the cloud function
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(ParseConstants.KEY_OBJECT_ID, mSavedSearches.get(position).getObjectID());
        params.put(ParseConstants.KEY_SAVE_NAME, newName);

        // Run cloud code to delete selected saved search
        ParseCloud.callFunctionInBackground("renameSavedSearch", params,
                new FunctionCallback<Object>() {
                    @Override
                    public void done(Object o, ParseException e) {
                        if (e != null) {  // An error occured running the cloud function
                            Toast.makeText(mContext, mContext.getString(R.string.toast_error_rename),
                                    Toast.LENGTH_LONG).show();
                        }
                        else {
                            // Rename item and update listView
                            mSavedSearches.get(position).setSearchName(newName);
                            notifyDataSetChanged();
                            Toast.makeText(mContext, mContext.getString(R.string.toast_success_rename),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showRenameDialog(final int position) {
        // Dialog that is shown to user during the rename process
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(mContext.getString(R.string.dialog_rename_title));
        alert.setMessage(mContext.getString(R.string.dialog_rename_message));

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);
        // Populate with existing saved search name
        input.setText(mSavedSearches.get(position).getSearchName());
        input.setSelectAllOnFocus(true);  // Select the entire saved search name
        input.setSingleLine();
        alert.setView(input);

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast toast;
                boolean match = false;
                String newName = input.getText().toString().trim();

                if (!newName.isEmpty()) {
                    // Check to see if the name has been changed
                    if (!newName.equals(mSavedSearches.get(position).getSearchName())) {
                        // Check for existing Saved Search name from this user
                        for (SavedSearch ss : mSavedSearches) {
                            // New name matches existing saved search name
                            if (newName.equals(ss.getSearchName())) {
                                match = true;

                                // Show toast about having an identically named saved search
                                toast = Toast.makeText(mContext, mContext.getString
                                        (R.string.toast_error_duplicate), Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                toast.show();

                                showRenameDialog(position);  // Show dialog again
                            }
                        }

                        if (!match) {
                            // Rename the search with the provided name
                            renameSavedSearch(newName, position);
                        }
                    }
                } else {  // Invalid name
                    showRenameDialog(position);  // Show dialog again
                }
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled - intentionally left blank
            }
        });

        alert.show();
    }

    private String timeSinceLastView(final Date lastView) {
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