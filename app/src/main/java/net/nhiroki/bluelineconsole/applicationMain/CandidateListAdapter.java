package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.List;

class CandidateListAdapter extends ArrayAdapter<CandidateEntry> {
    private final ListView listView;
    private int chosenNowExplicitly = CHOICE_NOT_SET_YET;

    private final BaseWindowActivity activity;

    private static final int CHOICE_NOT_SET_YET       = -1;
    private static final int CHOICE_UNAVAILABLE       = -2;
    private static final int CHOICE_KNOWN_BY_LISTVIEW = -3;

    CandidateListAdapter(BaseWindowActivity activity, List<CandidateEntry> objects, ListView listView) {
        super(activity, 0, objects);

        this.activity = activity;
        this.listView = listView;
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.candidateentryview, parent, false);
        }

        final CandidateEntry cand = this.getItem(position);

        final TextView titleTextView = convertView.findViewById(R.id.candidateTitleTextView);
        final ImageView iconView = convertView.findViewById(R.id.candidateIconView);
        final LinearLayout additionalLinearView = convertView.findViewById(R.id.candidatedetaillinearlistview);

        titleTextView.setText(cand.getTitle());

        additionalLinearView.removeAllViews();
        View detailView = cand.getView(convertView.getContext());
        if (detailView != null) {
            detailView.setClickable(false);
            additionalLinearView.addView(detailView);
        }

        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (detailView == null && !cand.isSubItem()) ? 24 : 18);
        final double pixelsPerSp = this.getContext().getResources().getDisplayMetrics().scaledDensity;
        if (cand.isSubItem()) {
            convertView.setPaddingRelative((int)(31.0 * pixelsPerSp), (int)(7.0 * pixelsPerSp),
                                           (int)(15.0 * pixelsPerSp), (int)(7.0 * pixelsPerSp));
        } else {
            convertView.setPaddingRelative((int)(15.0 * pixelsPerSp), (int)(7.0 * pixelsPerSp),
                                           (int)(15.0 * pixelsPerSp), (int)(7.0 * pixelsPerSp));
        }

        LinearLayout.LayoutParams iconLP = (LinearLayout.LayoutParams) iconView.getLayoutParams();
        iconLP.gravity = cand.hasLongView() ? Gravity.TOP : Gravity.CENTER_VERTICAL;
        iconView.setLayoutParams(iconLP);

        TypedValue selectedItemBackground = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.bluelineconsoleSelectedItemBackgroundColor, selectedItemBackground, true);
        convertView.setBackgroundColor((position == getChosenNowExplicitly())?  selectedItemBackground.data: Color.TRANSPARENT);

        if (cand.getIcon(getContext()) == null) {
            iconView.setImageResource(android.R.color.transparent);
            iconView.setVisibility(View.GONE);

        } else {
            iconView.setImageDrawable(cand.getIcon(getContext()));
            iconView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        this.chosenNowExplicitly = CHOICE_NOT_SET_YET;
        super.notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled (int position) {
        return position < getCount() && getItem(position).hasEvent();
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
