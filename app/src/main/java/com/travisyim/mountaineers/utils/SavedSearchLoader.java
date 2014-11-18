package com.travisyim.mountaineers.utils;

import android.text.TextUtils;

import com.parse.ParseObject;
import com.travisyim.mountaineers.objects.SavedSearch;

import java.util.ArrayList;
import java.util.List;

// This class takes in a list of ParseObjects and returns a list of SavedSearch objects
public class SavedSearchLoader {
    // Method called by ActivitySearchFragment
    public static List<SavedSearch> load(List<ParseObject> resultList){
        List<SavedSearch> searchList = new ArrayList<SavedSearch>();

        for (ParseObject result : resultList) {
            SavedSearch savedSearch = new SavedSearch();

            // Get all search attributes
            savedSearch.setObjectID(result.getObjectId());
            savedSearch.setSearchName(result.getString(ParseConstants.KEY_SAVE_NAME));
            savedSearch.setLastAccessDate(result.getDate(ParseConstants.KEY_LAST_ACCESS));
            savedSearch.setUpdateCounter(result.getInt(ParseConstants.KEY_UPDATE_COUNT));

            try {
                savedSearch.setQueryText(TextUtils.join(" ", result.getList(ParseConstants.KEY_KEYWORDS)));
            }
            catch (NullPointerException e) { /* Ignore error - ok for query text will be null */ }

            savedSearch.setActivityStartDate(DateUtil.convertFromUNC(result.getDate(ParseConstants.KEY_ACTIVITY_START_DATE)));
            savedSearch.setActivityEndDate(DateUtil.convertFromUNC(result.getDate(ParseConstants.KEY_ACTIVITY_END_DATE)));

            savedSearch.setAudience2030Somethings(result.getBoolean(ParseConstants.KEY_AUDIENCE_20_30_SOMETHINGS));
            savedSearch.setAudienceAdults(result.getBoolean(ParseConstants.KEY_AUDIENCE_ADULTS));
            savedSearch.setAudienceFamilies(result.getBoolean(ParseConstants.KEY_AUDIENCE_FAMILIES));
            savedSearch.setAudienceRetiredRovers(result.getBoolean(ParseConstants.KEY_AUDIENCE_RETIRED_ROVERS));
            savedSearch.setAudienceSingles(result.getBoolean(ParseConstants.KEY_AUDIENCE_SINGLES));
            savedSearch.setAudienceYouth(result.getBoolean(ParseConstants.KEY_AUDIENCE_YOUTH));

            savedSearch.setBranchTheMountaineers(result.getBoolean(ParseConstants.KEY_BRANCH_THE_MOUNTAINEERS));
            savedSearch.setBranchBellingham(result.getBoolean(ParseConstants.KEY_BRANCH_BELLINGHAM));
            savedSearch.setBranchEverett(result.getBoolean(ParseConstants.KEY_BRANCH_EVERETT));
            savedSearch.setBranchFoothills(result.getBoolean(ParseConstants.KEY_BRANCH_FOOTHILLS));
            savedSearch.setBranchKitsap(result.getBoolean(ParseConstants.KEY_BRANCH_KITSAP));
            savedSearch.setBranchOlympia(result.getBoolean(ParseConstants.KEY_BRANCH_OLYMPIA));
            savedSearch.setBranchOutdoorCenters(result.getBoolean(ParseConstants.KEY_BRANCH_OUTDOOR_CENTERS));
            savedSearch.setBranchSeattle(result.getBoolean(ParseConstants.KEY_BRANCH_SEATTLE));
            savedSearch.setBranchTacoma(result.getBoolean(ParseConstants.KEY_BRANCH_TACOMA));

            savedSearch.setClimbingBasicAlpine(result.getBoolean(ParseConstants.KEY_CLIMBING_BASIC_ALPINE));
            savedSearch.setClimbingIntermediateAlpine(result.getBoolean(ParseConstants.KEY_CLIMBING_INTERMEDIATE_ALPINE));
            savedSearch.setClimbingAidClimb(result.getBoolean(ParseConstants.KEY_CLIMBING_AID_CLIMB));
            savedSearch.setClimbingRockClimb(result.getBoolean(ParseConstants.KEY_CLIMBING_ROCK_CLIMB));

            savedSearch.setRatingForBeginners(result.getBoolean(ParseConstants.KEY_RATING_FOR_BEGINNERS));
            savedSearch.setRatingEasy(result.getBoolean(ParseConstants.KEY_RATING_EASY));
            savedSearch.setRatingModerate(result.getBoolean(ParseConstants.KEY_RATING_MODERATE));
            savedSearch.setRatingChallenging(result.getBoolean(ParseConstants.KEY_RATING_CHALLENGING));

            savedSearch.setSkiingCrossCountry(result.getBoolean(ParseConstants.KEY_SKIING_CROSS_COUNTRY));
            savedSearch.setSkiingBackcountry(result.getBoolean(ParseConstants.KEY_SKIING_BACKCOUNTRY));
            savedSearch.setSkiingGlacier(result.getBoolean(ParseConstants.KEY_SKIING_GLACIER));

            savedSearch.setSnowshoeingBeginner(result.getBoolean(ParseConstants.KEY_SNOWSHOEING_BEGINNER));
            savedSearch.setSnowshoeingBasic(result.getBoolean(ParseConstants.KEY_SNOWSHOEING_BASIC));
            savedSearch.setSnowshoeingIntermediate(result.getBoolean(ParseConstants.KEY_SNOWSHOEING_INTERMEDIATE));

            savedSearch.setTypeAdventureClub(result.getBoolean(ParseConstants.KEY_TYPE_ADVENTURE_CLUB));
            savedSearch.setTypeBackpacking(result.getBoolean(ParseConstants.KEY_TYPE_BACKPACKING));
            savedSearch.setTypeClimbing(result.getBoolean(ParseConstants.KEY_TYPE_CLIMBING));
            savedSearch.setTypeDayHiking(result.getBoolean(ParseConstants.KEY_TYPE_DAY_HIKING));
            savedSearch.setTypeExplorers(result.getBoolean(ParseConstants.KEY_TYPE_EXPLORERS));
            savedSearch.setTypeExploringNature(result.getBoolean(ParseConstants.KEY_TYPE_EXPLORING_NATURE));
            savedSearch.setTypeGlobalAdventures(result.getBoolean(ParseConstants.KEY_TYPE_GLOBAL_ADVENTURES));
            savedSearch.setTypeNavigation(result.getBoolean(ParseConstants.KEY_TYPE_NAVIGATION));
            savedSearch.setTypePhotography(result.getBoolean(ParseConstants.KEY_TYPE_PHOTOGRAPHY));
            savedSearch.setTypeSailing(result.getBoolean(ParseConstants.KEY_TYPE_SAILING));
            savedSearch.setTypeScrambling(result.getBoolean(ParseConstants.KEY_TYPE_SCRAMBLING));
            savedSearch.setTypeSeaKayaking(result.getBoolean(ParseConstants.KEY_TYPE_SEA_KAYAKING));
            savedSearch.setTypeSkiingSnowboarding(result.getBoolean(ParseConstants.KEY_TYPE_SKIING_SNOWBOARDING));
            savedSearch.setTypeSnowshoeing(result.getBoolean(ParseConstants.KEY_TYPE_SNOWSHOEING));
            savedSearch.setTypeStewardship(result.getBoolean(ParseConstants.KEY_TYPE_STEWARDSHIP));
            savedSearch.setTypeTrailRunning(result.getBoolean(ParseConstants.KEY_TYPE_TRAIL_RUNNING));
            savedSearch.setTypeUrbanAdventure(result.getBoolean(ParseConstants.KEY_TYPE_URBAN_ADVENTURE));
            savedSearch.setTypeYouth(result.getBoolean(ParseConstants.KEY_TYPE_YOUTH));

            searchList.add(savedSearch);  // Add current saved search to master list
        }

        return searchList;
    }
}