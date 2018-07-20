package net.nhiroki.bluelineconsole.applicationMain;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {
    private CandidateListAdapter resultCandidateListAdapter;
    private CommandSearchAggregator commandSearchAggregator;
    private ExecutorService threadPool;

    public static final int REQUEST_CODE_FOR_COMING_BACK = 1;

    private boolean camebackFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppNotification.update(this);

        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setContentView(R.layout.activity_main);

        this.findViewById(R.id.mainLinearLayout).setOnClickListener(new ExitOnClickListener());

        final ListView candidateListView = findViewById(R.id.candidateListView);
        resultCandidateListAdapter = new CandidateListAdapter(this, new ArrayList<CandidateEntry>(), candidateListView);
        candidateListView.setAdapter(resultCandidateListAdapter);

        candidateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                resultCandidateListAdapter.invokeEvent(position, MainActivity.this);
            }
        });

        final EditText mainInputText = findViewById(R.id.mainInputText);
        mainInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (resultCandidateListAdapter.isEmpty()) {
                    return false;
                }
                resultCandidateListAdapter.invokeFirstChoiceEvent(MainActivity.this);
                return true;
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

        ((TextView) findViewById(R.id.versionOnMainFooter)).setText(String.format(getString(R.string.displayedFullVersionString), BuildConfig.VERSION_NAME));

        mainInputText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                    candidateListView.requestFocus();
                    return MainActivity.this.resultCandidateListAdapter.selectSecondChoice();
                }
                return false;
            }
        });

        // Decrease topMargin (which is already negative) by 1 physical pixel to fill the gap. See the comment in activity_main.xml .
        View versionOnMainFooterWrapper = findViewById(R.id.versionOnMainFooterWrapper);
        ViewGroup.MarginLayoutParams versionOnMainFooterWrapperLayoutParam = (ViewGroup.MarginLayoutParams) versionOnMainFooterWrapper.getLayoutParams();
        versionOnMainFooterWrapperLayoutParam.setMargins(
                versionOnMainFooterWrapperLayoutParam.leftMargin,
                versionOnMainFooterWrapperLayoutParam.topMargin - 1,
                versionOnMainFooterWrapperLayoutParam.rightMargin,
                versionOnMainFooterWrapperLayoutParam.bottomMargin
        );
        versionOnMainFooterWrapper.setLayoutParams(versionOnMainFooterWrapperLayoutParam);
    }

    @Override
    protected void onResume() {
        super.onResume();

        EditText mainInputText = findViewById(R.id.mainInputText);

        threadPool = Executors.newSingleThreadExecutor();

        boolean showStartUpHelp = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_main_show_startup_help", true);

        if (commandSearchAggregator == null) {
            // first time after onCreate()
            commandSearchAggregator = new CommandSearchAggregator(this);

            if (!camebackFlag && showStartUpHelp) {
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.this.camebackFlag = true;
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

            if (camebackFlag) {
                List<CandidateEntry> cands = commandSearchAggregator.searchCandidateEntries(mainInputText.getText().toString(), MainActivity.this);

                resultCandidateListAdapter.clear();
                resultCandidateListAdapter.addAll(cands);
                resultCandidateListAdapter.notifyDataSetChanged();

                camebackFlag = false;
            } else {
                commandSearchAggregator.refresh(this);

                if (showStartUpHelp) {
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.camebackFlag = true;
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
                resultCandidateListAdapter.clear();
                resultCandidateListAdapter.notifyDataSetChanged();

                mainInputText.setText("");
            }
        }
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ViewGroup) findViewById(R.id.mainLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
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

        ConstraintLayout root = findViewById(R.id.mainLayoutRoot);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeight = -1;

            @Override
            public void onGlobalLayout() {
                ConstraintLayout root = findViewById(R.id.mainLayoutRoot);

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
            camebackFlag = true;
        }
    }

    @Override
    protected void onPause() {
        threadPool.shutdownNow();
        super.onPause();
        ((ViewGroup) findViewById(R.id.mainLinearLayout)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.mainInputTextWrapperLinearLayout)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.candidateViewWrapperLinearLayout)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
    }

    private void setWholeLayout() {
        final boolean textFilled = ! ((EditText)findViewById(R.id.mainInputText)).getText().toString().equals("");
        Point displaySize = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(displaySize);

        final int maxPanelWidth = (int)(600.0 * getResources().getDisplayMetrics().density);


        LinearLayout mainLinearLayout = findViewById(R.id.mainLinearLayout);
        mainLinearLayout.setGravity(textFilled ? Gravity.TOP : Gravity.CENTER_VERTICAL);
        final int panelWidth = Math.min(maxPanelWidth,
                                        textFilled ? displaySize.x
                                                   : (int)(displaySize.x * ((displaySize.x < displaySize.y) ? 0.87 : 0.7))
                                       );
        final int paddingHorizontal = (displaySize.x - panelWidth) / 2;
        mainLinearLayout.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);

        final double pixelsPerSp = getResources().getDisplayMetrics().scaledDensity;

        ConstraintLayout root = findViewById(R.id.mainLayoutRoot);
        EditText mainInputText = findViewById(R.id.mainInputText);
        // mainInputText: editTextSize * (1 (text) + 0.3 * 2 (padding)
        // If space is limited, split remaining height into 1(EditText):2(ListView and other margins)
        final double editTextSizeSp = Math.min(40.0, (root.getHeight() - findViewById(R.id.mainActivityHeaderWrapper).getHeight() * 2.0) / 4.8 / pixelsPerSp);
        mainInputText.setTextSize((int) editTextSizeSp);
        mainInputText.setPadding((int) (editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp));
    }

    private void executeSearch(CharSequence s) {
        List<CandidateEntry> cands = commandSearchAggregator.searchCandidateEntries(s.toString(), MainActivity.this);

        resultCandidateListAdapter.clear();
        resultCandidateListAdapter.addAll(cands);
        resultCandidateListAdapter.notifyDataSetChanged();

        setWholeLayout();

        if (cands.isEmpty()) {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(
                    (int)(7 * getResources().getDisplayMetrics().density + 0.5),
                    (int)(0 * getResources().getDisplayMetrics().density + 0.5),
                    (int)(7 * getResources().getDisplayMetrics().density + 0.5),
                    (int)(6 * getResources().getDisplayMetrics().density + 0.5))
            ;
        } else {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(
                    (int)(7 * getResources().getDisplayMetrics().density + 0.5),
                    (int)(6 * getResources().getDisplayMetrics().density + 0.5),
                    (int)(7 * getResources().getDisplayMetrics().density + 0.5),
                    (int)(6 * getResources().getDisplayMetrics().density + 0.5))
            ;
        }
    }

    private void onCommandInput(final CharSequence s) {
        if (commandSearchAggregator.isPrepared()) { // avoid waste waitUntilPrepared if already prepared
            executeSearch(s);
        } else {
            findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.VISIBLE);
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    commandSearchAggregator.waitUntilPrepared();
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

    private class ExitOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }
}
