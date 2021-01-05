package net.nhiroki.bluelineconsole.commands.netutils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;
import net.nhiroki.bluelineconsole.applicationMain.MainActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PingActivity extends BaseWindowActivity {
    private static final String TARGET_COMMAND = "/system/bin/ping";
    public static final String TARGET_COMMAND_SHORT = "ping";

    private Process _command;
    private InputStream _commandIS;
    private Thread _readerThread;
    private boolean _terminating = false;

    public PingActivity() {
        super(R.layout.activity_ping, false);
    }

    public static boolean commandAvailable() {
        return new File(TARGET_COMMAND).exists();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHeaderFooterTexts(TARGET_COMMAND_SHORT, TARGET_COMMAND_SHORT);
        this.setNestingPadding(1);

        this.changeBaseWindowElementSize(false);
        this.enableBaseWindowAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setResult(RESULT_OK, new Intent(this, MainActivity.class));

        Intent from_intent = this.getIntent();
        String host = from_intent.getStringExtra("host");

        ((TextView) findViewById(R.id.command_title_text_view)).setText(TARGET_COMMAND_SHORT + " " + host);

        try {
            ProcessBuilder pb = new ProcessBuilder(PingActivity.TARGET_COMMAND, host).redirectErrorStream(true);
            this._command = pb.start();
            this._commandIS = new BufferedInputStream(PingActivity.this._command.getInputStream());
        } catch (IOException e) {
            this.writeOutput(getString(R.string.err_could_not_start_command) + "\n");
        }
        ((TextView)findViewById(R.id.command_output_text_view)).setText("");

        this._readerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    byte[] buf = new byte[8192];
                    try {
                        int r = PingActivity.this._commandIS.read(buf);
                        if (r == -1) {
                            return;
                        }
                        if (r > 0) {
                            PingActivity.this.writeOutput(new String(buf, 0, r));
                        }
                    } catch (IOException e) {
                        PingActivity.this.writeOutput("\nIOException\n");
                        return;
                    }

                }
            }
        });
        this._readerThread.start();
    }

    private void writeOutput(final String s) {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                PingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.command_output_text_view)).setText(((TextView)findViewById(R.id.command_output_text_view)).getText() + s);
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

    @Override
    protected void onStop() {
        super.onStop();
        if (this._command != null) {
            this._command.destroy();
            this._command = null;
        }
        if (this._readerThread != null) {
            try {
                this._readerThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSize(true);
    }
}