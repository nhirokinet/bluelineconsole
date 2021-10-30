package net.nhiroki.bluelineconsole.commandSearchers.eachSearcher;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.List;

public class ColorDisplayCommandSearcher implements CommandSearcher {
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

    private static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        return -1;
    }

    private static int[] getColorFromCode(String colorCode) {
        int[] ret = new int[3];

        for (int i = 0; i < 3; ++i) {
            int firstHex = hexCharToInt(colorCode.charAt(i * 2 + 1));
            if (firstHex < 0) {
                return null;
            }
            int secondHex = hexCharToInt(colorCode.charAt(i * 2 + 2));
            if (secondHex < 0) {
                return null;
            }

            ret[i] = firstHex * 16 + secondHex;
        }

        return ret;
    }

    @NonNull
    @Override
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        if (s.length() == 7 && s.charAt(0) == '#') {
            int[] color = getColorFromCode(s);

            if (color == null) {
                return new ArrayList<>();
            }

            List<CandidateEntry> ret = new ArrayList<>();
            ret.add(new ColorDisplayCandidateEntry(s, color));
            return ret;
        }
        return new ArrayList<>();
    }

    private class ColorDisplayCandidateEntry implements CandidateEntry {
        private String title;
        private int[] color;

        public ColorDisplayCandidateEntry(String title, int[] color) {
            this.title = title;
            this.color = color;
        }

        @NonNull
        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public View getView(Context context) {
            final double pxPerDp = context.getResources().getDisplayMetrics().density;

            LinearLayout ret = new LinearLayout(context);
            ret.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout colorShow = new LinearLayout(context);
            colorShow.setBackgroundColor((255 << 24) + (this.color[0] << 16) + (this.color[1] << 8) + this.color[2]);
            LinearLayout.LayoutParams layoutParamsForColorShow = (LinearLayout.LayoutParams) colorShow.getLayoutParams();
            if (layoutParamsForColorShow == null) {
                layoutParamsForColorShow = new LinearLayout.LayoutParams(0, 0);
            }
            int colorShowSize = (int) (96.0 * pxPerDp);
            int colorShowMarginEnd = (int) (8.0 * pxPerDp);

            layoutParamsForColorShow.height = colorShowSize;
            layoutParamsForColorShow.width = colorShowSize;
            layoutParamsForColorShow.setMarginEnd(colorShowMarginEnd);
            colorShow.setLayoutParams(layoutParamsForColorShow);

            ret.addView(colorShow);

            LinearLayout detail = new LinearLayout(context);
            detail.setOrientation(LinearLayout.VERTICAL);

            TextView textView = new TextView(context);
            textView.setText(context.getString(R.string.rgb_color_red) + ":" +  color[0] + ", " + context.getString(R.string.rgb_color_green) + ":" + color[1] + ", " + context.getString(R.string.rgb_color_blue) + ":" + color[2]);

            detail.addView(textView);

            ret.addView(detail);
            return ret;
        }

        @Override
        public boolean hasLongView() {
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
        public boolean hasEvent() {
            return false;
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
