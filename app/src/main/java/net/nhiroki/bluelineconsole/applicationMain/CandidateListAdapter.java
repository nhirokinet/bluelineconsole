package net.nhiroki.bluelineconsole.applicationMain;

import android.app.Activity;
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

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.List;

class CandidateListAdapter extends ArrayAdapter<CandidateEntry> {
    private ListView listView;
    private int firstChoice = CHOICE_NOT_SET_YET;

    private Activity activity;

    private static final int CHOICE_NOT_SET_YET = -1;
    private static final int CHOICE_UNAVAILABLE = -2;

    CandidateListAdapter(Activity activity, List<CandidateEntry> objects, ListView listView) {
        super(activity, 0, objects);

        this.activity = activity;
        this.listView = listView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.candidateentryview, null);
        }

        final CandidateEntry cand = this.getItem(position);

        final TextView titleTextView = (TextView) (convertView.findViewById(R.id.candidateTitleTextView));
        final ImageView iconView = (ImageView) convertView.findViewById(R.id.candidateIconView);
        final LinearLayout additionalLinearView = (LinearLayout) convertView.findViewById(R.id.candidatedetaillinearlistview);

        titleTextView.setText(cand.getTitle());

        additionalLinearView.removeAllViews();
        View detailView = cand.getView(convertView.getContext());
        if (detailView != null) {
            detailView.setClickable(false);
            additionalLinearView.addView(detailView);
        }

        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, detailView == null ? 24 : 18);

        LinearLayout.LayoutParams iconLP = (LinearLayout.LayoutParams) iconView.getLayoutParams();
        iconLP.gravity = cand.hasLongView() ? Gravity.TOP : Gravity.CENTER_VERTICAL;
        iconView.setLayoutParams(iconLP);

        // TODO: It's better to make all children transparent when CandidateListView is focused: in devices with cursor UP/DOWN key
        convertView.setBackgroundColor((position == getFirstChoice())? Color.LTGRAY : Color.TRANSPARENT);

        if (cand.getIcon(getContext()) == null) {
            convertView.findViewById(R.id.candidateIconView).setPaddingRelative(0, 0, 0, 0);

            iconView.setImageResource(android.R.color.transparent);
            iconView.setVisibility(View.GONE);
        } else {
            int iconViewPadding = (int)(10 * getContext().getResources().getDisplayMetrics().density + 0.5);

            convertView.findViewById(R.id.candidateIconView).setPaddingRelative(0, iconViewPadding, iconViewPadding / 4, iconViewPadding);

            iconView.setImageDrawable(cand.getIcon(getContext()));
            iconView.setVisibility(View.VISIBLE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CandidateListAdapter.this.invokeEvent(position, getContext());
            }
        });

        convertView.findViewById(R.id.candidateIconView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CandidateListAdapter.this.invokeEvent(position, getContext());
            }
        });

        convertView.findViewById(R.id.candidateIntermediateLinearLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CandidateListAdapter.this.invokeEvent(position, getContext());
            }
        });

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        this.firstChoice = CHOICE_NOT_SET_YET;
        super.notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled (int position) {
        return position < getCount() && getItem(position).hasEvent();
    }

    public int getFirstChoice() {
        if (firstChoice != CHOICE_NOT_SET_YET) {
            return firstChoice;
        }

        for (int i = 0; i < this.getCount(); ++i) {
            if (this.getItem(i).hasEvent()) {
                this.firstChoice = i;
                return i;
            }
        }
        this.firstChoice = CHOICE_UNAVAILABLE;
        return firstChoice;
    }

    private int getSecondChoice() {
        int first = getFirstChoice();
        if (getFirstChoice() < 0) {
            first = 0;
        }
        for (int i = first + 1; i < this.getCount(); ++i) {
            if (this.getItem(i).hasEvent()) {
                this.firstChoice = i;
                return i;
            }
        }

        return CHOICE_UNAVAILABLE;
    }

    public boolean selectSecondChoice() {
        int choice = getSecondChoice();

        if (choice == CHOICE_UNAVAILABLE) {
            return false;
        }

        this.listView.requestFocus();
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
        if (getFirstChoice() >= 0) {
            invokeEvent(getFirstChoice(), context);
        }
    }
}
