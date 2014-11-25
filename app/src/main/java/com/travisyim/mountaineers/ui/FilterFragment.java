package com.travisyim.mountaineers.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseUser;
import com.travisyim.mountaineers.MountaineersApp;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.objects.FilterOptions;
import com.travisyim.mountaineers.utils.DateUtil;

import java.util.Calendar;
import java.util.Date;

public class FilterFragment extends Fragment {
    private FilterOptions mFilterOptions;
    private String mParentFragmentTitle;

    private TextView mDateHeader;
    private TextView mActivityTypeHeader;
    private TextView mRatingHeader;
    private TextView mAudienceHeader;
    private TextView mBranchHeader;
    private TextView mClimbingHeader;
    private TextView mSkiingHeader;
    private TextView mSnowshoeingHeader;
    private RelativeLayout mDateContent;
    private RelativeLayout mActivityTypeContent;
    private RelativeLayout mRatingContent;
    private RelativeLayout mAudienceContent;
    private RelativeLayout mBranchContent;
    private RelativeLayout mClimbingContent;
    private RelativeLayout mSkiingContent;
    private RelativeLayout mSnowshoeingContent;
    private Button mStartDate;
    private Button mEndDate;
    private Button mActiveButton;
    private TextView mTypeAll;
    private TextView mTypeNone;
    private CheckBox mTypeAdventureClub;
    private CheckBox mTypeBackpacking;
    private CheckBox mTypeClimbing;
    private CheckBox mTypeDayHiking;
    private CheckBox mTypeExplorers;
    private CheckBox mTypeExploringNature;
    private CheckBox mTypeGlobalAdventures;
    private CheckBox mTypeNavigation;
    private CheckBox mTypePhotography;
    private CheckBox mTypeSailing;
    private CheckBox mTypeScrambling;
    private CheckBox mTypeSeaKayaking;
    private CheckBox mTypeSkiingSnowboarding;
    private CheckBox mTypeSnowshoeing;
    private CheckBox mTypeStewardship;
    private CheckBox mTypeTrailRunning;
    private CheckBox mTypeUrbanAdventure;
    private CheckBox mTypeYouth;
    private TextView mRatingAll;
    private TextView mRatingNone;
    private CheckBox mRatingBeginners;
    private CheckBox mRatingEasy;
    private CheckBox mRatingModerate;
    private CheckBox mRatingChallenging;
    private TextView mAudienceAll;
    private TextView mAudienceNone;
    private CheckBox mAudienceAdults;
    private CheckBox mAudienceFamilies;
    private CheckBox mAudienceRetiredRovers;
    private CheckBox mAudienceSingles;
    private CheckBox mAudience2030Somethings;
    private CheckBox mAudienceYouth;
    private TextView mBranchAll;
    private TextView mBranchNone;
    private CheckBox mBranchTheMountaineers;
    private CheckBox mBranchBellingham;
    private CheckBox mBranchEverett;
    private CheckBox mBranchFoothills;
    private CheckBox mBranchKitsap;
    private CheckBox mBranchOlympia;
    private CheckBox mBranchOutdoorCenters;
    private CheckBox mBranchSeattle;
    private CheckBox mBranchTacoma;
    private TextView mClimbingAll;
    private TextView mClimbingNone;
    private CheckBox mClimbingBasicAlpine;
    private CheckBox mClimbingIntermediateAlpine;
    private CheckBox mClimbingAidClimb;
    private CheckBox mClimbingRockClimb;
    private TextView mSkiingAll;
    private TextView mSkiingNone;
    private CheckBox mSkiingCrossCountry;
    private CheckBox mSkiingBackcountry;
    private CheckBox mSkiingGlacier;
    private TextView mSnowshoeingAll;
    private TextView mSnowshoeingNone;
    private CheckBox mSnowshoeingBeginner;
    private CheckBox mSnowshoeingBasic;
    private CheckBox mSnowshoeingIntermediate;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_PARENT_TITLE = "parentFragmentTitle";
    private static final String ARG_FILTER_OPTIONS = "filterOptions";
    private boolean mLogOut = false;
    private boolean mIsAlreadyLoaded = false;
    private boolean mIsFavoritesSaved = false;

    // Returns a new instance of this fragment for the given section number
    public static FilterFragment newInstance(final float sectionNumber,
                                             final FilterOptions filterOptions,
                                             final String parentFragmentTitle) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        //TODO: Make sure the section number is in the format of 2.1.1
        args.putFloat(ARG_SECTION_NUMBER, sectionNumber);

        /* Save the FilterOptions into the arguments to be accessed later in onAttached (must wait
         * because member variables are not yet accessible */
        args.putSerializable(ARG_FILTER_OPTIONS, filterOptions);
        args.putString(ARG_PARENT_TITLE, parentFragmentTitle);
        fragment.setArguments(args);

        return fragment;
    }

    public FilterFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Get the parent fragment's title
        mParentFragmentTitle = getArguments().getString(ARG_PARENT_TITLE);

        // Get the reference to the filter options
        mFilterOptions = (FilterOptions) getArguments().getSerializable(ARG_FILTER_OPTIONS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_filter, container, false);

        // Get references to all views in fragment
        getViews(rootView);

        // Assign OnClickListeners for all interactive views
        setOnClickListeners();

        return rootView;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link android.app.Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();

        if (!mIsAlreadyLoaded) {
            mIsAlreadyLoaded = true;  // Change the flag value
            resetFilter();  // Clears all filtered values
            restoreFilterOptions();  // Restore previous filter
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mIsAlreadyLoaded = false;

        if (!mLogOut) {
            // Google Analytics tracking code
            Tracker t = ((MountaineersApp) getActivity().getApplication()).getTracker
                    (MountaineersApp.TrackerName.APP_TRACKER);

            // Check which fragment this filter was launched from and assign the screen name
            // Activity Search
            if (mParentFragmentTitle.equals(getString(R.string.title_browse))) {
                t.setScreenName(getString(R.string.title_browse));
            }
            // Completed Activity
            else if (mParentFragmentTitle.equals(getString(R.string.title_completed))) {
                t.setScreenName(getString(R.string.title_completed));
            }
            // Signed Up Activity
            else if (mParentFragmentTitle.equals(getString(R.string.title_signed_up))) {
                t.setScreenName(getString(R.string.title_signed_up));
            }
            // Favorite Activity
            else if (mParentFragmentTitle.equals(getString(R.string.title_bookmarked))) {
                t.setScreenName(getString(R.string.title_bookmarked));
            }
            else {  // Saved search activity search
                t.setScreenName(getString(R.string.title_browse) + " (" + getString(R.string.title_saved_searches) +")");
            }

            t.send(new HitBuilders.AppViewBuilder().build());

            // Check if filters were applied (the user did not just hit back / up)
            if (mIsFavoritesSaved) {  // Filter applied
                /* Check which fragment this filter was launched from and launch the corresponding
                 * onFiltersSelected method */
                // Activity Search
                if (mParentFragmentTitle.equals(getString(R.string.title_browse))) {
                    ((ActivitySearchFragment) getFragmentManager().findFragmentByTag
                            (mParentFragmentTitle)).onFiltersSelected(mFilterOptions);
                }
                // Completed Activity
                else if (mParentFragmentTitle.equals(getString(R.string.title_completed))) {
                    ((CompletedActivityFragment) getFragmentManager().findFragmentByTag
                            (mParentFragmentTitle)).onFiltersSelected(mFilterOptions);
                }
                // Signed Up Activity
                else if (mParentFragmentTitle.equals(getString(R.string.title_signed_up))) {
                    ((SignedUpActivityFragment) getFragmentManager().findFragmentByTag
                            (mParentFragmentTitle)).onFiltersSelected(mFilterOptions);
                }
                // Favorite Activity
                else if (mParentFragmentTitle.equals(getString(R.string.title_bookmarked))) {
                    ((FavoriteActivityFragment) getFragmentManager().findFragmentByTag
                            (mParentFragmentTitle)).onFiltersSelected(mFilterOptions);
                }
                else {  // Saved search activity search
                    ((ActivitySearchFragment) getFragmentManager().findFragmentByTag
                            (mParentFragmentTitle)).onFiltersSelected(mFilterOptions);
                }

                mIsFavoritesSaved = false;  // Reset flag since the filter object will be reused
            }

            // Reset the title back to that of the parent fragment
            getActivity().getActionBar().setTitle(mParentFragmentTitle);
        }
    }

    protected Dialog onCreateDialog() {
        final Calendar c = Calendar.getInstance();
        Date date;

        // Check if the button clicked is empty (i.e. no date has been assigned yet)
        if (mActiveButton.getText().equals("")) {
            if (mActiveButton == mStartDate) {  // Start date button
                if (mEndDate.getText().equals("")) {  // End date button is empty
                    // Set current date in StartDate button
                    mActiveButton.setText(DateUtil.convertToString
                            (new Date(), DateUtil.TYPE_BUTTON_DATE));
                }
                else {  // The end date may be in the past & present or future
                    // End date is in the future so set the start date to the current date
                    if (DateUtil.convertToDate(mEndDate.getText().toString(),
                            DateUtil.TYPE_BUTTON_DATE).getTime() > new Date().getTime()) {
                        mActiveButton.setText(DateUtil.convertToString
                                (new Date(), DateUtil.TYPE_BUTTON_DATE));
                    }
                    else {  // End date is in the past or present
                        // Set the start date equal to the end date
                        mActiveButton.setText(mEndDate.getText());
                    }
                }
            }
            else {  // End date button
                if (mStartDate.getText().equals("")) {  // Start date button is empty
                    // Set current date in End Date button
                    mActiveButton.setText(DateUtil.convertToString
                            (new Date(), DateUtil.TYPE_BUTTON_DATE));
                }
                else {  // The start date may be in the past & present or future
                    // Start date is in the past or present day so make end date today's date
                    if (DateUtil.convertToDate(mStartDate.getText().toString(),
                            DateUtil.TYPE_BUTTON_DATE).getTime() < new Date().getTime()) {
                        mActiveButton.setText(DateUtil.convertToString
                                (new Date(), DateUtil.TYPE_BUTTON_DATE));
                    }
                    else {  // Start date is in the future
                        // Set the end date equal to the start date
                        mActiveButton.setText(mStartDate.getText());
                    }
                }
            }
        }

        // Convert button text to Date
        date = DateUtil.convertToDate(mActiveButton.getText().toString(), DateUtil.TYPE_BUTTON_DATE);
        c.setTime(date);

        // Determine the year, month and day of the button
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Open a Date Picker dialog set to button's date
        return new DatePickerDialog(getActivity(), datePickerListener, year, month, day);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.search_filter, menu);
    }

    public interface OnFiltersSelectedListener {
        void onFiltersSelected(final FilterOptions filterOptions);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logOut:  // Log Out
                mLogOut = true;
                ParseUser.getCurrentUser().logOut();
                ((MainActivity) getActivity()).showLoginScreen();
                getFragmentManager().popBackStackImmediate();  // Go back to parent fragment
                return true;
            case R.id.action_clear:  // Clear All pressed
                // Reset filter criteria
                resetFilter();
                return true;
            case R.id.action_apply:  // Apply filter settings
                // Save current object and send back to previous fragment
                mIsFavoritesSaved = true;  // Flag as filter was applied (the user did not just hit back)
                saveFilterOptions();
                getFragmentManager().popBackStackImmediate();  // Go back to previous fragment
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mActiveButton = (Button) v;
            onCreateDialog().show();
        }
    };

    private View.OnClickListener allNoneClick = new View.OnClickListener() {
        @Override
        // Clicking All or None will either check or uncheck all of the category's boxes
        public void onClick(View v) {
            if (v == mTypeAll || v == mTypeNone) {  // Activity type options
                updateCheckedState(new CheckBox[] {mTypeAdventureClub, mTypeBackpacking,
                                mTypeClimbing, mTypeDayHiking, mTypeExplorers, mTypeExploringNature,
                                mTypeGlobalAdventures, mTypeNavigation, mTypePhotography,
                                mTypeSailing, mTypeScrambling, mTypeSeaKayaking,
                                mTypeSkiingSnowboarding, mTypeSnowshoeing, mTypeStewardship,
                                mTypeTrailRunning, mTypeUrbanAdventure, mTypeYouth},
                        v == mTypeAll);
            }
            else if (v == mRatingAll || v == mRatingNone) {  // Activity difficulty options
                updateCheckedState(new CheckBox[] {mRatingBeginners, mRatingEasy, mRatingModerate,
                        mRatingChallenging}, v == mRatingAll);
            }
            else if (v == mAudienceAll || v == mAudienceNone) {  // For options
                updateCheckedState(new CheckBox[] {mAudienceAdults, mAudienceFamilies, mAudienceRetiredRovers,
                        mAudienceSingles, mAudience2030Somethings, mAudienceYouth}, v == mAudienceAll);
            }
            else if (v == mBranchAll || v == mBranchNone) {  // Branch options
                updateCheckedState(new CheckBox[] {mBranchTheMountaineers, mBranchBellingham,
                        mBranchEverett, mBranchFoothills, mBranchKitsap, mBranchOlympia,
                        mBranchOutdoorCenters, mBranchSeattle, mBranchTacoma}, v == mBranchAll);
            }
            else if (v == mClimbingAll || v == mClimbingNone) {  // Climbing options
                updateCheckedState(new CheckBox[] {mClimbingBasicAlpine, mClimbingIntermediateAlpine,
                        mClimbingAidClimb, mClimbingRockClimb}, v == mClimbingAll);
            }
            else if (v == mSkiingAll || v == mSkiingNone) {  // Skiing/Snowboarding options
                updateCheckedState(new CheckBox[] {mSkiingCrossCountry, mSkiingBackcountry,
                        mSkiingGlacier}, v == mSkiingAll);
            }
            else if (v == mSnowshoeingAll || v == mSnowshoeingNone) {  // Snowshoeing options
                updateCheckedState(new CheckBox[] {mSnowshoeingBeginner, mSnowshoeingBasic,
                        mSnowshoeingIntermediate}, v == mSnowshoeingAll);
            }
        }

        // This method will either check or uncheck all CheckBoxes passed to it
        private void updateCheckedState(CheckBox[] checkBoxes, boolean check) {
            for (CheckBox checkBox : checkBoxes) {
                checkBox.setChecked(check);
            }
        }
    };

    private View.OnClickListener headerClick = new View.OnClickListener() {
        /* Clicking the header will show/hide respective All/None and +/- label and respective
         * containers */
        @Override
        public void onClick(View v) {  // Date options
            if (v == mDateHeader) {
                mDateContent.setVisibility(mDateContent.isShown() ? View.GONE : View.VISIBLE);

                if (mDateContent.isShown()) {  // Show collapse string
                    mDateHeader.setText(getString(R.string.filter_header_date_range_collapse));
                }
                else {  // Show expand string
                    mDateHeader.setText(getString(R.string.filter_header_date_range_expand));
                }
            }
            else if (v == mActivityTypeHeader) {  // Activity type options
                mActivityTypeContent.setVisibility
                        (mActivityTypeContent.isShown() ? View.GONE : View.VISIBLE);
                mTypeAll.setVisibility(mTypeAll.isShown() ? View.GONE : View.VISIBLE);
                mTypeNone.setVisibility(mTypeNone.isShown() ? View.GONE : View.VISIBLE);

                if (mActivityTypeContent.isShown()) {  // Show collapse string
                    mActivityTypeHeader.setText
                            (getString(R.string.filter_header_activity_type_collapse));
                }
                else {  // Show expand string
                    mActivityTypeHeader.setText
                            (getString(R.string.filter_header_activity_type_expand));
                }
            }
            else if (v == mRatingHeader) {  // Activity difficulty options
                mRatingContent.setVisibility
                        (mRatingContent.isShown() ? View.GONE : View.VISIBLE);
                mRatingAll.setVisibility(mRatingAll.isShown() ? View.GONE : View.VISIBLE);
                mRatingNone.setVisibility(mRatingNone.isShown() ? View.GONE : View.VISIBLE);

                if (mRatingContent.isShown()) {  // Show collapse string
                    mRatingHeader.setText
                            (getString(R.string.filter_header_activity_difficulty_collapse));
                }
                else {  // Show expand string
                    mRatingHeader.setText
                            (getString(R.string.filter_header_activity_difficulty_expand));
                }
            }
            else if (v == mAudienceHeader) {  // For options
                mAudienceContent.setVisibility(mAudienceContent.isShown() ? View.GONE : View.VISIBLE);
                mAudienceAll.setVisibility(mAudienceAll.isShown() ? View.GONE : View.VISIBLE);
                mAudienceNone.setVisibility(mAudienceNone.isShown() ? View.GONE : View.VISIBLE);

                if (mAudienceContent.isShown()) {  // Show collapse string
                    mAudienceHeader.setText(getString(R.string.filter_header_for_collapse));
                }
                else {  // Show expand string
                    mAudienceHeader.setText(getString(R.string.filter_header_for_expand));
                }
            }
            else if (v == mBranchHeader) {  // Branch options
                mBranchContent.setVisibility(mBranchContent.isShown() ? View.GONE : View.VISIBLE);
                mBranchAll.setVisibility(mBranchAll.isShown() ? View.GONE : View.VISIBLE);
                mBranchNone.setVisibility(mBranchNone.isShown() ? View.GONE : View.VISIBLE);

                if (mBranchContent.isShown()) {  // Show collapse string
                    mBranchHeader.setText(getString(R.string.filter_header_branch_collapse));
                }
                else {  // Show expand string
                    mBranchHeader.setText(getString(R.string.filter_header_branch_expand));
                }
            }
            else if (v == mClimbingHeader) {   // Climbing options
                mClimbingContent.setVisibility
                        (mClimbingContent.isShown() ? View.GONE : View.VISIBLE);
                mClimbingAll.setVisibility(mClimbingAll.isShown() ? View.GONE : View.VISIBLE);
                mClimbingNone.setVisibility(mClimbingNone.isShown() ? View.GONE : View.VISIBLE);

                if (mClimbingContent.isShown()) {  // Show collapse string
                    mClimbingHeader.setText(getString(R.string.filter_header_climbing_collapse));
                }
                else {  // Show expand string
                    mClimbingHeader.setText(getString(R.string.filter_header_climbing_expand));
                }
            }
            else if (v == mSkiingHeader) {   // Skiing options
                mSkiingContent.setVisibility(mSkiingContent.isShown() ? View.GONE : View.VISIBLE);
                mSkiingAll.setVisibility(mSkiingAll.isShown() ? View.GONE : View.VISIBLE);
                mSkiingNone.setVisibility(mSkiingNone.isShown() ? View.GONE : View.VISIBLE);

                if (mSkiingContent.isShown()) {  // Show collapse string
                    mSkiingHeader.setText(getString(R.string.filter_header_skiing_collapse));
                }
                else {  // Show expand string
                    mSkiingHeader.setText(getString(R.string.filter_header_skiing_expand));
                }
            }
            else if (v == mSnowshoeingHeader) {  // Snowshoeing options
                mSnowshoeingContent.setVisibility
                        (mSnowshoeingContent.isShown() ? View.GONE : View.VISIBLE);
                mSnowshoeingAll.setVisibility(mSnowshoeingAll.isShown() ? View.GONE : View.VISIBLE);
                mSnowshoeingNone.setVisibility
                        (mSnowshoeingNone.isShown() ? View.GONE : View.VISIBLE);

                if (mSnowshoeingContent.isShown()) {  // Show collapse string
                    mSnowshoeingHeader.setText
                            (getString(R.string.filter_header_snowshoeing_collapse));
                }
                else {  // Show expand string
                    mSnowshoeingHeader.setText(getString(R.string.filter_header_snowshoeing_expand));
                }
            }
        }
    };

    private DatePickerDialog.OnDateSetListener datePickerListener =
            new DatePickerDialog.OnDateSetListener() {
        // When the date dialog box is closed this method will be called
        String dateString;

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Build String representation of Date
            dateString = ((month + 1 < 10) ? ("0" + Integer.toString(month + 1)) :
                    Integer.toString(month + 1)) + "/";
            dateString += ((day < 10) ? 0 + Integer.toString(day) : Integer.toString(day)) + "/";
            dateString += year;

            // Put selected date in the appropriate button
            mActiveButton.setText(DateUtil.convertToString(DateUtil.convertToDate(dateString,
                    DateUtil.TYPE_BUTTON_DATE), DateUtil.TYPE_BUTTON_DATE));

            // Check if the selected date invalidates the other button's value
            if (mActiveButton == mStartDate) {  // Start date button
                if (!mEndDate.getText().equals("")) {  // End date has a value
                    if (DateUtil.convertToDate(mActiveButton.getText().toString(),
                            DateUtil.TYPE_BUTTON_DATE).getTime() > DateUtil.convertToDate
                            (mEndDate.getText().toString(), DateUtil.TYPE_BUTTON_DATE).getTime()) {
                        // Set end date equal to start date
                        mEndDate.setText(mActiveButton.getText());
                    }
                }
            }
            else {  // End date button
                if (!mStartDate.getText().equals("")) {  // Start date has a value
                    if (DateUtil.convertToDate(mActiveButton.getText().toString(),
                            DateUtil.TYPE_BUTTON_DATE).getTime() < DateUtil.convertToDate
                            (mStartDate.getText().toString(), DateUtil.TYPE_BUTTON_DATE).getTime()) {
                        // Set start date equal to end date
                        mStartDate.setText(mActiveButton.getText());
                    }
                }
            }
        }
    };

    private void getViews(View rootView) {
        // Headers
        mDateHeader = (TextView) rootView.findViewById(R.id.textViewDateHeader);
        mActivityTypeHeader = (TextView) rootView.findViewById(R.id.textViewActivityTypeHeader);
        mRatingHeader = (TextView) rootView.findViewById(R.id.textViewActivityRatingHeader);
        mAudienceHeader = (TextView) rootView.findViewById(R.id.textViewAudienceHeader);
        mBranchHeader = (TextView) rootView.findViewById(R.id.textViewBranchHeader);
        mClimbingHeader = (TextView) rootView.findViewById(R.id.textViewClimbingHeader);
        mSkiingHeader = (TextView) rootView.findViewById(R.id.textViewSkiingHeader);
        mSnowshoeingHeader = (TextView) rootView.findViewById(R.id.textViewSnowshoeingHeader);

        // Relative layouts containing option filters
        mDateContent = (RelativeLayout) rootView.findViewById(R.id.dateFilter);
        mActivityTypeContent = (RelativeLayout) rootView.findViewById(R.id.activityTypeFilter);
        mRatingContent = (RelativeLayout) rootView.findViewById(R.id.activityRatingFilter);
        mAudienceContent = (RelativeLayout) rootView.findViewById(R.id.audienceFilter);
        mBranchContent = (RelativeLayout) rootView.findViewById(R.id.branchFilter);
        mClimbingContent = (RelativeLayout) rootView.findViewById(R.id.climbingFilter);
        mSkiingContent = (RelativeLayout) rootView.findViewById(R.id.skiingFilter);
        mSnowshoeingContent = (RelativeLayout) rootView.findViewById(R.id.snowshoeingFilter);

        // Date range buttons
        mStartDate = (Button) rootView.findViewById(R.id.buttonStartDate);
        mEndDate = (Button) rootView.findViewById(R.id.buttonEndDate);

        // Checkboxes and All / None TextViews
        // Activity type options
        mTypeAll = (TextView) rootView.findViewById(R.id.textViewTypeAll);
        mTypeNone = (TextView) rootView.findViewById(R.id.textViewTypeNone);
        mTypeAdventureClub = (CheckBox) rootView.findViewById(R.id.checkBoxTypeAdventureClub);
        mTypeBackpacking = (CheckBox) rootView.findViewById(R.id.checkBoxTypeBackpacking);
        mTypeClimbing = (CheckBox) rootView.findViewById(R.id.checkBoxTypeClimbing);
        mTypeDayHiking = (CheckBox) rootView.findViewById(R.id.checkBoxTypeDayHiking);
        mTypeExplorers = (CheckBox) rootView.findViewById(R.id.checkBoxTypeExplorers);
        mTypeExploringNature = (CheckBox) rootView.findViewById(R.id.checkBoxTypeExploringNature);
        mTypeGlobalAdventures = (CheckBox) rootView.findViewById(R.id.checkBoxTypeGlobalAdventures);
        mTypeNavigation = (CheckBox) rootView.findViewById(R.id.checkBoxTypeNavigation);
        mTypePhotography = (CheckBox) rootView.findViewById(R.id.checkBoxTypePhotography);
        mTypeSailing = (CheckBox) rootView.findViewById(R.id.checkBoxTypeSailing);
        mTypeScrambling = (CheckBox) rootView.findViewById(R.id.checkBoxTypeScrambling);
        mTypeSeaKayaking = (CheckBox) rootView.findViewById(R.id.checkBoxTypeSeaKayaking);
        mTypeSkiingSnowboarding = (CheckBox) rootView.findViewById(R.id.checkBoxTypeSkiingSnowboarding);
        mTypeSnowshoeing = (CheckBox) rootView.findViewById(R.id.checkBoxTypeSnowshoeing);
        mTypeStewardship = (CheckBox) rootView.findViewById(R.id.checkBoxTypeStewardship);
        mTypeTrailRunning = (CheckBox) rootView.findViewById(R.id.checkBoxTypeTrailRunning);
        mTypeUrbanAdventure = (CheckBox) rootView.findViewById(R.id.checkBoxTypeUrbanAdventure);
        mTypeYouth = (CheckBox) rootView.findViewById(R.id.checkBoxTypeYouth);

        // Activity difficulty options
        mRatingAll = (TextView) rootView.findViewById(R.id.textViewRatingAll);
        mRatingNone = (TextView) rootView.findViewById(R.id.textViewRatingNone);
        mRatingBeginners = (CheckBox) rootView.findViewById(R.id.checkBoxRatingForBeginners);
        mRatingEasy = (CheckBox) rootView.findViewById(R.id.checkBoxRatingEasy);
        mRatingModerate = (CheckBox) rootView.findViewById(R.id.checkBoxRatingModerate);
        mRatingChallenging = (CheckBox) rootView.findViewById(R.id.checkBoxRatingChallenging);

        // For options
        mAudienceAll = (TextView) rootView.findViewById(R.id.textViewAudienceAll);
        mAudienceNone = (TextView) rootView.findViewById(R.id.textViewAudienceNone);
        mAudienceAdults = (CheckBox) rootView.findViewById(R.id.checkBoxAudienceAdults);
        mAudienceFamilies = (CheckBox) rootView.findViewById(R.id.checkBoxAudienceFamilies);
        mAudienceRetiredRovers = (CheckBox) rootView.findViewById(R.id.checkBoxAudienceRetiredRovers);
        mAudienceSingles = (CheckBox) rootView.findViewById(R.id.checkBoxAudienceSingles);
        mAudience2030Somethings = (CheckBox) rootView.findViewById(R.id.checkBoxAudience2030Somethings);
        mAudienceYouth = (CheckBox) rootView.findViewById(R.id.checkBoxAudienceYouth);

        // Branch options
        mBranchAll = (TextView) rootView.findViewById(R.id.textViewBranchAll);
        mBranchNone = (TextView) rootView.findViewById(R.id.textViewBranchNone);
        mBranchTheMountaineers = (CheckBox) rootView.findViewById(R.id.checkBoxBranchTheMountaineers);
        mBranchBellingham = (CheckBox) rootView.findViewById(R.id.checkBoxBranchBellingham);
        mBranchEverett = (CheckBox) rootView.findViewById(R.id.checkBoxBranchEverett);
        mBranchFoothills = (CheckBox) rootView.findViewById(R.id.checkBoxBranchFoothills);
        mBranchKitsap = (CheckBox) rootView.findViewById(R.id.checkBoxBranchKitsap);
        mBranchOlympia = (CheckBox) rootView.findViewById(R.id.checkBoxBranchOlympia);
        mBranchOutdoorCenters = (CheckBox) rootView.findViewById(R.id.checkBoxBranchOutdoorCenters);
        mBranchSeattle = (CheckBox) rootView.findViewById(R.id.checkBoxBranchSeattle);
        mBranchTacoma = (CheckBox) rootView.findViewById(R.id.checkBoxBranchTacoma);

        // Climbing options
        mClimbingAll = (TextView) rootView.findViewById(R.id.textViewClimbingAll);
        mClimbingNone = (TextView) rootView.findViewById(R.id.textViewClimbingNone);
        mClimbingBasicAlpine = (CheckBox) rootView.findViewById(R.id.checkBoxClimbingBasicAlpine);
        mClimbingIntermediateAlpine = (CheckBox) rootView.findViewById(R.id.checkBoxClimbingIntermediateAlpine);
        mClimbingAidClimb = (CheckBox) rootView.findViewById(R.id.checkBoxClimbingAidClimb);
        mClimbingRockClimb = (CheckBox) rootView.findViewById(R.id.checkBoxClimbingRockClimb);

        // Skiing/Snowboarding options
        mSkiingAll = (TextView) rootView.findViewById(R.id.textViewSkiingAll);
        mSkiingNone = (TextView) rootView.findViewById(R.id.textViewSkiingNone);
        mSkiingCrossCountry = (CheckBox) rootView.findViewById(R.id.checkBoxSkiingCrossCountry);
        mSkiingBackcountry = (CheckBox) rootView.findViewById(R.id.checkBoxSkiingBackcountry);
        mSkiingGlacier = (CheckBox) rootView.findViewById(R.id.checkBoxSkiingGlacier);

        // Snowshoeing options
        mSnowshoeingAll = (TextView) rootView.findViewById(R.id.textViewSnowshoeingAll);
        mSnowshoeingNone = (TextView) rootView.findViewById(R.id.textViewSnowshoeingNone);
        mSnowshoeingBeginner = (CheckBox) rootView.findViewById(R.id.checkBoxSnowshoeingBeginner);
        mSnowshoeingBasic = (CheckBox) rootView.findViewById(R.id.checkBoxSnowshoeingBasic);
        mSnowshoeingIntermediate = (CheckBox) rootView.findViewById(R.id.checkBoxSnowshoeingIntermediate);
    }

    private void resetFilter() {
        // This method resets all of the filter criteria
        // Date range buttons
        mStartDate.setText(null);
        mEndDate.setText(null);

        // Uncheck all CheckBoxes
        allNoneClick.onClick(mTypeNone);  // Activity type option reset
        allNoneClick.onClick(mRatingNone);  // Activity difficulty option reset
        allNoneClick.onClick(mAudienceNone);  // For option reset
        allNoneClick.onClick(mBranchNone);  // Branch option reset
        allNoneClick.onClick(mClimbingNone);  // Climbing option reset
        allNoneClick.onClick(mSkiingNone);  // Skiing/Snowboarding option reset
        allNoneClick.onClick(mSnowshoeingNone);  // Snowshoeing option reset

        // Restore header views to default
        // Date options
        mDateContent.setVisibility(View.VISIBLE);
        mDateHeader.setText(getString(R.string.filter_header_date_range_collapse));

        // Activity type options
        mActivityTypeContent.setVisibility(View.GONE);
        mTypeAll.setVisibility(View.GONE);
        mTypeNone.setVisibility(View.GONE);
        mActivityTypeHeader.setText(getString(R.string.filter_header_activity_type_expand));

        // Activity difficulty options
        mRatingContent.setVisibility(View.GONE);
        mRatingAll.setVisibility(View.GONE);
        mRatingNone.setVisibility(View.GONE);
        mRatingHeader.setText(getString(R.string.filter_header_activity_difficulty_expand));

        // For options
        mAudienceContent.setVisibility(View.GONE);
        mAudienceAll.setVisibility(View.GONE);
        mAudienceNone.setVisibility(View.GONE);
        mAudienceHeader.setText(getString(R.string.filter_header_for_expand));

        // Branch options
        mBranchContent.setVisibility(View.GONE);
        mBranchAll.setVisibility(View.GONE);
        mBranchNone.setVisibility(View.GONE);
        mBranchHeader.setText(getString(R.string.filter_header_branch_expand));

        // Climbing options
        mClimbingContent.setVisibility(View.GONE);
        mClimbingAll.setVisibility(View.GONE);
        mClimbingNone.setVisibility(View.GONE);
        mClimbingHeader.setText(getString(R.string.filter_header_climbing_expand));

        // Skiing options
        mSkiingContent.setVisibility(View.GONE);
        mSkiingAll.setVisibility(View.GONE);
        mSkiingNone.setVisibility(View.GONE);
        mSkiingHeader.setText(getString(R.string.filter_header_skiing_expand));

        // Snowshoeing options
        mSnowshoeingContent.setVisibility(View.GONE);
        mSnowshoeingAll.setVisibility(View.GONE);
        mSnowshoeingNone.setVisibility(View.GONE);
        mSnowshoeingHeader.setText(getString(R.string.filter_header_snowshoeing_expand));
    }

    private void restoreFilterOptions() {
        // Restore filters to what they previously were set to
        // Date range buttons - this container will always starts off visible (shown by default)
        if (mFilterOptions.getStartDate() != null) {
            mStartDate.setText(DateUtil.convertToString(mFilterOptions.getStartDate(),
                    DateUtil.TYPE_BUTTON_DATE));
        }

        if (mFilterOptions.getEndDate() != null) {
            mEndDate.setText(DateUtil.convertToString(mFilterOptions.getEndDate(),
                    DateUtil.TYPE_BUTTON_DATE));
        }

        // Activity type options - hidden by default with none checked
        if (mFilterOptions.isTypeAdventureClub() ||mFilterOptions.isTypeBackpacking() ||
                mFilterOptions.isTypeClimbing() || mFilterOptions.isTypeDayHiking() ||
                mFilterOptions.isTypeExplorers() || mFilterOptions.isTypeExploringNature() ||
                mFilterOptions.isTypeGlobalAdventures() ||
                mFilterOptions.isTypeNavigation() || mFilterOptions.isTypePhotography() ||
                mFilterOptions.isTypeSailing() || mFilterOptions.isTypeScrambling() ||
                mFilterOptions.isTypeSeaKayaking() || mFilterOptions.isTypeSkiingSnowboarding() ||
                mFilterOptions.isTypeSnowshoeing() || mFilterOptions.isTypeStewardship() ||
                mFilterOptions.isTypeTrailRunning() || mFilterOptions.isTypeUrbanAdventure() ||
                mFilterOptions.isTypeYouth()) {
            // Activity type filter previously applied
            mActivityTypeContent.setVisibility(View.VISIBLE);
            mTypeAll.setVisibility(View.VISIBLE);
            mTypeNone.setVisibility(View.VISIBLE);
            mActivityTypeHeader.setText(getString(R.string.filter_header_activity_type_collapse));

            // Apply previous checks
            mTypeAdventureClub.setChecked(mFilterOptions.isTypeAdventureClub());
            mTypeBackpacking.setChecked(mFilterOptions.isTypeBackpacking());
            mTypeClimbing.setChecked(mFilterOptions.isTypeClimbing());
            mTypeDayHiking.setChecked(mFilterOptions.isTypeDayHiking());
            mTypeExplorers.setChecked(mFilterOptions.isTypeExplorers());
            mTypeExploringNature.setChecked(mFilterOptions.isTypeExploringNature());
            mTypeGlobalAdventures.setChecked(mFilterOptions.isTypeGlobalAdventures());
            mTypeNavigation.setChecked(mFilterOptions.isTypeNavigation());
            mTypePhotography.setChecked(mFilterOptions.isTypePhotography());
            mTypeSailing.setChecked(mFilterOptions.isTypeSailing());
            mTypeScrambling.setChecked(mFilterOptions.isTypeScrambling());
            mTypeSeaKayaking.setChecked(mFilterOptions.isTypeSeaKayaking());
            mTypeSkiingSnowboarding.setChecked(mFilterOptions.isTypeSkiingSnowboarding());
            mTypeSnowshoeing.setChecked(mFilterOptions.isTypeSnowshoeing());
            mTypeStewardship.setChecked(mFilterOptions.isTypeStewardship());
            mTypeTrailRunning.setChecked(mFilterOptions.isTypeTrailRunning());
            mTypeUrbanAdventure.setChecked(mFilterOptions.isTypeUrbanAdventure());
            mTypeYouth.setChecked(mFilterOptions.isTypeYouth());
        }

        // Activity difficulty options - hidden by default
        if (mFilterOptions.isRatingForBeginners() || mFilterOptions.isRatingEasy() ||
                mFilterOptions.isRatingModerate() || mFilterOptions.isRatingChallenging()) {
            // Activity difficulty filter previously applied
            mRatingContent.setVisibility(View.VISIBLE);
            mRatingAll.setVisibility(View.VISIBLE);
            mRatingNone.setVisibility(View.VISIBLE);
            mRatingHeader.setText(getString(R.string.filter_header_activity_difficulty_collapse));

            // Apply previous checks
            mRatingBeginners.setChecked(mFilterOptions.isRatingForBeginners());
            mRatingEasy.setChecked(mFilterOptions.isRatingEasy());
            mRatingModerate.setChecked(mFilterOptions.isRatingModerate());
            mRatingChallenging.setChecked(mFilterOptions.isRatingChallenging());
        }

        // For options - hidden by default
        if (mFilterOptions.isAudienceAdults() || mFilterOptions.isAudienceFamilies() ||
                mFilterOptions.isAudienceRetiredRovers() || mFilterOptions.isAudienceSingles() ||
                mFilterOptions.isAudience2030Somethings() || mFilterOptions.isAudienceYouth()) {
            // For filter previously applied
            mAudienceContent.setVisibility(View.VISIBLE);
            mAudienceAll.setVisibility(View.VISIBLE);
            mAudienceNone.setVisibility(View.VISIBLE);
            mAudienceHeader.setText(getString(R.string.filter_header_for_collapse));

            // Apply previous checks
            mAudienceAdults.setChecked(mFilterOptions.isAudienceAdults());
            mAudienceFamilies.setChecked(mFilterOptions.isAudienceFamilies());
            mAudienceRetiredRovers.setChecked(mFilterOptions.isAudienceRetiredRovers());
            mAudienceSingles.setChecked(mFilterOptions.isAudienceSingles());
            mAudience2030Somethings.setChecked(mFilterOptions.isAudience2030Somethings());
            mAudienceYouth.setChecked(mFilterOptions.isAudienceYouth());
        }

        // Branch options - hidden by default
        if (mFilterOptions.isBranchTheMountaineers() || mFilterOptions.isBranchBellingham() ||
                mFilterOptions.isBranchEverett() || mFilterOptions.isBranchFoothills() ||
                mFilterOptions.isBranchKitsap() || mFilterOptions.isBranchOlympia() ||
                mFilterOptions.isBranchOutdoorCenters() || mFilterOptions.isBranchSeattle() ||
                mFilterOptions.isBranchTacoma()) {
            // Branch filter previously applied
            mBranchContent.setVisibility(View.VISIBLE);
            mBranchAll.setVisibility(View.VISIBLE);
            mBranchNone.setVisibility(View.VISIBLE);
            mBranchHeader.setText(getString(R.string.filter_header_branch_collapse));

            // Apply previous checks
            mBranchTheMountaineers.setChecked(mFilterOptions.isBranchTheMountaineers());
            mBranchBellingham.setChecked(mFilterOptions.isBranchBellingham());
            mBranchEverett.setChecked(mFilterOptions.isBranchEverett());
            mBranchFoothills.setChecked(mFilterOptions.isBranchFoothills());
            mBranchKitsap.setChecked(mFilterOptions.isBranchKitsap());
            mBranchOlympia.setChecked(mFilterOptions.isBranchOlympia());
            mBranchOutdoorCenters.setChecked(mFilterOptions.isBranchOutdoorCenters());
            mBranchSeattle.setChecked(mFilterOptions.isBranchSeattle());
            mBranchTacoma.setChecked(mFilterOptions.isBranchTacoma());
        }

        // Climbing options - hidden by default
        if (mFilterOptions.isClimbingBasicAlpine() ||mFilterOptions.isClimbingIntermediateAlpine() ||
                mFilterOptions.isClimbingAidClimb() || mFilterOptions.isClimbingRockClimb()) {
            // Climbing filter previously applied
            mClimbingContent.setVisibility(View.VISIBLE);
            mClimbingAll.setVisibility(View.VISIBLE);
            mClimbingNone.setVisibility(View.VISIBLE);
            mClimbingHeader.setText(getString(R.string.filter_header_climbing_collapse));

            // Apply previous checks
            mClimbingBasicAlpine.setChecked(mFilterOptions.isClimbingBasicAlpine());
            mClimbingIntermediateAlpine.setChecked(mFilterOptions.isClimbingIntermediateAlpine());
            mClimbingAidClimb.setChecked(mFilterOptions.isClimbingAidClimb());
            mClimbingRockClimb.setChecked(mFilterOptions.isClimbingRockClimb());
        }

        // Skiing/Snowboarding options - hidden by default
        if (mFilterOptions.isSkiingCrossCountry() || mFilterOptions.isSkiingBackcountry() ||
                mFilterOptions.isSkiingGlacier()) {
            // Skiing/Snowboarding filter previously applied
            mSkiingContent.setVisibility(View.VISIBLE);
            mSkiingAll.setVisibility(View.VISIBLE);
            mSkiingNone.setVisibility(View.VISIBLE);
            mSkiingHeader.setText(getString(R.string.filter_header_skiing_collapse));

            // Apply previous checks
            mSkiingCrossCountry.setChecked(mFilterOptions.isSkiingCrossCountry());
            mSkiingBackcountry.setChecked(mFilterOptions.isSkiingBackcountry());
            mSkiingGlacier.setChecked(mFilterOptions.isSkiingGlacier());
        }

        // Snowshoeing options - hidden by default
        if (mFilterOptions.isSnowshoeingBeginner() || mFilterOptions.isSnowshoeingBasic() ||
                mFilterOptions.isSnowshoeingIntermediate()) {
            // Snowshoeing filter previously applied
            mSnowshoeingContent.setVisibility(View.VISIBLE);
            mSnowshoeingAll.setVisibility(View.VISIBLE);
            mSnowshoeingNone.setVisibility(View.VISIBLE);
            mSnowshoeingHeader.setText(getString(R.string.filter_header_snowshoeing_collapse));

            // Apply previous checks
            mSnowshoeingBeginner.setChecked(mFilterOptions.isSnowshoeingBeginner());
            mSnowshoeingBasic.setChecked(mFilterOptions.isSnowshoeingBasic());
            mSnowshoeingIntermediate.setChecked(mFilterOptions.isSnowshoeingIntermediate());
        }
    }

    private void saveFilterOptions() {
        // Save filter values
        // Date range buttons
        mFilterOptions.setStartDate(DateUtil.convertToDate(mStartDate.getText().toString(),
                DateUtil.TYPE_BUTTON_DATE));
        mFilterOptions.setEndDate(DateUtil.convertToDate(mEndDate.getText().toString(),
                DateUtil.TYPE_BUTTON_DATE));

        // Checkboxes
        // Activity type options
        mFilterOptions.setTypeAdventureClub(mTypeAdventureClub.isChecked());
        mFilterOptions.setTypeBackpacking(mTypeBackpacking.isChecked());
        mFilterOptions.setTypeClimbing(mTypeClimbing.isChecked());
        mFilterOptions.setTypeDayHiking(mTypeDayHiking.isChecked());
        mFilterOptions.setTypeExplorers(mTypeExplorers.isChecked());
        mFilterOptions.setTypeExploringNature(mTypeExploringNature.isChecked());
        mFilterOptions.setTypeGlobalAdventures(mTypeGlobalAdventures.isChecked());
        mFilterOptions.setTypeNavigation(mTypeNavigation.isChecked());
        mFilterOptions.setTypePhotography(mTypePhotography.isChecked());
        mFilterOptions.setTypeSailing(mTypeSailing.isChecked());
        mFilterOptions.setTypeScrambling(mTypeScrambling.isChecked());
        mFilterOptions.setTypeSeaKayaking(mTypeSeaKayaking.isChecked());
        mFilterOptions.setTypeSkiingSnowboarding(mTypeSkiingSnowboarding.isChecked());
        mFilterOptions.setTypeSnowshoeing(mTypeSnowshoeing.isChecked());
        mFilterOptions.setTypeStewardship(mTypeStewardship.isChecked());
        mFilterOptions.setTypeTrailRunning(mTypeTrailRunning.isChecked());
        mFilterOptions.setTypeUrbanAdventure(mTypeUrbanAdventure.isChecked());
        mFilterOptions.setTypeYouth(mTypeYouth.isChecked());

        // Activity difficulty options
        mFilterOptions.setRatingForBeginners(mRatingBeginners.isChecked());
        mFilterOptions.setRatingEasy(mRatingEasy.isChecked());
        mFilterOptions.setRatingModerate(mRatingModerate.isChecked());
        mFilterOptions.setRatingChallenging(mRatingChallenging.isChecked());

        // For options
        mFilterOptions.setAudienceAdults(mAudienceAdults.isChecked());
        mFilterOptions.setAudienceFamilies(mAudienceFamilies.isChecked());
        mFilterOptions.setAudienceRetiredRovers(mAudienceRetiredRovers.isChecked());
        mFilterOptions.setAudienceSingles(mAudienceSingles.isChecked());
        mFilterOptions.setAudience2030Somethings(mAudience2030Somethings.isChecked());
        mFilterOptions.setAudienceYouth(mAudienceYouth.isChecked());

        // Branch options
        mFilterOptions.setBranchTheMountaineers(mBranchTheMountaineers.isChecked());
        mFilterOptions.setBranchBellingham(mBranchBellingham.isChecked());
        mFilterOptions.setBranchEverett(mBranchEverett.isChecked());
        mFilterOptions.setBranchFoothills(mBranchFoothills.isChecked());
        mFilterOptions.setBranchKitsap(mBranchKitsap.isChecked());
        mFilterOptions.setBranchOlympia(mBranchOlympia.isChecked());
        mFilterOptions.setBranchOutdoorCenters(mBranchOutdoorCenters.isChecked());
        mFilterOptions.setBranchSeattle(mBranchSeattle.isChecked());
        mFilterOptions.setBranchTacoma(mBranchTacoma.isChecked());

        // Climbing options
        mFilterOptions.setClimbingBasicAlpine(mClimbingBasicAlpine.isChecked());
        mFilterOptions.setClimbingIntermediateAlpine(mClimbingIntermediateAlpine.isChecked());
        mFilterOptions.setClimbingAidClimb(mClimbingAidClimb.isChecked());
        mFilterOptions.setClimbingRockClimb(mClimbingRockClimb.isChecked());

        // Skiing/Snowboarding options
        mFilterOptions.setSkiingCrossCountry(mSkiingCrossCountry.isChecked());
        mFilterOptions.setSkiingBackcountry(mSkiingBackcountry.isChecked());
        mFilterOptions.setSkiingGlacier(mSkiingGlacier.isChecked());

        // Snowshoeing options
        mFilterOptions.setSnowshoeingBeginner(mSnowshoeingBeginner.isChecked());
        mFilterOptions.setSnowshoeingBasic(mSnowshoeingBasic.isChecked());
        mFilterOptions.setSnowshoeingIntermediate(mSnowshoeingIntermediate.isChecked());
    }

    private void setOnClickListeners() {
        // Headers
        mDateHeader.setOnClickListener(headerClick);
        mActivityTypeHeader.setOnClickListener(headerClick);
        mRatingHeader.setOnClickListener(headerClick);
        mAudienceHeader.setOnClickListener(headerClick);
        mBranchHeader.setOnClickListener(headerClick);
        mClimbingHeader.setOnClickListener(headerClick);
        mSkiingHeader.setOnClickListener(headerClick);
        mSnowshoeingHeader.setOnClickListener(headerClick);

        // Date range buttons
        mStartDate.setOnClickListener(buttonClick);
        mEndDate.setOnClickListener(buttonClick);

        // All / None
        mTypeAll.setOnClickListener(allNoneClick);
        mTypeNone.setOnClickListener(allNoneClick);
        mRatingAll.setOnClickListener(allNoneClick);
        mRatingNone.setOnClickListener(allNoneClick);
        mAudienceAll.setOnClickListener(allNoneClick);
        mAudienceNone.setOnClickListener(allNoneClick);
        mBranchAll.setOnClickListener(allNoneClick);
        mBranchNone.setOnClickListener(allNoneClick);
        mClimbingAll.setOnClickListener(allNoneClick);
        mClimbingNone.setOnClickListener(allNoneClick);
        mSkiingAll.setOnClickListener(allNoneClick);
        mSkiingNone.setOnClickListener(allNoneClick);
        mSnowshoeingAll.setOnClickListener(allNoneClick);
        mSnowshoeingNone.setOnClickListener(allNoneClick);
    }
}