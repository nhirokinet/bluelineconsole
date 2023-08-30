package net.nhiroki.bluelineconsole.applicationMain;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import net.nhiroki.bluelineconsole.BuildConfig;
import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.lib.EditTextConfigurations;
import net.nhiroki.bluelineconsole.commandSearchers.CommandSearchAggregator;
import net.nhiroki.bluelineconsole.dataStore.deviceLocal.WidgetsSetting;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;


public class MainActivity extends BaseWindowActivity {
    private CandidateListAdapter resultCandidateListAdapter;
    private CommandSearchAggregator commandSearchAggregator = null;
    private ExecutorService threadPool = null;

    public static final int REQUEST_CODE_FOR_COMING_BACK = 1;

    private boolean cameBackFlag = false;
    private boolean comingBackFlag = false;

    private boolean showStartUpHelp = false;
    private boolean migrationLostHappened = false;

    private boolean homeItemExists = false;

    private EditText mainInputText;
    private ListView candidateListView;

    private int resumeId = 0;

    private boolean temporaryContentShown = false;

    private static MainActivity myActiveInstance = null;


    public MainActivity() {
        super(R.layout.main_activity_body, true);
    }

    public static void setIsComingBack(boolean flag) {
        if (myActiveInstance != null) {
            myActiveInstance.comingBackFlag = flag;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!this.iAmHomeActivity) {
            MainActivity.myActiveInstance = this;
        }

        this.mainInputText = findViewById(R.id.mainInputText);

        this.migrationLostHappened = WidgetsSetting.migrationLostHappened(this);
        this.showStartUpHelp = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(StartUpHelpActivity.PREF_KEY_SHOW_STARTUP_HELP, true);


        this.setHeaderFooterTexts(getString(R.string.app_name), String.format(getString(R.string.displayedFullVersionString), BuildConfig.VERSION_NAME));

        AppNotification.update(this);

        this.candidateListView = findViewById(R.id.candidateListView);
        resultCandidateListAdapter = new CandidateListAdapter(this, new ArrayList<>(), candidateListView);

        candidateListView.setAdapter(resultCandidateListAdapter);

        candidateListView.setOnItemClickListener((parent, view, position, id) -> resultCandidateListAdapter.invokeEvent(position, MainActivity.this));

        candidateListView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && v.onKeyDown(keyCode, event)) {
                return true;
            }

            //noinspection RedundantIfStatement
            if (event.getAction() == KeyEvent.ACTION_UP && v.onKeyUp(keyCode, event)) {
                return true;
            }

            return false;
        });

        EditTextConfigurations.applyCommandEditTextConfigurations(mainInputText, this);
        mainInputText.requestFocus();
        mainInputText.requestFocusFromTouch();

        Intent from_intent = this.getIntent();
        String search = from_intent.getStringExtra(Intent.EXTRA_TEXT);
        if (search != null) {
            mainInputText.setText(search);
        }

        mainInputText.setOnEditorActionListener((v, actionId, event) -> {
            if (resultCandidateListAdapter.isEmpty()) {
                return false;
            }
            if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
                resultCandidateListAdapter.invokeFirstChoiceEvent(MainActivity.this);
                return true;
            }
            return false;
        });

        mainInputText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                candidateListView.requestFocus();
                candidateListView.requestFocusFromTouch();
                return MainActivity.this.resultCandidateListAdapter.selectChosenNowAsListView() && candidateListView.onKeyDown(keyCode, event);
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        if (this.commandSearchAggregator != null) {
            this.commandSearchAggregator.close();
        }
        if (!this.iAmHomeActivity) {
            MainActivity.myActiveInstance = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        resultCandidateListAdapter.set_show_icons(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_appearance_show_icons", true));

        ++this.resumeId;
        this.comingBackFlag = false;

        EditTextConfigurations.applyCommandEditTextConfigurations(mainInputText, this);

        if (!this.themeStateMatchesConfig()) {
            this.finish();
            this.startActivity(new Intent(this, this.getClass()));
            return;
        }

        threadPool = Executors.newSingleThreadExecutor();

        if (commandSearchAggregator == null) {
            // first time after onCreate()
            commandSearchAggregator = new CommandSearchAggregator(this);
            mainInputText.addTextChangedListener(new MainInputTextListener(mainInputText.getText()));

        } else {
            if (!cameBackFlag) {
                if (this.iAmHomeActivity) {
                    if (! mainInputText.getText().toString().isEmpty()) {
                        this.changeInputText("");
                    }

                } else {
                    mainInputText.setText("");

                    if (!this.iAmHomeActivity) {
                        resultCandidateListAdapter.clear();
                        resultCandidateListAdapter.notifyDataSetChanged();
                    }
                }
            }
            // Refresh after searching temporary list
            commandSearchAggregator.refresh(this);
        }

        if (this.showStartUpHelp) {
            this.showStartUpHelp = false;
            this.cameBackFlag = true;
            startActivityForResult(new Intent(MainActivity.this, StartUpHelpActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
            return;
        }

        if (this.migrationLostHappened) {
            this.migrationLostHappened = false;
            this.cameBackFlag = true;
            startActivityForResult(new Intent(MainActivity.this, NotificationMigrationLostActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
            return;
        }

        final CharSequence currentMainInputTextContents = mainInputText.getText();
        if (this.cameBackFlag || this.iAmHomeActivity || ! currentMainInputTextContents.toString().isEmpty()) {
            this.onCommandInput(currentMainInputTextContents);
        }

        this.cameBackFlag = false;

        MainActivity.this.enableBaseWindowAnimation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.cameBackFlag = (requestCode == REQUEST_CODE_FOR_COMING_BACK) && (resultCode == RESULT_OK);
    }

    @Override
    protected void onPause() {
        ++this.resumeId;
        if (threadPool != null) {
            threadPool.shutdownNow();
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
        // This app should be as stateless as possible. When app disappears most activities should finish.
        super.onStop();
        if (!this.iAmHomeActivity && !this.comingBackFlag) {
            this.finish();
        }
    }

    @Override
    protected void enableWindowAnimationForElements() {
        super.enableWindowAnimationForElements();

        this.enableWindowAnimationForEachViewGroup(findViewById(R.id.mainRootLinearLayout));
        this.enableWindowAnimationForEachViewGroup(findViewById(R.id.mainInputTextWrapperLinearLayout));
        this.enableWindowAnimationForEachViewGroup(findViewById(R.id.candidateViewWrapperLinearLayout));
    }

    @Override
    protected void disableWindowAnimationForElements() {
        super.disableWindowAnimationForElements();

        this.disableWindowAnimationForEachViewGroup(findViewById(R.id.mainRootLinearLayout));
        this.disableWindowAnimationForEachViewGroup(findViewById(R.id.mainInputTextWrapperLinearLayout));
        this.disableWindowAnimationForEachViewGroup(findViewById(R.id.candidateViewWrapperLinearLayout));
    }

    public void changeInputText(String text) {
        mainInputText.setText(text);
        this.onCommandInput(mainInputText.getText());
    }

    private void setWholeLayout() {
        if (resultCandidateListAdapter.isEmpty()) {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(0, 0, 0, 0);
        } else {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(0, (int)(6 * getResources().getDisplayMetrics().density + 0.5), 0, 0);
        }

        final boolean contentFilled = !mainInputText.getText().toString().isEmpty() || this.homeItemExists;

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
        List<CandidateEntry> candidates = new ArrayList<>();

        if (! query.isEmpty()) {
            candidates.addAll(commandSearchAggregator.searchCandidateEntries(query, MainActivity.this));
        }

        if (this.iAmHomeActivity && query.isEmpty()) {
            List<CandidateEntry> homeScreenEntries = commandSearchAggregator.homeScreenDefaultCandidateEntries(this);
            candidates.addAll(homeScreenEntries);
            this.homeItemExists = ! homeScreenEntries.isEmpty();
        }

        if (! query.isEmpty()) {
            candidates.addAll(commandSearchAggregator.searchCandidateEntriesForLast(query, this));
        }

        resultCandidateListAdapter.clear();
        resultCandidateListAdapter.addAll(candidates);
        resultCandidateListAdapter.notifyDataSetChanged();

        if (! candidates.isEmpty()) {
            candidateListView.setSelection(0);
        }

        this.setWholeLayout();

        this.temporaryContentShown = true;
    }

    private void onCommandInput(final CharSequence query) {
        if (commandSearchAggregator.isPrepared() || (query.toString().isEmpty() && !this.iAmHomeActivity)) {
            findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.GONE);
            executeSearch(query.toString());

        } else {
            final int myResumeId = this.resumeId;

            if (!this.iAmHomeActivity || !this.temporaryContentShown) {
                findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.VISIBLE);
                resultCandidateListAdapter.clear();
                resultCandidateListAdapter.notifyDataSetChanged();
            }

            threadPool.execute(() -> {
                commandSearchAggregator.waitUntilPrepared();
                MainActivity.this.runOnUiThread(() -> {
                    if (MainActivity.this.resumeId != myResumeId) {
                        // Already different session, canceling the operation.
                        return;
                    }
                    executeSearch(mainInputText.getText().toString());
                    findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.GONE);
                });
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
            MainActivity.this.temporaryContentShown = false;
            onCommandInput(s);
        }

        @Override
        public void afterTextChanged(Editable s) { }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    }
}
