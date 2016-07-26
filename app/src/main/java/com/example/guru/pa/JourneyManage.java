package com.example.guru.pa;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class JourneyManage extends AppCompatActivity {

    /* 必备的三个量：一个List（也可以为数组）,一个Adapter,一个ListView */
    private ArrayList<String> strs;
    private ArrayList<Integer> mHash;
    private JourneyAdapter journeyAdapter;
    private SwipeMenuListView mListView;
    private ArrayList<Schedule> mScheduleArrayList;
    //private ArrayList<TagSchedule> mTagScheduleArrayList;
    private DataBaseOperator mDBOperator;
    private SearchView mSearchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_manage);

        /* ActionBar添加返回按钮 */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        mScheduleArrayList = new ArrayList();
        mDBOperator = new DataBaseOperator(this);
        journeyAdapter = new JourneyAdapter(this, mScheduleArrayList);

        /* 实例化SwipeMenuListView */
        mListView = (SwipeMenuListView) findViewById(R.id.journey_list);

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
        mListView.setAdapter(journeyAdapter);
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
                        //strs.remove(position);
                        deleteContent(position);
                        //journeyAdapter.notifyDataSetChanged();
                        // delete
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        mSearchView = (SearchView)findViewById(R.id.journey_search);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mScheduleArrayList.clear();
        if(mDBOperator.getAllSchedule() != null) {
            mScheduleArrayList.addAll(mDBOperator.getAllSchedule());
        }
        journeyAdapter.notifyDataSetChanged();

        //getFromCloud(User.userDownloadJourney());

        /*mHash = new ArrayList<Integer>();
        strs = new ArrayList<String>();

        //mTagScheduleArrayList = new ArrayList<TagSchedule>();
        mDBOperator = new DataBaseOperator(this);
        mScheduleArrayList = mDBOperator.getAllSchedule();

        displayContent(mScheduleArrayList);

        *//* 实例化ArrayAdapter *//*
        journeyAdapter = new JourneyAdapter(this, mScheduleArrayList);

        mListView.setAdapter(journeyAdapter);*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_journey, menu);

         /* 设置菜单项的搜索项 */
        MenuItem searchItem = menu.findItem(R.id.journey_search);

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
            //mScheduleArrayList = mDBOperator.getScheduleByContent(query);
            mScheduleArrayList.clear();
            ArrayList<Schedule> temp = mDBOperator.getScheduleByContent(query);
            if(temp != null){
                mScheduleArrayList.addAll(temp);
            }
            //arrayAdapter.addAll(mPMArrayList);
            journeyAdapter.notifyDataSetChanged();
            //strs.clear();
            //journeyAdapter.notifyDataSetChanged();
            //displayContent(mScheduleArrayList);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.journey_plus:
                openJourneyAdd();
                return true;
            case R.id.journey_search:
                openJourneySearch();
                return true;
            case R.id.journey_sort:
                /*if (mScheduleArrayList != null && mScheduleArrayList.size() > 0) {
                    openJourneySort();
                }*/
                Collections.sort(mScheduleArrayList);
                journeyAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void deleteContent(int position) {
        //int deleteId = mHash.get(position);
        //mHash.remove(position);
        int id = mScheduleArrayList.get(position).getScheduleId();
        mDBOperator.deleteScheduleById(id);
        mScheduleArrayList.remove(position);
        journeyAdapter.notifyDataSetChanged();
    }


    public void sendId(int position) {
        /*int sendId = mHash.get(position);
        Intent  intent = new Intent(this, JourneyDetail.class);
        intent.putExtra("pa.journey.manage.detail", sendId);

        startActivity(intent);*/
        Schedule schedule = mScheduleArrayList.get(position);
        Intent  intent = new Intent(this, JourneyDetail.class);
        intent.putExtra("pa.journey.manage.detail", schedule.getScheduleId());
        startActivity(intent);
    }


    public void openJourneyAdd(){
        Intent intent = new Intent(JourneyManage.this, AddJourney.class);
        startActivity(intent);
    }


    public void openJourneySearch(){

    }


    public void openJourneySort(){
        strs.clear();
        journeyAdapter.notifyDataSetChanged();
        Collections.sort(mScheduleArrayList);
        displayContent(mScheduleArrayList);
    }


    public void displayContent(ArrayList<Schedule> list) {
        if(list == null){
            Toast.makeText(JourneyManage.this, "无内容", Toast.LENGTH_SHORT).show();
        }
        else {
            String tempStr = "";
            String tempContent = "";
            Schedule tempSch = null;
            for (int i = 0; i < list.size(); ++ i) {
                tempSch = list.get(i);
                tempStr =  tempSch.getDate() + " " +
                           tempSch.getTime() + "\n";

                tempContent = tempSch.getContent();

                int maxLen = 20;
                if (tempContent.length() > maxLen) {
                    tempContent = tempContent.substring(0, maxLen - 1);
                }
                //mHash.add(tempSch.getScheduleId());
                strs.add(tempStr + tempContent);
            }
        }
    }

    /* 从云端获取 */
    private void getFromCloud(RequestParams params) {

        /* 发送到的url */
        String url = "getjourney/";

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

                                String date = ith.getString("date");
                                String time = ith.getString("time");
                                String noti = ith.getString("noti");
                                String extra = ith.getString("extra");

                                strs.add("日期："+date+"\n"
                                        +"时间："+time+"\n"
                                        +"提醒方式："+noti+"\n"
                                        +"备注："+extra+"\n"
                                        +"<云端存储>"
                                );
                            }
                            journeyAdapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    Toast.makeText(JourneyManage.this, "exception", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                if(User.mLoggedIn) {
                    /* 超时提示 */
                    Toast.makeText(JourneyManage.this, "连接超时，请检查网络连接", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
