package net.nhiroki.bluelineconsole.commands.netutils;

import android.content.Intent;

import net.nhiroki.bluelineconsole.applicationMain.MainActivity;
import net.nhiroki.bluelineconsole.commands.lib.SubprocessCommandActivity;


public class PingActivity extends SubprocessCommandActivity {
    private static final String TARGET_COMMAND = "/system/bin/ping";
    public static final String TARGET_COMMAND_SHORT = "ping";

    public PingActivity() {
        super(TARGET_COMMAND,TARGET_COMMAND_SHORT, true);
    }

    public static boolean commandAvailable() {
        return SubprocessCommandActivity.commandAvailable(TARGET_COMMAND);
    }

    @Override
    protected void onResume() {
        Intent from_intent = this.getIntent();
        String host = from_intent.getStringExtra("host");

        this._args = new String[]{host};

        super.onResume();

        setResult(RESULT_OK, new Intent(this, MainActivity.class));
    }
}
