package com.travisyim.mountaineers.objects;

import java.io.Serializable;
import java.util.Date;

public class FilterOptions implements Serializable {
    // Dates
    private Date mStartDate = null;
    private Date mEndDate = null;

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

    // Leader rating
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

    // Activity type
    private boolean mTypeAdventureClub = false;
    private boolean mTypeBackpacking = false;
    private boolean mTypeClimbing = false;
    private boolean mTypeDayHiking = false;
    private boolean mTypeExplorers = false;
    private boolean mTypeExploringNature = false;
    private boolean mTypeGlobalAdventures = false;
    private boolean mTypeMountainWorkshop = false;
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

    public FilterOptions() {
    }

    // Date options
    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date StartDate) {
        mStartDate = StartDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Date EndDate) {
        mEndDate = EndDate;
    }

    // Activity type options
    public boolean isTypeAdventureClub() {
        return mTypeAdventureClub;
    }

    public void setTypeAdventureClub(final boolean isTypeAdventureClub) {
        mTypeAdventureClub = isTypeAdventureClub;
    }

    public boolean isTypeBackpacking() {
        return mTypeBackpacking;
    }

    public void setTypeBackpacking(final boolean isTypeBackpacking) {
        mTypeBackpacking = isTypeBackpacking;
    }

    public boolean isTypeClimbing() {
        return mTypeClimbing;
    }

    public void setTypeClimbing(final boolean isTypeClimbing) {
        mTypeClimbing = isTypeClimbing;
    }

    public boolean isTypeDayHiking() {
        return mTypeDayHiking;
    }

    public void setTypeDayHiking(final boolean isTypeDayHiking) {
        mTypeDayHiking = isTypeDayHiking;
    }

    public boolean isTypeExplorers() {
        return mTypeExplorers;
    }

    public void setTypeExplorers(final boolean isTypeExplorers) {
        mTypeExplorers = isTypeExplorers;
    }

    public boolean isTypeExploringNature() {
        return mTypeExploringNature;
    }

    public void setTypeExploringNature(final boolean isTypeExploringNature) {
        mTypeExploringNature = isTypeExploringNature;
    }

    public boolean isTypeGlobalAdventures() {
        return mTypeGlobalAdventures;
    }

    public void setTypeGlobalAdventures(final boolean isTypeGlobalAdventures) {
        mTypeGlobalAdventures = isTypeGlobalAdventures;
    }

    public boolean isTypeMountainWorkshop() {
        return mTypeMountainWorkshop;
    }

    public void setTypeMountainWorkshop(final boolean isTypeMountainWorkshop) {
        mTypeMountainWorkshop = isTypeMountainWorkshop;
    }

    public boolean isTypeNavigation() {
        return mTypeNavigation;
    }

    public void setTypeNavigation(final boolean isTypeNavigation) {
        mTypeNavigation = isTypeNavigation;
    }

    public boolean isTypePhotography() {
        return mTypePhotography;
    }

    public void setTypePhotography(final boolean isTypePhotography) {
        mTypePhotography = isTypePhotography;
    }

    public boolean isTypeSailing() {
        return mTypeSailing;
    }

    public void setTypeSailing(final boolean isTypeSailing) {
        mTypeSailing = isTypeSailing;
    }

    public boolean isTypeScrambling() {
        return mTypeScrambling;
    }

    public void setTypeScrambling(final boolean isTypeScrambling) {
        mTypeScrambling = isTypeScrambling;
    }

    public boolean isTypeSeaKayaking() {
        return mTypeSeaKayaking;
    }

    public void setTypeSeaKayaking(final boolean isTypeSeaKayaking) {
        mTypeSeaKayaking = isTypeSeaKayaking;
    }

    public boolean isTypeSkiingSnowboarding() {
        return mTypeSkiingSnowboarding;
    }

    public void setTypeSkiingSnowboarding(final boolean isTypeSkiingSnowboarding) {
        mTypeSkiingSnowboarding = isTypeSkiingSnowboarding;
    }

    public boolean isTypeSnowshoeing() {
        return mTypeSnowshoeing;
    }

    public void setTypeSnowshoeing(final boolean isTypeSnowshoeing) {
        mTypeSnowshoeing = isTypeSnowshoeing;
    }

    public boolean isTypeStewardship() {
        return mTypeStewardship;
    }

    public void setTypeStewardship(final boolean isTypeStewardship) {
        mTypeStewardship = isTypeStewardship;
    }

    public boolean isTypeTrailRunning() {
        return mTypeTrailRunning;
    }

    public void setTypeTrailRunning(final boolean isTypeTrailRunning) {
        mTypeTrailRunning = isTypeTrailRunning;
    }

    public boolean isTypeUrbanAdventure() {
        return mTypeUrbanAdventure;
    }

    public void setTypeUrbanAdventure(final boolean isTypeUrbanAdventure) {
        mTypeUrbanAdventure = isTypeUrbanAdventure;
    }

    public boolean isTypeYouth() {
        return mTypeYouth;
    }

    public void setTypeYouth(final boolean isTypeYouth) {
        mTypeYouth = isTypeYouth;
    }

    // Leader rating options
    public boolean isRatingForBeginners() {
        return mRatingForBeginners;
    }

    public void setRatingForBeginners(final boolean isRatingForBeginners) {
        mRatingForBeginners = isRatingForBeginners;
    }

    public boolean isRatingEasy() {
        return mRatingEasy;
    }

    public void setRatingEasy(final boolean isRatingEasy) {
        mRatingEasy = isRatingEasy;
    }

    public boolean isRatingModerate() {
        return mRatingModerate;
    }

    public void setRatingModerate(final boolean isRatingModerate) {
        mRatingModerate = isRatingModerate;
    }

    public boolean isRatingChallenging() {
        return mRatingChallenging;
    }

    public void setRatingChallenging(final boolean isRatingChallenging) {
        mRatingChallenging = isRatingChallenging;
    }

    // Audience options
    public boolean isAudienceAdults() {
        return mAudienceAdults;
    }

    public void setAudienceAdults(final boolean isAudienceAdults) {
        mAudienceAdults = isAudienceAdults;
    }

    public boolean isAudienceFamilies() {
        return mAudienceFamilies;
    }

    public void setAudienceFamilies(final boolean isAudienceFamilies) {
        mAudienceFamilies = isAudienceFamilies;
    }

    public boolean isAudienceRetiredRovers() {
        return mAudienceRetiredRovers;
    }

    public void setAudienceRetiredRovers(final boolean isAudienceRetiredRovers) {
        mAudienceRetiredRovers = isAudienceRetiredRovers;
    }

    public boolean isAudienceSingles() {
        return mAudienceSingles;
    }

    public void setAudienceSingles(final boolean isAudienceSingles) {
        mAudienceSingles = isAudienceSingles;
    }

    public boolean isAudience2030Somethings() {
        return mAudience2030Somethings;
    }

    public void setAudience2030Somethings(final boolean isAudience2030Somethings) {
        mAudience2030Somethings = isAudience2030Somethings;
    }

    public boolean isAudienceYouth() {
        return mAudienceYouth;
    }

    public void setAudienceYouth(final boolean isAudienceYouth) {
        mAudienceYouth = isAudienceYouth;
    }

    // Branch options
    public boolean isBranchTheMountaineers() {
        return mBranchTheMountaineers;
    }

    public void setBranchTheMountaineers(final boolean isBranchTheMountaineers) {
        mBranchTheMountaineers = isBranchTheMountaineers;
    }

    public boolean isBranchBellingham() {
        return mBranchBellingham;
    }

    public void setBranchBellingham(final boolean isBranchBellingham) {
        mBranchBellingham = isBranchBellingham;
    }

    public boolean isBranchEverett() {
        return mBranchEverett;
    }

    public void setBranchEverett(final boolean isBranchEverett) {
        mBranchEverett = isBranchEverett;
    }

    public boolean isBranchFoothills() {
        return mBranchFoothills;
    }

    public void setBranchFoothills(final boolean isBranchFoothills) {
        mBranchFoothills = isBranchFoothills;
    }

    public boolean isBranchKitsap() {
        return mBranchKitsap;
    }

    public void setBranchKitsap(final boolean isBranchKitsap) {
        mBranchKitsap = isBranchKitsap;
    }

    public boolean isBranchOlympia() {
        return mBranchOlympia;
    }

    public void setBranchOlympia(final boolean isBranchOlympia) {
        mBranchOlympia = isBranchOlympia;
    }

    public boolean isBranchOutdoorCenters() {
        return mBranchOutdoorCenters;
    }

    public void setBranchOutdoorCenters(final boolean isBranchOutdoorCenters) {
        mBranchOutdoorCenters = isBranchOutdoorCenters;
    }

    public boolean isBranchSeattle() {
        return mBranchSeattle;
    }

    public void setBranchSeattle(final boolean isBranchSeattle) {
        mBranchSeattle = isBranchSeattle;
    }

    public boolean isBranchTacoma() {
        return mBranchTacoma;
    }

    public void setBranchTacoma(final boolean isBranchTacoma) {
        mBranchTacoma = isBranchTacoma;
    }

    // Climbing options
    public boolean isClimbingBasicAlpine() {
        return mClimbingBasicAlpine;
    }

    public void setClimbingBasicAlpine(final boolean isClimbingBasicAlpine) {
        mClimbingBasicAlpine = isClimbingBasicAlpine;
    }

    public boolean isClimbingIntermediateAlpine() {
        return mClimbingIntermediateAlpine;
    }

    public void setClimbingIntermediateAlpine(final boolean isClimbingIntermediateAlpine) {
        mClimbingIntermediateAlpine = isClimbingIntermediateAlpine;
    }

    public boolean isClimbingAidClimb() {
        return mClimbingAidClimb;
    }

    public void setClimbingAidClimb(final boolean isClimbingAidClimb) {
        mClimbingAidClimb = isClimbingAidClimb;
    }

    public boolean isClimbingRockClimb() {
        return mClimbingRockClimb;
    }

    public void setClimbingRockClimb(final boolean isClimbingRockClimb) {
        mClimbingRockClimb = isClimbingRockClimb;
    }

    // Skiing/Snowboarding options
    public boolean isSkiingCrossCountry() {
        return mSkiingCrossCountry;
    }

    public void setSkiingCrossCountry(final boolean isSkiingCrossCountry) {
        mSkiingCrossCountry = isSkiingCrossCountry;
    }

    public boolean isSkiingBackcountry() {
        return mSkiingBackcountry;
    }

    public void setSkiingBackcountry(final boolean isSkiingBackcountry) {
        mSkiingBackcountry = isSkiingBackcountry;
    }

    public boolean isSkiingGlacier() {
        return mSkiingGlacier;
    }

    public void setSkiingGlacier(final boolean isSkiingGlacier) {
        mSkiingGlacier = isSkiingGlacier;
    }

    // Snowshoeing options
    public boolean isSnowshoeingBeginner() {
        return mSnowshoeingBeginner;
    }

    public void setSnowshoeingBeginner(final boolean isSnowshoeingBeginner) {
        mSnowshoeingBeginner = isSnowshoeingBeginner;
    }

    public boolean isSnowshoeingBasic() {
        return mSnowshoeingBasic;
    }

    public void setSnowshoeingBasic(final boolean isSnowshoeingBasic) {
        mSnowshoeingBasic = isSnowshoeingBasic;
    }

    public boolean isSnowshoeingIntermediate() {
        return mSnowshoeingIntermediate;
    }

    public void setSnowshoeingIntermediate(final boolean isSnowshoeingIntermediate) {
        mSnowshoeingIntermediate = isSnowshoeingIntermediate;
    }
}