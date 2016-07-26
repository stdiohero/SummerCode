package com.example.guru.pa;


/**
 * Created by Haoyu on 2016/7/19.
 */
public class Schedule implements Comparable<Schedule>{
    private String date;
    private String time;
    private String content;
    private int scheduleId;

    public Schedule(int scheduleId, String date, String time, String content){
        this.content = content;
        this.scheduleId = scheduleId;
        this.date =date;
        this.time = time;
    }

    public Schedule() {}


    public String getContent() {return content;}

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setDate(String date){
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public void setContent(String content) {this.content = content;}

    @Override
    public String toString() {
        return "时间: " + date + " " + time + "\n" + "内容:\n" + content;
    }

    @Override
    public int compareTo(Schedule b) {
        int ret = this.getDate().compareTo(b.getDate());
        if (ret == 0) {
            return -(this.getTime().compareTo(b.getTime()));
        }
        else
            return -ret;
    }
}