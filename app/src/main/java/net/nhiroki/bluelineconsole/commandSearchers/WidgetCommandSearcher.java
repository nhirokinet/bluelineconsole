package net.nhiroki.bluelineconsole.commandSearchers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.applicationMain.MainActivity;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

import java.util.ArrayList;
import java.util.List;

public class WidgetCommandSearcher implements CommandSearcher {
    private AppWidgetsHostManager appWidgetsHostManager;

    @Override
    public void refresh(Context context) {
        this.appWidgetsHostManager = new AppWidgetsHostManager(context);
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void waitUntilPrepared() {
    }

    @NonNull
    @Override
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List<CandidateEntry> ret = new ArrayList<>();

        for (View widget: this.appWidgetsHostManager.createWidgetsForCommand(s)) {
            ret.add(new WidgetCandidateEntry(widget));
        }

        return ret;
    }

    public static class WidgetCandidateEntry implements CandidateEntry {
        private final View widget;

        public WidgetCandidateEntry(View widget) {
            MainActivity.WidgetSupportingLinearLayout linearLayout = new MainActivity.WidgetSupportingLinearLayout(widget.getContext());
            linearLayout.setPadding(0, 0, 0, 0);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(widget);
            this.widget = linearLayout;
        }

        @NonNull
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public View getView(Context context) {
            return widget;
        }

        @Override
        public boolean hasLongView() {
            return true;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return null;
        }

        @Override
        public Drawable getIcon(Context context) {
            return null;
        }

        @Override
        public boolean hasEvent() {
            return false;
        }

        @Override
        public boolean isSubItem() {
            return false;
        }

        @Override
        public boolean viewIsRecyclable() {
            return false;
        }
    }
}
