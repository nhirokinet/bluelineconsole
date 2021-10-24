package net.nhiroki.bluelineconsole.applicationMain;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.dataStore.persistent.HomeScreenSetting;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

import java.util.ArrayList;
import java.util.List;

public class PreferencesHomeScreenItemsActivity extends BaseWindowActivity {
    private AppWidgetsHostManager appWidgetsHostManager = null;
    private MyAdapter myAdapter;

    public PreferencesHomeScreenItemsActivity() {
        super(R.layout.preferences_home_screen_default_items, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.appWidgetsHostManager = new AppWidgetsHostManager(this);

        this.setHeaderFooterTexts(getString(R.string.preferences_title_for_header_home_screen_default_items), getString(R.string.preferences_title_for_footer_home_screen_default_items));
        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_IN_MOBILE, 2);

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();

        Button addQueryButton = findViewById(R.id.homeScreenAddQueryButton);
        addQueryButton.setOnClickListener(new View.OnClickListener(){
                                         @Override
                                         public void onClick(View v) {
                                             PreferencesHomeScreenItemsActivity.this.startActivity((new Intent(PreferencesHomeScreenItemsActivity.this, PreferencesHomeScreenEachDefaultItemCommandActivity.class)));
                                         }
                                     }
        );

        Button addWidgetButton = findViewById(R.id.homeScreenAddWidgetButton);
        addWidgetButton.setOnClickListener(new View.OnClickListener(){
                                         @Override
                                         public void onClick(View v) {
                                             PreferencesHomeScreenItemsActivity.this.startActivity(new Intent(PreferencesHomeScreenItemsActivity.this, PreferencesHomeScreenAddWidgetActivity.class));
                                         }
                                     }
        );

        ListView customListView = findViewById(R.id.homeScreenDefaultItemList);
        this.myAdapter = new MyAdapter(this);
        customListView.setAdapter(myAdapter);
        customListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (PreferencesHomeScreenItemsActivity.this.myAdapter.getItem(position).type == TYPE_WIDGET) {
                    Intent intent = new Intent(PreferencesHomeScreenItemsActivity.this, PreferencesHomeScreenEachWidgetActivity.class);
                    intent.putExtra(PreferencesHomeScreenEachWidgetActivity.INTENT_EXTRA_WIDGET_ID, PreferencesHomeScreenItemsActivity.this.myAdapter.getItem(position).homeScreenWidgetInfo.id);
                    PreferencesHomeScreenItemsActivity.this.startActivity(intent);
                } else if (PreferencesHomeScreenItemsActivity.this.myAdapter.getItem(position).type == TYPE_COMMAND) {
                    Intent intent = new Intent(PreferencesHomeScreenItemsActivity.this, PreferencesHomeScreenEachDefaultItemCommandActivity.class);
                    intent.putExtra(PreferencesHomeScreenEachDefaultItemCommandActivity.INTENT_EXTRA_ITEM_ID, PreferencesHomeScreenItemsActivity.this.myAdapter.getItem(position).homeScreenDefaultItem.id);
                    PreferencesHomeScreenItemsActivity.this.startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(true);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onStop() {
        super.originalOnStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.appWidgetsHostManager.garbageCollectForAppWidgetIds();

        this.myAdapter.clear();
        this.myAdapter.addAllWidgets(this.appWidgetsHostManager.fetchHomeScreenAppWidgets(), HomeScreenSetting.getInstance(this).getAllHomeScreenDefaultItems());
    }


    private static final int TYPE_WIDGET = 1;
    private static final int TYPE_COMMAND = 2;
    private static class ObjectOnList {
        public int type = 0;
        public AppWidgetsHostManager.HomeScreenWidgetInfo homeScreenWidgetInfo = null;
        public HomeScreenSetting.HomeScreenDefaultItem homeScreenDefaultItem = null;
    }

    private static class MyAdapter extends ArrayAdapter<ObjectOnList> {
        private final Context context;

        public MyAdapter(Context context) {
            super(context, 0, new ArrayList<ObjectOnList>());
            this.context = context;
        }

        @Override
        @NonNull
        public View getView(int position,  View convertView, @NonNull ViewGroup parent) {
            LinearLayout ret = new LinearLayout(context);
            ret.setOrientation(LinearLayout.VERTICAL);

            int verticalPadding = (int) (2 * this.context.getResources().getDisplayMetrics().density + 0.5);
            int horizontalPadding = (int) (10 * this.context.getResources().getDisplayMetrics().density + 0.5);
            ret.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

            TypedValue baseTextColor = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.bluelineconsoleBaseTextColor, baseTextColor, true);

            TextView titleView = new TextView(this.context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            int titleViewPadding = (int) (titleView.getTextSize() * 0.3);
            titleView.setPadding(titleViewPadding, titleViewPadding, titleViewPadding, titleViewPadding);
            titleView.setTextColor(baseTextColor.data);
            int type = this.getItem(position).type;
            titleView.setText(type == TYPE_WIDGET ? R.string.preferences_home_screen_widget_label:R.string.preferences_home_screen_command_label);
            ret.addView(titleView);


            if (this.getItem(position).type == TYPE_WIDGET) {
                TextView detailView = new TextView(this.context);
                detailView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                int detailViewPadding = (int) (detailView.getTextSize() * 0.3);
                detailView.setPadding(detailViewPadding, detailViewPadding, detailViewPadding, detailViewPadding);
                detailView.setTextColor(baseTextColor.data);
                if (this.getItem(position).homeScreenWidgetInfo.appWidgetProviderInfo != null) {
                    detailView.setText(this.getItem(position).homeScreenWidgetInfo.appWidgetProviderInfo.loadLabel(this.context.getPackageManager()));
                } else {
                    detailView.setText(this.context.getString(R.string.error_failure_could_not_connect_to_the_widget));
                }
                ret.addView(detailView);


            } else if (this.getItem(position).type == TYPE_COMMAND) {
                TextView detailView = new TextView(this.context);
                detailView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                int detailViewPadding = (int) (detailView.getTextSize() * 0.3);
                detailView.setPadding(detailViewPadding, detailViewPadding, detailViewPadding, detailViewPadding);
                detailView.setTextColor(baseTextColor.data);
                detailView.setText(this.getItem(position).homeScreenDefaultItem.data);
                ret.addView(detailView);
            }

            return ret;
        }

        public void addAllWidgets(List<AppWidgetsHostManager.HomeScreenWidgetInfo> allWidgets, List<HomeScreenSetting.HomeScreenDefaultItem> homeScreenDefaultItemList) {
            for (AppWidgetsHostManager.HomeScreenWidgetInfo widgetInfo: allWidgets) {
                ObjectOnList objectOnList = new ObjectOnList();
                objectOnList.type = TYPE_WIDGET;
                objectOnList.homeScreenWidgetInfo = widgetInfo;

                this.add(objectOnList);
            }

            for (HomeScreenSetting.HomeScreenDefaultItem item: homeScreenDefaultItemList) {
                ObjectOnList objectOnList = new ObjectOnList();
                objectOnList.type = TYPE_COMMAND;
                objectOnList.homeScreenDefaultItem = item;

                this.add(objectOnList);
            }
        }
    }
}
