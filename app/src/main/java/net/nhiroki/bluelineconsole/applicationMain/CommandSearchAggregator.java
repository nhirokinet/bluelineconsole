package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Context;

import net.nhiroki.bluelineconsole.commandSearchers.ApplicationCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.CalculatorCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.DateCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.HelpCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.NetUtilCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.PreferencesCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.SearchEngineCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.SearchEngineDefaultCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.URICommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;

import java.util.List;
import java.util.ArrayList;

public class CommandSearchAggregator {
    private List <CommandSearcher> commandSearcherList;

    CommandSearchAggregator(Context context) {
        commandSearcherList = new ArrayList<>();

        // Starting with specific string
        commandSearcherList.add(new HelpCommandSearcher());
        commandSearcherList.add(new PreferencesCommandSearcher());
        commandSearcherList.add(new DateCommandSearcher());
        commandSearcherList.add(new URICommandSearcher());
        commandSearcherList.add(new NetUtilCommandSearcher());

        // First character is limited
        commandSearcherList.add(new CalculatorCommandSearcher());
        commandSearcherList.add(new SearchEngineCommandSearcher(context));

        // Command searchers which may return tons candidate should comes to the last of "search result"
        commandSearcherList.add(new ApplicationCommandSearcher(context));

        // Always add default search engine at the last
        commandSearcherList.add(new SearchEngineDefaultCommandSearcher(context));

        refresh(context);
    }

    public void close() {
        for (CommandSearcher cs : commandSearcherList) {
            cs.close();
        }
    }

    // May just start thread. In sequential execution it may be better to call this earlier.
    // Returns so early that users can wait.
    public void refresh(Context context) {
        for (CommandSearcher cs : commandSearcherList) {
            cs.refresh(context);
        }
    }

    public boolean isPrepared() {
        for (CommandSearcher cs : commandSearcherList) {
            if (!cs.isPrepared()) {
                return false;
            }
        }
        return true;
    }

    public void waitUntilPrepared() {
        for (CommandSearcher cs : commandSearcherList) {
            cs.waitUntilPrepared();
        }
    }

    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List<CandidateEntry> cands = new ArrayList<>();
        if (s.equals("")) {
            return cands;
        }

        for (CommandSearcher cs : commandSearcherList) {
            cands.addAll(cs.searchCandidateEntries(s, context));
        }

        return cands;
    }
}

