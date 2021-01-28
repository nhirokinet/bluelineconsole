package net.nhiroki.bluelineconsole.commands.urls;

import java.util.Set;

public class WebSearchEngine {
    public WebSearchEngine(String id_for_preference_value, String name,String display_name, String display_name_locale_independent, String url_base, boolean has_query, boolean preset, boolean varies_with_locale, Set<String> disabledIds) {
        this.id_for_preference_value = id_for_preference_value;
        this.name = name;
        this.display_name = display_name;
        this.display_name_locale_independent = display_name_locale_independent;
        this.url_base = url_base;
        this.has_query = has_query;
        this.enabled = !disabledIds.contains(id_for_preference_value);
        this.preset = preset;
        this.varies_with_locale = varies_with_locale;
    }

    public final String id_for_preference_value;
    public final String name;
    public final String display_name;
    public final String display_name_locale_independent;
    public final String url_base;
    public final boolean has_query;
    public final boolean enabled;
    public final boolean preset;
    public final boolean varies_with_locale;
}
