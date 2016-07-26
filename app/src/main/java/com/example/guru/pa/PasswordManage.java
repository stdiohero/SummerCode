package com.example.guru.pa;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import java.lang.String;

public class PasswordManage extends AppCompatActivity {

    private PasswordOperate mPasswordOperate;
    private ArrayList<PasswordMessage> mPMArrayList;
    private PasswordAdapter arrayAdapter;
    private SwipeMenuListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        /* ActionBar添加返回按钮 */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mPMArrayList = new ArrayList<PasswordMessage>();
        mPasswordOperate = new PasswordOperate(this);

        /* 实例化ArrayAdapter */
        arrayAdapter = new PasswordAdapter(this, mPMArrayList);

        /* 实例化SwipeMenuListView */
        mListView = (SwipeMenuListView) findViewById(R.id.password_list);

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
                openItem.setTitle("编辑");
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
                        // 编辑
                        Intent intent = new Intent(PasswordManage.this, AddPassword.class);
                        intent.putExtra("update_PM_id",mPMArrayList.get(position).getId());
                        intent.putExtra("local",mPMArrayList.get(position).getLocal());
                        intent.putExtra("purpose",mPMArrayList.get(position).getPurpose());
                        intent.putExtra("uname",mPMArrayList.get(position).getUsername());
                        intent.putExtra("passwd",mPMArrayList.get(position).getPassword());
                        intent.putExtra("extra",mPMArrayList.get(position).getExtra());
                        startActivity(intent);
                        //PasswordManage.this.finish();
                        break;
                    case 1:
                        // 删除
                        deleteContent(position);
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
        mPMArrayList.clear();
        if(mPasswordOperate.getAllPasswordMessage() == null){
        }
        else {
            mPMArrayList.addAll(mPasswordOperate.getAllPasswordMessage());
        }
        getFromCloud(User.userDownloadPassword());
        arrayAdapter.notifyDataSetChanged();
    }


    public void deleteContent(int position){
        boolean local = mPMArrayList.get(position).getLocal();
        int id = mPMArrayList.get(position).getId();
        if(local) {
            mPasswordOperate.deleteByid(id);
            mPMArrayList.remove(position);
            arrayAdapter.notifyDataSetChanged();
        } else {
            //Map<String, Integer> map = new HashMap<String, Integer>();
            //map.put("id",Integer.valueOf(id));
            RequestParams req = new RequestParams();
            req.put("username", User.mUsername);
            req.put("token", User.mToken);
            req.put("id",id);
            //
            deleteFromCloud(req,position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_password, menu);

         /* 设置菜单项的搜索项 */
        MenuItem searchItem = menu.findItem(R.id.password_search);

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
            //arrayAdapter.clear();
            mPMArrayList.clear();
            ArrayList<PasswordMessage> temp = mPasswordOperate.getAccountByPurpose(query);
            if(temp != null){
                mPMArrayList.addAll(temp);
            }
            //arrayAdapter.addAll(mPMArrayList);
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
            case R.id.password_plus:
                Intent intent = new Intent(PasswordManage.this, AddPassword.class);
                startActivity(intent);
                //PasswordManage.this.finish();
                return true;
            case android.R.id.home:
                PasswordManage.this.finish();
                return true;
            case R.id.password_search:

                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 从云端获取 */
    private void getFromCloud(RequestParams params) {

        /* 发送到的url */
        String url = "getpassword/";

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

                                Integer id = ith.getInt("id");
                                String purpose =DES3Utils.decryptMode(ith.getString("purpose"));
                                String uname = DES3Utils.decryptMode(ith.getString("uname"));
                                String passwd = DES3Utils.decryptMode(ith.getString("passwd"));
                                String extra = DES3Utils.decryptMode(ith.getString("extra"));
                                mPMArrayList.add(new PasswordMessage(id, false, purpose, uname, passwd, extra));
                            }
                            arrayAdapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    Toast.makeText(PasswordManage.this, "exception", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                if(User.mLoggedIn) {
                    /* 超时提示 */
                    Toast.makeText(PasswordManage.this, "云端加载超时", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /* 云端删除 */
    private void deleteFromCloud(RequestParams params, final int position) {

        /* 发送到的url */
        String url = "deletepassword/";

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
                            mPMArrayList.remove(position);
                            if(position == mPMArrayList.size()-1) {
                                mPMArrayList.add(new PasswordMessage(-1,true,"111","111","111","111"));
                            }
                            arrayAdapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    Toast.makeText(PasswordManage.this, "exception", Toast.LENGTH_SHORT).show();
                }

                /* 提示返回信息 */
                Toast.makeText(PasswordManage.this, res, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                /* 超时提示 */
                Toast.makeText(PasswordManage.this, "连接超时，请检查网络连接", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
