package net.nhiroki.bluelineconsole.applicationMain.theming;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class AppThemeDirectoryTests {
    @Test
    public void minimumAppThemeDirectoryFunctionalityTest() {
        CharSequence[] keys = AppThemeDirectory.getThemePreferenceKeys();
        Set<CharSequence> tmp = new HashSet<>();

        for (CharSequence key: keys) {
            if (tmp.contains(key)) {
                throw new RuntimeException("Theme ID conflicts");
            }
            tmp.add(key);
        }
    }
}
