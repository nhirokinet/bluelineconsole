package net.nhiroki.bluelineconsole.commandSearchers.eachSearcher;

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
        List<CandidateEntry> cands = new ArrayList<>();

        WebSearchEngine searchEngine = this._searchEngineDB.getDefaultEngineByPreference(context);

        if (searchEngine != null) {
            cands.add(new SearchEngineCommandSearcher.SearchEngineCandidateEntry(context, query, searchEngine.display_name, searchEngine.url_base));
        }
        return cands;
    }
}
