package com.example.guru.pa;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Zaki on 2016/7/25.
 */
public class PasswordAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<PasswordMessage> mList;
    private LayoutInflater mLayoutInflater = null;

    public PasswordAdapter(Context context, ArrayList<PasswordMessage> list) {
        this.mContext = context;
        this.mList = list;
        mLayoutInflater  = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        CompleteListViewHolder completeListViewHolder;
        if(convertView == null) {
            v  = mLayoutInflater.inflate(R.layout.layout_listview, null);
            completeListViewHolder = new CompleteListViewHolder(v);
            v.setTag(completeListViewHolder);
        } else {
            completeListViewHolder = (CompleteListViewHolder) v.getTag();
        }

        completeListViewHolder.tv1.setText(mList.get(position).getPurpose());
        completeListViewHolder.tv2.setText("账号："+String.valueOf(mList.get(position).getUsername()));
        completeListViewHolder.tv3.setText("密码："+String.valueOf(mList.get(position).getPassword()));

        if(mList.get(position).getLocal()) {
            completeListViewHolder.iv1.setImageResource(R.drawable.android);
        } else {
            completeListViewHolder.iv1.setImageResource(R.drawable.cloud);
        }

        return v;
    }



    class CompleteListViewHolder {
        public TextView tv1;
        public TextView tv2;
        public TextView tv3;
        public ImageView iv1;

        public CompleteListViewHolder(View base) {
            tv1 = (TextView) base.findViewById(R.id.layout_listview_title);
            tv2 = (TextView) base.findViewById(R.id.layout_listview_c1);
            tv3 = (TextView) base.findViewById(R.id.layout_listview_c2);
            iv1 = (ImageView) base.findViewById(R.id.layout_listview_icon);
        }
    }
}
