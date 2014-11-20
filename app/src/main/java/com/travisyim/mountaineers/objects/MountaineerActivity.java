package com.travisyim.mountaineers.objects;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.Date;

public class MountaineerActivity implements Serializable {
    private Date mActivityStartDate;
    private Date mActivityEndDate;
    private Date mActivityCreationDate;
    private Date mActivityAddedAt;
    private Date mActivityUpdatedAt = null;
    private String mActivityUrl;
    private int mAvailabilityLeader = -999;
    private int mAvailabilityParticipant = -999;
    private String mBranch;
    private boolean mCanceled = false;
    private double mEndLatitude = -999;
    private double mEndLongitude = -999;
    private boolean mFavorite = false;
    private String mImageUrl;
    private JSONArray mLeaderName;
    private String mTitle;
    private JSONArray mType;
    private String mObjectID;
    private Date mRegistrationCloseTime;
    private Date mRegistrationOpenTime;
    private String mRole;
    private double mStartLatitude = -999;
    private double mStartLongitude = -999;
    private String mStatus;
    private boolean mUserActivity = false;

    // Filter Categories
    // Audience
    private boolean mAudienceAdults = false;
    private boolean mAudienceFamilies = false;
    private boolean mAudienceRetiredRovers = false;
    private boolean mAudienceSingles = false;
    private boolean mAudience2030Somethings = false;
    private boolean mAudienceYouth = false;
    // Branch
    private boolean mBranchTheMountaineers = false;
    private boolean mBranchBellingham = false;
    private boolean mBranchEverett = false;
    private boolean mBranchFoothills = false;
    private boolean mBranchKitsap = false;
    private boolean mBranchOlympia = false;
    private boolean mBranchOutdoorCenters = false;
    private boolean mBranchSeattle = false;
    private boolean mBranchTacoma = false;
    // Climbing
    private boolean mClimbingBasicAlpine = false;
    private boolean mClimbingIntermediateAlpine = false;
    private boolean mClimbingAidClimb = false;
    private boolean mClimbingRockClimb = false;
    // Leader Rating
    private boolean mRatingForBeginners = false;
    private boolean mRatingEasy = false;
    private boolean mRatingModerate = false;
    private boolean mRatingChallenging = false;
    // Skiing
    private boolean mSkiingCrossCountry = false;
    private boolean mSkiingBackcountry = false;
    private boolean mSkiingGlacier = false;
    // Snowshoeing
    private boolean mSnowshoeingBeginner = false;
    private boolean mSnowshoeingBasic = false;
    private boolean mSnowshoeingIntermediate = false;
    // Type
    private boolean mTypeAdventureClub = false;
    private boolean mTypeBackpacking = false;
    private boolean mTypeClimbing = false;
    private boolean mTypeDayHiking = false;
    private boolean mTypeExplorers = false;
    private boolean mTypeExploringNature = false;
    private boolean mTypeGlobalAdventures = false;
    private boolean mTypeNavigation = false;
    private boolean mTypePhotography = false;
    private boolean mTypeSailing = false;
    private boolean mTypeScrambling = false;
    private boolean mTypeSeaKayaking = false;
    private boolean mTypeSkiingSnowboarding = false;
    private boolean mTypeSnowshoeing = false;
    private boolean mTypeStewardship = false;
    private boolean mTypeTrailRunning = false;
    private boolean mTypeUrbanAdventure = false;
    private boolean mTypeYouth = false;

    // Flag for user read/unread state
    private boolean mIsUnread = false;

    public MountaineerActivity() {
    }

    public final Date getActivityStartDate() {
        return mActivityStartDate;
    }

    public final void setActivityStartDate(final Date activityStartDate) {
        mActivityStartDate = activityStartDate;
    }

    public final Date getActivityEndDate() {
        return mActivityEndDate;
    }

    public final void setActivityEndDate(final Date activityEndDate) {
        mActivityEndDate = activityEndDate;
    }

    public final Date getActivityCreationDate() {
        return mActivityCreationDate;
    }

    public final void setActivityCreationDate(final Date activityCreationDate) {
        mActivityCreationDate = activityCreationDate;
    }

    public final Date getActivityAddedAt() {
        return mActivityAddedAt;
    }

    public final void setActivityAddedAt(final Date activityAddedAt) {
        mActivityAddedAt = activityAddedAt;
    }

    public final Date getActivityUpdatedAt() {
        return mActivityUpdatedAt;
    }

    public final void setActivityUpdatedAt(final Date activityUpdatedAt) {
        mActivityUpdatedAt = activityUpdatedAt;
    }

    public final String getActivityUrl() {
        return mActivityUrl;
    }

    public final void setActivityUrl(final String activityUrl) {
        mActivityUrl = activityUrl;
    }

    public final int getAvailabilityLeader() {
        return mAvailabilityLeader;
    }

    public final void setAvailabilityLeader(final int availabilityLeader) {
        mAvailabilityLeader = availabilityLeader;
    }

    public final int getAvailabilityParticipant() {
        return mAvailabilityParticipant;
    }

    public final void setAvailabilityParticipant(final int availabilityParticipant) {
        mAvailabilityParticipant = availabilityParticipant;
    }

    public final String getBranch() {
        return mBranch;
    }

    public final void setBranch(final String branch) {
        mBranch = branch;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public void setCanceled(boolean isCanceled) {
        mCanceled = isCanceled;
    }

    public final double getEndLatitude() {
        return mEndLatitude;
    }

    public final void setEndLatitude(final double endLatitude) {
        mEndLatitude = endLatitude;
    }

    public final double getEndLongitude() {
        return mEndLongitude;
    }

    public final void setEndLongitude(final double endLongitude) {
        mEndLongitude = endLongitude;
    }

    public boolean isFavorite() {
        return mFavorite;
    }

    public void setFavorite(boolean favorite) {
        mFavorite = favorite;
    }

    public final String getImageUrl() {
        return mImageUrl;
    }

    public final void setImageUrl(final String imageUrl) {
        mImageUrl = imageUrl;
    }

    public final JSONArray getLeaderName() {
        return mLeaderName;
    }

    public final void setLeaderName(final JSONArray leaderName) {
        mLeaderName = leaderName;
    }

    public final String getTitle() {
        return mTitle;
    }

    public final void setTitle(final String title) {
        mTitle = title;
    }

    public String getObjectID() {
        return mObjectID;
    }

    public void setObjectID(String objectID) {
        mObjectID = objectID;
    }

    public final Date getRegistrationCloseTime() {
        return mRegistrationCloseTime;
    }

    public final void setRegistrationCloseTime(final Date registrationCloseTime) {
        mRegistrationCloseTime = registrationCloseTime;
    }

    public final Date getRegistrationOpenTime() {
        return mRegistrationOpenTime;
    }

    public final void setRegistrationOpenTime(final Date registrationOpenTime) {
        mRegistrationOpenTime = registrationOpenTime;
    }

    public final String getUserRole() {
        return mRole;
    }

    public final void setUserRole(final String role) {
        mRole = role;
    }

    public final double getStartLatitude() {
        return mStartLatitude;
    }

    public final void setStartLatitude(final double startLatitude) {
        mStartLatitude = startLatitude;
    }

    public final double getStartLongitude() {
        return mStartLongitude;
    }

    public final void setStartLongitude(final double startLongitude) {
        mStartLongitude = startLongitude;
    }

    public final String getStatus() {
        return mStatus;
    }

    public final void setStatus(final String status) {
        mStatus = status;
    }

    public final JSONArray getType() {
        return mType;
    }

    public final void setType(final JSONArray type) {
        mType = type;
    }

    public boolean isUserActivity() {
        return mUserActivity;
    }

    public void setUserActivity(boolean userActivity) {
        mUserActivity = userActivity;
    }

    public boolean isTypeAdventureClub() {
        return mTypeAdventureClub;
    }

    public void setTypeAdventureClub(boolean isTypeAdventureClub) {
        mTypeAdventureClub = isTypeAdventureClub;
    }

    public boolean isTypeBackpacking() {
        return mTypeBackpacking;
    }

    public void setTypeBackpacking(boolean isTypeBackpacking) {
        mTypeBackpacking = isTypeBackpacking;
    }

    public boolean isTypeClimbing() {
        return mTypeClimbing;
    }

    public void setTypeClimbing(boolean isTypeClimbing) {
        mTypeClimbing = isTypeClimbing;
    }

    public boolean isTypeDayHiking() {
        return mTypeDayHiking;
    }

    public void setTypeDayHiking(boolean isTypeDayHiking) {
        mTypeDayHiking = isTypeDayHiking;
    }

    public boolean isTypeExplorers() {
        return mTypeExplorers;
    }

    public void setTypeExplorers(boolean isTypeExplorers) {
        mTypeExplorers = isTypeExplorers;
    }

    public boolean isTypeExploringNature() {
        return mTypeExploringNature;
    }

    public void setTypeExploringNature(boolean isTypeExploringNature) {
        mTypeExploringNature = isTypeExploringNature;
    }

    public boolean isTypeGlobalAdventures() {
        return mTypeGlobalAdventures;
    }

    public void setTypeGlobalAdventures(boolean isTypeGlobalAdventures) {
        mTypeGlobalAdventures = isTypeGlobalAdventures;
    }

    public boolean isTypeNavigation() {
        return mTypeNavigation;
    }

    public void setTypeNavigation(boolean isTypeNavigation) {
        mTypeNavigation = isTypeNavigation;
    }

    public boolean isTypePhotography() {
        return mTypePhotography;
    }

    public void setTypePhotography(boolean isTypePhotography) {
        mTypePhotography = isTypePhotography;
    }

    public boolean isTypeSailing() {
        return mTypeSailing;
    }

    public void setTypeSailing(boolean isTypeSailing) {
        mTypeSailing = isTypeSailing;
    }

    public boolean isTypeScrambling() {
        return mTypeScrambling;
    }

    public void setTypeScrambling(boolean isTypeScrambling) {
        mTypeScrambling = isTypeScrambling;
    }

    public boolean isTypeSeaKayaking() {
        return mTypeSeaKayaking;
    }

    public void setTypeSeaKayaking(boolean isTypeSeaKayaking) {
        mTypeSeaKayaking = isTypeSeaKayaking;
    }

    public boolean isTypeSkiingSnowboarding() {
        return mTypeSkiingSnowboarding;
    }

    public void setTypeSkiingSnowboarding(boolean isTypeSkiingSnowboarding) {
        mTypeSkiingSnowboarding = isTypeSkiingSnowboarding;
    }

    public boolean isTypeSnowshoeing() {
        return mTypeSnowshoeing;
    }

    public void setTypeSnowshoeing(boolean isTypeSnowshoeing) {
        mTypeSnowshoeing = isTypeSnowshoeing;
    }

    public boolean isTypeStewardship() {
        return mTypeStewardship;
    }

    public void setTypeStewardship(boolean isTypeStewardship) {
        mTypeStewardship = isTypeStewardship;
    }

    public boolean isTypeTrailRunning() {
        return mTypeTrailRunning;
    }

    public void setTypeTrailRunning(boolean isTypeTrailRunning) {
        mTypeTrailRunning = isTypeTrailRunning;
    }

    public boolean isTypeUrbanAdventure() {
        return mTypeUrbanAdventure;
    }

    public void setTypeUrbanAdventure(boolean isTypeUrbanAdventure) {
        mTypeUrbanAdventure = isTypeUrbanAdventure;
    }

    public boolean isTypeYouth() {
        return mTypeYouth;
    }

    public void setTypeYouth(boolean isTypeYouth) {
        mTypeYouth = isTypeYouth;
    }

    public boolean isRatingForBeginners() {
        return mRatingForBeginners;
    }

    public void setRatingForBeginners(boolean isRatingForBeginners) {
        mRatingForBeginners = isRatingForBeginners;
    }

    public boolean isRatingEasy() {
        return mRatingEasy;
    }

    public void setRatingEasy(boolean isRatingEasy) {
        mRatingEasy = isRatingEasy;
    }

    public boolean isRatingModerate() {
        return mRatingModerate;
    }

    public void setRatingModerate(boolean isRatingModerate) {
        mRatingModerate = isRatingModerate;
    }

    public boolean isRatingChallenging() {
        return mRatingChallenging;
    }

    public void setRatingChallenging(boolean isRatingChallenging) {
        mRatingChallenging = isRatingChallenging;
    }

    public boolean isAudienceAdults() {
        return mAudienceAdults;
    }

    public void setAudienceAdults(boolean isAudienceAdults) {
        mAudienceAdults = isAudienceAdults;
    }

    public boolean isAudienceFamilies() {
        return mAudienceFamilies;
    }

    public void setAudienceFamilies(boolean isAudienceFamilies) {
        mAudienceFamilies = isAudienceFamilies;
    }

    public boolean isAudienceRetired() {
        return mAudienceRetiredRovers;
    }

    public void setAudienceRetired(boolean isAudienceRetired) {
        mAudienceRetiredRovers = isAudienceRetired;
    }

    public boolean isAudienceSingles() {
        return mAudienceSingles;
    }

    public void setAudienceSingles(boolean isAudienceSingles) {
        mAudienceSingles = isAudienceSingles;
    }

    public boolean isAudience2030Somethings() {
        return mAudience2030Somethings;
    }

    public void setAudience2030Somethings(boolean isAudience2030Somethings) {
        mAudience2030Somethings = isAudience2030Somethings;
    }

    public boolean isAudienceYouth() {
        return mAudienceYouth;
    }

    public void setAudienceYouth(boolean isAudienceYouth) {
        mAudienceYouth = isAudienceYouth;
    }

    public boolean isBranchTheMountaineers() {
        return mBranchTheMountaineers;
    }

    public void setBranchTheMountaineers(boolean isBranchTheMountaineers) {
        mBranchTheMountaineers = isBranchTheMountaineers;
    }

    public boolean isBranchBellingham() {
        return mBranchBellingham;
    }

    public void setBranchBellingham(boolean isBranchBellingham) {
        mBranchBellingham = isBranchBellingham;
    }

    public boolean isBranchEverett() {
        return mBranchEverett;
    }

    public void setBranchEverett(boolean isBranchEverett) {
        mBranchEverett = isBranchEverett;
    }

    public boolean isBranchFoothills() {
        return mBranchFoothills;
    }

    public void setBranchFoothills(boolean isBranchFoothills) {
        mBranchFoothills = isBranchFoothills;
    }

    public boolean isBranchKitsap() {
        return mBranchKitsap;
    }

    public void setBranchKitsap(boolean isBranchKitsap) {
        mBranchKitsap = isBranchKitsap;
    }

    public boolean isBranchOlympia() {
        return mBranchOlympia;
    }

    public void setBranchOlympia(boolean isBranchOlympia) {
        mBranchOlympia = isBranchOlympia;
    }

    public boolean isBranchOutdoorCenters() {
        return mBranchOutdoorCenters;
    }

    public void setBranchOutdoorCenters(boolean isBranchOutdoorCenters) {
        mBranchOutdoorCenters = isBranchOutdoorCenters;
    }

    public boolean isBranchSeattle() {
        return mBranchSeattle;
    }

    public void setBranchSeattle(boolean isBranchSeattle) {
        mBranchSeattle = isBranchSeattle;
    }

    public boolean isBranchTacoma() {
        return mBranchTacoma;
    }

    public void setBranchTacoma(boolean isBranchTacoma) {
        mBranchTacoma = isBranchTacoma;
    }

    public boolean isClimbingBasicAlpine() {
        return mClimbingBasicAlpine;
    }

    public void setClimbingBasicAlpine(boolean isClimbingBasicAlpine) {
        mClimbingBasicAlpine = isClimbingBasicAlpine;
    }

    public boolean isClimbingIntermediateAlpine() {
        return mClimbingIntermediateAlpine;
    }

    public void setClimbingIntermediateAlpine(boolean isClimbingIntermediateAlpine) {
        mClimbingIntermediateAlpine = isClimbingIntermediateAlpine;
    }

    public boolean isClimbingAidClimb() {
        return mClimbingAidClimb;
    }

    public void setClimbingAidClimb(boolean isClimbingAidClimb) {
        mClimbingAidClimb = isClimbingAidClimb;
    }

    public boolean isClimbingRockClimb() {
        return mClimbingRockClimb;
    }

    public void setClimbingRockClimb(boolean isClimbingRockClimb) {
        mClimbingRockClimb = isClimbingRockClimb;
    }

    public boolean isSkiingCrossCountry() {
        return mSkiingCrossCountry;
    }

    public void setSkiingCrossCountry(boolean isSkiingCrossCountry) {
        mSkiingCrossCountry = isSkiingCrossCountry;
    }

    public boolean isSkiingBackcountry() {
        return mSkiingBackcountry;
    }

    public void setSkiingBackcountry(boolean isSkiingBackcountry) {
        mSkiingBackcountry = isSkiingBackcountry;
    }

    public boolean isSkiingGlacier() {
        return mSkiingGlacier;
    }

    public void setSkiingGlacier(boolean isSkiingGlacier) {
        mSkiingGlacier = isSkiingGlacier;
    }

    public boolean isSnowshoeingBeginner() {
        return mSnowshoeingBeginner;
    }

    public void setSnowshoeingBeginner(boolean isSnowshoeingBeginner) {
        mSnowshoeingBeginner = isSnowshoeingBeginner;
    }

    public boolean isSnowshoeingBasic() {
        return mSnowshoeingBasic;
    }

    public void setSnowshoeingBasic(boolean isSnowshoeingBasic) {
        mSnowshoeingBasic = isSnowshoeingBasic;
    }

    public boolean isSnowshoeingIntermediate() {
        return mSnowshoeingIntermediate;
    }

    public void setSnowshoeingIntermediate(boolean isSnowshoeingIntermediate) {
        mSnowshoeingIntermediate = isSnowshoeingIntermediate;
    }

    public boolean isUnread() {
        return mIsUnread;
    }

    public void setUnread(final boolean isUnread) {
        mIsUnread = isUnread;
    }
}