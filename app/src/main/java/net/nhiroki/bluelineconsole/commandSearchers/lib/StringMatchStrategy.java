package net.nhiroki.bluelineconsole.commandSearchers.lib;

import android.content.Context;
import android.preference.PreferenceManager;

import net.nhiroki.bluelineconsole.R;

public class StringMatchStrategy {
    public static final int SUBSTRING = 1;
    public static final int SKIPPED_SUBSTRING = 2;

    public static final int[] STRATEGY_LIST = new int[]{ SUBSTRING, SKIPPED_SUBSTRING };

    public static final String PREF_NAME = "pref_text_match_strategy";

    public static CharSequence getStrategyName(Context context, int strategy) {
        switch (strategy) {
            case SUBSTRING:
                return context.getString(R.string.pref_string_match_strategy_substring);

            case SKIPPED_SUBSTRING:
                return context.getString(R.string.pref_string_match_strategy_skipped_substring);

            default:
                throw new RuntimeException("Strategy not found");
        }
    }

    public static String getStrategyPrefValue(int strategy) {
        switch (strategy) {
            case SUBSTRING:
                return "substring";

            case SKIPPED_SUBSTRING:
                return "skipped_substring";

            default:
                throw new RuntimeException("Strategy not found");
        }
    }

    public static int getStrategyPreference(Context context) {
        String name = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_NAME, "substring");

        if (name.equals("substring")) {
            return SUBSTRING;
        }

        if (name.equals("skipped_substring")) {
            return SKIPPED_SUBSTRING;
        }

        return SUBSTRING; // default
    }

    /**
     * @return score that smaller is better, or -1 if not match
     */
    public static int match(Context context, String query, String target, boolean startingMustMatch) {
        final int strategy = getStrategyPreference(context);

        String query_lower = query.toLowerCase();
        String target_lower = target.toLowerCase();

        if (query_lower.length() == 0) {
            return -1;
        }

        if (strategy == SUBSTRING) {
            boolean ok = startingMustMatch ? target_lower.startsWith(query_lower)
                                           : target_lower.contains(query_lower);

            if (ok) {
                return target_lower.indexOf(query_lower);
            } else {
                return -1;
            }

        } else if(strategy == SKIPPED_SUBSTRING) {
            int query_cur = 0;
            int score = -1;

            if (startingMustMatch) {
                if (query_lower.charAt(0) != target_lower.charAt(0)) {
                    return -1;
                }
            }

            for (int target_cur = 0; target_cur < target_lower.length(); ++target_cur) {
                if (query_lower.charAt(query_cur) == target_lower.charAt(target_cur)) {
                    if (score == -1) {
                        score = target_cur;
                    }

                    ++query_cur;

                    if (query_cur == query_lower.length()) {
                        return score;
                    }
                }
            }
            return -1;

        } else {
            // Never happens
            throw new RuntimeException("Strategy not found");
        }
    }
}
