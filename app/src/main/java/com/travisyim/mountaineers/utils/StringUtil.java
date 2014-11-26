package com.travisyim.mountaineers.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StringUtil {
    public static String[] getKeywords(String str) {
        Set<String> keywords = new HashSet<String>();
        Set<String> tempKeywords = new HashSet<String>();

        // Modify all query String occurrences of "'s" to "s" (i.e. drops the apostrophe) and "ê" tp "e"
        String modifiedStr = str.trim().toLowerCase().replace("'s", "s").replace("’s", "s").replace("ê", "e");

        // Add all keywords when the modified query String is split by spaces
        keywords.addAll(Arrays.asList(modifiedStr.split("\\s+")));
        // Add all keywords when the modified query String is split by spaces
        keywords.addAll(Arrays.asList(modifiedStr.split("\\b")));

        // Go through all keywords and remove any that are not of interest to the search
        for (Iterator<String> i = keywords.iterator(); i.hasNext();) {
            String queryKeyword = i.next();

            if (queryKeyword.trim().isEmpty()) {  // Empty String
                i.remove();
            }
            // Single non-alphanumeric character
            else if (queryKeyword.length() == 1 && queryKeyword.matches("^.*[^a-zA-Z0-9 ].*$")) {
                i.remove();
            }
            /* Ignore the left-overs from possessive forms, contractions and plurals (e.g. I'm,
             * don't, let's, you're, she'd, would've, she'll) */
            else if (queryKeyword.equals("m") || queryKeyword.equals("t") ||
                    queryKeyword.equals("s") || queryKeyword.equals("re") ||
                    queryKeyword.equals("d") || queryKeyword.equals("ve") ||
                    queryKeyword.equals("ll")) {
                i.remove();
            }
            // Ignore any of the following short words
            else if (queryKeyword.equals("the") || queryKeyword.equals("in") ||
                    queryKeyword.equals("and") || queryKeyword.equals("to") ||
                    queryKeyword.equals("of") || queryKeyword.equals("at") ||
                    queryKeyword.equals("for") || queryKeyword.equals("from") ||
                    queryKeyword.equals("a") || queryKeyword.equals("an")) {
                i.remove();
            }
        }

        // Add temp keywords to the original query keywords
        keywords.addAll(tempKeywords);

        // Return String array
        return keywords.toArray(new String[keywords.size()]);
    }
}