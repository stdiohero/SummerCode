package com.example.guru.pa;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zaki on 2016/7/25.
 */
public class BillAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<BillVO> mList;
    private LayoutInflater mLayoutInflater = null;

    public BillAdapter(Context context, ArrayList<BillVO> list) {
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
        completeListViewHolder.tv2.setText("收入：￥ "+String.valueOf(mList.get(position).getIncome()));
        completeListViewHolder.tv3.setText("支出：￥ "+String.valueOf(mList.get(position).getExpend()));

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
