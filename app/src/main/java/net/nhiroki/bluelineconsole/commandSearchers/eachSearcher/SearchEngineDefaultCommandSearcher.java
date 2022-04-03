package net.nhiroki.bluelineconsole.commandSearchers.eachSearcher;

import android.content.Context;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.commands.urls.WebSearchEngine;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEnginesDatabase;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;

import java.util.ArrayList;
import java.util.List;

public class SearchEngineDefaultCommandSearcher implements CommandSearcher {
    private final WebSearchEnginesDatabase _searchEngineDB;

    public SearchEngineDefaultCommandSearcher(Context context) {
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
    public List<CandidateEntry> searchCandidateEntries(String query, Context context) {
        List<CandidateEntry> candidates = new ArrayList<>();

        WebSearchEngine searchEngine = this._searchEngineDB.getDefaultEngineByPreference(context);

        if (searchEngine != null) {
            candidates.add(new SearchEngineCommandSearcher.SearchEngineCandidateEntry(context, query, searchEngine.display_name, searchEngine.url_base));
        }
        return candidates;
    }
}
