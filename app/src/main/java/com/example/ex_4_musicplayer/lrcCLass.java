package com.example.ex_4_musicplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class lrcCLass {
    private String minute;
    private String second;
    private String oneLrc;
    private String  millisecond;
    private long time1;

    public void setTime1(long time1) {
        this.time1 = time1;
    }

    public void setMillisecond(String millisecond) {
        this.millisecond = millisecond;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public void setOneLrc(String oneLrc) {
        this.oneLrc = oneLrc;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public long getTime1() {
        return time1;
    }

    public String getMinute() {
        return minute;
    }

    public String getMillisecond() {
        return millisecond;
    }

    public String getSecond() {
        return second;
    }

    public String getOneLrc() {
        return oneLrc;
    }
}
