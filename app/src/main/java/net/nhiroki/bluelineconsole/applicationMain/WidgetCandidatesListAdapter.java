package net.nhiroki.bluelineconsole.applicationMain;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.nhiroki.bluelineconsole.R;

import java.util.List;

public class WidgetCandidatesListAdapter extends ArrayAdapter<AppWidgetProviderInfo> {
    public WidgetCandidatesListAdapter(@NonNull Context context, int resource, @NonNull List<AppWidgetProviderInfo> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AppWidgetProviderInfo item = this.getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.preferences_widget_candidate_entry_view, parent, false);
        }

        ((TextView)convertView.findViewById(R.id.widgetEntryNameView)).setText(item.loadLabel(this.getContext().getPackageManager()));
        ((ImageView)convertView.findViewById(R.id.widgetEntryPreviewView)).setImageDrawable(item.loadPreviewImage(this.getContext(), DisplayMetrics.DENSITY_DEFAULT));

        return convertView;
    }

    @Override
    public boolean isEnabled (int position) {
        return true;
    }
}
