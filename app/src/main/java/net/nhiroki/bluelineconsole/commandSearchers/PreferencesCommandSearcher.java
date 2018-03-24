package net.nhiroki.bluelineconsole.commandSearchers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.BuildConfig;
import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.MainActivity;
import net.nhiroki.bluelineconsole.applicationMain.PreferencesActivity;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.List;

public class PreferencesCommandSearcher implements CommandSearcher {
    private String commandString = "config";

    @Override
    public void refresh(Context context) {
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

    @Override
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List<CandidateEntry> cands = new ArrayList<>();

        if (s.toLowerCase().equals(commandString)) {
            cands.add(new PreferencesCandidateEntry());
        }

        return cands;
    }

    private class PreferencesCandidateEntry implements CandidateEntry {
        @Override
        public String getTitle() {
            return PreferencesCommandSearcher.this.commandString;
        }

        @Override
        public View getView(Context context) {
            TextView packageNameView = new TextView(context);
            packageNameView.setText(context.getString(R.string.summary_text_for_preferences_of_this_app));
            return packageNameView;
        }

        @Override
        public boolean hasLongView() {
            return false;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return new EventLauncher() {
                @Override
                public void launch(Activity activity) {
                    activity.startActivityForResult(new Intent(activity, PreferencesActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                }
            };
        }

        @Override
        public Drawable getIcon(Context context) {
            try {
                return context.getPackageManager().getApplicationIcon(BuildConfig.APPLICATION_ID);
            } catch (PackageManager.NameNotFoundException e) {
                // NameNotFoundException never happens as this function is just getting the icon for this app.
                throw new RuntimeException("NameNotFoundException when finding my icon.");
            }
        }

        @Override
        public boolean hasEvent() {
            return true;
        }
    }
}
