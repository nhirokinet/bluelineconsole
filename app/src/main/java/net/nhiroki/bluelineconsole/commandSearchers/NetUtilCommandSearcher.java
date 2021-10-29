package net.nhiroki.bluelineconsole.commandSearchers;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;
import net.nhiroki.bluelineconsole.applicationMain.MainActivity;
import net.nhiroki.bluelineconsole.commandSearchers.lib.StringMatchStrategy;
import net.nhiroki.bluelineconsole.commands.netutils.PingActivity;
import net.nhiroki.bluelineconsole.commands.netutils.Ping6Activity;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.List;

public class NetUtilCommandSearcher implements CommandSearcher {
    private final boolean _pingAvailable;
    private final boolean _ping6Available;

    public NetUtilCommandSearcher() {
        this._pingAvailable = PingActivity.commandAvailable();
        this._ping6Available = Ping6Activity.commandAvailable();
    }

    @Override
    public void refresh(Context context) {}

    @Override
    public void close() {}

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void waitUntilPrepared() {}

    @Override
    @NonNull
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List <CandidateEntry> ret = new ArrayList<>();

        if (s.contains(" ")){
            int split = s.indexOf(' ');
            String engine = s.substring(0, split).toLowerCase();
            String query = s.substring(split + 1);

            if (!query.contains(" ") && (query.length() == 0 || query.charAt(0) != '-')) {
                if (this._pingAvailable) {
                    if (StringMatchStrategy.match(context, engine, PingActivity.TARGET_COMMAND_SHORT, true) != -1) {
                        ret.add(new PingCandidateEntry(s.split(" ", 2)[1]));
                    }
                }
                if (this._ping6Available) {
                    if (StringMatchStrategy.match(context, engine, Ping6Activity.TARGET_COMMAND_SHORT, true) != -1) {
                        ret.add(new Ping6CandidateEntry(s.split(" ", 2)[1]));
                    }
                }
            }
        }
        return ret;
    }

    private static class PingCandidateEntry implements CandidateEntry {
        private final String _host;

        PingCandidateEntry(String host) {
            this._host = host;
        }

        @Override
        @NonNull
        public String getTitle() {
            return PingActivity.TARGET_COMMAND_SHORT + " " + this._host;
        }

        @Override
        public View getView(Context context) {
            return null;
        }

        @Override
        public boolean hasLongView() {
            return false;
        }

        @Override
        public EventLauncher getEventLauncher(final Context context) {
            return new EventLauncher() {
                @Override
                public void launch(BaseWindowActivity activity) {
                    Intent intent = new Intent(context, PingActivity.class);
                    intent.putExtra("host", PingCandidateEntry.this._host);
                    activity.startActivityForResult(intent, MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                }
            };
        }

        @Override
        public Drawable getIcon(Context context) {
            return null;
        }

        @Override
        public boolean hasEvent() {
            return !this._host.equals("");
        }

        @Override
        public boolean isSubItem() {
            return false;
        }

        @Override
        public boolean viewIsRecyclable() {
            return true;
        }
    }

    private static class Ping6CandidateEntry implements CandidateEntry {
        private final String _host;

        Ping6CandidateEntry(String host) {
            this._host = host;
        }

        @Override
        @NonNull
        public String getTitle() {
            return Ping6Activity.TARGET_COMMAND_SHORT + " " + this._host;
        }

        @Override
        public View getView(Context context) {
            return null;
        }

        @Override
        public boolean hasLongView() {
            return false;
        }

        @Override
        public EventLauncher getEventLauncher(final Context context) {
            return new EventLauncher() {
                @Override
                public void launch(BaseWindowActivity activity) {
                    Intent intent = new Intent(context, Ping6Activity.class);
                    intent.putExtra("host", Ping6CandidateEntry.this._host);
                    activity.startActivityForResult(intent, MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                }
            };
        }

        @Override
        public Drawable getIcon(Context context) {
            return null;
        }

        @Override
        public boolean hasEvent() {
            return !this._host.equals("");
        }

        @Override
        public boolean isSubItem() {
            return false;
        }

        @Override
        public boolean viewIsRecyclable() {
            return true;
        }
    }
}
