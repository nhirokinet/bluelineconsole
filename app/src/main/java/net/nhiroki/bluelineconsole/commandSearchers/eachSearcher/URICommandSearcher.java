package net.nhiroki.bluelineconsole.commandSearchers.eachSearcher;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.List;

public class URICommandSearcher implements CommandSearcher {
    @Override
    public void refresh(Context context) {
    }

    @Override
    @NonNull
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        List<CandidateEntry> cands = new ArrayList<>();

        if (s.startsWith("http://") || s.startsWith("https://")) {
            cands.add(new URLOpenCandidateEntry(s));
        }

        return cands;
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
        public View getView(Context context) {
            return null;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return new EventLauncher() {
                @Override
                public void launch(BaseWindowActivity activity) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    activity.finish();
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
