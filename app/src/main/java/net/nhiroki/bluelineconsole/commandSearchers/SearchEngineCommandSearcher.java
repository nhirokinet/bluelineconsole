package net.nhiroki.bluelineconsole.commandSearchers;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEngine;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEnginesDatabase;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.List;

public class SearchEngineCommandSearcher implements CommandSearcher {
    private final WebSearchEnginesDatabase _searchEngineDB;

    public SearchEngineCommandSearcher(Context context) {
        this._searchEngineDB = new WebSearchEnginesDatabase(context);
        this.refresh(context);
    }

    @Override
    public void refresh(Context context) {
        this._searchEngineDB.refresh(context);
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
    @NonNull
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List<CandidateEntry> cands = new ArrayList<>();

        if (s.contains(" ")){
            int split = s.indexOf(' ');
            String engine = s.substring(0, split).toLowerCase();
            String query = s.substring(split + 1);

            if (!engine.equals("")) {
                List<WebSearchEngine> searchEngines = this._searchEngineDB.searchEngineListByNameQuery(context, engine);

                for (WebSearchEngine e: searchEngines) {
                    cands.add(new SearchEngineCandidateEntry(context, query, e.display_name, e.url_base));
                }
            }

        } else {
            if (!s.equals("")) {
                List<WebSearchEngine> urls = this._searchEngineDB.searchStaticPageListByNameQuery(context, s);
                for (WebSearchEngine e: urls) {
                    cands.add(new StaticPageCandidateEntry(context, e.display_name, e.url_base));
                }
            }
        }
        return cands;
    }

    private static class StaticPageCandidateEntry implements CandidateEntry {
        private final String urlBase;
        private final String title;

        StaticPageCandidateEntry(Context context, String pageName, String urlBase) {
            this.urlBase = urlBase;
            this.title = String.format(context.getString(R.string.result_static_webpage_format), pageName);
        }

        @Override
        public boolean hasLongView() {
            return false;
        }

        @Override
        @NonNull
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
                public void launch(BaseWindowActivity activity) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlBase)));
                    activity.finish();
                }
            };
        }

        @Override
        public Drawable getIcon(Context context) {
            return ContextCompat.getDrawable(context, android.R.drawable.ic_menu_compass);
        }

        @Override
        public boolean hasEvent() {
            return true;
        }

        @Override
        public boolean isSubItem() {
            return false;
        }
    }

    private static class SearchEngineCandidateEntry implements CandidateEntry {
        private final String query;
        private final String urlBase;
        private final String title;

        SearchEngineCandidateEntry(Context context, String query, String engineName, String urlBase) {
            this.query = query;
            this.urlBase = urlBase;
            this.title = String.format(context.getString(R.string.result_search_query_on_engine_format), query, engineName);
        }

        @Override
        public boolean hasLongView() {
            return false;
        }

        @Override
        @NonNull
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
                public void launch(BaseWindowActivity activity) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlBase + Uri.encode(query))));
                    activity.finish();
                }
            };
        }

        @Override
        public Drawable getIcon(Context context) {
            return ContextCompat.getDrawable(context, android.R.drawable.ic_menu_search);
        }

        @Override
        public boolean hasEvent() {
            return true;
        }

        @Override
        public boolean isSubItem() {
            return false;
        }
    }
}
