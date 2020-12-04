package net.nhiroki.bluelineconsole.commandSearchers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Pair;
import android.view.View;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SearchEngineCommandSearcher implements CommandSearcher {
    private static final Set <String> WIKIPEDIA_SUPPORTING_LANGS = new HashSet<>(
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
            // List is created on 2018/03/10. I picked up domains with an independent search engine page from https://www.yahoo.co.jp/everything/world/ .
            // I tried to write as correct title as possible mostly by reading title of top page and search result page.
            // If you find anything wrong in this list, your pull request is welcome.

            // qc and espanol must be selected by manual specification by caller

            // Québec state in Canada. Specify if locale is français (Canada) : French (Canada).
            put("qc", new Pair<String, String>("Yahoo", "https://qc.search.yahoo.com/search?p="));
            // Manually specify if locale is español (Estados Unidos) : Spanish (United States).
            put("espanol", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));

            // by country code
            put("AR", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("BR", new Pair<String, String>("Yahoo", "https://br.search.yahoo.com/search?p="));
            put("CA", new Pair<String, String>("Yahoo Canada", "https://ca.search.yahoo.com/search?p="));
            put("CL", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("DE", new Pair<String, String>("Yahoo", "https://de.search.yahoo.com/search?p="));
            put("FR", new Pair<String, String>("Yahoo", "https://fr.search.yahoo.com/search?p="));
            put("HK", new Pair<String, String>("Yahoo雅虎香港", "https://hk.search.yahoo.com/search?p="));
            put("ID", new Pair<String, String>("Yahoo", "https://id.search.yahoo.com/search?p="));
            put("IE", new Pair<String, String>("Yahoo", "https://uk.search.yahoo.com/search?p="));
            put("IN", new Pair<String, String>("Yahoo India", "https://in.search.yahoo.com/search?p="));
            put("IT", new Pair<String, String>("Yahoo Italia", "https://it.search.yahoo.com/search?p="));
            // Despite Yahoo! JAPAN is not listed on https://www.yahoo.com/everything/world/ , Yahoo! JAPAN has the license to use the name.
            // See: https://en.wikipedia.org/wiki/Yahoo%21_Japan (2018/03/10)
            put("JP", new Pair<String, String>("Yahoo! JAPAN", "https://search.yahoo.co.jp/search?p="));
            put("MX", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("MY", new Pair<String, String>("Yahoo Malaysia", "https://malaysia.search.yahoo.com/search?p="));
            put("PE", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("PH", new Pair<String, String>("Yahoo", "https://ph.search.yahoo.com/search?p="));
            put("SE", new Pair<String, String>("Yahoo", "https://se.search.yahoo.com/search?p="));
            put("SG", new Pair<String, String>("Yahoo", "https://sg.search.yahoo.com/search?p="));
            put("TW", new Pair<String, String>("Yahoo奇摩", "https://tw.search.yahoo.com/search?p="));
            put("UK", new Pair<String, String>("Yahoo", "https://uk.search.yahoo.com/search?p="));
            put("US", new Pair<String, String>("Yahoo", "https://search.yahoo.com/search?p="));
            put("VE", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("VN", new Pair<String, String>("Yahoo", "https://vn.search.yahoo.com/search?p="));
            put("ZA", new Pair<String, String>("Yahoo", "https://uk.search.yahoo.com/search?p="));
        }
    };

    @Override
    public void refresh(Context context) {

    }

    @Override
    public void close() {}

    @Override
    public void waitUntilPrepared() {}

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List<CandidateEntry> cands = new ArrayList<>();
        if (s.contains(" ")){
            Locale locale = context.getResources().getConfiguration().locale;

            int split = s.indexOf(' ');
            String engine = s.substring(0, split).toLowerCase();
            String query = s.substring(split + 1);

            if (!engine.equals("")) {
                if ("wikipedia".startsWith(engine)) {
                    String langCode = locale.getLanguage();
                    if (! WIKIPEDIA_SUPPORTING_LANGS.contains(langCode)) {
                        langCode = "en";
                    }
                    cands.add(new SearchEngineCandidateEntry(context, query, "Wikipedia (" + locale.getLanguage() + ")",
                            "https://" + locale.getLanguage() + ".wikipedia.org/wiki/"));
                    cands.add(new SearchEngineCandidateEntry(context, query, "Wikipedia (en)",
                            "https://en.wikipedia.org/wiki/"));
                }

                if ("duckduckgo".startsWith(engine)) {
                    cands.add(new SearchEngineCandidateEntry(context, query, "DuckDuckGo", "https://duckduckgo.com/?q="));
                }

                if ("qwant".startsWith(engine)) {
                    cands.add(new SearchEngineCandidateEntry(context, query, "Qwant", "https://www.qwant.com/?q="));
                }

                if ("metager".startsWith(engine)) {
                    cands.add(new SearchEngineCandidateEntry(context, query, "MetaGer", "https://metager.org/meta/meta.ger3?eingabe="));
                }

                if ("swisscows".startsWith(engine)) {
                    cands.add(new SearchEngineCandidateEntry(context, query, "Swisscows", "https://swisscows.com/web?query="));
                }

                if ("bing".startsWith(engine)) {
                    cands.add(new SearchEngineCandidateEntry(context, query, "Bing", "https://www.bing.com/search?q=" ));
                    cands.add(new SearchEngineCandidateEntry(context, query, "Bing (English, US)", "https://www.bing.com/search?setlang=en-us&q=" ));
                }

                if ("yahoo".startsWith(engine)) {
                    String countryCode = locale.getCountry();
                    String localeAsString = locale.toString();

                    if (localeAsString.equals("fr_CA")) {
                        countryCode = "qc";
                    }
                    if (localeAsString.equals("es_US")) {
                        countryCode = "espanol";
                    }


                    if (! YAHOO_SEARCH_IN_THE_WORLD.containsKey(countryCode)) {
                        countryCode = "US";
                    }
                    cands.add(new SearchEngineCandidateEntry(context, query, YAHOO_SEARCH_IN_THE_WORLD.get(countryCode).first, YAHOO_SEARCH_IN_THE_WORLD.get(countryCode).second));
                    cands.add(new SearchEngineCandidateEntry(context, query, "Yahoo (United States)", YAHOO_SEARCH_IN_THE_WORLD.get("US").second));
                }

                if ("google".startsWith(engine)) {
                    cands.add(new SearchEngineCandidateEntry(context, query, "Google", "https://www.google.com/search?q=" ));
                    cands.add(new SearchEngineCandidateEntry(context, query, "Google (English)", "https://www.google.com/search?hl=en&q=" ));
                }
            }
        }
        return cands;
    }

    private class SearchEngineCandidateEntry implements CandidateEntry {
        String query;
        String engineName;
        String urlBase;
        String title;

        SearchEngineCandidateEntry(Context context, String query, String engineName, String urlBase) {
            this.query = query;
            this.engineName = engineName;
            this.urlBase = urlBase;
            this.title = String.format(context.getString(R.string.formatSearchQueryOnEngine), query, engineName);
        }

        @Override
        public boolean hasLongView() {
            return false;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public View getView(Context context) {
            return null;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return new EventLauncher() {
                @Override
                public void launch(Activity activity) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlBase + Uri.encode(query))));
                    activity.finish();
                }
            };
        }

        @Override
        public Drawable getIcon(Context context) {
            return context.getDrawable(android.R.drawable.ic_menu_search);
        }

        @Override
        public boolean hasEvent() {
            return true;
        }
    }
}
