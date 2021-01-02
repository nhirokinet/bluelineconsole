package net.nhiroki.bluelineconsole.commands.applications;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.LocaleList;

import net.nhiroki.bluelineconsole.dataStore.cache.ApplicationInformation;
import net.nhiroki.bluelineconsole.dataStore.cache.ApplicationInformationCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ApplicationDatabase {
    private List<ApplicationInformation> applicationInformationList;
    private Map<String, ApplicationInfo> androidApplicationInfoMap; // performance hack
    private boolean preparationCompleted = false;
    private List<Thread> waitingThreads = new ArrayList<>();

    private Thread loader;

    public ApplicationDatabase(final Context context) {
        loader = new Thread() {
            @Override
            public void run() {
                load(context);
            }
        };
        loader.start();
    }

    public void waitUntilPrepared() {
        Thread th = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        th.start();
        registerWaitingThread(th);
        try {
            th.join();
        } catch (InterruptedException e) {

        }
    }

    public void close() {
        loader.interrupt();

        for (Thread th: waitingThreads) {
            th.interrupt();
        }
    }

    private synchronized void setPreparationCompleted() {
        this.preparationCompleted = true;

        for (Thread th: waitingThreads) {
            th.interrupt();
        }
    }

    public synchronized boolean isPrepared() {
        return this.preparationCompleted;
    }

    private synchronized void registerWaitingThread(Thread thread) {
        if (this.preparationCompleted) {
            thread.interrupt();
            return;
        }

        waitingThreads.add(thread);
    }

    private void load(Context context) {
        PackageManager packageManager = context.getPackageManager();
        applicationInformationList = new ArrayList<>();
        androidApplicationInfoMap = new HashMap<>();

        final String localeStr;
        if (Build.VERSION.SDK_INT >= 24) {
            String localeStrTmp = "";
            LocaleList localeList = context.getResources().getConfiguration().getLocales();
            for (int i = 0; i < localeList.size(); ++i) {
                localeStrTmp += localeList.get(i).toString() + ",";
            }
            localeStr = localeStrTmp;
        } else {
            Locale locale = context.getResources().getConfiguration().locale;
            localeStr = locale.toString();
        }

        ApplicationInformationCache applicationInformationCache = new ApplicationInformationCache(context);

        Map <String, ApplicationInformation> applicationMap = new HashMap<>();
        Set <String> appCacheToRemove = new HashSet<>();

        for (ApplicationInformation app : applicationInformationCache.getAllApplicationCaches()) {
            applicationMap.put(app.getPackageName(), app);
            appCacheToRemove.add(app.getPackageName());
        }

        List <ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(0);
        for (ApplicationInfo applicationInfo : applicationInfoList) {

            PackageInfo packageInfo;
            try {
                packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                // Maybe the package has just been removed after starting. This happens rarely and just to be ignored.
                continue;
            }

            Integer thisAppVersion = packageInfo.versionCode;

            ApplicationInformation applicationInformation;

            if (applicationMap.containsKey(applicationInfo.packageName)) {
                ApplicationInformation cachedInfo;
                cachedInfo = applicationMap.get(applicationInfo.packageName);
                appCacheToRemove.remove(applicationInfo.packageName);

                if (cachedInfo.getVersion() == thisAppVersion && (cachedInfo.getLaunchable() == false || cachedInfo.getLocale().equals(localeStr))) {
                    applicationInformation = cachedInfo;
                } else {
                    boolean launchable = context.getPackageManager().getLaunchIntentForPackage(applicationInfo.packageName) != null;
                    String thisAppTitle = "";
                    if (launchable) {
                        thisAppTitle = packageManager.getApplicationLabel(applicationInfo).toString();
                    }

                    applicationInformation = new ApplicationInformation(applicationInfo.packageName, localeStr, thisAppVersion, thisAppTitle, launchable);
                    applicationInformationCache.updateCache(applicationInformation);
                }
            } else {
                boolean launchable = context.getPackageManager().getLaunchIntentForPackage(applicationInfo.packageName) != null;
                String thisAppTitle = "";
                if (launchable) {
                    thisAppTitle = packageManager.getApplicationLabel(applicationInfo).toString();
                }
                applicationInformation = new ApplicationInformation(applicationInfo.packageName, localeStr, thisAppVersion, thisAppTitle, launchable);
                applicationInformationCache.updateCache(applicationInformation);
            }
            if (applicationInformation.getLaunchable()) {
                applicationInformationList.add(applicationInformation);
            }
            androidApplicationInfoMap.put(applicationInformation.getPackageName(), applicationInfo);
        }

        Collections.sort(applicationInformationList, new Comparator<ApplicationInformation>() {
            @Override
            public int compare(ApplicationInformation o1, ApplicationInformation o2) {
                // The method itself is not important.
                // It is important that the result is deterministic and consistent.
                return o1.getPackageName().compareTo(o2.getPackageName());
            }
        });

        for (String removeFromCache : appCacheToRemove) {
            applicationInformationCache.deleteCache(removeFromCache);
        }
        setPreparationCompleted();
    }

    public void load() {

    }

    // returns editable reference, but it is not assumed to be edited outside.
    public List<ApplicationInformation> getApplicationInformationList() {
        return applicationInformationList;
    }

    // performance hack
    public ApplicationInfo getAndroidApplicationInfo (String packageName) {
        return androidApplicationInfoMap.get(packageName);
    }
}
