package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CandidateListAdapter extends ArrayAdapter<CandidateEntry> {
    private final ListView listView;
    private int chosenNowExplicitly = CHOICE_NOT_SET_YET;

    private final MainActivity activity;

    private static final int CHOICE_NOT_SET_YET       = -1;
    private static final int CHOICE_UNAVAILABLE       = -2;
    private static final int CHOICE_KNOWN_BY_LISTVIEW = -3;

    private final Map<Integer, View> unrecyclableViews = new HashMap<>();
    private boolean shall_hide_icon = false ;
    CandidateListAdapter(MainActivity activity, List<CandidateEntry> objects, ListView listView) {
        super(activity, 0, objects);

        this.activity = activity;
        this.listView = listView;
        shall_hide_icon = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pref_appearance_hide_icon",false);
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.candidateentryview, parent, false);
        }

        final CandidateEntry candidate = this.getItem(position);
        if (! candidate.viewIsRecyclable() && this.unrecyclableViews.containsKey(position)) {
            return this.unrecyclableViews.get(position);
        }

        final Drawable icon = candidate.getIcon(getContext());
        final String title = candidate.getTitle();

        final TextView titleTextView = convertView.findViewById(R.id.candidateTitleTextView);
        final ImageView iconView = convertView.findViewById(R.id.candidateIconView);
        final LinearLayout additionalLinearView = convertView.findViewById(R.id.candidatedetaillinearlistview);
        final LinearLayout additionalLongLinearView = convertView.findViewById(R.id.candidatelongdetaillinearlistview);

        titleTextView.setText(title == null ? "" : title);
        titleTextView.setVisibility(title == null ? View.GONE : View.VISIBLE);

        additionalLinearView.removeAllViews();
        additionalLongLinearView.removeAllViews();
        View detailView = candidate.getView(this.activity);
        if (detailView != null) {
            detailView.setClickable(false);
            if (candidate.hasLongView()) {
                additionalLongLinearView.addView(detailView);
            } else {
                additionalLinearView.addView(detailView);
            }
        }

        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (detailView == null && !candidate.isSubItem()) ? 24 : 18);
        final double pixelsPerSp = this.getContext().getResources().getDisplayMetrics().scaledDensity;
        if (candidate.isSubItem()) {
            convertView.setPaddingRelative((int)(31.0 * pixelsPerSp), (int)(7.0 * pixelsPerSp),
                                           (int)(15.0 * pixelsPerSp), (int)(7.0 * pixelsPerSp));
        } else {
            convertView.setPaddingRelative((int)(15.0 * pixelsPerSp), (int)(7.0 * pixelsPerSp),
                                           (int)(15.0 * pixelsPerSp), (int)(7.0 * pixelsPerSp));
        }

        LinearLayout.LayoutParams iconLP = (LinearLayout.LayoutParams) iconView.getLayoutParams();
        iconLP.gravity = candidate.hasLongView() ? Gravity.TOP : Gravity.CENTER_VERTICAL;
        iconView.setLayoutParams(iconLP);

        TypedValue selectedItemBackground = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.bluelineconsoleSelectedItemBackgroundColor, selectedItemBackground, true);
        convertView.setBackgroundColor((position == getChosenNowExplicitly())?  selectedItemBackground.data: Color.TRANSPARENT);

        if (icon == null) {
            iconView.setImageResource(android.R.color.transparent);
            iconView.setVisibility(View.GONE);

        } else {
            iconView.setImageDrawable(icon);


            iconView.setVisibility(
                    shall_hide_icon ? View.GONE : View.VISIBLE
            );
        }

        if (! candidate.viewIsRecyclable()) {
            this.unrecyclableViews.put(position, convertView);
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        this.unrecyclableViews.clear();
        this.chosenNowExplicitly = CHOICE_NOT_SET_YET;
        super.notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled (int position) {
        return position < getCount() && getItem(position).hasEvent();
    }

    @Override
    public int getItemViewType(int position) {
        if (! this.getItem(position).viewIsRecyclable()) {
            // WidgetHost behavior differs against below:
            // return AdapterView.ITEM_VIEW_TYPE_IGNORE;
            return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
        }
        return super.getItemViewType(position);
    }

    public int getChosenNowExplicitly() {
        if (chosenNowExplicitly != CHOICE_NOT_SET_YET) {
            return chosenNowExplicitly;
        }

        for (int i = 0; i < this.getCount(); ++i) {
            if (this.getItem(i).hasEvent()) {
                this.chosenNowExplicitly = i;
                return i;
            }
        }
        this.chosenNowExplicitly = CHOICE_UNAVAILABLE;
        return chosenNowExplicitly;
    }

    public boolean selectChosenNowAsListView() {
        int choice = this.getChosenNowExplicitly();

        if (choice == CHOICE_UNAVAILABLE) {
            return false;
        }

        this.chosenNowExplicitly = CHOICE_KNOWN_BY_LISTVIEW;
        this.listView.setSelection(choice);
        return true;
    }

    public void invokeEvent(int position, Context context) {
        EventLauncher eventLauncher = this.getItem(position).getEventLauncher(context);

        if (eventLauncher != null) {
            eventLauncher.launch(activity);
        }
    }

    public void invokeFirstChoiceEvent(Context context) {
        if (getChosenNowExplicitly() >= 0) {
            invokeEvent(getChosenNowExplicitly(), context);
        }
    }
}
