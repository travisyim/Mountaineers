package com.travisyim.mountaineers.adapters;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.objects.FilterOptions;
import com.travisyim.mountaineers.objects.MountaineerActivity;
import com.travisyim.mountaineers.utils.ActivityComparator;
import com.travisyim.mountaineers.utils.DateUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityAdapter extends ArrayAdapter<MountaineerActivity> {
    private Context mContext;
    private List<MountaineerActivity> mActivities;  // Contains activities attached to the adapter
    // A master copy of the full list of the activities
    private List<MountaineerActivity> mMasterActivities = new ArrayList<MountaineerActivity>();
    private Toast toast;
    private long mLastViewed;
    private int mScreenWidth;
    private boolean mIsSavedSearch = false;

    public ActivityAdapter(Context context, List<MountaineerActivity> activities, Date lastViewed) {
        super(context, R.layout.activity_item, activities);

        WindowManager wm;
        Display display;
        Point size;

        mContext = context;
        mActivities = activities;

        // Saved last viewed date if this was launched from a saved search
        if (lastViewed != null) {
            mIsSavedSearch = true;
            mLastViewed = lastViewed.getTime();
        }

        // Get screen width in pixels
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;

        // Master copy of all activities so that they will not get erased
        mMasterActivities.addAll(activities);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        MountaineerActivity activity;
        String availability;
        String regInfo;
        long updatedDate;
        int availabilityParticipant;
        int availabilityLeader;

        // Check to see if view for item exists
        if (convertView == null) { // No
            convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_item, null);
            holder = new ViewHolder();
            holder.imageViewActivity = (ImageView) convertView.findViewById(R.id.imageViewActivity);
            holder.textViewName = (TextView) convertView.findViewById(R.id.textViewName);
            holder.textViewType = (TextView) convertView.findViewById(R.id.textViewType);
            holder.textViewDate = (TextView) convertView.findViewById(R.id.textViewDate);
            holder.textViewLeader = (TextView) convertView.findViewById(R.id.textViewLeader);
            holder.textViewAvailability = (TextView) convertView.findViewById(R.id.textViewAvailability);
            holder.textViewRegInfo = (TextView) convertView.findViewById(R.id.textViewRegInfo);
            holder.textViewRole = (TextView) convertView.findViewById(R.id.textViewRole);
            holder.textViewStatus = (TextView) convertView.findViewById(R.id.textViewStatus);
            convertView.setTag(holder);
        }
        else { // Yes
            holder = (ViewHolder) convertView.getTag();
        }

        activity = mActivities.get(position);

        // Assign the properties to this holder item
        // Picture
        if (activity.getImageUrl() != null) {  // Image defined
            Picasso.with(getContext()).load(activity.getImageUrl())
//                    .placeholder(R.drawable.default_activity).error(R.drawable.default_activity)
                    .resize(mScreenWidth / 4, 0)
                    .into(holder.imageViewActivity);
        }

        // Determine if this is an unseen activity - if so, bold the title
        if (mIsSavedSearch) {  // This was launched from a saved search
            if (activity.isUnread() &&
                    (activity.getActivityStartDate().getTime() + 24 * 60 * 60 * 1000) >
                            new Date().getTime()) {  // Unseen by user and activity occurs in the future
                // Bold the activity title
                holder.textViewName.setTypeface(null, Typeface.BOLD);
            }
            else {
                // Unbold the activity title
                holder.textViewName.setTypeface(null, Typeface.NORMAL);
            }
        }

        holder.textViewName.setText(activity.getTitle());  // Activity title

        // Activity type
        try {
            if (activity.getType() != null) {
                // Remove quotes and backslash due to JSON string formatting
                holder.textViewType.setText
                        (activity.getType().join(", ").replaceAll("\"", "").replaceAll("\\\\", ""));
            }
        }
        catch (JSONException e) {/* Intentionally left blank */}

        // Format date range
        if (activity.getActivityStartDate().equals(activity.getActivityEndDate())) {
            // Single day
            holder.textViewDate.setText(DateUtil.convertToString(activity.getActivityStartDate(),
                    DateUtil.TYPE_ACTIVITY_DATE_WITH_YEAR));
        }
        else {  // Multiple days
            holder.textViewDate.setText
                    (DateUtil.convertToString(activity.getActivityStartDate(),
                            DateUtil.TYPE_ACTIVITY_DATE_NO_YEAR) + " - " +
                            DateUtil.convertToString(activity.getActivityEndDate(),
                                    DateUtil.TYPE_ACTIVITY_DATE_WITH_YEAR));
        }

        /* Leader has been defined (usually is missing from actual activity search but is present
         * for user activities) */
        if (activity.getLeaderName() != null) {
            try {
                // Remove quotes due to JSON string formatting
                holder.textViewLeader.setText(mContext.getString(R.string.leader)
                        + activity.getLeaderName().join(", ").replaceAll("\"", ""));
            }
            catch (JSONException e) {/* Intentionally left blank */}
        }
        else {
            holder.textViewLeader.setVisibility(View.GONE);
        }

        // Availability - only show if this activity is in the future
        if (new Date().getTime() > activity.getActivityStartDate().getTime() + (24 * 60 * 60 * 1000)) {
            holder.textViewAvailability.setVisibility(View.GONE);
        }
        else {
            // Build string for Availability
            // Check to see if this is defined for this activity
            if (activity.getAvailabilityParticipant() != -999 || activity.getAvailabilityLeader() != -999) {
                availability = mContext.getString(R.string.availability);
                availabilityParticipant = activity.getAvailabilityParticipant();
                availabilityLeader = activity.getAvailabilityLeader();

                // Participant availability
                availability += mContext.getResources().getQuantityString(R.plurals.numberOfParticipants,
                        Math.abs(availabilityParticipant), Math.abs(availabilityParticipant));

                if (availabilityParticipant < 0) { // Waitlist
                    availability += mContext.getString(R.string.waitlist);
                }

                // Leader availability
                if (availabilityLeader != -999) {
                    availability += ", " + mContext.getResources().getQuantityString(
                            R.plurals.numberOfLeaders, Math.abs(availabilityLeader),
                            Math.abs(availabilityLeader));

                    if (availabilityLeader < 0) { // Waitlist
                        availability += mContext.getString(R.string.waitlist);
                    }
                }

                holder.textViewAvailability.setText(availability);
            }
        }

        // Registration information
        if (!activity.isUserActivity()) {  // Not a user activity
            if (activity.isCanceled()) {  // Activity has been canceled
                regInfo = mContext.getString(R.string.activity_canceled);
            }
            // Check if activity is in the past
            else if (activity.getActivityEndDate().getTime() + 24 * 60 * 60 * 1000 <= new Date().getTime()) {
                regInfo = mContext.getString(R.string.activity_ended);
            }
            // Activity registration has ended
            else if (activity.getRegistrationCloseTime().getTime() < new Date().getTime()) {
                regInfo = mContext.getString(R.string.registration_ended);
            }
            // Activity registration has not yet opened
            else if (activity.getRegistrationOpenTime().getTime() > new Date().getTime()) {
                regInfo = mContext.getString(R.string.registration_opens) +
                        DateUtil.convertToString(activity.getRegistrationOpenTime(),
                        DateUtil.TYPE_ACTIVITY_DATE_WITH_YEAR);
            }
            else {  // Registration currently open
                regInfo = mContext.getString(R.string.registration_closes) +
                        DateUtil.convertToString(activity.getRegistrationCloseTime(),
                        DateUtil.TYPE_ACTIVITY_DATE_WITH_YEAR);
            }

            holder.textViewRegInfo.setText(regInfo);
            holder.textViewStatus.setVisibility(View.GONE);  // Hide status visibility
        }
        else {  // This is a user activity and does not have any registration information
            holder.textViewRegInfo.setVisibility(View.GONE);

            // Status - only exists for user activity
            if (activity.getStatus() != null) {  // Exists
                holder.textViewStatus.setText(mContext.getString(R.string.status) + activity.getStatus());
            }
            else {  // Not present so hide
                holder.textViewStatus.setVisibility(View.GONE);
            }
        }

        // Role - only exists for user activity
        if (activity.getUserRole() != null) {  // Exists
            holder.textViewRole.setText(mContext.getString(R.string.role) + activity.getUserRole());
        }
        else {  // Not present so hide
            holder.textViewRole.setVisibility(View.GONE);
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView imageViewActivity;
        TextView textViewName;
        TextView textViewType;
        TextView textViewDate;
        TextView textViewLeader;
        TextView textViewAvailability;
        TextView textViewRegInfo;
        TextView textViewRole;
        TextView textViewStatus;
    }

    public int getMasterActivityListCount() {
        return mMasterActivities.size();
    }

    public Date getMaxStartDate() {
        // Gets the max start start in the master list (already sorted by date)
        return mMasterActivities.get(mMasterActivities.size() - 1).getActivityStartDate();
    }

    public Date getMinStartDate() {
        // Gets the min start start in the master list (already sorted by date)
        return mMasterActivities.get(0).getActivityStartDate();
    }

    public void setMasterActivityList(final List<MountaineerActivity> activities) {
        mMasterActivities.clear();  // Clear all activities in the master list
        mMasterActivities.addAll(activities);  // Save all new activity results
    }

    public void applyTextFilter(final String queryText) {
        Set<String> keywords = new HashSet<String>();
        int newIndex = 0;
        boolean found;

        this.clear();  // Clear all activities from the adapter

        // Filter the activities list based on the search text
        if (queryText != null && !queryText.isEmpty()) {  // Search string defined
            // Add all keywords when query String is split by spaces
            keywords.addAll(Arrays.asList(queryText.trim().toLowerCase().split("\\s+")));
            // Add all keywords when query String is split by spaces
            keywords.addAll(Arrays.asList(queryText.trim().toLowerCase().split("\\b")));

            // Loop through master list of activities to search for keywords
            for (MountaineerActivity activity : mMasterActivities) {
                found = false;

                /* Search activity name and leader name to ensure all keywords are present (all
                 * keywords must be found) */
                for (String keyword : keywords) {
                    try {
                        // Ensure we are not evaluating either blank space or "'s"
                        if (!keyword.trim().isEmpty() && !keyword.equals("'s") &&
                                !keyword.equals("'") && !keyword.equals("s")) {
                            // Search for the given keyword as a whole word (bounded by \b)
                            if (activity.getTitle().toLowerCase().matches(".*\\b" + keyword + "\\b.*")
                                    || activity.getLeaderName().join(" ").toLowerCase()
                                    .matches(".*\\b" + keyword + "\\b.*")) {
                                found = true;
                                break;
                            }
                        }
                    }
                    catch (JSONException e) { /* Intentionally left blank */}
                }

                if (found) {  // All keywords found so add them to the listview adapter
                    // Check if this is an activity search launched by a saved search
                    if (!mIsSavedSearch) {  // Not a saved search
                        this.add(activity);
                    }
                    else {  // Saved search
                        if (addToListAsUnseen(activity, newIndex)) {
                            newIndex++;
                        }
                    }
                }
            }
        }
        else {  // No search string defined
            // Check if this is an activity search launched by a saved search
            if (!mIsSavedSearch) {  // Not a saved search
                this.addAll(mMasterActivities);  // Add all activities
            }
            else {  // Saved search
                // Loop through master list of activities to bring unseen activities to the top
                for (MountaineerActivity activity : mMasterActivities) {
                    if (addToListAsUnseen(activity, newIndex)) {
                        newIndex++;
                    }
                }
            }
        }

        // Reorganize list if it contains new activities and this was launched from saved search (track unseen activities)
        if (mIsSavedSearch) {
            reorganizeNewList(newIndex);
        }

        notifyDataSetChanged();
    }

    public void applyFilterOptions(final FilterOptions filterOptions) {
        List<MountaineerActivity> mTempActivities = new ArrayList<MountaineerActivity>();
        long filterStartDate;
        long filterEndDate;

        // Create copy of activities that made it through the text filter
        mTempActivities.addAll(mActivities);

        this.clear();  // Clear all activities from the adapter

        // Filter the activities list based on the date range
        for (MountaineerActivity activity : mTempActivities) {
            // Date requirements
            // If no start date is defined then set the date at the beginning (i.e. 01/01/1970)
            if (filterOptions.getStartDate() == null) {
                filterStartDate = Long.MIN_VALUE;
            }
            else {  // Start date defined
                filterStartDate = filterOptions.getStartDate().getTime();
            }

            // If no start date is defined then set the date in the far future
            if (filterOptions.getEndDate() == null) {
                filterEndDate = Long.MAX_VALUE;
            }
            else {  // End date defined
                filterEndDate = filterOptions.getEndDate().getTime();
            }

            /* See if the current activity's start date falls outside the filterStartDate or the
             * endStartDate */
            if (filterStartDate > activity.getActivityStartDate().getTime() ||
                    activity.getActivityStartDate().getTime() > filterEndDate) {
                continue;  // Failed - move onto next activity in loop
            }

            // Activity type options
            // Check if they're not all true or not all false
            if (!areAllEqual(filterOptions.isTypeAdventureClub(), filterOptions.isTypeBackpacking(),
                    filterOptions.isTypeClimbing(), filterOptions.isTypeDayHiking(),
                    filterOptions.isTypeExplorers(), filterOptions.isTypeExploringNature(),
                    filterOptions.isTypeGlobalAdventures(),
                    filterOptions.isTypeNavigation(), filterOptions.isTypePhotography(),
                    filterOptions.isTypeSailing(), filterOptions.isTypeScrambling(),
                    filterOptions.isTypeSeaKayaking(), filterOptions.isTypeSkiingSnowboarding(),
                    filterOptions.isTypeSnowshoeing(), filterOptions.isTypeStewardship(),
                    filterOptions.isTypeTrailRunning(), filterOptions.isTypeUrbanAdventure(),
                    filterOptions.isTypeYouth())) {
                /* The user has selected discrete filters for this category.  Check to see if the 
                 * activity falls into one of the categories selected. */
                if (!((filterOptions.isTypeAdventureClub() && activity.isTypeAdventureClub()) ||
                        (filterOptions.isTypeBackpacking() && activity.isTypeBackpacking()) ||
                        (filterOptions.isTypeClimbing() && activity.isTypeClimbing()) ||
                        (filterOptions.isTypeDayHiking() && activity.isTypeDayHiking()) ||
                        (filterOptions.isTypeExplorers() && activity.isTypeExplorers()) ||
                        (filterOptions.isTypeExploringNature() && activity.isTypeExploringNature()) ||
                        (filterOptions.isTypeGlobalAdventures() && activity.isTypeGlobalAdventures()) ||
                        (filterOptions.isTypeNavigation() && activity.isTypeNavigation()) ||
                        (filterOptions.isTypePhotography() && activity.isTypePhotography()) ||
                        (filterOptions.isTypeSailing() && activity.isTypeSailing()) ||
                        (filterOptions.isTypeScrambling() && activity.isTypeScrambling()) ||
                        (filterOptions.isTypeSeaKayaking() && activity.isTypeSeaKayaking()) ||
                        (filterOptions.isTypeSkiingSnowboarding() && activity.isTypeSkiingSnowboarding()) ||
                        (filterOptions.isTypeSnowshoeing() && activity.isTypeSnowshoeing()) ||
                        (filterOptions.isTypeStewardship() && activity.isTypeStewardship()) ||
                        (filterOptions.isTypeTrailRunning() && activity.isTypeTrailRunning()) ||
                        (filterOptions.isTypeUrbanAdventure() && activity.isTypeUrbanAdventure()) ||
                        (filterOptions.isTypeYouth() && activity.isTypeYouth()))) {
                    continue;  // Failed - move onto next activity in loop
                }
            }

            // Rating options
            // Check if they're not all true or not all false
            if (!areAllEqual(filterOptions.isRatingForBeginners(), filterOptions.isRatingEasy(),
                    filterOptions.isRatingModerate(), filterOptions.isRatingChallenging())) {
                /* The user has selected discrete filters for this category.  Check to see if the 
                 * activity falls into one of the categories selected. */
                if (!((filterOptions.isRatingForBeginners() && activity.isRatingForBeginners()) ||
                        (filterOptions.isRatingEasy() && activity.isRatingEasy()) ||
                        (filterOptions.isRatingModerate() && activity.isRatingModerate()) ||
                        (filterOptions.isRatingChallenging() && activity.isRatingChallenging()))) {
                    continue;  // Failed - move onto next activity in loop
                }
            }
            
            // Audience options
            if (!areAllEqual(filterOptions.isAudienceAdults(), filterOptions.isAudienceFamilies(),
                    filterOptions.isAudienceRetiredRovers(), filterOptions.isAudienceSingles(),
                    filterOptions.isAudience2030Somethings(), filterOptions.isAudienceYouth())) {
                /* The user has selected discrete filters for this category.  Check to see if the
                 * activity falls into one of the categories selected. */
                if (!((filterOptions.isAudienceAdults() && activity.isAudienceAdults()) ||
                        (filterOptions.isAudienceFamilies() && activity.isAudienceFamilies()) ||
                        (filterOptions.isAudienceRetiredRovers() && activity.isAudienceRetired()) ||
                        (filterOptions.isAudienceSingles() && activity.isAudienceSingles()) ||
                        (filterOptions.isAudience2030Somethings() && activity.isAudience2030Somethings()) ||
                        (filterOptions.isAudienceYouth() && activity.isAudienceYouth()))) {
                    continue;  // Failed - move onto next activity in loop
                }
             }

            // Branch options
            if (!areAllEqual(filterOptions.isBranchTheMountaineers(), filterOptions.isBranchBellingham(),
                    filterOptions.isBranchEverett(), filterOptions.isBranchFoothills(),
                    filterOptions.isBranchKitsap(), filterOptions.isBranchOlympia(),
                    filterOptions.isBranchOutdoorCenters(), filterOptions.isBranchSeattle(),
                    filterOptions.isBranchTacoma())) {
                /* The user has selected discrete filters for this category.  Check to see if the
                 * activity falls into one of the categories selected. */
                if (!((filterOptions.isBranchTheMountaineers() && activity.isBranchTheMountaineers()) ||
                        (filterOptions.isBranchBellingham() && activity.isBranchBellingham()) ||
                        (filterOptions.isBranchEverett() && activity.isBranchEverett()) ||
                        (filterOptions.isBranchFoothills() && activity.isBranchFoothills()) ||
                        (filterOptions.isBranchKitsap() && activity.isBranchKitsap()) ||
                        (filterOptions.isBranchOlympia() && activity.isBranchOlympia()) ||
                        (filterOptions.isBranchOutdoorCenters() && activity.isBranchOutdoorCenters())
                        || (filterOptions.isBranchSeattle() && activity.isBranchSeattle()) ||
                        (filterOptions.isBranchTacoma() && activity.isBranchTacoma()))) {
                    continue;  // Failed - move onto next activity in loop
                }
            }

            // Climbing options
            if (filterOptions.isClimbingBasicAlpine() ||
                    filterOptions.isClimbingIntermediateAlpine() ||
                    filterOptions.isClimbingAidClimb() || filterOptions.isClimbingRockClimb()) {
                /* The user has selected discrete filters for this category.  Check to see if the
                 * activity falls into one of the categories selected. */
                if (!((filterOptions.isClimbingBasicAlpine() && activity.isClimbingBasicAlpine()) ||
                        (filterOptions.isClimbingIntermediateAlpine() &&
                                activity.isClimbingIntermediateAlpine()) ||
                        (filterOptions.isClimbingAidClimb() && activity.isClimbingAidClimb()) ||
                        (filterOptions.isClimbingRockClimb() && activity.isClimbingRockClimb()))) {
                    continue;  // Failed - move onto next activity in loop
                }
            }

            // Skiing/Snowboarding options
            if (filterOptions.isSkiingCrossCountry() || filterOptions.isSkiingBackcountry() ||
                    filterOptions.isSkiingGlacier()) {
                /* The user has selected discrete filters for this category.  Check to see if the
                 * activity falls into one of the categories selected. */
                if (!((filterOptions.isSkiingCrossCountry() && activity.isSkiingCrossCountry()) ||
                        (filterOptions.isSkiingBackcountry() && activity.isSkiingBackcountry()) ||
                        (filterOptions.isSkiingGlacier() && activity.isSkiingGlacier()))) {
                    continue;  // Failed - move onto next activity in loop
                }
            }
            
            // Snowshoeing options
            if (filterOptions.isSnowshoeingBeginner() || filterOptions.isSnowshoeingBasic() ||
                    filterOptions.isSnowshoeingIntermediate()) {
                /* The user has selected discrete filters for this category.  Check to see if the
                 * activity falls into one of the categories selected. */
                if (!((filterOptions.isSnowshoeingBeginner() && activity.isSnowshoeingBeginner()) ||
                        (filterOptions.isSnowshoeingBasic() && activity.isSnowshoeingBasic()) ||
                        (filterOptions.isSnowshoeingIntermediate() &&
                                activity.isSnowshoeingIntermediate()))) {
                    continue;  // Failed - move onto next activity in loop
                }
            }

            /* Add the activity if it has passed all of the filter criteria (loop did not move onto
             * next element before reaching this point) */
            this.add(activity);
        }

        // Check if there are any results before filtering
        if (mMasterActivities.size() > 0) { // Yes
            // Check if a previous toast is visible
            if (toast != null && toast.getView().getWindowVisibility() == View.VISIBLE) {
                toast.cancel();  // Dismiss previous toast
            }

            toast = Toast.makeText(getContext(), getContext().getString(R.string.toast_activities_found,
                    mActivities.size()), Toast.LENGTH_SHORT);

            toast.show();
        }

        notifyDataSetChanged();
    }

    public static boolean areAllEqual(boolean... values) {
        // Check to see if all values are the same
        for (int i = 1; i < values.length; i++) {
            if (values[i] != values[0]) {
                return false;
            }
        }

        return true;
    }

    public void updateLastViewed(final long lastViewed){
        mLastViewed = lastViewed;
    }

    /* This method determines whether an applicable activity is unseen and if so, should it be
     * placed at the top or in the seen section (e.g. do not show past activities with new updates) */
    private boolean addToListAsUnseen(final MountaineerActivity activity, int newIndex) {
        long updatedDate;

        // Compare the addedAt or updatedAt date (if it exists) to see if it postdates the lastViewed date
        if (activity.getActivityUpdatedAt() != null) {  // Updated date defined so use this
            updatedDate = activity.getActivityUpdatedAt().getTime();
        }
        else {  // Use the added date
            updatedDate = activity.getActivityAddedAt().getTime();
        }

        /* Check if the activity is newer than the last time the user viewed the saved search and is
         * occurring in the future */
        if (mLastViewed < updatedDate &&
                (activity.getActivityStartDate().getTime() + 24 * 60 * 60 * 1000) >
                        new Date().getTime()) {  // Yes
            activity.setUnread(true);  // Mark this activity as unseen by the user

            if (mActivities.size() > 0) {  // Not the first activity being added to the list
                // Add activity to the top of the list (at the bottom of the unseen section)
                mActivities.add(newIndex, activity);
            }
            else {  // First activity - add to list
                mActivities.add(activity);
            }

            return true;
        }
        else {  // No
            mActivities.add(activity);  // Add activity to bottom of list
            return false;
        }
    }

    private void reorganizeNewList(final int newCount) {
        /* This method takes in the list of activities to be shown in activity search launched by
         * saved search.  This list is already sorted such that all of the unseen activities are at
         * the top of the list.  This method goes through the new activity section of this list and
         * sorts the activities based on the activity title if there are multiple activities
         * occurring on the same date. */
        int startSubList = 0;
        int endSubList = 0;

        if (newCount > 1) {  // Make sure there are at least two new activities
            for (int i = 0; i < newCount - 1; i++) {
                // Check if the next activity has the same start date
                if (mActivities.get(i).getActivityStartDate().equals(mActivities.get(i + 1).getActivityStartDate())) {
                    // Yes, it has the same start date
                    endSubList = i + 1;
                }
                else {  // Reached a set of activities where their date differ
                    // Check to ensure there is more than one activity with the same start date
                    if (startSubList != endSubList) {
                        // Create a sub-list of the activities with the same start dates and sort it based on activity title
                        Collections.sort(mActivities.subList(startSubList, endSubList + 1), new ActivityComparator());
                    }

                    startSubList = ++endSubList;  // Increment the counters
                }
            }

            // Check to ensure there is more than one activity with the same start date
            if (startSubList != endSubList) {
                // Create a sub-list of the activities with the same start dates and sort it based on activity title
                Collections.sort(mActivities.subList(startSubList, endSubList + 1), new ActivityComparator());
            }
        }
    }
}