package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.dataStore.deviceLocal.WidgetsSetting;

public class NotificationMigrationLostActivity extends BaseWindowActivity {
    public NotificationMigrationLostActivity() {
        super(R.layout.notification_migration_lost_body, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHeaderFooterTexts(getString(R.string.startup_help_title_for_header_and_footer), null);

        setResult(RESULT_OK, new Intent(this, MainActivity.class));

        this.setWindowLocationGravity(Gravity.CENTER_VERTICAL);

        // Window nest count is 1, but make margin larger because background window is smaller and hides
        this.setWindowBoundarySize(ROOT_WINDOW_ALWAYS_HORIZONTAL_MARGIN, 2);

        findViewById(R.id.startUpOKButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                WidgetsSetting.resetMigrationLostFlag(NotificationMigrationLostActivity.this);
                NotificationMigrationLostActivity.this.finish();
            }
        });

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(hasFocus);
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }
}