package net.nhiroki.bluelineconsole.commandSearchers;

import android.content.Context;

import net.nhiroki.bluelineconsole.commands.help.HelpCandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;

import java.util.ArrayList;
import java.util.List;

public class HelpCommandSearcher implements CommandSearcher {
    @Override
    public void refresh(Context context) {

    }

    @Override
    public void close() {}

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void waitUntilPrepared() {}

    @Override
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List<CandidateEntry> cands = new ArrayList<>();

        if (s.toLowerCase().equals("help")) {
            cands.add(new HelpCandidateEntry());
        }

        return cands;
    }
}
