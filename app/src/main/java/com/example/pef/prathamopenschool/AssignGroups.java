package com.example.pef.prathamopenschool;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class AssignGroups extends AppCompatActivity {

    Spinner states_spinner, blocks_spinner, villages_spinner;
    String deviceIMEI = "";
    public String checkBoxIds[], group1 = "0", group2 = "0", group3 = "0", group4 = "0", group5 = "0";
    public boolean grpFound = false;
    int cnt = 0;
    VillageDBHelper database;
    StatusDBHelper sdb;
    GroupDBHelper gdb;
    Button btnAssign;
    StudentDBHelper stdDB;
    List<String> Blocks;
    List<Group> listJsonGrp;
    List<GroupList> dbgroupList;
    int vilID;
    Context context, villageContext, grpcontext, sdhContext, stdContext;
    public static ProgressDialog progress;
    JSONArray grpJsonArray, stdJsonArray;

    Context sessionContex;
    ScoreDBHelper scoreDBHelper;
    PlayVideo playVideo;
    boolean timer;

    List<String> deletedGrpIDs = new ArrayList<>();

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,CrlDashboard.class);
        this.finish();
        startActivity(intent);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_groups);

        MainActivity.sessionFlg = false;
        sessionContex = this;
        playVideo = new PlayVideo();

        // Hide Actionbar
        getSupportActionBar().hide();

        progress = new ProgressDialog(AssignGroups.this);

        villages_spinner = (Spinner) findViewById(R.id.spinner_selectVillage);

        // Hide Village Spinner based on HLearning / RI
        if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("10")) // H Learning
        {
            villages_spinner.setVisibility(View.VISIBLE);
        } else if (MultiPhotoSelectActivity.programID.equals("2")) // RI
        {
            villages_spinner.setVisibility(View.GONE);
        } else {
            villages_spinner.setVisibility(View.VISIBLE);
        }


        btnAssign = (Button) findViewById(R.id.allocateGroups);

        context = this;

        // Generate Device ID
        deviceIMEI = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        stdContext = this;
        stdDB = new StudentDBHelper(stdContext);
        gdb = new GroupDBHelper(context);

      /*  sdhContext = this;

        grpcontext = this;

        // Memory Allocation
        villageContext = this;*/
        database = new VillageDBHelper(context);

        states_spinner = (Spinner) findViewById(R.id.spinner_SelectState);
        //Get Villages Data for States AllSpinners
        List<String> States = database.GetState();
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> StateAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, States);
        // Hint for AllSpinners
        states_spinner.setPrompt("Select State");
        states_spinner.setAdapter(StateAdapter);

        states_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = states_spinner.getSelectedItem().toString();
                populateBlock(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btnAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    group1 = group2 = group3 = group4 = group5 = "0";
                    cnt = 0;
                    for (int i = 0; i < checkBoxIds.length; i++) {
                        CheckBox checkBox = (CheckBox) findViewById(i);

                        if (checkBox.isChecked() && group1.equals("0")) {
                            group1 = (String) checkBox.getTag();
                            cnt++;
                        } else if (checkBox.isChecked() && group2.equals("0")) {
                            cnt++;
                            group2 = (String) checkBox.getTag();
                        } else if (checkBox.isChecked() && group3.equals("0")) {
                            cnt++;
                            group3 = (String) checkBox.getTag();
                        } else if (checkBox.isChecked() && group4.equals("0")) {
                            cnt++;
                            group4 = (String) checkBox.getTag();
                        } else if (checkBox.isChecked() && group5.equals("0")) {
                            cnt++;
                            group5 = (String) checkBox.getTag();
                        } else if (checkBox.isChecked()) {
                            cnt++;
                        }

                    }

                    if (cnt < 1) {
                        Toast.makeText(AssignGroups.this, "Please Select atleast one Group !!!", Toast.LENGTH_SHORT).show();
                    } else if (cnt >= 1 && cnt <= 5) {
                        try {
                            //   MultiPhotoSelectActivity.dilog.showDilog(context, "Assigning Groups");
                            progress.setMessage("Please Wait...");
                            progress.setCanceledOnTouchOutside(false);
                            progress.show();

                            Thread mThread = new Thread() {
                                @Override
                                public void run() {

                                    if (!checkGroupInDB(group1) && !group1.equals("")) {
                                        Group obj = getGroupInfo(group1);
                                        //   obj.GroupCode = "";
                                        gdb.replaceData(obj);   // Debugging till here
                                    }
                                    if (!checkGroupInDB(group2) && !group2.equals("")) {
                                        Group obj = getGroupInfo(group2);
                                        //   obj.GroupCode = "";
                                        gdb.replaceData(obj);
                                    }
                                    if (!checkGroupInDB(group3) && !group3.equals("")) {
                                        Group obj = getGroupInfo(group3);
                                        obj.GroupCode = "";
                                        gdb.replaceData(obj);
                                    }
                                    if (!checkGroupInDB(group4) && !group4.equals("")) {
                                        Group obj = getGroupInfo(group4);
                                        //   obj.GroupCode = "";
                                        gdb.replaceData(obj);
                                    }
                                    if (!checkGroupInDB(group5) && !group5.equals("")) {
                                        Group obj = getGroupInfo(group5);
                                        //      obj.GroupCode = "";
                                        gdb.replaceData(obj);
                                    }

                                    List<Student> stdList = null;
                                    try {
                                        stdList = PopulateStudentsFromJson();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    for (int i = 0; i < stdList.size(); i++) {
                                        Student sobj = stdList.get(i);
                                        stdDB.replaceData(sobj);
                                    }


                                    // Delete Records of Deleted Students
                                    stdDB = new StudentDBHelper(AssignGroups.this);
                                    boolean result = stdDB.deleteDeletedStdRecords();


                                    StatusDBHelper statusDBHelper = new StatusDBHelper(context);

                                    statusDBHelper.Update("group1", (group1));
                                    statusDBHelper.Update("group2", (group2));
                                    statusDBHelper.Update("group3", (group3));
                                    statusDBHelper.Update("group4", (group4));
                                    statusDBHelper.Update("group5", (group5));
                                    statusDBHelper.updateTrailerCountbyGroupID(0, group1);
                                    statusDBHelper.updateTrailerCountbyGroupID(0, group2);
                                    statusDBHelper.updateTrailerCountbyGroupID(0, group3);
                                    statusDBHelper.updateTrailerCountbyGroupID(0, group4);
                                    statusDBHelper.updateTrailerCountbyGroupID(0, group5);
                                    statusDBHelper.Update("village", Integer.toString(vilID));
                                    statusDBHelper.Update("deviceId", deviceIMEI.equals(null) ? "0000" : deviceIMEI);
                                    statusDBHelper.Update("ActivatedDate", new Utility().GetCurrentDateTime(false));
                                    statusDBHelper.Update("ActivatedForGroups", group1 + "," + group2 + "," + group3 + "," + group4 + "," + group5);
                                    BackupDatabase.backup(AssignGroups.this);

                                    // Update Group Table
                                    updateGroupTable();
                                    // Delete Groups where Device ID is deleted & also delete associated students & update status table
                                    deleteGroups();
                                    // replace all null values in db if exists
                                    new checkforNulls().execute();


                                    /*StatusDBHelper statusDBHelper2 = new StatusDBHelper(context);
                                    boolean res = statusDBHelper2.updateTrailerCount(0, group1);
                                    res = statusDBHelper2.updateTrailerCount(0, group2);*/

                                    //  MultiPhotoSelectActivity.dilog.dismissDilog();
                                    AssignGroups.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(AssignGroups.this, " Groups Assigned Successfully !!!", Toast.LENGTH_LONG).show();
                                            if (progress != null && progress.isShowing())
                                                progress.dismiss();
                                        }
                                    });
                                }
                            };
                            mThread.start();

                            //showDialogue(grp1 + " and " + grp2 + " groups are assigned to this tablet.");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //Toast.makeText(getApplicationContext(),"grp1 : "+group1+"grp2 : "+group2,Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AssignGroups.this, " You can select Maximum 5 Groups !!! ", Toast.LENGTH_SHORT).show();
                    }
                    //showDialogue("please select two groups for each tablet");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        });


    }//onCreate


    public class checkforNulls extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // Runs on UI thread
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Runs on the background thread

            try {
                // todo replace null values with dummy values
                CrlDBHelper cdb = new CrlDBHelper(AssignGroups.this);
                AserDBHelper adb = new AserDBHelper(AssignGroups.this);
                GroupDBHelper gdb = new GroupDBHelper(AssignGroups.this);
                StudentDBHelper sdb = new StudentDBHelper(AssignGroups.this);
                VillageDBHelper vdb = new VillageDBHelper(AssignGroups.this);
                AssessmentScoreDBHelper assdb = new AssessmentScoreDBHelper(AssignGroups.this);
                AttendanceDBHelper attdb = new AttendanceDBHelper(AssignGroups.this);
                ScoreDBHelper scrdb = new ScoreDBHelper(AssignGroups.this);
                StatusDBHelper statdb = new StatusDBHelper(AssignGroups.this);
                SessionDBHelper sessdb = new SessionDBHelper(AssignGroups.this);

                sessdb.replaceNulls();
                cdb.replaceNulls();
                adb.replaceNulls();
                gdb.replaceNulls();
                sdb.replaceNulls();
                vdb.replaceNulls();
                assdb.replaceNulls();
                attdb.replaceNulls();
                scrdb.replaceNulls();
                statdb.replaceNulls();

                BackupDatabase.backup(AssignGroups.this);

            } catch (Exception e) {
                e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            // Runs on the UI thread
        }

    }


    private void deleteGroups() {
        // Delete Records of Deleted Students
        gdb = new GroupDBHelper(AssignGroups.this);
        List<Group> deletedGroupsList = gdb.GetAllDeletedGroups();

        // Delete students for all deleted groups
        for (int i = 0; i < deletedGroupsList.size(); i++) {
            StudentDBHelper studentDBHelper = new StudentDBHelper(this);
            boolean res = studentDBHelper.deleteDeletedGrpsStdRecords(deletedGroupsList.get(i).GroupID);
        }

        // update Status Table (reset groupid to 0 & activated groups replace
        for (int i = 0; i < deletedGroupsList.size(); i++) {
            StatusDBHelper statusDBHelper = new StatusDBHelper(AssignGroups.this);
            String grp1 = statusDBHelper.getValue("group1");
            String grp2 = statusDBHelper.getValue("group2");
            String grp3 = statusDBHelper.getValue("group3");
            String grp4 = statusDBHelper.getValue("group4");
            String grp5 = statusDBHelper.getValue("group5");
            if (deletedGroupsList.get(i).GroupID.equalsIgnoreCase(grp1)) {
                statusDBHelper.Update("group1", "0");
                String act = statusDBHelper.getValue("ActivatedForGroups");
                act = act.replace(grp1, "0");
                statusDBHelper.Update("ActivatedForGroups", act);
            }
            if (deletedGroupsList.get(i).GroupID.equalsIgnoreCase(grp2)) {
                statusDBHelper.Update("group2", "0");
                String act = statusDBHelper.getValue("ActivatedForGroups");
                act = act.replace(grp2, "0");
                statusDBHelper.Update("ActivatedForGroups", act);
            }
            if (deletedGroupsList.get(i).GroupID.equalsIgnoreCase(grp3)) {
                statusDBHelper.Update("group3", "0");
                String act = statusDBHelper.getValue("ActivatedForGroups");
                act = act.replace(grp3, "0");
                statusDBHelper.Update("ActivatedForGroups", act);
            }
            if (deletedGroupsList.get(i).GroupID.equalsIgnoreCase(grp4)) {
                statusDBHelper.Update("group4", "0");
                String act = statusDBHelper.getValue("ActivatedForGroups");
                act = act.replace(grp4, "0");
                statusDBHelper.Update("ActivatedForGroups", act);
            }
            if (deletedGroupsList.get(i).GroupID.equalsIgnoreCase(grp5)) {
                statusDBHelper.Update("group5", "0");
                String act = statusDBHelper.getValue("ActivatedForGroups");
                act = act.replace(grp5, "0");
                statusDBHelper.Update("ActivatedForGroups", act);
            }
        }

        // delete groups
        boolean result = gdb.deleteDeletedGrpRecords();
    }

    // Delete Records of Deleted Groups
    private void updateGroupTable() {

        // Group 1
        try {
            List<Group> grpList = null;
            try {
                int vilID = gdb.getVillageIDByGroupID(group1);
                grpList = PopulateGroupsFromJson(vilID);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // insert group 1 data
            for (int i = 0; i < grpList.size(); i++) {
                if (grpList.get(i).GroupID.equalsIgnoreCase(group1)) {
                    Group gobj = grpList.get(i);
                    gdb.replaceData(gobj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Group 2
        try {
            List<Group> grpList = null;
            try {
                int vilID = gdb.getVillageIDByGroupID(group2);
                grpList = PopulateGroupsFromJson(vilID);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // insert group 2 data
            for (int i = 0; i < grpList.size(); i++) {
                if (grpList.get(i).GroupID.equalsIgnoreCase(group2)) {
                    Group gobj = grpList.get(i);
                    gdb.replaceData(gobj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Group 3
        try {
            List<Group> grpList = null;
            try {
                int vilID = gdb.getVillageIDByGroupID(group3);
                grpList = PopulateGroupsFromJson(vilID);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // insert group 3 data
            for (int i = 0; i < grpList.size(); i++) {
                if (grpList.get(i).GroupID.equalsIgnoreCase(group3)) {
                    Group gobj = grpList.get(i);
                    gdb.replaceData(gobj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Group 4
        try {
            List<Group> grpList = null;
            try {
                int vilID = gdb.getVillageIDByGroupID(group4);
                grpList = PopulateGroupsFromJson(vilID);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // insert group 4 data
            for (int i = 0; i < grpList.size(); i++) {
                if (grpList.get(i).GroupID.equalsIgnoreCase(group4)) {
                    Group gobj = grpList.get(i);
                    gdb.replaceData(gobj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Group 5
        try {
            List<Group> grpList = null;
            try {
                int vilID = gdb.getVillageIDByGroupID(group5);
                grpList = PopulateGroupsFromJson(vilID);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // insert group 5 data
            for (int i = 0; i < grpList.size(); i++) {
                if (grpList.get(i).GroupID.equalsIgnoreCase(group5)) {
                    Group gobj = grpList.get(i);
                    gdb.replaceData(gobj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public boolean checkGroupInDB(String grpID) {

        boolean flag = false;

        for (int k = 0; k < dbgroupList.size(); k++) {
            if (dbgroupList.get(k).getGroupId().equals(grpID)) {
                flag = true;
                return true;
            }
        }
        return flag;
    }


    public Group getGroupInfo(String grpID) {

        Group obj = new Group();

        for (int k = 0; k < listJsonGrp.size(); k++) {
            if (listJsonGrp.get(k).GroupID.equals(grpID)) {
                return listJsonGrp.get(k);
            }

        }
        return obj;
    }


    public void populateBlock(String selectedState) {
        blocks_spinner = (Spinner) findViewById(R.id.spinner_SelectBlock);
        //Get Villages Data for Blocks AllSpinners
        Blocks = database.GetStatewiseBlock(selectedState);
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<String> BlockAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, Blocks);
        // Hint for AllSpinners
        blocks_spinner.setPrompt("Select Block");
        blocks_spinner.setAdapter(BlockAdapter);

        blocks_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBlock = blocks_spinner.getSelectedItem().toString();

                if (MultiPhotoSelectActivity.programID.equals("1") || MultiPhotoSelectActivity.programID.equals("3") || MultiPhotoSelectActivity.programID.equals("10")) // H Learning
                {
                    populateVillage(selectedBlock);
                } else if (MultiPhotoSelectActivity.programID.equals("2")) // RI
                {
                    populateRIVillage(selectedBlock);
                } else {
                    populateVillage(selectedBlock);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    // Block Not present in Groups
    public void populateRIVillage(String selectedBlock) {

        vilID = database.GetVillageIDByBlock(selectedBlock);
        try {
            populateGroups(vilID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void populateVillage(String selectedBlock) {

        //Get Villages Data for Villages filtered by block for Spinners
        List<VillageList> BlocksVillages = database.GetVillages(selectedBlock);
        //Creating the ArrayAdapter instance having the Villages list
        ArrayAdapter<VillageList> VillagesAdapter = new ArrayAdapter<VillageList>(this, R.layout.custom_spinner, BlocksVillages);
        // Hint for AllSpinners
        villages_spinner.setPrompt("Select Village");
        villages_spinner.setAdapter(VillagesAdapter);
        villages_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VillageList village = (VillageList) parent.getItemAtPosition(position);
                vilID = village.getVillageId();
                try {
                    populateGroups(vilID);  //Populate groups According to JSON & DB in Checklist instead of using spinner
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void populateGroups(int vilID) throws JSONException {

        // Check Spinner Emptyness
        int VillagesSpinnerValue = villages_spinner.getSelectedItemPosition();

        if (VillagesSpinnerValue > 0 || MultiPhotoSelectActivity.programID.equals("2")) {

            // Showing Groups from Database
            checkBoxIds = null;
            GroupDBHelper groupDBHelper = new GroupDBHelper(context);

            dbgroupList = groupDBHelper.GetGroups(vilID);
            List<GroupList> groupList = new ArrayList<GroupList>(dbgroupList);


            // Pulling Group List from JSON
            try {
                listJsonGrp = PopulateGroupsFromJson(vilID);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (int j = 0; j < listJsonGrp.size(); j++) {
                boolean flag = false;
                for (int k = 0; k < groupList.size(); k++) {
                    if (groupList.get(k).getGroupId().equals(listJsonGrp.get(j).GroupID)) {
                        flag = true;
                    }
                }
                if (flag == false) {
                    // NEW DONT ADD GROUP
                    if (listJsonGrp.get(j).DeviceID.equalsIgnoreCase("Deleted")) {
                        // Don't add group
                    } else
                        groupList.add(new GroupList(listJsonGrp.get(j).GroupID, listJsonGrp.get(j).GroupName));
                }

            }

            groupList.remove(0);

            LinearLayout my_layout = (LinearLayout) findViewById(R.id.assignGroup1);
            LinearLayout my_layout1 = (LinearLayout) findViewById(R.id.assignGroup2);

            my_layout.removeAllViews();
            my_layout1.removeAllViews();

            checkBoxIds = new String[groupList.size()];
            int half = Math.round(groupList.size() / 2);

            for (int i = 0; i < groupList.size(); i++) {

                GroupList grp = groupList.get(i);
                String groupName = grp.getGroupName();
                String groupId = grp.getGroupId();

                TableRow row = new TableRow(AssignGroups.this);
                //row.setId(groupId);
                checkBoxIds[i] = groupId;

                //dynamically create checkboxes. i.e no. of students in group = no. of checkboxes
                row.setLayoutParams(new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
                CheckBox checkBox = new CheckBox(AssignGroups.this);

                try {
                    checkBox.setId(i);
                    checkBox.setTag(groupId);
                    checkBox.setText(groupName);
                } catch (Exception e) {

                }
                checkBox.setTextSize(20);
                checkBox.setTextColor(Color.BLACK);
                checkBox.setBackgroundColor(Color.LTGRAY);

                row.addView(checkBox);
                if (i >= half)
                    my_layout1.addView(row);
                else
                    my_layout.addView(row);
            }


            // Animation Effect on Groups populate
            LinearLayout image = (LinearLayout) findViewById(R.id.LinearLayoutGroups);
            Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide);
            image.startAnimation(animation1);

            Button btnAssign = (Button) findViewById(R.id.allocateGroups);
            btnAssign.setVisibility(View.VISIBLE);

        } else {
            LinearLayout my_layout = (LinearLayout) findViewById(R.id.assignGroup1);
            LinearLayout my_layout1 = (LinearLayout) findViewById(R.id.assignGroup2);
            my_layout.removeAllViews();
            my_layout1.removeAllViews();
        }

    }


    public List<Student> PopulateStudentsFromJson() throws JSONException {

        String tempGid;

        List<Student> jsonStdList = new ArrayList<Student>();

        // insert your code to run only when application is started first time here
        //stdContext = this;

        // For Loading Student Json From External Storage (Assets)
//        JSONObject jsonObject = new JSONObject(loadStudentJSONFromAsset());
        stdJsonArray = new JSONArray(loadStudentJSONFromAsset());

/*
        JSONObject jsonObject = new JSONObject(loadStudentJSONFromAsset());
        stdJsonArray = jsonObject.getJSONArray("studentlist");
*/

// HANDLE NULL INT FOR AGE
        for (int i = 0; i < stdJsonArray.length(); i++) {

            JSONObject stdJsonObject = stdJsonArray.getJSONObject(i);

            //Check Specific GID
            tempGid = stdJsonObject.getString("GroupId");

            if ((group1.equals(tempGid)) || (group2.equals(tempGid)) || (group3.equals(tempGid)) || (group4.equals(tempGid)) || (group5.equals(tempGid))) {

                Student stdObj = new Student();

                stdObj.StudentID = stdJsonObject.getString("StudentId");
                stdObj.StudentUID = stdJsonObject.getString("StudentUID");
                stdObj.FirstName = stdJsonObject.getString("FirstName");
                stdObj.LastName = stdJsonObject.getString("LastName");
                String ageCheck = stdJsonObject.getString("Age");
                if (ageCheck.equals("null")) {
                    stdObj.Age = 0;
                } else {
                    stdObj.Age = stdJsonObject.getInt("Age");
                }

                String cls = stdJsonObject.getString("Class");
                if (cls.length() > 0) {
                    stdObj.stdClass = Integer.parseInt(cls);
                } else {
                    stdObj.stdClass = 0;
                }


                stdObj.CreatedBy = stdJsonObject.getString("CreatedBy");

                String updateDate = stdJsonObject.getString("UpdatedDate");
                if (updateDate == null || updateDate.equalsIgnoreCase("null"))
                    stdObj.UpdatedDate = "";
                else
                    stdObj.UpdatedDate = updateDate;


                String gen = stdJsonObject.getString("Gender");
                if (gen.equals("Male") || gen.equals("M") || gen.equals("1")) {
                    stdObj.Gender = "Male";
                } else if (gen.equals("Female") || gen.equals("F") || gen.equals("2")) {
                    stdObj.Gender = "Female";
                } else if (gen.trim().equalsIgnoreCase("")) {
                    stdObj.Gender = "Male";
                } else {
                    // Default
                    stdObj.Gender = stdJsonObject.getString("Gender");
                }

                stdObj.GroupID = stdJsonObject.getString("GroupId");

                jsonStdList.add(stdObj);

            }

            //db.insertData(grpobj);
        }

        return jsonStdList;
    }

    // Reading Student Json From SDCard
    public String loadStudentJSONFromAsset() {

        String stdJsonStr = null;

        try {
            File stdJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/", "Student.json");
            FileInputStream stream = new FileInputStream(stdJsonSDCard);
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                stdJsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stdJsonStr;
    }


    public List<Group> PopulateGroupsFromJson(int vilID) throws JSONException {

        int tempVid;

        List<Group> jsonGrpList = new ArrayList<Group>();

        // insert your code to run only when application is started first time here
        //grpcontext = this;

        // For Loading Group Json From External Storage (Assets)
        grpJsonArray = new JSONArray(loadGroupJSONFromAsset());

        for (int i = 0; i < grpJsonArray.length(); i++) {


            JSONObject grpJsonObject = grpJsonArray.getJSONObject(i);

            //Check Specific VID
            tempVid = grpJsonObject.getInt("VillageId");

            if (vilID == tempVid) {

                Group grpobj = new Group();

                grpobj.GroupID = grpJsonObject.getString("GroupId");
                grpobj.GroupName = grpJsonObject.getString("GroupName");
                grpobj.VillageID = grpJsonObject.getInt("VillageId");
                grpobj.ProgramID = grpJsonObject.getInt("ProgramId");

                try {
                    grpobj.GroupCode = grpJsonObject.getString("GroupCode");
                    grpobj.SchoolName = grpJsonObject.getString("SchoolName");
                    grpobj.VillageName = grpJsonObject.getString("VIllageName");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    grpobj.DeviceID = grpJsonObject.getString("DeviceId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // New Dont Show deleted groups from Json
                if (grpobj.DeviceID.equalsIgnoreCase("deleted")) {
                    // Don't add deleted groups
                    deletedGrpIDs.add(grpobj.GroupID);
                } else {
                    jsonGrpList.add(grpobj);
                }
            }

            //db.insertData(grpobj);
        }

        return jsonGrpList;
    }

    // Reading Group Json From SDCard
    public String loadGroupJSONFromAsset() {
        String grpJsonStr = null;

        try {
            File grpJsonSDCard = new File(Environment.getExternalStorageDirectory() + "/.POSinternal/Json/", "Group.json");
            FileInputStream stream = new FileInputStream(grpJsonSDCard);
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                grpJsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

        } catch (Exception e) {
        }

        return grpJsonStr;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MultiPhotoSelectActivity.pauseFlg) {
            MultiPhotoSelectActivity.cd.cancel();
            MultiPhotoSelectActivity.pauseFlg = false;
            MultiPhotoSelectActivity.duration = MultiPhotoSelectActivity.timeout;

        }
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
                    System.exit(0);
                    finishAffinity();

                }
            }
        }.start();

    }

}