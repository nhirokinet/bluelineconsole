package net.nhiroki.bluelineconsole.applicationMain.theming;

import android.content.Context;
import android.preference.PreferenceManager;

import net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme.BlueLineConsoleDarkTheme;
import net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme.BlueLineConsoleDefaultTheme;
import net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme.BlueLineConsoleLightTheme;
import net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme.MarineTheme;
import net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme.OldComputerTheme;

import java.util.HashMap;
import java.util.Map;


public class AppThemeDirectory {
    public static final String PREF_NAME_THEME = "pref_appearance_theme";

    private static final AppTheme[] THEMES = {
            new BlueLineConsoleDefaultTheme(),
            new BlueLineConsoleLightTheme(),
            new BlueLineConsoleDarkTheme(),
            new MarineTheme(),
            new OldComputerTheme(),
    };

    private static Map<String, AppTheme> themeMap;


    public static CharSequence[] getThemePreferenceKeys() {
        CharSequence[] ret = new CharSequence[THEMES.length];
        for (int i = 0; i < THEMES.length; ++i) {
            ret[i] = THEMES[i].getThemeID();
        }
        return ret;
    }

    public static CharSequence[] getThemePreferenceTitles(Context context) {
        CharSequence[] ret = new CharSequence[THEMES.length];
        for (int i = 0; i < THEMES.length; ++i) {
            ret[i] = THEMES[i].getThemeTitle(context);
        }
        return ret;
    }

    public static AppTheme loadAppTheme(Context context) {
        String themeName = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_NAME_THEME, "");
        return loadAppTheme(themeName);
    }

    public static AppTheme loadAppTheme(String themeName) {
        if (themeMap == null) {
            themeMap = new HashMap<>();
            for (AppTheme theme: THEMES) {
                themeMap.put(theme.getThemeID(), theme);
            }
        }

        AppTheme ret = themeMap.get(themeName);
        if (ret != null) {
            return ret;
        }
        return THEMES[0];
    }
}
