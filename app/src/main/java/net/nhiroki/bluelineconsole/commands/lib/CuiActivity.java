package net.nhiroki.bluelineconsole.commands.lib;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;


public class CuiActivity extends BaseWindowActivity {
    protected final String CALLER_COMMAND;
    private final boolean _stream;


    public CuiActivity(String name, boolean stream) {
        super(R.layout.command_cui, false);
        this.CALLER_COMMAND = name;
        this._stream = stream;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHeaderFooterTexts(CALLER_COMMAND, null);
        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_ALWAYS, 1);

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();

        if (this._stream) {
            // Invert ScrollView position because ScrollView itself is inverted
            boolean isRtl = this.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
            this.findViewById(R.id.command_output_scroll_view).setVerticalScrollbarPosition(isRtl ? View.SCROLLBAR_POSITION_RIGHT : View.SCROLLBAR_POSITION_LEFT);
            this.findViewById(R.id.command_output_scroll_view).setRotation(180.0f);
            this.findViewById(R.id.command_output_text_view).setRotation(180.0f);
        }
    }

    protected void prepare() {}

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

        ((TextView) findViewById(R.id.command_title_text_view)).setText(this.getTitleForCUI());
        ((TextView)findViewById(R.id.command_output_text_view)).setText("");

        this.prepare();
    }

    protected String getTitleForCUI() {
        return "";
    }

    protected void writeOutput(final String s) {
        @SuppressLint("SetTextI18n") Thread th = new Thread(() -> CuiActivity.this.runOnUiThread(() ->
                ((TextView)findViewById(R.id.command_output_text_view)).setText(((TextView)findViewById(R.id.command_output_text_view)).getText() + s)));

        th.start();
        try {
            th.join();
        } catch (InterruptedException e) {
            th.interrupt();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(true);
    }
}