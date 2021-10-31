package net.nhiroki.bluelineconsole.applicationMain;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import android.widget.ListView;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.BuildConfig;
import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.commandSearchers.CommandSearchAggregator;
import net.nhiroki.bluelineconsole.dataStore.deviceLocal.WidgetsSetting;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

import static android.view.inputmethod.EditorInfo.IME_FLAG_FORCE_ASCII;


public class MainActivity extends BaseWindowActivity {
    private CandidateListAdapter _resultCandidateListAdapter;
    private CommandSearchAggregator _commandSearchAggregator = null;
    private ExecutorService _threadPool = null;

    public static final String PREF_KEY_MAIN_EDITTEXT_FLAG_FORCE_ASCII = "pref_mainedittext_flagforceascii";
    public static final String PREF_KEY_MAIN_EDITTEXT_HINT_LOCALE_ENGLISH = "pref_mainedittext_hint_locale_english";

    public static final int REQUEST_CODE_FOR_COMING_BACK = 1;

    private boolean _camebackFlag = false;

    private boolean showStartUpHelp = false;
    private boolean _migrationLostHappened = false;

    private boolean _homeItemExists = false;

    private final AppWidgetsHostManager appWidgetsHostManager = new AppWidgetsHostManager(this);

    private EditText mainInputText;
    private ListView candidateListView;

    private int resumeId = 0;


    public MainActivity() {
        super(R.layout.main_activity_body, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mainInputText = findViewById(R.id.mainInputText);

        this._migrationLostHappened = WidgetsSetting.migrationLostHappened(this);
        this.showStartUpHelp = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(StartUpHelpActivity.PREF_KEY_SHOW_STARTUP_HELP, true);


        this.setHeaderFooterTexts(getString(R.string.app_name), String.format(getString(R.string.displayedFullVersionString), BuildConfig.VERSION_NAME));

        AppNotification.update(this);

        this.candidateListView = findViewById(R.id.candidateListView);
        _resultCandidateListAdapter = new CandidateListAdapter(this, new ArrayList<CandidateEntry>(), candidateListView);

        candidateListView.setAdapter(_resultCandidateListAdapter);

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
        if (this._commandSearchAggregator != null) {
            this._commandSearchAggregator.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ++this.resumeId;

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
            this.finish();
            this.startActivity(new Intent(this, this.getClass()));
            return;
        }

        _threadPool = Executors.newSingleThreadExecutor();

        if (_commandSearchAggregator == null) {
            // first time after onCreate()
            _commandSearchAggregator = new CommandSearchAggregator(this);
            mainInputText.addTextChangedListener(new MainInputTextListener(mainInputText.getText()));

        } else {
            _commandSearchAggregator.refresh(this);
            if (! _camebackFlag) {
                mainInputText.setText("");

                if (!this._iAmHomeActivity) {
                    _resultCandidateListAdapter.clear();
                    _resultCandidateListAdapter.notifyDataSetChanged();
                }
            }
        }

        if (this.showStartUpHelp) {
            this.showStartUpHelp = false;
            this._camebackFlag = true;
            startActivityForResult(new Intent(MainActivity.this, StartUpHelpActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
            return;
        }

        if (this._migrationLostHappened) {
            this._migrationLostHappened = false;
            this._camebackFlag = true;
            startActivityForResult(new Intent(MainActivity.this, NotificationMigrationLostActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
            return;
        }

        final CharSequence currentMainInputTextContents = mainInputText.getText();
        if (this._camebackFlag || this._iAmHomeActivity || ! currentMainInputTextContents.toString().isEmpty()) {
            this.onCommandInput(currentMainInputTextContents);
        }

        this._camebackFlag = false;

        MainActivity.this.enableBaseWindowAnimation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this._camebackFlag = (requestCode == REQUEST_CODE_FOR_COMING_BACK) && (resultCode == RESULT_OK);
    }

    @Override
    protected void onPause() {
        ++this.resumeId;
        if (_threadPool != null) {
            _threadPool.shutdownNow();
        }
        super.onPause();
    }

    @Override
    protected void onHeightChange() {
        super.onHeightChange();
        this.setWholeLayout();
    }

    @Override
    protected void onStop() {
        this.appWidgetsHostManager.stopListening();
        super.onStop();
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
        if (_resultCandidateListAdapter.isEmpty()) {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(0, 0, 0, 0);
        } else {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(0, (int)(6 * getResources().getDisplayMetrics().density + 0.5), 0, 0);
        }

        final boolean contentFilled = !mainInputText.getText().toString().isEmpty() || this._homeItemExists;

        this.setWindowBoundarySize(contentFilled ? ROOT_WINDOW_FULL_WIDTH_IN_MOBILE : ROOT_WINDOW_ALWAYS_HORIZONTAL_MARGIN, 0);

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

    private void executeSearch(String query) {
        List<CandidateEntry> cands = new ArrayList<>();

        if (! query.isEmpty()) {
            cands.addAll(_commandSearchAggregator.searchCandidateEntries(query, MainActivity.this));
        }

        if (this._iAmHomeActivity && query.isEmpty()) {
            List<CandidateEntry> homeScreenEntries = _commandSearchAggregator.homeScreenDefaultCandidateEntries(this);
            cands.addAll(homeScreenEntries);
            this._homeItemExists = ! homeScreenEntries.isEmpty();
        }

        if (! query.isEmpty()) {
            cands.addAll(_commandSearchAggregator.searchCandidateEntriesForLast(query, this));
        }

        _resultCandidateListAdapter.clear();
        _resultCandidateListAdapter.addAll(cands);
        _resultCandidateListAdapter.notifyDataSetChanged();

        if (! cands.isEmpty()) {
            candidateListView.setSelection(0);
        }

        this.setWholeLayout();
    }

    private void onCommandInput(final CharSequence query) {
        if (_commandSearchAggregator.isPrepared() || (query.toString().isEmpty() && !this._iAmHomeActivity)) {
            findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.GONE);
            executeSearch(query.toString());

        } else {
            findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.VISIBLE);
            final int myResumeId = this.resumeId;

            _resultCandidateListAdapter.clear();
            _resultCandidateListAdapter.notifyDataSetChanged();

            _threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    _commandSearchAggregator.waitUntilPrepared();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (MainActivity.this.resumeId != myResumeId) {
                                // Already different session, canceling the operation.
                                return;
                            }
                            executeSearch(query.toString());
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
