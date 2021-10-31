package net.nhiroki.bluelineconsole.applicationMain.lib;

import static android.view.inputmethod.EditorInfo.IME_FLAG_FORCE_ASCII;

import android.content.Context;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.widget.EditText;

import java.util.Locale;


public class EditTextConfigurations {
    public static final String PREF_KEY_MAIN_EDITTEXT_FLAG_FORCE_ASCII = "pref_mainedittext_flagforceascii";
    public static final String PREF_KEY_MAIN_EDITTEXT_HINT_LOCALE_ENGLISH = "pref_mainedittext_hint_locale_english";


    public static void applyCommandEditTextConfigurations(EditText editText, Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_KEY_MAIN_EDITTEXT_FLAG_FORCE_ASCII, false)) {
            editText.setImeOptions(editText.getImeOptions() | IME_FLAG_FORCE_ASCII);
        } else {
            editText.setImeOptions(editText.getImeOptions() & ~IME_FLAG_FORCE_ASCII);
        }

        if (Build.VERSION.SDK_INT >= 24) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_KEY_MAIN_EDITTEXT_HINT_LOCALE_ENGLISH, false)) {
                editText.setImeHintLocales(new LocaleList(new Locale("en")));
            } else {
                editText.setImeHintLocales(null);
            }
        }
    }
}
