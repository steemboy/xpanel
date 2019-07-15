package arca.xpanel;

import android.app.AppOpsManager;
import android.app.Application;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arca.xpanel.services.service_devadmin;

public class singleton extends Application {
    public int uid;
    public String name;
    public static singleton app;
    public AppOpsManager ops;
    public SharedPreferences set;
    public DevicePolicyManager dpm;
    public CameraManager camera;
    private UsageStatsManager usm;
    public PackageManager pm;
    public ComponentName adminComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        name = getPackageName();
        pm = getPackageManager();
        uid = android.os.Process.myUid();
        adminComponent = new ComponentName(this, service_devadmin.class);

        ops = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        usm = (UsageStatsManager) getSystemService(Service.USAGE_STATS_SERVICE);
        set = PreferenceManager.getDefaultSharedPreferences(this);
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        camera = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }

    public boolean check_writeSet() {
        return Settings.System.canWrite(this);
    }

    public boolean check_usageStats() {
        return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, name) == AppOpsManager.MODE_ALLOWED;
    }

    public boolean check_alert() {
        return Settings.canDrawOverlays(this);
    }

    public boolean chec_devadmin() {
        return dpm.isAdminActive(adminComponent);
    }

    public boolean check_camera() {
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public String get_last_app() {
        long time = System.currentTimeMillis();
        List<UsageStats> list = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
        if(list.size() > 1) {
            Collections.sort(list, (o1, o2) -> Long.compare(o1.getLastTimeUsed(), o2.getLastTimeUsed()));
            ArrayList<UsageStats> apps = new ArrayList<>();
            int i =  check(list.get(list.size() - 1).getPackageName()) ? 1 : 2;
            String s;
            for (UsageStats us : list)
                try {
                    s = us.getPackageName();
                    if ((pm.getApplicationInfo(s, 0).flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !s.equals(name) && !check(s))
                        apps.add(us);
                } catch (PackageManager.NameNotFoundException ignored) { }

            return apps.get(apps.size() - i).getPackageName();
        }
        return "";
    }

    private boolean check(String pakage) {
        List<IntentFilter> filters = new ArrayList<>();
        pm.getPreferredActivities(filters, new ArrayList<>(), pakage);
        for(IntentFilter i : filters)
            if(i.hasAction(Intent.ACTION_MAIN) && i.hasCategory(Intent.CATEGORY_HOME) && i.hasCategory(Intent.CATEGORY_DEFAULT))
                return true;
        return false;
    }
}