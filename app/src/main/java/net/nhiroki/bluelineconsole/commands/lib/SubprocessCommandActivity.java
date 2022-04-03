package net.nhiroki.bluelineconsole.commands.lib;

import android.os.Bundle;

import net.nhiroki.bluelineconsole.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SubprocessCommandActivity extends CuiActivity {
    private final String targetCommand;
    protected String[] _args;

    private Process _command;
    private InputStream _commandIS;

    private Thread _readerThread;


    public SubprocessCommandActivity(String targetCommand, String targetCommandShort, boolean stream) {
        super(targetCommandShort, stream);
        this.targetCommand = targetCommand;
    }

    public static boolean commandAvailable(String targetCommand) {
        return new File(targetCommand).exists();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this._readerThread = new Thread(() -> {
            while (true) {
                byte[] buf = new byte[8192];
                try {
                    int r = SubprocessCommandActivity.this._commandIS.read(buf);
                    if (r == -1) {
                        return;
                    }
                    if (r > 0) {
                        SubprocessCommandActivity.this.writeOutput(new String(buf, 0, r));
                    }
                } catch (IOException e) {
                    SubprocessCommandActivity.this.writeOutput("\nIOException\n");
                    return;
                }

            }
        });
        this._readerThread.start();
    }

    @Override
    protected String getTitleForCUI() {
        StringBuilder title = new StringBuilder(CALLER_COMMAND);
        for (String arg: this._args) {
            title.append(" ").append(arg);
        }
        return title.toString();
    }

    @Override
    protected void prepare() {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(this.targetCommand);
            cmd.addAll(Arrays.asList(_args));
            ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);
            this._command = pb.start();
            this._commandIS = new BufferedInputStream(this._command.getInputStream());

        } catch (IOException e) {
            this.writeOutput(getString(R.string.error_failure_could_not_start_command) + "\n");
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
                this._readerThread.interrupt();
            }
        }
    }
}
