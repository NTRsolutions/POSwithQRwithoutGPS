package com.example.pef.prathamopenschool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

public class SyncActivityLogs extends DBHelper {

    Utility Util;
    final String ERRORTABLENAME = "Logs";
    Context c;

    SyncActivityLogs(Context c) {
        super(c);
        this.c = c;
        database = getWritableDatabase();
        Util = new Utility();
    }

    public void addToDB(String method, Exception e, String type) {

        contentValues.put("CurrentDateTime", Util.GetCurrentDateTime(false));
        contentValues.put("ExceptionMsg", e.getMessage());
        contentValues.put("ExceptionStackTrace", e.getStackTrace().toString());
        contentValues.put("MethodName", method);
        contentValues.put("Type", type);
        contentValues.put("GroupId", LogInPage.groupId);
        contentValues.put("DeviceId", CrlDashboard.deviceID.equals(null) ? "0000" : CrlDashboard.deviceID);
        contentValues.put("LogDetail", "SyncActivityLog");
        database.insert(ERRORTABLENAME, null, contentValues);

    }

    public void addLog(String method, String type) {
        if (ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String deviceId = ((TelephonyManager) c
                .getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();

        contentValues.put("GroupId", LogInPage.groupId);
        contentValues.put("DeviceId", deviceId);
        contentValues.put("LogDetail", "StudentLog");
        database.insert(ERRORTABLENAME, null, contentValues);


    }

    public void addLog(String method, String type, String msg) {
        if (ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        @SuppressLint("HardwareIds") String deviceId = ((TelephonyManager) c
                .getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();

        contentValues.put("GroupId", LogInPage.groupId);
        contentValues.put("DeviceId", deviceId);
        contentValues.put("LogDetail", "StudentLog");
        database.insert(ERRORTABLENAME, null, contentValues);


    }
}
