package net.nhiroki.bluelineconsole.dataStore.cache;

public class ApplicationInformation {
    private final String packageName;
    private final String locale;
    private final int version;
    private final String label;
    private final boolean launchable;

    public ApplicationInformation(String packageName, String locale, int version, String label, boolean launchable) {
        this.packageName = packageName;
        this.locale = locale;
        this.version = version;
        this.label = label;
        this.launchable = launchable;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getLocale() {
        return locale;
    }

    public int getVersion() {
        return version;
    }

    public String getLabel() {
        return label;
    }

    public boolean getLaunchable() {
        return launchable;
    }
}
