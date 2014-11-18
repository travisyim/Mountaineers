package com.travisyim.mountaineers.utils;

import com.parse.ParseObject;
import com.travisyim.mountaineers.objects.Mountaineer;
import com.travisyim.mountaineers.objects.MountaineerActivity;

import java.util.ArrayList;
import java.util.List;

// This class takes in a list of ParseObjects and returns a list of MountaineerActivity objects
public class ActivityLoader {
    public static enum ActivityType {
        COMPLETED,
        SIGNED_UP
    }

    // Method called by ActivitySearchFragment
    public static List<MountaineerActivity> load(List<ParseObject> resultList,
                                                 List<String> favoritesList){
        return backgroundLoad(resultList, favoritesList, false, null, null);
    }

    // Method called by FavoriteActivityFragment
    public static List<MountaineerActivity> load (List<ParseObject> resultList, boolean isFavorites){
        return backgroundLoad(resultList, null, isFavorites, null, null);
    }

    // Method called by CompletedActivityFragment & SignedUpActivityFragment
    public static List<MountaineerActivity> load (List<ParseObject> resultList,
                                                  List<String> favoritesList, Mountaineer member,
                                                  ActivityType type){
        return backgroundLoad(resultList, favoritesList, false, member, type);
    }

    private static List<MountaineerActivity> backgroundLoad(List<ParseObject> resultList,
                                                            List<String> favoritesList,
                                                            boolean isFavorites, Mountaineer member,
                                                            ActivityType type){
        List<MountaineerActivity> activityList = new ArrayList<MountaineerActivity>();
        int favoriteObjectIdPosition;

        for (ParseObject result : resultList) {
            MountaineerActivity activity = new MountaineerActivity();

            // Activity start and end dates
            activity.setActivityStartDate(DateUtil.convertFromUNC(result.getDate
                    (ParseConstants.KEY_ACTIVITY_START_DATE)));
            activity.setActivityEndDate(DateUtil.convertFromUNC(result.getDate
                    (ParseConstants.KEY_ACTIVITY_END_DATE)));
            // Activity URL
            activity.setActivityUrl(result.getString(ParseConstants.KEY_ACTIVITY_URL));
            // Leader availability
            if (result.get(ParseConstants.KEY_AVAILABILITY_LEADER) != null) {
                activity.setAvailabilityLeader(result.getInt(ParseConstants.KEY_AVAILABILITY_LEADER));
            }
            // Participant availability
            activity.setAvailabilityParticipant
                    (result.getInt(ParseConstants.KEY_AVAILABILITY_PARTICIPANT));
            activity.setBranch(result.getString(ParseConstants.KEY_BRANCH));  // Branch
            // End latitude
            activity.setEndLatitude(result.getDouble(ParseConstants.KEY_END_LATITUDE));
            // End longitude
            activity.setEndLongitude(result.getDouble(ParseConstants.KEY_END_LONGITUDE));
            // Activity picture URL
            activity.setImageUrl(result.getString(ParseConstants.KEY_IMAGE_URL));
            activity.setLeaderName(result.getJSONArray(ParseConstants.KEY_LEADER_NAME));  // Leader name
            // Activity title
            activity.setTitle(result.getString(ParseConstants.KEY_ACTIVITY_TITLE));
            activity.setObjectID(result.getObjectId());  // Parse object ID
            // Registration open and close dates
            activity.setRegistrationOpenTime(DateUtil.convertFromUNC(result.getDate
                    (ParseConstants.KEY_REGISTRATION_OPEN_TIME)));
            activity.setRegistrationCloseTime(DateUtil.convertFromUNC(result.getDate
                    (ParseConstants.KEY_REGISTRATION_CLOSE_TIME)));
            // Start latitude
            activity.setStartLatitude(result.getDouble(ParseConstants.KEY_START_LATITUDE));
            // Start longitude
            activity.setStartLongitude(result.getDouble(ParseConstants.KEY_START_LONGITUDE));
            // Status
            activity.setStatus(result.getString(ParseConstants.KEY_STATUS));
            // Type
            activity.setType(result.getJSONArray(ParseConstants.KEY_TYPE));

            // Favorite lookup
            if (isFavorites) {
                // All activities are favorites as defined in the query to the Parse backend
                activity.setFavorite(true);  // Set favorite flag to true
            }
            else {  // Applies to Activity Search, Completed and Signed Up activities
                if (favoritesList != null) {
                    favoriteObjectIdPosition = favoritesList.indexOf(activity.getObjectID());

                    if (favoriteObjectIdPosition >= 0) {  // This activity is a favorite
                        activity.setFavorite(true);  // Set favorite flag to true
                        // Remove the favorite ObjectId from the list (for efficiency)
                        favoritesList.remove(favoriteObjectIdPosition);
                    }
                }
            }

            // Add special information for user history / future activities
            if (member != null) {
                if (type == ActivityType.COMPLETED) {  // Completed activities
                    for (int i = 0; i < member.getPastActivity()[0].length; i++) {
                        // Find the activity with the same URL
                        if (activity.getActivityUrl().equals(member.getPastActivity()[2][i])) {
                            activity.setUserRole(member.getPastActivity()[4][i]);  // Role
                            activity.setStatus(member.getPastActivity()[5][i]);  // Status
                            break;
                        }
                    }
                }
                else {  // Signed Up activities
                    for (int i = 0; i < member.getCurrentActivity()[0].length; i++) {
                        // Find the activity with the same URL
                        if (activity.getActivityUrl().equals(member.getCurrentActivity()[2][i])) {
                            activity.setUserRole(member.getCurrentActivity()[4][i]);  // Role
                            activity.setStatus(member.getCurrentActivity()[5][i]);  // Status
                            break;
                        }
                    }
                }

                // Flag as user activity
                activity.setUserActivity(true);
            }

            // Set filter flags
            // Audience
            activity.setAudienceAdults(result.getBoolean(ParseConstants.KEY_AUDIENCE_ADULTS));
            activity.setAudienceFamilies(result.getBoolean(ParseConstants.KEY_AUDIENCE_FAMILIES));
            activity.setAudienceRetired(result.getBoolean(ParseConstants.KEY_AUDIENCE_RETIRED_ROVERS));
            activity.setAudienceSingles(result.getBoolean(ParseConstants.KEY_AUDIENCE_SINGLES));
            activity.setAudience2030Somethings(result.getBoolean(ParseConstants.KEY_AUDIENCE_20_30_SOMETHINGS));
            activity.setAudienceYouth(result.getBoolean(ParseConstants.KEY_AUDIENCE_YOUTH));
            // Branch
            activity.setBranchTheMountaineers(result.getBoolean(ParseConstants.KEY_BRANCH_THE_MOUNTAINEERS));
            activity.setBranchBellingham(result.getBoolean(ParseConstants.KEY_BRANCH_BELLINGHAM));
            activity.setBranchEverett(result.getBoolean(ParseConstants.KEY_BRANCH_EVERETT));
            activity.setBranchFoothills(result.getBoolean(ParseConstants.KEY_BRANCH_FOOTHILLS));
            activity.setBranchKitsap(result.getBoolean(ParseConstants.KEY_BRANCH_KITSAP));
            activity.setBranchOlympia(result.getBoolean(ParseConstants.KEY_BRANCH_OLYMPIA));
            activity.setBranchOutdoorCenters(result.getBoolean(ParseConstants.KEY_BRANCH_OUTDOOR_CENTERS));
            activity.setBranchSeattle(result.getBoolean(ParseConstants.KEY_BRANCH_SEATTLE));
            activity.setBranchTacoma(result.getBoolean(ParseConstants.KEY_BRANCH_TACOMA));
            // Climbing
            activity.setClimbingBasicAlpine(result.getBoolean(ParseConstants.KEY_CLIMBING_BASIC_ALPINE));
            activity.setClimbingIntermediateAlpine(result.getBoolean(ParseConstants.KEY_CLIMBING_INTERMEDIATE_ALPINE));
            activity.setClimbingAidClimb(result.getBoolean(ParseConstants.KEY_CLIMBING_AID_CLIMB));
            activity.setClimbingRockClimb(result.getBoolean(ParseConstants.KEY_CLIMBING_ROCK_CLIMB));
            // Leader Rating
            activity.setRatingForBeginners(result.getBoolean(ParseConstants.KEY_RATING_FOR_BEGINNERS));
            activity.setRatingEasy(result.getBoolean(ParseConstants.KEY_RATING_EASY));
            activity.setRatingModerate(result.getBoolean(ParseConstants.KEY_RATING_MODERATE));
            activity.setRatingChallenging(result.getBoolean(ParseConstants.KEY_RATING_CHALLENGING));
            // Skiing
            activity.setSkiingCrossCountry(result.getBoolean(ParseConstants.KEY_SKIING_CROSS_COUNTRY));
            activity.setSkiingBackcountry(result.getBoolean(ParseConstants.KEY_SKIING_BACKCOUNTRY));
            activity.setSkiingGlacier(result.getBoolean(ParseConstants.KEY_SKIING_GLACIER));
            // Snowshoeing
            activity.setSnowshoeingBeginner(result.getBoolean(ParseConstants.KEY_SNOWSHOEING_BEGINNER));
            activity.setSnowshoeingBasic(result.getBoolean(ParseConstants.KEY_SNOWSHOEING_BASIC));
            activity.setSnowshoeingIntermediate(result.getBoolean(ParseConstants.KEY_SNOWSHOEING_INTERMEDIATE));
            // Type
            activity.setTypeAdventureClub(result.getBoolean(ParseConstants.KEY_TYPE_ADVENTURE_CLUB));
            activity.setTypeBackpacking(result.getBoolean(ParseConstants.KEY_TYPE_BACKPACKING));
            activity.setTypeClimbing(result.getBoolean(ParseConstants.KEY_TYPE_CLIMBING));
            activity.setTypeDayHiking(result.getBoolean(ParseConstants.KEY_TYPE_DAY_HIKING));
            activity.setTypeExplorers(result.getBoolean(ParseConstants.KEY_TYPE_EXPLORERS));
            activity.setTypeExploringNature(result.getBoolean(ParseConstants.KEY_TYPE_EXPLORING_NATURE));
            activity.setTypeGlobalAdventures(result.getBoolean(ParseConstants.KEY_TYPE_GLOBAL_ADVENTURES));
            activity.setTypeNavigation(result.getBoolean(ParseConstants.KEY_TYPE_NAVIGATION));
            activity.setTypePhotography(result.getBoolean(ParseConstants.KEY_TYPE_PHOTOGRAPHY));
            activity.setTypeSailing(result.getBoolean(ParseConstants.KEY_TYPE_SAILING));
            activity.setTypeScrambling(result.getBoolean(ParseConstants.KEY_TYPE_SCRAMBLING));
            activity.setTypeSeaKayaking(result.getBoolean(ParseConstants.KEY_TYPE_SEA_KAYAKING));
            activity.setTypeSkiingSnowboarding(result.getBoolean(ParseConstants.KEY_TYPE_SKIING_SNOWBOARDING));
            activity.setTypeSnowshoeing(result.getBoolean(ParseConstants.KEY_TYPE_SNOWSHOEING));
            activity.setTypeStewardship(result.getBoolean(ParseConstants.KEY_TYPE_STEWARDSHIP));
            activity.setTypeTrailRunning(result.getBoolean(ParseConstants.KEY_TYPE_TRAIL_RUNNING));
            activity.setTypeUrbanAdventure(result.getBoolean(ParseConstants.KEY_TYPE_URBAN_ADVENTURE));
            activity.setTypeYouth(result.getBoolean(ParseConstants.KEY_TYPE_YOUTH));

            // Canceled status
            if (activity.getStatus().toLowerCase().equals("canceled")) {
                activity.setCanceled(true);
            }

            activityList.add(activity);  // Add activity to list
        }

        return activityList;
    }
}