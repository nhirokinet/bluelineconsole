package net.nhiroki.bluelineconsole.commandSearchers.eachSearcher;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.MainActivity;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;
import net.nhiroki.bluelineconsole.lib.StringValidator;

import java.util.ArrayList;
import java.util.List;

public class URICommandSearcher implements CommandSearcher {
    @Override
    public void refresh(Context context) {
    }

    @Override
    @NonNull
    public List<CandidateEntry> searchCandidateEntries(String query, Context context) {
        List<CandidateEntry> candidates = new ArrayList<>();

        if (StringValidator.isValidURLAccepted(query, false, context)) {
            candidates.add(new URLOpenCandidateEntry(query));
        }

        return candidates;
    }

    @Override
    public void close() {}

    @Override
    public void waitUntilPrepared() {}

    @Override
    public boolean isPrepared() {
        return true;
    }

    private static class URLOpenCandidateEntry implements CandidateEntry {
        final String url;

        URLOpenCandidateEntry(String url) {
            this.url = url;
        }

        @Override
        @NonNull
        public String getTitle() {
            return url;
        }

        @Override
        public boolean hasLongView() {
            return false;
        }

        @Override
        public View getView(MainActivity mainActivity) {
            return null;
        }

        @Override
        public EventLauncher getEventLauncher(final Context context) {
            return activity -> {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    activity.finish();
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, R.string.error_failure_could_not_open_url, Toast.LENGTH_LONG).show();
                }
            };
        }

        @Override
        public boolean hasEvent() {
            return true;
        }

        @Override
        public Drawable getIcon(Context context) {
            return null;
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
