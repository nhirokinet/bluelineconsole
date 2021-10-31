package net.nhiroki.bluelineconsole.commandSearchers.eachSearcher;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

import java.util.ArrayList;
import java.util.List;

public class WidgetCommandSearcher implements CommandSearcher {
    private AppWidgetsHostManager appWidgetsHostManager;

    private static class WidgetSupportingLinearLayout extends LinearLayout {
        private int currentWidth = 0;

        WidgetSupportingLinearLayout(Context context) {
            super(context);
        }

        // updateAppWidgetSize() call seems to be mandatory for some widgets.
        // To achieve this, know precise size on onSizeChanged and apply this.
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            if (w == 0 || h == 0) {
                return;
            }
            this.currentWidth = w - this.getPaddingLeft() - this.getPaddingRight();

            if (this.currentWidth <= 0) {
                return;
            }
            double density = this.getContext().getResources().getDisplayMetrics().density;

            for (int i = 0; i < this.getChildCount(); ++i) {
                try {
                    View child = this.getChildAt(i);
                    if (child instanceof AppWidgetHostView) {
                        int height = child.getLayoutParams().height;
                        ((AppWidgetHostView) child).updateAppWidgetSize(null, (int)(currentWidth / density), (int)(height / density), (int)(currentWidth / density), (int)(height / density));
                        child.invalidate();
                    }
                } catch (Exception e) {
                    // For case that children changes in another thread
                }
            }
        }

        @Override
        public void addView(View child) {
            super.addView(child);

            double density = this.getContext().getResources().getDisplayMetrics().density;

            if (currentWidth > 0 && child instanceof AppWidgetHostView) {
                int height = child.getLayoutParams().height;
                ((AppWidgetHostView) child).updateAppWidgetSize(null, (int)(currentWidth / density), (int)(height / density), (int)(currentWidth / density), (int)(height / density));
                child.invalidate();
            }
        }
    }

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
            WidgetSupportingLinearLayout linearLayout = new WidgetSupportingLinearLayout(widget.getContext());
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
