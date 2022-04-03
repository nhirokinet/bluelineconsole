package net.nhiroki.bluelineconsole.applicationMain.theming;

import android.content.Context;
import android.preference.PreferenceManager;

import net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme.BlueLineConsoleDarkTheme;
import net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme.BlueLineConsoleDefaultTheme;
import net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme.BlueLineConsoleLightTheme;
import net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme.MarineTheme;
import net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme.OldComputerTheme;

import java.util.HashSet;
import java.util.Set;


public class AppThemeDirectory {
    public static final String PREF_NAME_THEME = "pref_appearance_theme";

    private static final AppTheme[] themes = {
            new BlueLineConsoleDefaultTheme(),
            new BlueLineConsoleLightTheme(),
            new BlueLineConsoleDarkTheme(),
            new MarineTheme(),
            new OldComputerTheme(),
    };

    public static CharSequence[] getThemePreferenceKeys() {
        CharSequence[] ret = new CharSequence[themes.length];
        for (int i = 0; i < themes.length; ++i) {
            ret[i] = themes[i].getThemeID();
        }
        return ret;
    }

    public static CharSequence[] getThemePreferenceTitles(Context context) {
        CharSequence[] ret = new CharSequence[themes.length];
        for (int i = 0; i < themes.length; ++i) {
            ret[i] = themes[i].getThemeTitle(context);
        }
        return ret;
    }

    public static AppTheme loadAppTheme(Context context) {
        String themeName = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_NAME_THEME, themes[0].getThemeID());
        return loadAppTheme(themeName);
    }

    public static AppTheme loadAppTheme(String themeName) {
        for (AppTheme theme: themes) {
            if (theme.getThemeID().equals(themeName)) {
                return theme;
            }
        }
        return new BlueLineConsoleDefaultTheme();
    }

    public static void assertThemeIDDoesNotConflict() {
        Set <String> tmp = new HashSet<>();

        for (AppTheme appTheme: themes) {
            if (tmp.contains(appTheme.getThemeID())) {
                throw new RuntimeException("Theme ID conflicts");
            }
            tmp.add(appTheme.getThemeID());
        }
    }
}
