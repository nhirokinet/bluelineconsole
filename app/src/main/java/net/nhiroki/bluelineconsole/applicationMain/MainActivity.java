package net.nhiroki.bluelineconsole.applicationMain;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.BuildConfig;
import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.dataStore.deviceLocal.WidgetsSetting;
import net.nhiroki.bluelineconsole.dataStore.persistent.HomeScreenSetting;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

import static android.view.inputmethod.EditorInfo.IME_FLAG_FORCE_ASCII;

public class MainActivity extends BaseWindowActivity {
    private CandidateListAdapter _resultCandidateListAdapter;
    private CommandSearchAggregator _commandSearchAggregator;
    private ExecutorService _threadPool;

    public static final String PREF_KEY_MAIN_EDITTEXT_FLAG_FORCE_ASCII = "pref_mainedittext_flagforceascii";
    public static final String PREF_KEY_MAIN_EDITTEXT_HINT_LOCALE_ENGLISH = "pref_mainedittext_hint_locale_english";
    public static final int REQUEST_CODE_FOR_COMING_BACK = 1;

    private boolean _camebackFlag = false;
    private boolean _paused = false;

    private boolean _migrationLostHappened = false;

    private boolean _widgetExists = false;
    private boolean _homeItemExists = false;

    private WidgetSupportingLinearLayout linearLayoutForWidgets = null;
    private ListView.FixedViewInfo linearLayoutForWidgetsInfo = null;

    private final ArrayList<ListView.FixedViewInfo> headerViewInfos = new ArrayList<>();

    private final AppWidgetsHostManager appWidgetsHostManager = new AppWidgetsHostManager(this);
    public MainActivity() {
        super(R.layout.main_activity_body, true);
    }

    private static class WidgetSupportingLinearLayout extends LinearLayout {
        private int currentWidth = 0;

        public WidgetSupportingLinearLayout(Context context) {
            super(context);
        }

        // updateAppWidgetSize() call seems to be mandatory for some widgets.
        // To achieve this, know precise size on onSizeChanged and apply this.
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            this.currentWidth = w - this.getPaddingLeft() - this.getPaddingRight();

            double density = this.getContext().getResources().getDisplayMetrics().density;

            for (int i = 0; i < this.getChildCount(); ++i) {
                try {
                    View child = this.getChildAt(i);
                    if (child instanceof AppWidgetHostView) {
                        int height = child.getLayoutParams().height;
                        ((AppWidgetHostView) child).updateAppWidgetSize(null, (int)(currentWidth / density), (int)(height / density), (int)(currentWidth / density), (int)(height / density));
                    }
                } catch (Exception e) {

                }
            }
        }

        @Override
        public void addView(View child) {
            super.addView(child);

            double density = this.getContext().getResources().getDisplayMetrics().density;

            if (child instanceof AppWidgetHostView) {
                int height = child.getLayoutParams().height;
                ((AppWidgetHostView) child).updateAppWidgetSize(null, (int)(currentWidth / density), (int)(height / density), (int)(currentWidth / density), (int)(height / density));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this._migrationLostHappened = WidgetsSetting.migrationLostHappened(this);

        this.setHeaderFooterTexts(getString(R.string.app_name), String.format(getString(R.string.displayedFullVersionString), BuildConfig.VERSION_NAME));

        AppNotification.update(this);

        final ListView candidateListView = findViewById(R.id.candidateListView);
        _resultCandidateListAdapter = new CandidateListAdapter(this, new ArrayList<CandidateEntry>(), candidateListView);

        this.linearLayoutForWidgets = new WidgetSupportingLinearLayout(this);
        this.linearLayoutForWidgets.setPadding(0, 0, 0, 0);
        this.linearLayoutForWidgets.setOrientation(LinearLayout.VERTICAL);
        linearLayoutForWidgetsInfo = candidateListView.new FixedViewInfo();
        linearLayoutForWidgetsInfo.view = this.linearLayoutForWidgets;
        linearLayoutForWidgetsInfo.data = null;
        linearLayoutForWidgetsInfo.isSelectable = false;

        /* As of Android-30 SDK, ListView seems to occasionally crash when addHeaderView (or Possibly addFooterView) was called, adapter is set, and then the number of entries decreased.
         *
         * This is reported in StackOverflow on 2011/12/08 as a question, and workaround is suggested on 2012/03/19.
         *  https://stackoverflow.com/questions/8431342/listview-random-indexoutofboundsexception-on-froyo
         *
         * In this situation StackTrace is more similar to this one:
         *  https://stackoverflow.com/questions/13136166/i-am-getting-random-crash-on-my-listview-android-widget-headerviewlistadapter-is
         *
         * This workaround is written as vendor specific problem, but in my environment it also reproduces in Android Emulator.
         *
         * Reading the code of Android SDK-30, it seems that the following things happen:
         *  - When ListView.addHeaderView or ListView.addFooterView, ListView's internal ArrayAdapter is converted to HeaderViewListAdapter unless it is already instanceof HeaderViewListAdapter.
         *  - ListView.dispatchDraw(Canvas) calls mAdapter.isEnabled(int), which will crash when header is added.
         *  - For original ArrayAdapter, isEnabled(int position) simply returns true, regardless of position. This is just inherited from BaseAdapter.
         *  - For HeaderViewListAdapter, isEnabled(int position) lookup for position.
         *    - The occasional crashes occured here, by looking up footer info list, and which exceeds the size.
         *
         * Therefore this is likely to be a bug in SDK, and is likely to be caused that ListView.dispatchDraw lookups adapter's status for out-of-bound index,
         * but triggered by addHeaderView because original ArrayAdapter simply returns true for isEnabled(int position), even if position is out-of-bound.
         *
         * This workaround relies on the implementation that:
         *   - ListView.addHeaderView does not convert the adapter if it is already HeaderViewListAdapter
         *   - ListView.mHeaderViewInfos and HeaderViewListAdapter.mHeaderViewInfos may be different objects, and HeaderViewListAdapter.mHeaderViewInfos is actually used.
         */

        HeaderViewListAdapter internalHeaderAdapter = new HeaderViewListAdapter(headerViewInfos, null, _resultCandidateListAdapter) {
            @Override
            public boolean isEnabled(int position) {
                try {
                    return super.isEnabled(position);
                } catch (IndexOutOfBoundsException e) {
                    return false;
                }
            }
        };

        candidateListView.setAdapter(internalHeaderAdapter);

        /* In combination with ArrayAdapter and ListView, it seems to happen that ArrayAdapter.getView is called but just for measurement or something like that,
         * and result View is not used.
         * For widgets, for each AppWidgetId, only the AppWidgetHostView that is created last seems to work, and older one won't be updated.
         * So, if the AppWidgetHostView is created when ArrayAdapter.getView is called, but older AppWidgetHostView is actually displayed,
         * the widget get stuck.
         * For header, View can directly injected, so using this. For this reason, widgets always appears first.
         */
        ((ListView)findViewById(R.id.candidateListView)).addHeaderView(linearLayoutForWidgets);

        candidateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _resultCandidateListAdapter.invokeEvent(position, MainActivity.this);
            }
        });

        candidateListView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && v.onKeyDown(keyCode, event)) {
                    return true;
                }

                //noinspection RedundantIfStatement
                if (event.getAction() == KeyEvent.ACTION_UP && v.onKeyUp(keyCode, event)) {
                    return true;
                }

                return false;
            }
        });

        final EditText mainInputText = findViewById(R.id.mainInputText);
        if (Build.VERSION.SDK_INT >= 24) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_MAIN_EDITTEXT_HINT_LOCALE_ENGLISH, false)) {
                mainInputText.setImeHintLocales(new LocaleList(new Locale("en")));
            }
        }

        // flagForceAscii enabled at layout.
        // Disabling on layout and enabling here did not resolve my problem.
        // Also it seems to go around every languages even disabled here
        if (! PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_MAIN_EDITTEXT_FLAG_FORCE_ASCII, false)) {
            mainInputText.setImeOptions(mainInputText.getImeOptions() & ~IME_FLAG_FORCE_ASCII);
        }
        mainInputText.requestFocus();
        mainInputText.requestFocusFromTouch();

        mainInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (_resultCandidateListAdapter.isEmpty()) {
                    return false;
                }
                if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
                    _resultCandidateListAdapter.invokeFirstChoiceEvent(MainActivity.this);
                    return true;
                }
                return false;
            }
        });

        mainInputText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                    candidateListView.requestFocus();
                    candidateListView.requestFocusFromTouch();
                    return MainActivity.this._resultCandidateListAdapter.selectChosenNowAsListView() && candidateListView.onKeyDown(keyCode, event);
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        this._commandSearchAggregator.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this._paused = false;

        final EditText mainInputText = findViewById(R.id.mainInputText);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_MAIN_EDITTEXT_FLAG_FORCE_ASCII, false)) {
            mainInputText.setImeOptions(mainInputText.getImeOptions() | IME_FLAG_FORCE_ASCII);
        } else {
            mainInputText.setImeOptions(mainInputText.getImeOptions() & ~IME_FLAG_FORCE_ASCII);
        }

        if (Build.VERSION.SDK_INT >= 24) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_MAIN_EDITTEXT_HINT_LOCALE_ENGLISH, false)) {
                mainInputText.setImeHintLocales(new LocaleList(new Locale("en")));
            } else {
                mainInputText.setImeHintLocales(null);
            }
        }

        if (!this.getCurrentTheme().equals(this.readThemeFromConfig())) {
            Intent intent = this.getPackageManager().getLaunchIntentForPackage("net.nhiroki.bluelineconsole");
            this.finish();
            this.startActivity(intent);
        }
        _threadPool = Executors.newSingleThreadExecutor();

        final boolean showStartUpHelp = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(StartUpHelpActivity.PREF_KEY_SHOW_STARTUP_HELP, true);

        if (_commandSearchAggregator == null) {
            // first time after onCreate()
            _commandSearchAggregator = new CommandSearchAggregator(this);

            if (!_camebackFlag && showStartUpHelp) {
                MainActivity.this._camebackFlag = true;
                startActivityForResult(new Intent(MainActivity.this, StartUpHelpActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
            }
            if (_migrationLostHappened) {
                this._migrationLostHappened = false;
                MainActivity.this._camebackFlag = true;
                startActivityForResult(new Intent(MainActivity.this, NotificationMigrationLostActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
            }
            mainInputText.addTextChangedListener(new MainInputTextListener(mainInputText.getText()));

            if (this._iAmHomeActivity) {
                this.executeSearch(mainInputText.getText());
            }

        } else {
            if (_camebackFlag) {
                if (_migrationLostHappened) {
                    this._migrationLostHappened = false;
                    MainActivity.this._camebackFlag = true;
                    startActivityForResult(new Intent(MainActivity.this, NotificationMigrationLostActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                }
                List<CandidateEntry> cands = _commandSearchAggregator.searchCandidateEntries(mainInputText.getText().toString(), MainActivity.this);
                _commandSearchAggregator.refresh(this);

                _resultCandidateListAdapter.clear();
                _resultCandidateListAdapter.addAll(cands);
                _resultCandidateListAdapter.notifyDataSetChanged();

                _camebackFlag = false;

                _commandSearchAggregator.refresh(this);

            } else {
                if (!this._iAmHomeActivity) {
                    _commandSearchAggregator.refresh(this);

                    if (showStartUpHelp) {
                        MainActivity.this._camebackFlag = true;
                        startActivityForResult(new Intent(MainActivity.this, StartUpHelpActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                    }
                    if (_migrationLostHappened) {
                        this._migrationLostHappened = false;
                        MainActivity.this._camebackFlag = true;
                        startActivityForResult(new Intent(MainActivity.this, NotificationMigrationLostActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                    }
                    _resultCandidateListAdapter.clear();
                    _resultCandidateListAdapter.notifyDataSetChanged();

                    mainInputText.setText("");
                } else {
                    if (showStartUpHelp) {
                        MainActivity.this._camebackFlag = true;
                        startActivityForResult(new Intent(MainActivity.this, StartUpHelpActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                    }
                    if (_migrationLostHappened) {
                        this._migrationLostHappened = false;
                        MainActivity.this._camebackFlag = true;
                        startActivityForResult(new Intent(MainActivity.this, NotificationMigrationLostActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                    }
                    mainInputText.setText("");
                    this.executeSearch("");
                }
            }
        }

        MainActivity.this.enableBaseWindowAnimation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_COMING_BACK && resultCode == RESULT_OK) {
            _camebackFlag = true;
        }
        _camebackFlag = true;
    }

    @Override
    protected void onPause() {
        this.appWidgetsHostManager.stopListening();
        _threadPool.shutdownNow();
        this._paused = true;
        super.onPause();
    }

    @Override
    protected void onHeightChange() {
        super.onHeightChange();
        this.setWholeLayout();
    }

    @Override
    protected void enableWindowAnimationForElements() {
        super.enableWindowAnimationForElements();

        this.enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.mainRootLinearLayout));
        this.enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.mainInputTextWrapperLinearLayout));
        this.enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.candidateViewWrapperLinearLayout));
    }

    @Override
    protected void disableWindowAnimationForElements() {
        super.disableWindowAnimationForElements();

        this.disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.mainRootLinearLayout));
        this.disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.mainInputTextWrapperLinearLayout));
        this.disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.candidateViewWrapperLinearLayout));
    }

    private void setWholeLayout() {
        final EditText mainInputText = findViewById(R.id.mainInputText);
        final boolean contentFilled = !mainInputText.getText().toString().equals("") || this._widgetExists || this._homeItemExists;

        this.setWindowBoundarySize(contentFilled ? ROOT_WINDOW_FULL_WIDTH_IN_MOBILE : ROOT_WINDOW_ALWAYS_HORZONTAL_MARGIN, 0);

        this.setWindowLocationGravity(contentFilled ? Gravity.TOP : Gravity.CENTER_VERTICAL);

        final double pixelsPerSp = getResources().getDisplayMetrics().scaledDensity;

        // mainInputText: editTextSize * (1 (text) + 0.3 * 2 (padding)
        // If space is limited, split remaining height into 1(EditText):2(ListView and other margins)
        final double editTextSizeSp = Math.min(40.0, this.getWindowBodyAvailableHeight() / 4.8 / pixelsPerSp);
        mainInputText.setTextSize((int) editTextSizeSp);
        mainInputText.setPadding((int) (editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp));

        mainInputText.requestFocus();
        mainInputText.requestFocusFromTouch();
    }

    private void executeSearch(CharSequence s) {
        List<CandidateEntry> cands = _commandSearchAggregator.searchCandidateEntries(s.toString(), MainActivity.this);

        final EditText mainInputText = findViewById(R.id.mainInputText);

        this._widgetExists = false;
        linearLayoutForWidgets.removeAllViews();
        if (this._iAmHomeActivity && mainInputText.getText().toString().equals("")) {
            for (View widget: this.appWidgetsHostManager.createHomeScreenWidgets()) {
                this._widgetExists = true;
                linearLayoutForWidgets.addView(widget);
            }

            List<HomeScreenSetting.HomeScreenDefaultItem> homeScreenDefaultItemList = HomeScreenSetting.getInstance(this).getAllHomeScreenDefaultItems();

            this._homeItemExists = !homeScreenDefaultItemList.isEmpty();
            if (_commandSearchAggregator.isPrepared() || homeScreenDefaultItemList.isEmpty()) { // avoid waste waitUntilPrepared if already prepared
                findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.GONE);

                for (HomeScreenSetting.HomeScreenDefaultItem item: homeScreenDefaultItemList) {
                    cands.addAll(_commandSearchAggregator.searchCandidateEntries(item.data, MainActivity.this));
                    for (View widget: this.appWidgetsHostManager.createWidgetsForCommand(item.data)) {
                        this._widgetExists = true;
                        linearLayoutForWidgets.addView(widget);
                    }
                }


            } else {
                findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.VISIBLE);
                _threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        _commandSearchAggregator.waitUntilPrepared();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (MainActivity.this._paused) {
                                    // If activity is paused, doing something here is at least waste, sometimes dangerous
                                    return;
                                }
                                executeSearch("");
                                findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        }

        for (View widget: this.appWidgetsHostManager.createWidgetsForCommand(mainInputText.getText().toString())) {
            this._widgetExists = true;
            linearLayoutForWidgets.addView(widget);
        }

        if (this._widgetExists) {
            if (this.headerViewInfos.isEmpty()) {
                this.headerViewInfos.add(linearLayoutForWidgetsInfo);
            }
        } else {
            this.headerViewInfos.clear();
        }

        _resultCandidateListAdapter.clear();
        _resultCandidateListAdapter.addAll(cands);
        _resultCandidateListAdapter.notifyDataSetChanged();

        if (this._widgetExists) {
            final ListView candidateListView = findViewById(R.id.candidateListView);
            candidateListView.setSelection(0);
        }

        if (cands.isEmpty() && !this._widgetExists) {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(0, 0, 0, 0);
        } else {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(0, (int)(6 * getResources().getDisplayMetrics().density + 0.5), 0, 0);
        }

        setWholeLayout();
    }

    private void onCommandInput(final CharSequence s) {
        if (_commandSearchAggregator.isPrepared()) { // avoid waste waitUntilPrepared if already prepared
            findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.GONE);
            executeSearch(s);
        } else {
            findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.VISIBLE);
            _threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    _commandSearchAggregator.waitUntilPrepared();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (MainActivity.this._paused) {
                                // If activity is paused, doing something here is at least waste, sometimes dangerous
                                return;
                            }
                            executeSearch(s);
                            findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.GONE);
                        }
                    });
                }
            });
        }
    }

    private class MainInputTextListener implements TextWatcher {
        public MainInputTextListener(CharSequence s) {
            if(! s.toString().equals("")) {
                onCommandInput(s);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onCommandInput(s);
        }

        @Override
        public void afterTextChanged(Editable s) { }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    }
}
