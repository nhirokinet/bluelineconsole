package net.nhiroki.bluelineconsole.applicationMain;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.lib.EditTextConfigurations;
import net.nhiroki.bluelineconsole.dataStore.persistent.HomeScreenSetting;

public class PreferencesHomeScreenEachDefaultItemCommandActivity extends BaseWindowActivity {
    public static final String INTENT_EXTRA_ITEM_ID = "item_id";

    private HomeScreenSetting.HomeScreenDefaultItem myItem;

    public PreferencesHomeScreenEachDefaultItemCommandActivity() {
        super(R.layout.preferences_home_screen_each_default_item_command, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHeaderFooterTexts(getString(R.string.preferences_title_for_header_home_screen_default_items), getString(R.string.preferences_title_for_footer_home_screen_default_items));
        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_IN_MOBILE, 3);

        final EditText commandInputEditText = this.findViewById(R.id.home_screen_command_edit_command);
        EditTextConfigurations.applyCommandEditTextConfigurations(commandInputEditText, this);

        this.findViewById(R.id.home_screen_command_each_submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesHomeScreenEachDefaultItemCommandActivity.this.myItem.type = HomeScreenSetting.HOME_SCREEN_TYPE_COMMAND;
                PreferencesHomeScreenEachDefaultItemCommandActivity.this.myItem.data = ((TextView)PreferencesHomeScreenEachDefaultItemCommandActivity.this.findViewById(R.id.home_screen_command_edit_command)).getText().toString();

                if (PreferencesHomeScreenEachDefaultItemCommandActivity.this.myItem.id == -1) {
                    HomeScreenSetting.getInstance(PreferencesHomeScreenEachDefaultItemCommandActivity.this).addHomeScreenDefaultItem(PreferencesHomeScreenEachDefaultItemCommandActivity.this.myItem);

                } else {
                    HomeScreenSetting.getInstance(PreferencesHomeScreenEachDefaultItemCommandActivity.this).updateHomeScreenDefaultItem(PreferencesHomeScreenEachDefaultItemCommandActivity.this.myItem);
                }

                PreferencesHomeScreenEachDefaultItemCommandActivity.this.finish();
            }
        });

        this.findViewById(R.id.home_screen_command_each_delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreferencesHomeScreenEachDefaultItemCommandActivity.this.myItem.id != -1) {
                    HomeScreenSetting.getInstance(PreferencesHomeScreenEachDefaultItemCommandActivity.this).deleteHomeScreenDefaultItem(PreferencesHomeScreenEachDefaultItemCommandActivity.this.myItem.id);
                    PreferencesHomeScreenEachDefaultItemCommandActivity.this.finish();
                }
            }
        });

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(true);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onStop() {
        super.originalOnStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent from_intent = this.getIntent();
        int item_id = from_intent.getIntExtra(INTENT_EXTRA_ITEM_ID, -1);

        if (item_id == -1) {
            this.myItem = new HomeScreenSetting.HomeScreenDefaultItem();
            this.myItem.id = -1;

            ((TextView)this.findViewById(R.id.home_screen_command_each_submit_button)).setText(R.string.button_add);
            ((TextView)this.findViewById(R.id.home_screen_command_edit_command)).setText("");
            this.findViewById(R.id.home_screen_command_each_delete_button).setVisibility(View.GONE);

        } else {
            this.myItem = HomeScreenSetting.getInstance(this).getHomeScreenDefaultItemById(item_id);
            ((TextView)this.findViewById(R.id.home_screen_command_each_submit_button)).setText(R.string.button_update);
            ((TextView)this.findViewById(R.id.home_screen_command_edit_command)).setText(this.myItem.data);
            this.findViewById(R.id.home_screen_command_each_delete_button).setVisibility(View.VISIBLE);
        }
    }
}
