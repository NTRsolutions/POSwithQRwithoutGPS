package com.example.pef.prathamopenschool;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pef.prathamopenschool.gps.EventBusMSG;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SignInActivity extends AppCompatActivity {

    List<JSONArray> students;
    List<String> groupNames, assignedIds;
    StatusDBHelper statusDBHelper;
    StudentDBHelper studentDBHelper;
    GroupDBHelper groupDBHelper;
    TextView tv_title;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button next;
    Utility utility;
    ArrayList<JSONObject> attendenceData;
    Context sessionContex;
    boolean timer;
    String newNodeList;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    int aajKaSawalPlayed = 3;
    boolean doubleBackToExitPressedOnce = false;
    String checkQJson;

    private SignInActivity sInstance;
    SimpleDateFormat mDateFormat = new SimpleDateFormat("hh:mm:ss.SS a");
    Dialog gpsTimeDialog;
    TextView tv_msg, tv_msgBottom;
    boolean appName = false;
    StatusDBHelper s;
    private String loginMode;
//    public static String sessionStartTime;

    ImageButton btn_Regular_5to7, btn_Regular_8to14, btn_Regular_14to18, btn_vocational;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        EventBus.getDefault().register(SignInActivity.this);

        btn_Regular_5to7 = findViewById(R.id.btn_Regular_5to7);
        btn_Regular_8to14 = findViewById(R.id.btn_Regular_8to14);
        btn_Regular_14to18 = findViewById(R.id.btn_Regular_14to18);
        btn_vocational = findViewById(R.id.btn_vocational);

        // Multiphotoselect initialization
        MultiPhotoSelectActivity.dilog = new DilogBoxForProcess();
        MultiPhotoSelectActivity.programID = new Utility().getProgramId();
        MultiPhotoSelectActivity.timeout = (long) 20000 * 60;
        MultiPhotoSelectActivity.sessionId = "NA";
        MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        MultiPhotoSelectActivity.deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Check if location & gpstime is available
        s = new StatusDBHelper(this);
        boolean androidIDAvailable = false;
        boolean SerialIDAvailable = false;
        boolean apkVersion = false;

        androidIDAvailable = s.initialDataAvailable("AndroidID");
        SerialIDAvailable = s.initialDataAvailable("SerialID");
        apkVersion = s.initialDataAvailable("apkVersion");
        appName = s.initialDataAvailable("appName");

        if (appName == false) {
            s = new StatusDBHelper(this);
            // app name
            if (MultiPhotoSelectActivity.programID.equals("1"))
                s.insertInitialData("appName", "Pratham Digital - H Learning");
            else if (MultiPhotoSelectActivity.programID.equals("2"))
                s.insertInitialData("appName", "Pratham Digital - Read India");
            else if (MultiPhotoSelectActivity.programID.equals("3"))
                s.insertInitialData("appName", "Pratham Digital - Second Chance");
            else if (MultiPhotoSelectActivity.programID.equals("10"))
                s.insertInitialData("appName", "Pratham Digital - Pratham Institute");
            else if (MultiPhotoSelectActivity.programID.equals("8"))
                s.insertInitialData("appName", "Pratham Digital - ECE");
            else if (MultiPhotoSelectActivity.programID.equals("13"))
                s.insertInitialData("appName", "Pratham Digital - Hamara Gaon");
            else
                s.insertInitialData("appName", "Pratham Digital");

        } else {
            s = new StatusDBHelper(this);
            // app name
            if (MultiPhotoSelectActivity.programID.equals("1"))
                s.Update("appName", "Pratham Digital - H Learning");
            else if (MultiPhotoSelectActivity.programID.equals("2"))
                s.Update("appName", "Pratham Digital - Read India");
            else if (MultiPhotoSelectActivity.programID.equals("3"))
                s.Update("appName", "Pratham Digital - Second Chance");
            else if (MultiPhotoSelectActivity.programID.equals("10"))
                s.Update("appName", "Pratham Digital - Pratham Institute");
            else if (MultiPhotoSelectActivity.programID.equals("8"))
                s.Update("appName", "Pratham Digital - ECE");
            else if (MultiPhotoSelectActivity.programID.equals("13"))
                s.Update("appName", "Pratham Digital - Hamara Gaon");
            else
                s.Update("appName", "Pratham Digital");

            // set Title according to program
            if (MultiPhotoSelectActivity.programID.equals("1"))
                setTitle("Pratham Digital - H Learning");
            else if (MultiPhotoSelectActivity.programID.equals("2"))
                setTitle("Pratham Digital - Read India");
            else if (MultiPhotoSelectActivity.programID.equals("3"))
                setTitle("Pratham Digital - Second Chance");
            else if (MultiPhotoSelectActivity.programID.equals("10"))
                setTitle("Pratham Digital - Pratham Institute");
            else if (MultiPhotoSelectActivity.programID.equals("8"))
                setTitle("Pratham Digital - ECE");
            else if (MultiPhotoSelectActivity.programID.equals("13"))
                setTitle("Pratham Digital - Hamara Gaon");
            else
                setTitle("Pratham Digital");

        }

        String deviceID = "";
        deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        if (androidIDAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("AndroidID", deviceID);
        } else {
            s = new StatusDBHelper(this);
            s.Update("AndroidID", deviceID);
        }

        if (SerialIDAvailable == false) {
            s = new StatusDBHelper(this);
            s.insertInitialData("SerialID", Build.SERIAL);
        } else {
            s = new StatusDBHelper(this);
            s.Update("SerialID", Build.SERIAL);
        }

        if (apkVersion == false) {
            s = new StatusDBHelper(this);
            PackageInfo pInfo = null;
            String verCode = "";
            try {
                pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
                verCode = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            s.insertInitialData("apkVersion", verCode);
        } else {
            s = new StatusDBHelper(this);
            PackageInfo pInfo = null;
            String verCode = "";
            try {
                pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
                verCode = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            s.Update("apkVersion", verCode);
        }
        sInstance = this;

    }


    @Subscribe
    public void onEvent(Short msg) {
        if (msg == EventBusMSG.UPDATE_TRACK) {
            try {
                MyApplication.getInstance().stopLocationUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /****************************************************************************************************************/

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public void doAfterSomeTime() {
        Runnable delayedTask = new Runnable() {
            @Override
            public void run() {
                if (gpsTimeDialog.isShowing()) {
                    tv_msgBottom.setVisibility(View.VISIBLE);
                }
            }
        };
        mainThreadHandler.postDelayed(delayedTask, 60000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MultiPhotoSelectActivity.sessionId = "NA";

        BackupDatabase.backup(this);
        loginMode = s.getValue("loginMode");

        checkGPSEnabled();

        // Reset Timer
        MyApplication.resetGPSFixTimer();
        MyApplication.startGPSFixTimer();

        // session
        MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;
        if (MultiPhotoSelectActivity.pauseFlg) {
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg = false;
        }

        // Update ProgramID in Status Table
        StatusDBHelper s = new StatusDBHelper(this);
        s.Update("ProgramID", MultiPhotoSelectActivity.programID);
        BackupDatabase.backup(this);

        if (MultiPhotoSelectActivity.programID.equals("10")) {
            btn_Regular_5to7.setVisibility(View.GONE);
            btn_Regular_8to14.setVisibility(View.GONE);
            btn_Regular_14to18.setVisibility(View.VISIBLE);
            btn_vocational.setVisibility(View.VISIBLE);

        } else {
            btn_Regular_5to7.setVisibility(View.VISIBLE);
            btn_Regular_8to14.setVisibility(View.VISIBLE);
            btn_Regular_14to18.setVisibility(View.GONE);
            btn_vocational.setVisibility(View.GONE);
        }
    }


    public void checkGPSEnabled() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Check if enabled and if not send user to the GPS settings
        if (!enabled) {
            Toast.makeText(this, "Please turn on GPS !!!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            MultiPhotoSelectActivity.sessionId = new Utility().GetUniqueID().toString();
            Intent goToAdminLogin = new Intent(SignInActivity.this, AdminActivity.class);
//            finish();
            startActivity(goToAdminLogin);
        }
        if (id == R.id.action_leaderboard) {
            MultiPhotoSelectActivity.sessionId = new Utility().GetUniqueID().toString();
            Intent goToAdminLogin = new Intent(SignInActivity.this, StudentsAppUsage.class);
//            Intent goToAdminLogin = new Intent(SignInActivity.this, TabUsage.class);
//            finish();
            startActivity(goToAdminLogin);
        }
        if (id == R.id.action_QrLogin) {
            MultiPhotoSelectActivity.sessionId = new Utility().GetUniqueID().toString();
            Intent i = new Intent(this, QRLogin.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();

        MultiPhotoSelectActivity.pauseFlg = true;

        MultiPhotoSelectActivity.cd = new CountDownTimer(MultiPhotoSelectActivity.duration, 1000) {
            //cd = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                MultiPhotoSelectActivity.duration = millisUntilFinished;
                timer = true;
            }

            @Override
            public void onFinish() {
                timer = false;
                MainActivity.sessionFlg = true;
                if (!CardAdapter.vidFlg) {
                    scoreDBHelper = new ScoreDBHelper(sessionContex);
                    playVideo.calculateEndTime(scoreDBHelper);
                    BackupDatabase.backup(sessionContex);
                    try {
                        System.exit(0);
                        finishAffinity();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ask the user if they want to enable GPS
     */
    private void promptEnableGps() {
        new AlertDialog.Builder(this)
                .setMessage("enable_gps")
                .setPositiveButton("enable",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }
                )
                .setNegativeButton("later",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                )
                .show();
    }


    public void Login5to7Multiphoto(View view) {
        MultiPhotoSelectActivity.sessionId = new Utility().GetUniqueID().toString();

        if (loginMode.contains("QR")) {
            Intent i = new Intent(this, QRLogin.class);
            startActivity(i);
        } else {
            Intent i = new Intent(this, MultiPhotoSelectActivity.class);
            i.putExtra("ageGroup", "5");
            MyApplication.ageGrp = "5";
            startActivity(i);
        }
    }

    public void Login8to14Multiphoto(View view) {
        MultiPhotoSelectActivity.sessionId = new Utility().GetUniqueID().toString();

        if (loginMode.contains("QR")) {
            Intent i = new Intent(this, QRLogin.class);
            startActivity(i);
        } else {
            Intent i = new Intent(this, MultiPhotoSelectActivity.class);
            i.putExtra("ageGroup", "8");
            MyApplication.ageGrp = "8";
            startActivity(i);
        }
    }

    public void Login14to18Multiphoto(View view) {
        MultiPhotoSelectActivity.sessionId = new Utility().GetUniqueID().toString();
        if (loginMode.contains("QR")) {
            Intent i = new Intent(this, QRLogin.class);
            startActivity(i);
        } else {
            Intent i = new Intent(this, MultiPhotoSelectActivity.class);
            i.putExtra("ageGroup", "15");
            MyApplication.ageGrp = "15";
            startActivity(i);
        }
    }

    public void LoginVocationalMultiphoto(View view) {
        MultiPhotoSelectActivity.sessionId = new Utility().GetUniqueID().toString();
        if (loginMode.contains("QR")) {
            Intent i = new Intent(this, QRLogin.class);
            startActivity(i);
        } else {
            Intent i = new Intent(this, MultiPhotoSelectActivity.class);
            i.putExtra("ageGroup", "25");
            MyApplication.ageGrp = "25";
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
        finishAffinity();
        super.onBackPressed();
    }


}
