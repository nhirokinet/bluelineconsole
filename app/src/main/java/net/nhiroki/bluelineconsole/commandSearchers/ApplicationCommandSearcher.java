package net.nhiroki.bluelineconsole.commandSearchers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.commands.applications.ApplicationDatabase;
import net.nhiroki.bluelineconsole.data_store.cache.ApplicationInformation;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ApplicationCommandSearcher implements CommandSearcher {
    private ApplicationDatabase applicationDatabase;

    public ApplicationCommandSearcher(Context context) {
    }

    @Override
    public void refresh(Context context) {
        this.applicationDatabase = new ApplicationDatabase(context);
    }

    @Override
    public void close() {
        this.applicationDatabase.close();
    }

    @Override
    public boolean isPrepared() {
        return this.applicationDatabase.isPrepared();
    }

    @Override
    public void waitUntilPrepared() {
        this.applicationDatabase.waitUntilPrepared();
    }

    @Override
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List<CandidateEntry> cands = new ArrayList<>();

        List<Pair<Integer, CandidateEntry>> appcands = new ArrayList<Pair<Integer, CandidateEntry>>();
        for (ApplicationInformation applicationInformation : applicationDatabase.getApplicationInformationList()) {
            final String appLabel = applicationInformation.getLabel();
            final ApplicationInfo androidApplicationInfo = applicationDatabase.getAndroidApplicationInfo(applicationInformation.getPackageName());

            if (appLabel.toLowerCase().contains(s.toLowerCase())) {
                appcands.add(new Pair<Integer, CandidateEntry>(appLabel.toLowerCase().indexOf(s.toLowerCase()), new AppOpenCandidateEntry(context, applicationInformation, androidApplicationInfo, appLabel)));
                continue;
            }

            if (applicationInformation.getPackageName().toLowerCase().contains(s.toLowerCase())) {
                appcands.add(new Pair<Integer, CandidateEntry>(100000 + appLabel.toLowerCase().indexOf(s.toLowerCase()), new AppOpenCandidateEntry(context, applicationInformation, androidApplicationInfo, appLabel)));
                continue;
            }
        }

        Collections.sort(appcands, new Comparator<Pair<Integer, CandidateEntry>>() {
            @Override
            public int compare(Pair<Integer, CandidateEntry> o1, Pair<Integer, CandidateEntry> o2) {
                if (o1.first > o2.first) {
                    return 1;
                }
                if (o1.first < o2.first) {
                    return -1;
                }
                return 0;
            }
        });

        for (Pair<Integer, CandidateEntry> entry : appcands) {
            cands.add(entry.second);
        }

        return cands;
    }

    private class AppOpenCandidateEntry implements CandidateEntry {
        private ApplicationInformation applicationInformation;
        private ApplicationInfo androidApplicationInfo;
        private String title;
        private boolean displayPackagename;

        // Getting app title in Android is slow, so app title also should be given via constructor from cache.
        AppOpenCandidateEntry(Context context, ApplicationInformation applicationInformation, ApplicationInfo androidApplicationInfo, String appTitle) {
            this.applicationInformation = applicationInformation;
            this.androidApplicationInfo = androidApplicationInfo;
            this.title = appTitle;
            displayPackagename = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_apps_show_package_name", false);
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public View getView(Context context) {
            if(!displayPackagename) {
                return null;
            }

            String packageName = AppOpenCandidateEntry.this.applicationInformation.getPackageName();
            TextView packageNameView = new TextView(context);
            packageNameView.setText(packageName);
            return packageNameView;
        }

        @Override
        public boolean hasEvent() {
            return true;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return new EventLauncher() {
                @Override
                public void launch(Activity activity) {
                    String packageName = AppOpenCandidateEntry.this.applicationInformation.getPackageName();
                    Intent intent = activity.getPackageManager().getLaunchIntentForPackage(AppOpenCandidateEntry.this.applicationInformation.getPackageName());
                    if (intent == null) {
                        Toast.makeText(activity, String.format(activity.getString(R.string.notFoundOpeningApplicationWithClass), packageName), Toast.LENGTH_LONG).show();
                        return;
                    }
                    activity.startActivity(intent);
                    activity.finish();
                }
            };
        }

        @Override
        public boolean hasLongView() {
            return false;
        }

        @Override
        public Drawable getIcon(Context context) {
            return context.getPackageManager().getApplicationIcon(androidApplicationInfo);
        }
    }
}
