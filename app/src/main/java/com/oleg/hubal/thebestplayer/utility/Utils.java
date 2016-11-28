package com.oleg.hubal.thebestplayer.utility;

import android.app.ActivityManager;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by User on 22.11.2016.
 */

public class Utils {

    public static boolean isServiceRunning(String serviceName, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String parseDurationToDate(long duration) {
        return (new SimpleDateFormat("mm:ss")).format(new Date(duration));
    }
}
