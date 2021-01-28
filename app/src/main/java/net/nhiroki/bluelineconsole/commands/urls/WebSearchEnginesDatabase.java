package net.nhiroki.bluelineconsole.commands.urls;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Pair;

import net.nhiroki.bluelineconsole.commandSearchers.lib.StringMatchStrategy;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLEntry;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WebSearchEnginesDatabase {
    public static final String PREF_KEY_DEFAULT_SEARCH = "pref_default_search";
    public static final String PREF_KEY_DISABLED_URLS = "pref_url_disabled_ids";

    private static final Set<String> WIKIPEDIA_SUPPORTING_LANGS = new HashSet<>(
            Arrays.asList(
                    // List as of 2018/03/10, from https://www.wikipedia.org/
                    // TODO: processing domains with hyphen

                    // 1 000 000+ articles
                    "de", "en", "es", "fr", "it", "nl", "ja", "pl", "ru", "ceb", "sv", "vi", "war",

                    // 100 000+ articles
                    "ar", "az", "bg", "zh-min-nan", "be", "ca", "cs", "da", "et", "el", "eo", "eu", "fa", "gl", "ko",
                    "hy", "hi", "hr", "id", "he", "ka", "la", "lt", "hu", "ms", "min", "no", "ce", "uz", "pt", "kk",
                    "ro", "simple", "sk", "sl", "sr", "sh", "fi", "ta", "th", "tr", "uk", "ur", "vo", "zh",

                    // 10 000+ articles
                    "af", "als", "am", "an", "ast", "bn", "map-bms", "ba", "be-tarask", "bpy", "bar", "bs", "br", "cv",
                    "fo", "fy", "ga", "gd", "gu", "hsb", "io", "ilo", "ia", "os", "is", "jv", "kn", "ht", "ku", "ckb",
                    "ky", "mrj", "lv", "lb", "li", "lmo", "mai", "mk", "mg", "ml", "mr", "xmf", "arz", "mzn", "cdo", "mn",
                    "my", "new", "ne", "nap", "oc", "or", "pa", "pnb", "pms", "nds", "qu", "cy", "sa", "sah", "sco", "sq",
                    "scn", "si", "su", "sw", "tl", "tt", "te", "tg", "azb", "bug", "vec", "wa", "yi", "yo", "zh-yue", "bat-smg",

                    // 1 000+ articles
                    "ace", "kbd", "ang", "ab", "roa-rup", "frp", "arc", "gn", "av", "ay", "bjn", "bh", "bcl", "bi", "bo", "bxr",
                    "cbk-zam", "co", "za", "se", "pdc", "dv", "nv", "dsb", "eml", "myv", "ext", "hif", "fur", "gv", "gag", "ki",
                    "glk", "gan", "hak", "xal", "ha", "haw", "ig", "ie", "kl", "pam", "csb", "kw", "km", "rw", "kv", "kg", "gom",
                    "lo", "lad", "lbe", "lez", "lij", "ln", "jbo", "lrc", "lg", "mt", "zh-classical", "ty", "mi", "mwl", "mdf",
                    "nah", "na", "nds-nl", "frr", "nrm", "nov", "mhr", "as", "pi", "pag", "pap", "ps", "koi", "pfl", "pcd",
                    "krc", "kaa", "crh", "ksh", "rm", "rue", "sc", "stq", "nso", "sn", "sd", "szl", "so", "srn", "kab", "roa-tara",
                    "tet", "tpi", "to", "tk", "tyv", "udm", "ug", "vep", "fiu-vro", "vls", "wo", "wuu", "diq", "zea",

                    // 100+ articles
                    "ak", "bm", "ch", "ny", "ee", "ff", "got", "iu", "ik", "ks", "ltg", "fj", "cr", "pih", "om", "pnt", "dz", "rmy",
                    "rn", "sm", "sg", "st", "tn", "cu", "ss", "ti", "chr", "chy", "ve", "ts", "tum", "tw", "xh", "zu"
            )
    );

    // key -> pair(engine name, search base URL)
    // Most keys are country code, but some exceptions exist.
    private static final Map<String, Pair<String, String>> YAHOO_SEARCH_IN_THE_WORLD = new HashMap<String, Pair<String, String>>() {
        {
            // List is created on 2018/03/10. I picked up domains with an independent search engine page from https://www.yahoo.com/everything/world/ .
            // I tried to write as correct title as possible mostly by reading title of top page and search result page.
            // If you find anything wrong in this list, your pull request is welcome.

            // qc and espanol must be selected by manual specification by caller

            // Québec state in Canada. Specify if locale is français (Canada) : French (Canada).
            put("qc", new Pair<>("Yahoo", "https://qc.search.yahoo.com/search?p="));
            // Manually specify if locale is español (Estados Unidos) : Spanish (United States).
            put("espanol", new Pair<>("Yahoo", "https://espanol.search.yahoo.com/search?p="));

            // by country code
            put("AR", new Pair<>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("BR", new Pair<>("Yahoo", "https://br.search.yahoo.com/search?p="));
            put("CA", new Pair<>("Yahoo Canada", "https://ca.search.yahoo.com/search?p="));
            put("CL", new Pair<>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("DE", new Pair<>("Yahoo", "https://de.search.yahoo.com/search?p="));
            put("FR", new Pair<>("Yahoo", "https://fr.search.yahoo.com/search?p="));
            put("HK", new Pair<>("Yahoo雅虎香港", "https://hk.search.yahoo.com/search?p="));
            put("ID", new Pair<>("Yahoo", "https://id.search.yahoo.com/search?p="));
            put("IE", new Pair<>("Yahoo", "https://uk.search.yahoo.com/search?p="));
            put("IN", new Pair<>("Yahoo India", "https://in.search.yahoo.com/search?p="));
            put("IT", new Pair<>("Yahoo Italia", "https://it.search.yahoo.com/search?p="));
            // Despite Yahoo! JAPAN is not listed on https://www.yahoo.com/everything/world/ , Yahoo! JAPAN has the license to use the name.
            // See: https://en.wikipedia.org/wiki/Yahoo%21_Japan (2018/03/10)
            put("JP", new Pair<>("Yahoo! JAPAN", "https://search.yahoo.co.jp/search?p="));
            put("MX", new Pair<>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("MY", new Pair<>("Yahoo Malaysia", "https://malaysia.search.yahoo.com/search?p="));
            put("PE", new Pair<>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("PH", new Pair<>("Yahoo", "https://ph.search.yahoo.com/search?p="));
            put("SE", new Pair<>("Yahoo", "https://se.search.yahoo.com/search?p="));
            put("SG", new Pair<>("Yahoo", "https://sg.search.yahoo.com/search?p="));
            put("TW", new Pair<>("Yahoo奇摩", "https://tw.search.yahoo.com/search?p="));
            put("UK", new Pair<>("Yahoo", "https://uk.search.yahoo.com/search?p="));
            put("US", new Pair<>("Yahoo", "https://search.yahoo.com/search?p="));
            put("VE", new Pair<>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("VN", new Pair<>("Yahoo", "https://vn.search.yahoo.com/search?p="));
            put("ZA", new Pair<>("Yahoo", "https://uk.search.yahoo.com/search?p="));
        }
    };

    private List<URLEntry> _customEntries = new ArrayList<>();
    private Set<String> _disabledURLIDs = new HashSet<>();

    public WebSearchEnginesDatabase(Context context) {
        this.refresh(context);
    }

    public void refresh(Context context) {
        this._customEntries = URLPreferences.getInstance(context).getAllEntries();
        this._disabledURLIDs = this.getDisabledEntries(context);
    }

    // allow_duplicate is for the case of referencing to the entry selected before changing locale
    public List<WebSearchEngine> getURLListForLocale(final Locale locale, final boolean allow_duplicate) {
        List<WebSearchEngine> ret = new ArrayList<>();

        for (URLEntry e: this._customEntries) {
            ret.add(new WebSearchEngine("custom-web-" + e.id, e.name, e.display_name, e.display_name, e.url_base, e.has_query, false, false, this._disabledURLIDs));
        }

        String wikipediaLangCode = locale.getLanguage();
        if (!WIKIPEDIA_SUPPORTING_LANGS.contains(wikipediaLangCode)) {
            wikipediaLangCode = "en";
        }
        ret.add(new WebSearchEngine("default-web-wikipedia", "wikipedia", "Wikipedia (" + wikipediaLangCode + ")", "Wikipedia", "https://" + wikipediaLangCode + ".wikipedia.org/wiki/", true, true, true, this._disabledURLIDs));
        if (allow_duplicate || !wikipediaLangCode.equals("en")) {
            ret.add(new WebSearchEngine("default-web-wikipedia-en", "wikipedia", "Wikipedia (en)", "Wikipedia (English)", "https://en.wikipedia.org/wiki/", true, true, false, this._disabledURLIDs));
        }

        ret.add(new WebSearchEngine("default-web-duckduckgo", "duckduckgo", "DuckDuckGo", "DuckDuckGo", "https://duckduckgo.com/?q=", true, true, false, this._disabledURLIDs));

        ret.add(new WebSearchEngine("default-web-bing", "bing", "Bing", "Bing", "https://www.bing.com/search?q=", true, true, false, this._disabledURLIDs));
        ret.add(new WebSearchEngine("default-web-bing-en", "bing", "Bing (English)", "Bing (English)", "https://www.bing.com/search?setlang=en-us&q=", true, true, false, this._disabledURLIDs));

        String yahooCountryCode = locale.getCountry();
        final String localeAsString = locale.toString();

        if (localeAsString.equals("fr_CA")) {
            yahooCountryCode = "qc";
        }
        if (localeAsString.equals("es_US")) {
            yahooCountryCode = "espanol";
        }

        if (!YAHOO_SEARCH_IN_THE_WORLD.containsKey(yahooCountryCode)) {
            yahooCountryCode = "US";
        }
        ret.add(new WebSearchEngine("default-web-yahoo", "yahoo", YAHOO_SEARCH_IN_THE_WORLD.get(yahooCountryCode).first, "Yahoo", YAHOO_SEARCH_IN_THE_WORLD.get(yahooCountryCode).second, true, true, true, this._disabledURLIDs));
        if (allow_duplicate || ! yahooCountryCode.equals("US")) {
            ret.add(new WebSearchEngine("default-web-yahoo-en-us", "yahoo", "Yahoo (United States)", "Yahoo (English, US)", YAHOO_SEARCH_IN_THE_WORLD.get("US").second, true, true, false, this._disabledURLIDs));
        }

        ret.add(new WebSearchEngine("default-web-google", "google", "Google", "Google", "https://www.google.com/search?q=", true, true, false, this._disabledURLIDs));
        ret.add(new WebSearchEngine("default-web-google-en", "google", "Google (English)", "Google (English)", "https://www.google.com/search?hl=en&q=", true, true, false, this._disabledURLIDs));

        return ret;
    }

    public WebSearchEngine getURLByIdForPreferences(String pref_str, Locale locale) {
        for (WebSearchEngine e: this.getURLListForLocale(locale, true)) {
            if (e.id_for_preference_value.equals(pref_str)) {
                return e;
            }
        }

        return null;
    }

    public void setEntryEnabledById(Context context, String entry_id, boolean enabled) {
        Set<String> val = this.getDisabledEntries(context);
        if (enabled) {
            val.remove(entry_id);
        } else {
            val.add(entry_id);
        }

        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEdit.putStringSet(PREF_KEY_DISABLED_URLS, val);
        prefEdit.apply();
    }

    public void unsetEntryEnabledById(Context context, String entry_id) {
        Set<String> val = this.getDisabledEntries(context);
        val.remove(entry_id);

        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEdit.putStringSet(PREF_KEY_DISABLED_URLS, val);
        prefEdit.apply();
    }

    public Set<String> getDisabledEntries(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(PREF_KEY_DISABLED_URLS, new HashSet<String>());
    }


    public WebSearchEngine getDefaultEngineByPreference(Context context) {
        final String pref_str = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_KEY_DEFAULT_SEARCH, "none");
        final Locale locale = context.getResources().getConfiguration().locale;

        if (pref_str == null) {
            return null;
        }

        if (pref_str.equals("none")) {
            return null;
        }

        for (WebSearchEngine e: this.getURLListForLocale(locale, true)) {
            if (e.id_for_preference_value.equals(pref_str) && e.has_query) {
                return e;
            }
        }

        return null;
    }

    public List<WebSearchEngine> searchEngineListByNameQuery(Context context, String engine) {
        List<WebSearchEngine> ret = new ArrayList<>();

        for (WebSearchEngine e: this.getURLListForLocale(context.getResources().getConfiguration().locale, false)) {
            if (e.enabled && e.has_query && StringMatchStrategy.match(context, engine, e.name, true) != -1) {
                ret.add(e);
            }
        }

        return ret;
    }

    public List<WebSearchEngine> searchStaticPageListByNameQuery(Context context, String query) {
        List<WebSearchEngine> ret = new ArrayList<>();

        for (WebSearchEngine e: this.getURLListForLocale(context.getResources().getConfiguration().locale, false)) {
            if (e.enabled && !e.has_query && StringMatchStrategy.match(context, query, e.name, true) != -1) {
                ret.add(e);
            }
        }

        return ret;
    }
}