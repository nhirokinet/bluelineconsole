package net.nhiroki.bluelineconsole.dataStore.persistent;

import androidx.annotation.StringRes;

import net.nhiroki.bluelineconsole.R;

public class URLEntry {
    public int id;
    public String name;
    public String display_name;
    public String url_base;
    public boolean has_query;

    public @StringRes int validate() {
        if (this.name.equals("")) {
            return R.string.error_invalid_url_command_name;
        }

        for (int i = 0; i < this.name.length(); ++i) {
            char c = this.name.charAt(i);
            if (c == ' ') {
                return R.string.error_invalid_url_command_name;
            }
        }

        if (this.display_name.equals("")) {
            return R.string.error_empty_display_name;
        }

        if (!url_base.startsWith("https://") && !url_base.startsWith("http://")) {
            return R.string.error_invalid_url_least_validation_for_web;
        }
        int url_slash_count = 0;
        for (int i = 0; i < this.url_base.length(); ++i) {
            if (this.url_base.charAt(i) == '/') {
                ++url_slash_count;
            }
        }
        if (url_slash_count < 3) {
            return R.string.error_invalid_url_least_validation_for_web;
        }
        return 0;
    }
}
