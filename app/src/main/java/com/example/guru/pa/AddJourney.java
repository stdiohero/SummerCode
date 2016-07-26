package com.example.guru.pa;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class AddJourney extends AppCompatActivity {

    private Spinner spinner_T;
    private Spinner spinner_Way;
    private ArrayAdapter<String> adapter_T;
    private ArrayAdapter<String> adapter_Way;
    private EditText mBackUp = null;
    private CalendarViewScrollable mDate = null;
    private TimePicker mTime = null;
    private String mScheduleContent = null;
    private String mGottenDate = null;
    private SimpleDateFormat mDateFormat = null;
    private String mGottenTime = null;
    private DataBaseOperator mDBOperator = null;
    private int gottenId;
    private int mSelectedWayPosition;
    private int mSelectedTPosition;
    private int mYear;
    private int mMonth;
    private int mDay;
    private Intent intentService;
    private AlarmService mService;
    private boolean mBound = false;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journey);

        /* ActionBar添加返回按钮 */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 初始化控件
        spinner_T = (Spinner) findViewById(R.id.journey_T);
        spinner_Way = (Spinner) findViewById(R.id.journey_Way);
        // 建立数据源
        String[] Items1 = getResources().getStringArray(R.array.spinner_T);
        String[] Items2 = getResources().getStringArray(R.array.spinner_Way);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> _Adapter1=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, Items1);
        ArrayAdapter<String> _Adapter2=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, Items2);

        _Adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _Adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        spinner_T.setAdapter(_Adapter1);
        spinner_Way.setAdapter(_Adapter2);

        //获得Spinner的状态
        //spinner_T的状态根据spinner_Way的状态而改变
        spinner_Way.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                mSelectedWayPosition = position;
                if (position == 0){
                    spinner_T.setVisibility(View.GONE);
                    mSelectedTPosition = -1;
                }
                else {
                    mSelectedTPosition = spinner_T.getSelectedItemPosition();
                    spinner_T.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}

        });
        spinner_T.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                mSelectedTPosition = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        //与service绑定
        intentService = new Intent(this, AlarmService.class);
        bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);


        // 获得当前日历选中的日期
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mDate = (CalendarViewScrollable) findViewById(R.id.journey_cal);
        mGottenDate = mDateFormat.format(mDate.getDate()); //默认日期
        mDate.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                ++ month;
                String m = String.valueOf(month);
                String d = String.valueOf(dayOfMonth);
                if (month < 10) m = "0" + m;
                if (dayOfMonth < 10) d = "0" + d;
                mGottenDate = String.valueOf(year) + "-" + m + "-" + d;
                mYear = year;
                mMonth = month + 1;
                mDay = dayOfMonth;
            }
        });
        //获得当前选中的时间
        mTime = (TimePicker)findViewById(R.id.journey_timePicker);
        mGottenTime = new SimpleDateFormat("HH:mm").format(mDate.getDate()); //默认时间
        mTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute){
                String h = String.valueOf(hourOfDay);
                String m = String.valueOf(minute);
                if (hourOfDay < 10) {
                    h = "0" + h;
                }
                if (minute < 10) {
                    m = "0" + m;
                }
                mGottenTime = h + ":" + m;
            }
        });
        /**
         * 初始化界面（若为编辑状态）
         */
        Intent intent = getIntent();
        gottenId = -1;
        if (intent != null) {
            gottenId = intent.getIntExtra("pa.journey.detail.edit", -1);
            if (gottenId > 0) {
                DataBaseOperator initDB = new DataBaseOperator(this);
                Schedule schedule = initDB.getScheduleById(gottenId);
                TagSchedule tagSchedule = initDB.getTagScheduleById(gottenId);

                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
                SimpleDateFormat sdfMinute = new SimpleDateFormat("mm");
                long sDate = 0;
                int sHour = 0;
                int sMinute = 0;

                if (schedule != null) {
                    String date = schedule.getDate();
                    String time = schedule.getTime();
                    String content = schedule.getContent();
                    try {
                        sDate = sdfDate.parse(date).getTime();
                        sHour = (int)sdfHour.parse(time).getTime();
                        sMinute = (int)sdfMinute.parse(time).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    mDate.setDate(sDate);
                  //  mTime.setHour(sHour);
                   // mTime.setMinute(sMinute);
                    EditText editText = (EditText)findViewById(R.id.journey_backup);
                    editText.setText(content);
                }

                if (tagSchedule != null) {
                    //这个好像没用
                    spinner_Way.setSelection(1, true);
                    spinner_T.setSelection(tagSchedule.getRemindId(), true);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveJourney(View view) throws ParseException {
        Schedule schedule = new Schedule();
        mDBOperator = new DataBaseOperator(this);
        mBackUp = (EditText)findViewById(R.id.journey_backup);
        mScheduleContent = mBackUp.getText().toString();

        if (mScheduleContent.equals("")){
            Toast.makeText(AddJourney.this,"备注不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        schedule.setContent(mScheduleContent);
        schedule.setDate(mGottenDate);
        schedule.setTime(mGottenTime);

        if (gottenId > 0) {
            schedule.setScheduleId(gottenId);
            mDBOperator.updateSchedule(schedule);
            if (mSelectedWayPosition != 0) {
                updateTagSchedule(gottenId);
                mService.cancelAlarm(this, gottenId);
                setAlarmClock(schedule, gottenId);
            }
            else {
                mService.cancelAlarm(this, gottenId);
                mDBOperator.deleteTagScheduleById(gottenId);
                Toast.makeText(AddJourney.this, "提醒已删除", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(AddJourney.this, "行程修改成功", Toast.LENGTH_SHORT).show();
        }
        else{
            int scheduleId =  mDBOperator.saveSchedule(schedule);
            if (mSelectedWayPosition != 0) {
                saveTagSchedule(scheduleId);
                setAlarmClock(schedule, scheduleId);
            }
            Toast.makeText(AddJourney.this, "行程添加成功", Toast.LENGTH_SHORT).show();
        }

        //销毁当前activity
        this.finish();
        if (mDBOperator != null){
            mDBOperator.closeDB();
        }

    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AlarmService.LocalBinder binder = (AlarmService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void setAlarmClock(Schedule schedule, int alarmId) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        long triggerMills = sdf.parse(mGottenDate + " " +mGottenTime).getTime();
        long intervalMills = MainActivity.INTERVAL_MILLS[mSelectedTPosition];

        mService.setTimeAndContent(
                triggerMills, intervalMills, alarmId, schedule.getContent()
        );
        startService(intentService);
        Toast.makeText(AddJourney.this, "提醒设置成功", Toast.LENGTH_SHORT).show();
    }

    public void cancelJourney(View view){
        onBackPressed();
    }

    public void onBackPressed(){

        if (mDBOperator != null){
            mDBOperator.closeDB();
        }
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public int saveTagSchedule(int scheduleId){
        TagSchedule tagSchedule = new TagSchedule();
        tagSchedule.setScheduleId(scheduleId);
        tagSchedule.setRemindId(mSelectedTPosition);
        tagSchedule.setYear(mYear);
        tagSchedule.setMonth(mMonth);
        tagSchedule.setDay(mDay);
        int tagId = mDBOperator.saveTagSchedule(tagSchedule);
        return tagId;
    }

    public void updateTagSchedule(int scheduleId){
        TagSchedule tagSchedule = new TagSchedule();
        tagSchedule.setScheduleId(scheduleId);
        tagSchedule.setRemindId(mSelectedTPosition);
        tagSchedule.setYear(mYear);
        tagSchedule.setMonth(mMonth);
        tagSchedule.setDay(mDay);
        mDBOperator.updateTagSchedule(tagSchedule);
    }

}
