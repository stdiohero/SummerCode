package com.example.guru.pa;

import android.app.Dialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import cz.msebera.android.httpclient.Header;

public class Bill extends AppCompatActivity {

    private BillDBOperator mDBOperator = null;
    private ArrayList<BillVO> mBillList = null;
    private ArrayList<Integer> mHash = null;
    private BillAdapter arrayAdapter;
    private SwipeMenuListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

//        mHash = new ArrayList<Integer>();
        mBillList = new ArrayList<BillVO>();
        mDBOperator = new BillDBOperator(this);

        /* 实例化ArrayAdapter */
        //arrayAdapter = new ArrayAdapter<BillVO>(this, android.R.layout.simple_list_item_1, mBillList);
        arrayAdapter = new BillAdapter(this, mBillList);

        /* 实例化SwipeMenuListView */
        mListView = (SwipeMenuListView) findViewById(R.id.bill_list);

        /* 设置mListView的View最小高度 */
        mListView.setMinimumHeight(180);

        /* 实例化SwipeMenuCreator */
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                /* create "open" item */
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                openItem.setWidth(180);
                openItem.setTitle("详情");
                openItem.setTitleSize(18);
                openItem.setTitleColor(Color.rgb(0x00, 0x00, 0x00));
                // 添加到SwipeMenu
                menu.addMenuItem(openItem);

                /* create "delete" item */
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(180);
                deleteItem.setTitle("删除"); /* 未来会换成icon */
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                // 添加到SwipeMenu
                menu.addMenuItem(deleteItem);
            }
        };

        /* 给mListView设置Adapter,MenuCreator,设置滑动方向 */
        mListView.setAdapter(arrayAdapter);
        mListView.setMenuCreator(creator);
        mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        /* 给mListView添加按钮点击监听 */
        mListView.setOnMenuItemClickListener(   new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // open
                        sendId(position);
                        break;
                    case 1:
                        deleteContent(position);
                        // delete
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        mBillList.clear();
        ArrayList<BillVO> temp = mDBOperator.getAllBill();
        if(temp != null) {
            mBillList.addAll(temp);
        }
        getFromCloud(User.userDownloadBill());
        arrayAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_bill,menu);


        /* 设置菜单项的搜索项 */
        MenuItem searchItem = menu.findItem(R.id.bill_search);

        /* 给搜索项添加展开和缩起监听器 */
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                return true;
            }
        });

        SearchView sv = (SearchView) searchItem.getActionView();
        sv.setQueryHint(getString(R.string.searchInfo));
        sv.setIconifiedByDefault(true);
        sv.setOnQueryTextListener(oQueryTextListener);

        return super.onCreateOptionsMenu(menu);

    }

    SearchView.OnQueryTextListener oQueryTextListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) {
            //action when press button search
            arrayAdapter.clear();
            mBillList = mDBOperator.getBillByContent(query);
            arrayAdapter.addAll(mBillList);
            arrayAdapter.notifyDataSetChanged();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.bill_plus:
                Intent intent = new Intent(Bill.this, AddBill.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.bill_sort:
                Collections.sort(mBillList);
                arrayAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void sendId(int position) {
        //int sendId = mHash.get(position);
        BillVO billVO = mBillList.get(position);
        Intent  intent = new Intent(this, MoneyDetail.class);
        intent.putExtra("billId", billVO.getBillId());
        intent.putExtra("local", billVO.getLocal());
        intent.putExtra("date", billVO.getDate());
        intent.putExtra("income", billVO.getIncome());
        intent.putExtra("incomeSource", billVO.getIncomeSource());
        intent.putExtra("expend", billVO.getExpend());
        intent.putExtra("expendDes", billVO.getExpendDes());
        intent.putExtra("backup", billVO.getBackup());
        startActivity(intent);
    }


    public void deleteContent(int position){
        boolean local = mBillList.get(position).getLocal();
        int id = mBillList.get(position).getBillId();
        if(local) {
            mDBOperator.deleteBillById(id);
            mBillList.remove(position);
            arrayAdapter.notifyDataSetChanged();
        } else {
            RequestParams req = new RequestParams();
            req.put("username", User.mUsername);
            req.put("token", User.mToken);
            req.put("id",id);
            //
            deleteFromCloud(req,position);
        }
    }


    /* 从云端获取 */
    private void getFromCloud(RequestParams params) {

        /* 发送到的url */
        String url = "getbill/";

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
                        case "30000":
                            JSONObject list = response.getJSONObject("list");

                            for(int i = 0; i < list.length(); i++) {
                                JSONObject ith = new JSONObject(list.getString(String.valueOf(i)));

                                Integer billId = ith.getInt("id");
                                Integer year = ith.getInt("year");
                                Integer month = ith.getInt("month");
                                Integer day = ith.getInt("day");
                                Integer income = ith.getInt("income");
                                String incomeSource = ith.getString("incomeSource");
                                Integer expend = ith.getInt("expend");
                                String expendDes = ith.getString("expendDes");
                                String backup = ith.getString("backup");

                                BillVO billVO = new BillVO();
                                billVO.setBillId(billId);
                                billVO.setLocal(false);
                                billVO.setYear(year);
                                billVO.setMonth(month);
                                billVO.setDay(day);
                                billVO.setIncome(income);
                                billVO.setIncomeSource(incomeSource);
                                billVO.setExpend(expend);
                                billVO.setExpendDes(expendDes);
                                billVO.setBackup(backup);
                                mBillList.add(billVO);
                            }
                            arrayAdapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    Toast.makeText(Bill.this, "exception", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                if(User.mLoggedIn) {
                    /* 超时提示 */
                    Toast.makeText(Bill.this, "连接超时，请检查网络连接", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /* 云端删除 */
    private void deleteFromCloud(RequestParams params, final int position) {

        /* 发送到的url */
        String url = "deletebill/";

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
                        case "40000":
                            mBillList.remove(position);
                            /*if(position == mBillList.size()-1) {
                                mPMArrayList.add(new PasswordMessage(-1,true,"111","111","111","111"));
                            }*/
                            arrayAdapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    Toast.makeText(Bill.this, "exception", Toast.LENGTH_SHORT).show();
                }

                /* 提示返回信息 */
                Toast.makeText(Bill.this, res, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                /* 超时提示 */
                Toast.makeText(Bill.this, "连接超时，请检查网络连接", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
