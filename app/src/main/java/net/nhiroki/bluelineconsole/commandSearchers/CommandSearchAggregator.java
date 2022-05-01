package net.nhiroki.bluelineconsole.commandSearchers;

import android.content.Context;

import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.ApplicationCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.CalculatorCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.CalendarCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.ColorDisplayCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.ContactSearchCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.DateCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.HelpCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.NetUtilCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.PreferencesCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.SearchEngineCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.SearchEngineDefaultCommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.URICommandSearcher;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.WidgetCommandSearcher;
import net.nhiroki.bluelineconsole.dataStore.persistent.HomeScreenSetting;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.query.Query;
import net.nhiroki.bluelineconsole.query.QueryType;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommandSearchAggregator {
    private final List <CommandSearcher> commandSearcherList = new LinkedList<>();
    private final Map<QueryType, CommandSearcher> specialisedCommandSearchers = new EnumMap<>(QueryType.class);
    private final List <CommandSearcher> commandSearcherListAlwaysLast = new LinkedList<>();

    private AppWidgetsHostManager appWidgetsHostManager = null;


    public CommandSearchAggregator(Context context) {
        // Starting with specific string
        commandSearcherList.add(new HelpCommandSearcher());
        commandSearcherList.add(new PreferencesCommandSearcher());
        commandSearcherList.add(new DateCommandSearcher());
        commandSearcherList.add(new URICommandSearcher());
        commandSearcherList.add(new NetUtilCommandSearcher());
        commandSearcherList.add(new CalendarCommandSearcher());

        // Fully user-defined
        commandSearcherList.add(new WidgetCommandSearcher());

        // First character is limited
        commandSearcherList.add(new CalculatorCommandSearcher());
        commandSearcherList.add(new SearchEngineCommandSearcher(context));
        commandSearcherList.add(new ColorDisplayCommandSearcher());

        // Command searchers which may return tons candidate should comes to the last of "search result"
        commandSearcherList.add(new ContactSearchCommandSearcher());
        commandSearcherList.add(new ApplicationCommandSearcher());

        // This should be separately called and order does not matter, and results are placed at last.
        commandSearcherListAlwaysLast.add(new SearchEngineDefaultCommandSearcher(context));
        initSpecialised();
        refresh(context);
    }


    private void initSpecialised(){
        for (CommandSearcher searcher: commandSearcherList) {
            if(searcher.getSpecialisation() != QueryType.NONE){
                specialisedCommandSearchers.put(searcher.getSpecialisation(), searcher);
            }
        }
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
        this.appWidgetsHostManager = new AppWidgetsHostManager(context);
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

    public List<CandidateEntry> searchSpecialised(Query s, Context context){
        CommandSearcher commandSearcher = specialisedCommandSearchers.get(s.getType());
        if (commandSearcher != null){
            return commandSearcher.searchCandidateEntries(s.getText(), context);
        }
        return Collections.emptyList();
    }

    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List<CandidateEntry> candidates = new ArrayList<>();
        if (s.isEmpty()) {
            return candidates;
        }

        for (CommandSearcher cs : commandSearcherList) {
            candidates.addAll(cs.searchCandidateEntries(s, context));
        }

        return candidates;
    }

    public List<CandidateEntry> searchCandidateEntriesForLast(String s, Context context) {
        List<CandidateEntry> candidates = new ArrayList<>();
        if (s.isEmpty()) {
            return candidates;
        }

        for (CommandSearcher cs : commandSearcherListAlwaysLast) {
            candidates.addAll(cs.searchCandidateEntries(s, context));
        }

        return candidates;
    }

    public List<CandidateEntry> homeScreenDefaultCandidateEntries(Context context) {
        List<HomeScreenSetting.HomeScreenDefaultItem> homeScreenDefaultItemList = HomeScreenSetting.getInstance(context).getAllHomeScreenDefaultItems();

        List<CandidateEntry> ret = new ArrayList<>();

        List<AppWidgetsHostManager.HomeScreenWidgetViewItem> widgetInfoList = this.appWidgetsHostManager.createHomeScreenWidgets();

        int widgetInfoListId = 0;

        for (HomeScreenSetting.HomeScreenDefaultItem item: homeScreenDefaultItemList) {
            while (widgetInfoListId < widgetInfoList.size() && widgetInfoList.get(widgetInfoListId).afterDefaultItem < item.id) {
                ret.add(new WidgetCommandSearcher.WidgetCandidateEntry(widgetInfoList.get(widgetInfoListId).widgetView));
                ++widgetInfoListId;
            }

            ret.addAll(this.searchCandidateEntries(item.data, context));
        }

        while (widgetInfoListId < widgetInfoList.size()) {
            ret.add(new WidgetCommandSearcher.WidgetCandidateEntry(widgetInfoList.get(widgetInfoListId).widgetView));
            ++widgetInfoListId;
        }

        return ret;
    }
}

