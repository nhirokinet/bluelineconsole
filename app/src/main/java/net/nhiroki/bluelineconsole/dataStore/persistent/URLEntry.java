package net.nhiroki.bluelineconsole.dataStore.persistent;

import android.content.Context;

import androidx.annotation.StringRes;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.lib.StringValidator;

public class URLEntry {
    public int id;
    public String name;
    public String display_name;
    public String url_base;
    public boolean has_query;

    public @StringRes int validate(Context context) {
        if (this.name.equals("")) {
            return R.string.error_invalid_command_name;
        }

        for (int i = 0; i < this.name.length(); ++i) {
            char c = this.name.charAt(i);
            if (c == ' ') {
                return R.string.error_invalid_command_name;
            }
        }

        if (this.display_name.equals("")) {
            return R.string.error_empty_display_name;
        }

        if (! StringValidator.isValidURLAccepted(url_base, true, context)) {
            return StringValidator.getPreferenceURLArbitrarySchemeAccepted(context) ? R.string.error_invalid_url_least_validation_for_web_arbitrary_schema
                                                                                    : R.string.error_invalid_url_least_validation_for_web;
        }

        return 0;
    }
}
