package net.nhiroki.bluelineconsole.applicationMain;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.view.View;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.BuildConfig;
import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;

import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends BaseWindowActivity {
    private CandidateListAdapter _resultCandidateListAdapter;
    private CommandSearchAggregator _commandSearchAggregator;
    private ExecutorService _threadPool;

    public static final int REQUEST_CODE_FOR_COMING_BACK = 1;

    private boolean _camebackFlag = false;

    public MainActivity() {
        super(R.layout.main_activity_body);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(getString(R.string.app_name));
        ((TextView) findViewById(R.id.baseWindowMainFooterTextView)).setText(String.format(getString(R.string.displayedFullVersionString), BuildConfig.VERSION_NAME));

        AppNotification.update(this);

        LinearLayout mainLL = findViewById(R.id.mainInputTextWrapperLinearLayout);
        LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();
        mainLP.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        mainLL.setLayoutParams(mainLP);

        final EditText mainInputText = findViewById(R.id.mainInputText);
        mainInputText.requestFocus();

        final ListView candidateListView = findViewById(R.id.candidateListView);
        _resultCandidateListAdapter = new CandidateListAdapter(this, new ArrayList<CandidateEntry>(), candidateListView);
        candidateListView.setAdapter(_resultCandidateListAdapter);

        candidateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _resultCandidateListAdapter.invokeEvent(position, MainActivity.this);
            }
        });

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

        candidateListView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && v.onKeyDown(keyCode, event)) {
                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_UP && v.onKeyUp(keyCode, event)) {
                    return true;
                }

                if (mainInputText.onKeyDown(keyCode, event)) {
                    mainInputText.requestFocus();
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
                    return MainActivity.this._resultCandidateListAdapter.selectSecondChoice();
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        EditText mainInputText = findViewById(R.id.mainInputText);

        _threadPool = Executors.newSingleThreadExecutor();

        boolean showStartUpHelp = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_main_show_startup_help", true);

        if (_commandSearchAggregator == null) {
            // first time after onCreate()
            _commandSearchAggregator = new CommandSearchAggregator(this);

            if (!_camebackFlag && showStartUpHelp) {
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.this._camebackFlag = true;
                                startActivityForResult(new Intent(MainActivity.this, StartUpHelpActvity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                            }
                        });
                    }
                });
                th.start();
                try {
                    th.join();
                } catch (InterruptedException e) {
                }
            }
            mainInputText.addTextChangedListener(new MainInputTextListener(mainInputText.getText()));

        } else {
            if (_camebackFlag) {
                List<CandidateEntry> cands = _commandSearchAggregator.searchCandidateEntries(mainInputText.getText().toString(), MainActivity.this);
                _commandSearchAggregator.refresh(this);

                _resultCandidateListAdapter.clear();
                _resultCandidateListAdapter.addAll(cands);
                _resultCandidateListAdapter.notifyDataSetChanged();

                _camebackFlag = false;

            } else {
                _commandSearchAggregator.refresh(this);

                if (showStartUpHelp) {
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this._camebackFlag = true;
                                    startActivityForResult(new Intent(MainActivity.this, StartUpHelpActvity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                                }
                            });
                        }
                    });
                    th.start();
                    try {
                        th.join();
                    } catch (InterruptedException e) {
                    }
                }
                _resultCandidateListAdapter.clear();
                _resultCandidateListAdapter.notifyDataSetChanged();

                mainInputText.setText("");
            }
        }
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.enableBaseWindowAnimation();

                        ((ViewGroup) findViewById(R.id.mainRootLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
                        ((ViewGroup) findViewById(R.id.mainInputTextWrapperLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
                        ((ViewGroup) findViewById(R.id.candidateViewWrapperLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

                    }
                });
            }
        });
        th.start();
        try {
            th.join();
        } catch (InterruptedException e) {
        }

        ConstraintLayout root = findViewById(R.id.baseMainLayoutRoot);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeight = -1;

            @Override
            public void onGlobalLayout() {
                ConstraintLayout root = findViewById(R.id.baseMainLayoutRoot);

                if (previousHeight == root.getHeight()) {
                    return;
                }

                previousHeight = root.getHeight();

                MainActivity.this.setWholeLayout();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_COMING_BACK && resultCode == RESULT_OK) {
            _camebackFlag = true;
        }
    }

    @Override
    protected void onPause() {
        _threadPool.shutdownNow();
        super.onPause();
    }

    private void setWholeLayout() {
        final boolean textFilled = ! ((EditText)findViewById(R.id.mainInputText)).getText().toString().equals("");
        Point displaySize = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(displaySize);

        final int maxPanelWidth = (int)(600.0 * getResources().getDisplayMetrics().density);

        LinearLayout mainLinearLayout = findViewById(R.id.baseWindowRootLinearLayout);
        mainLinearLayout.setGravity(textFilled ? Gravity.TOP : Gravity.CENTER_VERTICAL);
        final int panelWidth = Math.min(maxPanelWidth,
                                        textFilled ? displaySize.x
                                                   : (int)(displaySize.x * ((displaySize.x < displaySize.y) ? 0.87 : 0.7))
                                       );
        final int paddingHorizontal = (displaySize.x - panelWidth) / 2;
        mainLinearLayout.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);

        final double pixelsPerSp = getResources().getDisplayMetrics().scaledDensity;

        ConstraintLayout root = findViewById(R.id.baseMainLayoutRoot);
        EditText mainInputText = findViewById(R.id.mainInputText);
        // mainInputText: editTextSize * (1 (text) + 0.3 * 2 (padding)
        // If space is limited, split remaining height into 1(EditText):2(ListView and other margins)
        final double editTextSizeSp = Math.min(40.0, (root.getHeight() - findViewById(R.id.baseWindowHeaderWrapper).getHeight() * 2.0) / 4.8 / pixelsPerSp);
        mainInputText.setTextSize((int) editTextSizeSp);
        mainInputText.setPadding((int) (editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp));

        mainInputText.requestFocus();
    }

    private void executeSearch(CharSequence s) {
        List<CandidateEntry> cands = _commandSearchAggregator.searchCandidateEntries(s.toString(), MainActivity.this);

        _resultCandidateListAdapter.clear();
        _resultCandidateListAdapter.addAll(cands);
        _resultCandidateListAdapter.notifyDataSetChanged();

        setWholeLayout();

        if (cands.isEmpty()) {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(0, 0, 0, 0);
        } else {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(0, (int)(6 * getResources().getDisplayMetrics().density + 0.5), 0, 0);
        }
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
