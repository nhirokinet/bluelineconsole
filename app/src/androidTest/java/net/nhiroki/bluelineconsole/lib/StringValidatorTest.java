package net.nhiroki.bluelineconsole.lib;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class StringValidatorTest  extends AndroidTestCase {
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void urlValidationTest() {
        final SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit();

        prefEdit.remove("pref_url_arbitrary_scheme");
        prefEdit.apply();

        assertTrue(StringValidator.isValidURLAccepted("http://example.com/", true, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://example.com/", true, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("https://example.com", true, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://example.com", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://example.com:12345", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://example.com/日本語 ", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://example.com:12345/日本語 ", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("https://", true, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https:///", true, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("hoge://example.com/", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("https:/", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("https:", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("https", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("http", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("htt", false, this.getContext()));

        prefEdit.putBoolean("pref_url_arbitrary_scheme", true);
        prefEdit.apply();

        assertTrue(StringValidator.isValidURLAccepted("http://example.com/", true, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://example.com/", true, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("https://example.com", true, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://example.com", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://example.com/日本語", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("https://", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("hoge:///", true, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("hoge:///", true, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("git+ssh://example.com/", true, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("git+ssh://192.0.2.1:123456/", true, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("git+ssh:///", true, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("git+ssh:/", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("git+ssh:", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("git+ssh", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("hoge123@", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("hoge123+-123://", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("hoge123+-123://とあるサーバー/", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("hoge123+-123://とあるサーバー/とあるファイル", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("1hoge123+-123://とあるサーバー/とあるファイル", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("+hoge123+-123://とあるサーバー/とあるファイル", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("-hoge123+-123://とあるサーバー/とあるファイル", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("AAA://とあるサーバー/", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("ZZZ://とあるサーバー/", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("aa0://とあるサーバー/", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("z9z://とあるサーバー/", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("0z9z://とあるサーバー/", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("9z9z://とあるサーバー/", false, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("hoge://fuga:12345/piyo/", true, this.getContext()));
        assertTrue(StringValidator.isValidURLAccepted("hoge://fuga:12345/piyo/", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("abc@def://example.com/", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("https:/", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("https:", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("https", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("http", false, this.getContext()));
        assertFalse(StringValidator.isValidURLAccepted("htt", false, this.getContext()));
    }
}
