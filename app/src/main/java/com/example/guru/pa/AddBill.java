package com.example.guru.pa;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import cz.msebera.android.httpclient.Header;

public class AddBill extends AppCompatActivity {

    private EditText incomeText = null;
    private EditText incomeSourceText = null;
    private EditText expendText = null;
    private EditText expendDesText = null;
    private EditText mBackupText = null;
    private CalendarViewScrollable calendarView = null;
    private int mYear;
    private int mMonth;
    private int mDay;
    private BillDBOperator mDBOperator = null;

    private int mID;
    private boolean mLocal;
    private String mDate;
    private int mIncome;
    private int mExpend;
    private String mIncomeSource;
    private String mExpendDes;
    private String mBackup;

    public static int check = 0;
    RadioGroup radiogroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        radiogroup = (RadioGroup)findViewById(R.id.addbill_radiogroup);
        incomeText = (EditText)findViewById(R.id.addbill_income);
        incomeSourceText = (EditText)findViewById(R.id.addbill_incomesource);
        expendText = (EditText)findViewById(R.id.addbill_expend);
        expendDesText = (EditText)findViewById(R.id.addbill_expendpurpose);
        mBackupText = (EditText)findViewById(R.id.addbill_extra);

        /* ActionBar添加返回按钮 */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 获得当前日历选中的日期
        //dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        calendarView = (CalendarViewScrollable) findViewById(R.id.calendarView);
       // calendarDate = dateFormat.format(calendarView.getDate());
        mYear = Integer.parseInt((new SimpleDateFormat("yyyy")).format(calendarView.getDate()));
        mMonth = Integer.parseInt((new SimpleDateFormat("MM")).format(calendarView.getDate()));
        mDay = Integer.parseInt((new SimpleDateFormat("dd")).format(calendarView.getDate()));
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {

                mYear = year;
                mMonth = month + 1;
                mDay = dayOfMonth;
            }
        });

        //添加事件监听器
        radiogroup.setOnCheckedChangeListener(new RadioGroupListener());

        /**
         * 初始化界面（若为编辑状态）
         */
        Intent intent = getIntent();
        if(intent.hasExtra("billId")) {
            mID = intent.getIntExtra("billId", 0);
            mLocal = intent.getBooleanExtra("local", true);
            mDate = intent.getStringExtra("date");
            mIncome = intent.getIntExtra("income", 0);
            mExpend = intent.getIntExtra("expend", 0);
            mIncomeSource = intent.getStringExtra("incomeSource");
            mExpendDes = intent.getStringExtra("expendDes");
            mBackup = intent.getStringExtra("backup");

            SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd");
            //SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
            //SimpleDateFormat sdfMinute = new SimpleDateFormat("mm");
            long sDate = 0;
            //int sHour = 0;
            //int sMinute = 0;
            try {
                sDate = sdfDate.parse(mDate).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            calendarView.setDate(sDate);
            incomeText.setText(mIncome + "");
            incomeSourceText.setText(mIncomeSource);
            expendText.setText(mExpend + "");
            expendDesText.setText(mExpendDes);
            mBackupText.setText(mBackup);

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

    //监听保存方式
    private class RadioGroupListener implements RadioGroup.OnCheckedChangeListener{
        public void onCheckedChanged(RadioGroup group, int checkedId){
            if(checkedId ==  R.id.addbill_radio_cloud)
                check=1;
            else if(checkedId ==  R.id.addbill_radio_local)
                check=0;
        }
    }


    public void saveBill(View view) throws Exception {
        String incomeStr = incomeText.getText().toString();
        String incomeSourceStr = incomeSourceText.getText().toString();
        String expendStr = expendText.getText().toString();
        String expendDesStr = expendDesText.getText().toString();
        String backup = mBackupText.getText().toString();

        if (incomeStr.equals("") || expendStr.equals("")){
            Toast.makeText(AddBill.this,"收入或支出不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mID != 0) {
            /* 修改信息 */
            if (check == 0) {

                /* 修改本地数据 */
                mDBOperator = new BillDBOperator(this);
                BillVO billVO = new BillVO();
                billVO.setIncome(Integer.parseInt(incomeStr));
                billVO.setIncomeSource(incomeSourceStr);
                billVO.setExpend(Integer.parseInt(expendStr));
                billVO.setExpendDes(expendDesStr);
                billVO.setBackup(backup);
                billVO.setDay(mDay);
                billVO.setMonth(mMonth);
                billVO.setYear(mYear);

                if (mLocal) {
                    /* 如果原本是本地的就更新 */
                    billVO.setBillId(mID);
                    billVO.setLocal(true);
                    mDBOperator.updateBill(billVO);
                } else {
                    /* 如果原本是云端的就新建 */
                    mDBOperator.saveBill(billVO);
                }

                if (mDBOperator != null) {
                    mDBOperator.closeDB();
                }
                Intent intent = new Intent();
                intent.putExtra("billId", billVO.getBillId());
                intent.putExtra("local", billVO.getLocal());
                intent.putExtra("date", billVO.getDate());
                intent.putExtra("income", billVO.getIncome());
                intent.putExtra("incomeSource", billVO.getIncomeSource());
                intent.putExtra("expend", billVO.getExpend());
                intent.putExtra("expendDes", billVO.getExpendDes());
                intent.putExtra("backup", billVO.getBackup());
                setResult(2, intent);
                finish();

            } else if (check == 1) {
                /* 修改云端数据 */
                RequestParams requestParams = new RequestParams();
                requestParams.put("username", User.mUsername);
                requestParams.put("token", User.mToken);
                requestParams.put("year", mYear);
                requestParams.put("month", mMonth);
                requestParams.put("day", mDay);
                requestParams.put("income", Integer.parseInt(incomeStr));
                requestParams.put("incomeSource", incomeSourceStr);
                requestParams.put("expend", Integer.parseInt(expendStr));
                requestParams.put("expendDes", expendDesStr);
                requestParams.put("backup", backup);

                if (!mLocal) {
                    /* 如果原本是云端的就更新 */
                    requestParams.put("id", mID);
                }
                addToCloud(requestParams);
            }

        } else {
            /* 增加信息 */
            if (check == 0) {

                /* 增加本地数据 */
                mDBOperator = new BillDBOperator(this);
                BillVO billVO = new BillVO();
                billVO.setIncome(Integer.parseInt(incomeStr));
                billVO.setIncomeSource(incomeSourceStr);
                billVO.setExpend(Integer.parseInt(expendStr));
                billVO.setExpendDes(expendDesStr);
                billVO.setBackup(backup);
                billVO.setDay(mDay);
                billVO.setMonth(mMonth);
                billVO.setYear(mYear);
                mDBOperator.saveBill(billVO);

                if (mDBOperator != null) {
                    mDBOperator.closeDB();
                }

                Intent intent = new Intent();
                intent.putExtra("billId", billVO.getBillId());
                intent.putExtra("local", billVO.getLocal());
                intent.putExtra("date", billVO.getDate());
                intent.putExtra("income", billVO.getIncome());
                intent.putExtra("incomeSource", billVO.getIncomeSource());
                intent.putExtra("expend", billVO.getExpend());
                intent.putExtra("expendDes", billVO.getExpendDes());
                intent.putExtra("backup", billVO.getBackup());
                setResult(2, intent);
                finish();

            } else if (check == 1) {
                /* 增加云端数据 */
                RequestParams requestParams = new RequestParams();
                requestParams.put("username", User.mUsername);
                requestParams.put("token", User.mToken);
                requestParams.put("year", mYear);
                requestParams.put("month", mMonth);
                requestParams.put("day", mDay);
                requestParams.put("income", Integer.parseInt(incomeStr));
                requestParams.put("incomeSource", incomeSourceStr);
                requestParams.put("expend", Integer.parseInt(expendStr));
                requestParams.put("expendDes", expendDesStr);
                requestParams.put("backup", backup);
                addToCloud(requestParams);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        check = 0;
    }


    public void cancelBill(View view) {
        onBackPressed();
    }

    /* 添加到云端 */
    private void addToCloud(RequestParams params) {

        /* 发送到的url */
        String url = "postbill/";

        /* POST请求 */
        HttpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String status = null;
                String res = "11";

                try {
                    status = response.getString("status");
                    res = response.getString("response");

                    /* 判断返回码 */
                    switch(status) {
                        case "20000":
                            AddBill.this.onBackPressed();
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    Toast.makeText(AddBill.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                /* 提示返回信息 */
                Toast.makeText(AddBill.this, res, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                /* 超时提示 */
                Toast.makeText(AddBill.this, "连接超时，请检查网络连接", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
