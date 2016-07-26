package com.example.guru.pa;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import android.app.AlertDialog;
import android.database.sqlite.SQLiteException;
import android.database.Cursor;
import android.widget.Toast;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import cz.msebera.android.httpclient.Header;

public class LogIn extends AppCompatActivity {
    private EditText edname;
    private EditText edpassword;
    private Button login;
    private SVProgressHUD mSVProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* 添加返回按钮 */
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        mSVProgressHUD = new SVProgressHUD(this);

        edname = (EditText) findViewById(R.id.editText);
        edpassword = (EditText) findViewById(R.id.editText2);
        login = (Button) findViewById(R.id.login);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edname.getText().toString();
                String password = edpassword.getText().toString();

                if (name.equals("") || password.equals("")){
                    Toast.makeText(LogIn.this,"用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    /* 用户登陆 */
                    onLogIn("login/", User.userLogIn(name, password));
                    mSVProgressHUD.showWithStatus("正在登陆...");

                }

            }
        });

        /* 注册按钮 */
        TextView regist = (TextView) findViewById(R.id.regist);
        if(regist != null){
            regist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LogIn.this, Register.class);
                    startActivity(intent);
                }
            });
        }
    }

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            LogIn.this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    /* 登陆 */
    private void onLogIn(String url, RequestParams params) {

        HttpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String status = null;
                String res = "11";

                try {
                    status = response.getString("status");
                    res = response.getString("response");

                    switch(status) {
                        case "10000":
                            User.mLoggedIn = true;
                            User.mUsername = response.getString("username");
                            User.mToken = response.getString("token");
                            onGetInfo("getuserinfo/",User.userGetInfo());
                            LogIn.this.onBackPressed();
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    Toast.makeText(LogIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(LogIn.this, res, Toast.LENGTH_SHORT).show();
                mSVProgressHUD.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Toast.makeText(LogIn.this, "连接超时", Toast.LENGTH_SHORT).show();
                mSVProgressHUD.dismiss();
            }
        });
    }

    /* 获取用户信息 */
    private void onGetInfo(String url, RequestParams params) {

        HttpClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String status = "-1";
                String res = "获取失败";
                try {
                    status = response.getString("status");
                    res = response.getString("response");
                    if(status.equals("666")) {
                        User.mUsername = response.getString("username");
                        User.mNickname = response.getString("nickname");
                        User.mSex = response.getInt("sex");
                        User.mExtra = response.getString("extra");
                        User.userSave();
                    }
                } catch (JSONException e) {
                    Toast.makeText(LogIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(LogIn.this, res, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

}
