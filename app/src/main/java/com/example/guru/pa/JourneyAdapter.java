package com.example.guru.pa;

import android.content.Context;
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
public class JourneyAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<Schedule> mList;
    private LayoutInflater mLayoutInflater = null;

    public JourneyAdapter(Context context, ArrayList<Schedule> list) {
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

        completeListViewHolder.tv1.setText(mList.get(position).getDate());
        completeListViewHolder.tv2.setText("时间："+String.valueOf(mList.get(position).getTime()));
        completeListViewHolder.tv3.setText("内容："+String.valueOf(mList.get(position).getContent()));

        /*if(mList.get(position).getLocal()) {
            completeListViewHolder.iv1.setImageResource(R.drawable.android);
        } else {
            completeListViewHolder.iv1.setImageResource(R.drawable.cloud);
        }*/

        completeListViewHolder.iv1.setImageResource(R.drawable.android);

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
