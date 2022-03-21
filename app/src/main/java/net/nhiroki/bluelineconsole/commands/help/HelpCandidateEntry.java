package net.nhiroki.bluelineconsole.commands.help;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.BuildConfig;
import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.MainActivity;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

public class HelpCandidateEntry implements CandidateEntry {
    @Override
    @NonNull
    public String getTitle() {
        return "help";
    }

    @SuppressLint({"RtlHardcoded", "SetTextI18n"})
    @Override
    public View getView(MainActivity mainActivity) {
        LinearLayout ret = new LinearLayout(mainActivity);
        ret.setOrientation(LinearLayout.VERTICAL);

        TypedValue baseTextColor = new TypedValue();
        mainActivity.getTheme().resolveAttribute(R.attr.bluelineconsoleBaseTextColor, baseTextColor, true);

        TextView versionView = new TextView(mainActivity);
        versionView.setText(mainActivity.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        versionView.setTypeface(Typeface.DEFAULT_BOLD);
        versionView.setTextSize(15);
        versionView.setTextColor(baseTextColor.data);
        ret.addView(versionView);

        String detailString = mainActivity.getString(R.string.app_help_text);
        TextView detailView = new TextView(mainActivity);
        detailView.setText(detailString);
        detailView.setTextSize(15);
        detailView.setTextColor(baseTextColor.data);
        detailView.setGravity(Gravity.LEFT); // Currently RTL not supported
        ret.addView(detailView);

        return ret;
    }

    @Override
    public EventLauncher getEventLauncher(Context context) {
        return null;
    }

    @Override
    public boolean hasEvent() {
        return false;
    }

    @Override
    public boolean hasLongView() {
        return true;
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
    public boolean isSubItem() {
        return false;
    }

    @Override
    public boolean viewIsRecyclable() {
        return true;
    }
}
