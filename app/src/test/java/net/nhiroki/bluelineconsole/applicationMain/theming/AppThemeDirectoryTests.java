package net.nhiroki.bluelineconsole.applicationMain.theming;

import org.junit.Test;

public class AppThemeDirectoryTests {
    @Test
    public void minimumAppThemeDirectoryFunctionalityTest() {
        AppThemeDirectory.assertThemeIDDoesNotConflict();
    }
}
