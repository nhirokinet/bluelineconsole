package net.nhiroki.bluelineconsole.applicationMain;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.R;

public class PreferencesAccentColorActivity extends BaseWindowActivity {

    public PreferencesAccentColorActivity() {
        super(R.layout.preferences_accent_color, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHeaderFooterTexts(getString(R.string.preferences_title_for_header_and_footer_accent_color), null);
        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_IN_MOBILE, 2);

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();

        this.loadCurrentColor(this.getAccentColor());

        ((SeekBar)this.findViewById(R.id.pref_accent_color_red_seekbar)).setOnSeekBarChangeListener(new ColorSeekBarChangeListener());
        ((SeekBar)this.findViewById(R.id.pref_accent_color_green_seekbar)).setOnSeekBarChangeListener(new ColorSeekBarChangeListener());
        ((SeekBar)this.findViewById(R.id.pref_accent_color_blue_seekbar)).setOnSeekBarChangeListener(new ColorSeekBarChangeListener());

        ((RadioGroup)this.findViewById(R.id.pref_accent_color_category_radio_button_group)).setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        PreferencesAccentColorActivity.this.writeNewAccentColorPreferences();
                    }
                }
        );

        this.findViewById(R.id.pref_accent_color_red_decrement_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int oldval = ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_red_seekbar)).getProgress();
                        if (oldval > 0) {
                            ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_red_seekbar)).setProgress(oldval - 1);
                            PreferencesAccentColorActivity.this.writeNewAccentColorPreferences();
                        }
                    }
                }
        );
        this.findViewById(R.id.pref_accent_color_red_increment_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int oldval = ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_red_seekbar)).getProgress();
                        if (oldval < 255) {
                            ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_red_seekbar)).setProgress(oldval + 1);
                            PreferencesAccentColorActivity.this.writeNewAccentColorPreferences();
                        }
                    }
                }
        );
        
        this.findViewById(R.id.pref_accent_color_green_decrement_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int oldval = ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_green_seekbar)).getProgress();
                        if (oldval > 0) {
                            ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_green_seekbar)).setProgress(oldval - 1);
                            PreferencesAccentColorActivity.this.writeNewAccentColorPreferences();
                        }
                    }
                }
        );
        this.findViewById(R.id.pref_accent_color_green_increment_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int oldval = ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_green_seekbar)).getProgress();
                        if (oldval < 255) {
                            ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_green_seekbar)).setProgress(oldval + 1);
                            PreferencesAccentColorActivity.this.writeNewAccentColorPreferences();
                        }
                    }
                }
        );

        this.findViewById(R.id.pref_accent_color_blue_decrement_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int oldval = ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_blue_seekbar)).getProgress();
                        if (oldval > 0) {
                            ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_blue_seekbar)).setProgress(oldval - 1);
                            PreferencesAccentColorActivity.this.writeNewAccentColorPreferences();
                        }
                    }
                }
        );
        this.findViewById(R.id.pref_accent_color_blue_increment_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int oldval = ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_blue_seekbar)).getProgress();
                        if (oldval < 255) {
                            ((SeekBar) PreferencesAccentColorActivity.this.findViewById(R.id.pref_accent_color_blue_seekbar)).setProgress(oldval + 1);
                            PreferencesAccentColorActivity.this.writeNewAccentColorPreferences();
                        }
                    }
                }
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(true);
    }

    @SuppressLint("DefaultLocale")
    private void loadCurrentColor (int color) {
        int red = (color >> 16) & 0xff;
        int green = (color >> 8) & 0xff;
        int blue = color & 0xff;

        int brightness = (red + green + blue) / 3;

        TextView monitor = this.findViewById(R.id.pref_accent_color_monitor);
        monitor.setBackgroundColor(color);
        // Calculate readable text color from background, and thus not by theme.
        int readableColorOnMonitor = brightness > 80 ? Color.BLACK : Color.WHITE;
        monitor.setTextColor(readableColorOnMonitor);
        this.findViewById(R.id.pref_accent_color_monitor_border).setBackgroundColor(readableColorOnMonitor);
        monitor.setText(String.format("#%02X%02X%02X", red, green, blue));


        ((SeekBar) this.findViewById(R.id.pref_accent_color_red_seekbar)).setProgress(red);
        ((SeekBar) this.findViewById(R.id.pref_accent_color_green_seekbar)).setProgress(green);
        ((SeekBar) this.findViewById(R.id.pref_accent_color_blue_seekbar)).setProgress(blue);

        ((TextView) this.findViewById(R.id.pref_accent_color_red_text)).setText(String.format("%3d", red));
        ((TextView) this.findViewById(R.id.pref_accent_color_green_text)).setText(String.format("%3d", green));
        ((TextView) this.findViewById(R.id.pref_accent_color_blue_text)).setText(String.format("%3d", blue));

        String accentColorPreference = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME_ACCENT_COLOR, PREF_VALUE_ACCENT_COLOR_THEME_DEFAULT);
        if (accentColorPreference.startsWith(PREF_VALUE_ACCENT_COLOR_PREFIX_COLOR)) {
            ((RadioButton) findViewById(R.id.pref_accent_color_custom_radio_button)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.pref_accent_color_theme_default_radio_button)).setChecked(true);
        }

        if (this.themeSupportsAccentColorChange()) {
            if (accentColorPreference.startsWith(PREF_VALUE_ACCENT_COLOR_PREFIX_COLOR)) {
                this.findViewById(R.id.pref_accent_color_red_seekbar).setEnabled(true);
                this.findViewById(R.id.pref_accent_color_green_seekbar).setEnabled(true);
                this.findViewById(R.id.pref_accent_color_blue_seekbar).setEnabled(true);

                this.findViewById(R.id.pref_accent_color_red_decrement_button).setEnabled(red > 0);
                this.findViewById(R.id.pref_accent_color_red_increment_button).setEnabled(red < 255);
                this.findViewById(R.id.pref_accent_color_green_decrement_button).setEnabled(green > 0);
                this.findViewById(R.id.pref_accent_color_green_increment_button).setEnabled(green < 255);
                this.findViewById(R.id.pref_accent_color_blue_decrement_button).setEnabled(blue > 0);
                this.findViewById(R.id.pref_accent_color_blue_increment_button).setEnabled(blue < 255);

            } else {
                this.findViewById(R.id.pref_accent_color_red_seekbar).setEnabled(false);
                this.findViewById(R.id.pref_accent_color_green_seekbar).setEnabled(false);
                this.findViewById(R.id.pref_accent_color_blue_seekbar).setEnabled(false);

                this.findViewById(R.id.pref_accent_color_red_decrement_button).setEnabled(false);
                this.findViewById(R.id.pref_accent_color_red_increment_button).setEnabled(false);
                this.findViewById(R.id.pref_accent_color_green_decrement_button).setEnabled(false);
                this.findViewById(R.id.pref_accent_color_green_increment_button).setEnabled(false);
                this.findViewById(R.id.pref_accent_color_blue_decrement_button).setEnabled(false);
                this.findViewById(R.id.pref_accent_color_blue_increment_button).setEnabled(false);
            }

        } else {
            ((TextView)this.findViewById(R.id.pref_accent_color_title_textview)).setText(String.format(this.getString(R.string.preferences_accent_color_theme_without_accent_color), this.getCurrentThemeName()));

            this.findViewById(R.id.pref_accent_color_theme_default_radio_button).setVisibility(View.GONE);
            this.findViewById(R.id.pref_accent_color_custom_radio_button).setVisibility(View.GONE);
            this.findViewById(R.id.pref_accent_color_monitor_border).setVisibility(View.GONE);
            this.findViewById(R.id.pref_accent_color_seek_bar_table).setVisibility(View.GONE);
        }
    }

    private void writeNewAccentColorPreferences() {
        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(PreferencesAccentColorActivity.this).edit();

        if (((RadioGroup)this.findViewById(R.id.pref_accent_color_category_radio_button_group)).getCheckedRadioButtonId() == R.id.pref_accent_color_custom_radio_button) {
            int red = ((SeekBar) this.findViewById(R.id.pref_accent_color_red_seekbar)).getProgress();
            int green = ((SeekBar) this.findViewById(R.id.pref_accent_color_green_seekbar)).getProgress();
            int blue = ((SeekBar) this.findViewById(R.id.pref_accent_color_blue_seekbar)).getProgress();

            prefEdit.putString(PREF_NAME_ACCENT_COLOR, "color-" + red + "-" + green + "-" + blue);

        } else {
            prefEdit.putString(PREF_NAME_ACCENT_COLOR, PREF_VALUE_ACCENT_COLOR_THEME_DEFAULT);
        }

        prefEdit.apply();
        this.onAccentColorChanged();
        this.loadCurrentColor(this.getAccentColor());
    }

    private class ColorSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            PreferencesAccentColorActivity.this.writeNewAccentColorPreferences();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}