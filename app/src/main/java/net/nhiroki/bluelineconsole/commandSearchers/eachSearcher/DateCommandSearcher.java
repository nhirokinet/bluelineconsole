package net.nhiroki.bluelineconsole.commandSearchers.eachSearcher;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextClock;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.MainActivity;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DateCommandSearcher implements CommandSearcher {
    @Override
    public void refresh(Context context) {
    }

    @Override
    @NonNull
    public List<CandidateEntry> searchCandidateEntries(String query, Context context) {
        List<CandidateEntry> candidates = new ArrayList<>();

        if (query.equalsIgnoreCase("date")) {
            candidates.add(new ClockCandidateEntry());
        }

        return candidates;
    }

    @Override
    public void close() {}

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void waitUntilPrepared() {}

    private static class ClockCandidateEntry implements CandidateEntry {
        @Override
        @NonNull
        public String getTitle() {
            return "date";
        }

        @Override
        public View getView(MainActivity mainActivity) {
            LinearLayout clockView = new LinearLayout(mainActivity);
            clockView.setOrientation(LinearLayout.VERTICAL);

            TypedValue baseTextColor = new TypedValue();
            mainActivity.getTheme().resolveAttribute(R.attr.bluelineconsoleBaseTextColor, baseTextColor, true);

            Locale locale = mainActivity.getResources().getConfiguration().locale;

            TextClock textDate = new TextClock(mainActivity){
                @Override
                protected void onDetachedFromWindow() {
                    super.onDetachedFromWindow();

                    // TODO: survey for detail (or possibly stop using TextClock before it: I want to make date command more flexible)
                    // Why? Without this hack, TextClock seems not to remove timer event after removed from ListView, which leads to crash.
                    // It happened on ADV with Nexus 5X, Android 8.0 with patch level 2017/11/5, with default configuration.
                    // It does not happen on ADV with Nexus 5X, Android 6.0 with patch level 2016/9/6.
                    //     (so no need to use onVisibilityAggregated, which was introduced on API level 24, in Android 6)
                    // It does not happen on BlackBerry KeyOne BBB100-6, Android 7.1 with patch level 2018/02/05.

                    if (Build.VERSION.SDK_INT >= 24) {
                        onVisibilityAggregated(false);
                    }
                }
            };
            textDate.setFormat12Hour(DateFormat.getBestDateTimePattern(locale, "yyyyMMddEEE"));
            textDate.setFormat24Hour(DateFormat.getBestDateTimePattern(locale, "yyyyMMddEEE"));
            textDate.setGravity(Gravity.START);
            textDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            textDate.setTextColor(baseTextColor.data);
            clockView.addView(textDate);

            TextClock textTime = new TextClock(mainActivity){
                @Override
                protected void onDetachedFromWindow() {
                    super.onDetachedFromWindow();

                    if (Build.VERSION.SDK_INT >= 24) {
                        onVisibilityAggregated(false);
                    }

                }
            };
            textTime.setFormat12Hour(DateFormat.getBestDateTimePattern(locale, "ahmmss"));
            textTime.setFormat24Hour(DateFormat.getBestDateTimePattern(locale, "HHmmss"));
            textTime.setGravity(Gravity.START);
            textTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
            textTime.setTextColor(baseTextColor.data);
            textTime.setPaddingRelative(
                    (int)(30 * mainActivity.getResources().getDisplayMetrics().scaledDensity + 0.5),
                    0, 0, 0
            );
            clockView.addView(textTime);

            return clockView;
        }

        @Override
        public boolean hasLongView() {
            return true;
        }

        @Override
        public boolean hasEvent() {
            return false;
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
        public boolean isSubItem() {
            return false;
        }

        @Override
        public boolean viewIsRecyclable() {
            return true;
        }
    }
}
