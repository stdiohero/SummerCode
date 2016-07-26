package com.example.guru.pa;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class AccountCenter extends AppCompatActivity {

    private Spinner mSex;
    private ArrayAdapter<String> adapter_account;
    private Button logout;
    private TextView mUsername;
    private TextView mNickname;
    private TextView mExtra;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountcenter);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        logout = (Button) findViewById(R.id.account_logout);
        if(logout != null) {
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLogOut("logout/",User.userLogout());

                }
            });
        }

        mSex = (Spinner) findViewById(R.id.account_sex);
        // 建立数据源
        String[] Items = getResources().getStringArray(R.array.spinner_account);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> _Adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, Items);

        _Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        mSex.setAdapter(_Adapter);

        mUsername = (TextView) findViewById(R.id.account_username);
        mNickname = (TextView) findViewById(R.id.account_nickname);
        mExtra = (TextView) findViewById(R.id.account_extra);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mUsername.setText(User.mUsername);
        mNickname.setText(User.mNickname);
        mExtra.setText(User.mExtra);
        mSex.setSelection(User.mSex);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        switch (id){
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* 登出 */
    private void onLogOut(String url, RequestParams params) {

        Intent intent2 = new Intent(AccountCenter.this, LogIn.class);
        startActivity(intent2);
        AccountCenter.this.finish();
        User.userReset();
        Toast.makeText(AccountCenter.this, "登出成功", Toast.LENGTH_SHORT).show();

        HttpClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String status = null;
                String res = "11";

                try {
                    status = response.getString("status");
                    res = response.getString("response");

                    switch(status) {
                        /*case "10002":

                            //Intent intent = new Intent(AccountCenter.this, MainActivity.class);
                            //startActivity(intent);
                            break;
                        case "10004":
                            User.userReset();
                            break;*/
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    Toast.makeText(AccountCenter.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                //Toast.makeText(AccountCenter.this, status+","+res, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }


}
