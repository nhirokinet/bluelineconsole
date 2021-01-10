package net.nhiroki.bluelineconsole.commandSearchers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEngine;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEnginesDatabase;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchEngineCommandSearcher implements CommandSearcher {
    private WebSearchEnginesDatabase _searchEngineDB;

    public SearchEngineCommandSearcher(Context context) {
        this._searchEngineDB = new WebSearchEnginesDatabase(context);
        this.refresh(context);
    }

    @Override
    public void refresh(Context context) {
        this._searchEngineDB.refresh();
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
                List<WebSearchEngine> searchEngines = this._searchEngineDB.getEngineListByNameQuery(context, engine, locale);

                for (WebSearchEngine e: searchEngines) {
                    cands.add(new SearchEngineCandidateEntry(context, query, e.display_name, e.url_base));
                }
            }

        } else {
            if (!s.equals("")) {
                List<WebSearchEngine> urls = this._searchEngineDB.getStaticPageListByNameQuery(context, s);
                for (WebSearchEngine e: urls) {
                    cands.add(new StaticPageCandidateEntry(context, e.display_name, e.url_base));
                }
            }
        }
        return cands;
    }

    private class StaticPageCandidateEntry implements CandidateEntry {
        String pageName;
        String urlBase;
        String title;

        StaticPageCandidateEntry(Context context, String pageName, String urlBase) {
            this.pageName = pageName;
            this.urlBase = urlBase;
            this.title = String.format(context.getString(R.string.formatStaticPageEntry), pageName);
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
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlBase)));
                    activity.finish();
                }
            };
        }

        @Override
        public Drawable getIcon(Context context) {
            return context.getDrawable(android.R.drawable.ic_menu_compass);
        }

        @Override
        public boolean hasEvent() {
            return true;
        }
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
