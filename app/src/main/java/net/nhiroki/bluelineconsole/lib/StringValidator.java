package net.nhiroki.bluelineconsole.lib;

import android.content.Context;

import androidx.preference.PreferenceManager;


public class StringValidator {
    private static final String PREF_URL_ACCEPT_ARBITRARY_SCHEME = "pref_url_arbitrary_scheme";


    public static boolean isValidURLAccepted(String url, boolean pathMustHaveStarted, Context context) {
        if (getPreferenceURLArbitrarySchemeAccepted(context)) {
            if (url.length() < 4) {
                // "a://" is the shortest accepted
                return false;
            }

            // Schema neme check
            // https://datatracker.ietf.org/doc/html/rfc3986#section-3.1
            if (! (('A' <= url.charAt(0) && url.charAt(0) <= 'Z' || 'a' <= url.charAt(0) && url.charAt(0) <= 'z'))) {
                return false;
            }
            int curPos;
            for (curPos = 1; curPos < url.length(); ++curPos) {
                if (! (('A' <= url.charAt(curPos) && url.charAt(curPos) <= 'Z') || ('a' <= url.charAt(curPos) && url.charAt(curPos) <= 'z') ||
                        ('0' <= url.charAt(curPos) && url.charAt(curPos) <= '9') || url.charAt(curPos) == '+' || url.charAt(curPos) == '-' || url.charAt(curPos) == '.')) {
                    break;
                }
            }
            if (curPos + 2 >= url.length()) {
                return false;
            }

            if (url.charAt(curPos) != ':' || url.charAt(curPos + 1) != '/' || url.charAt(curPos + 2) != '/') {
                return false;
            }

        } else {
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                return false;
            }

        }
        if (pathMustHaveStarted) {
            int url_slash_count = 0;
            for (int i = 0; i < url.length(); ++i) {
                if (url.charAt(i) == '/') {
                    ++url_slash_count;
                }
            }
            if (url_slash_count < 3) {
                return false;
            }
        }

        return true;
    }

    public static boolean getPreferenceURLArbitrarySchemeAccepted(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_URL_ACCEPT_ARBITRARY_SCHEME, false);
    }
}
