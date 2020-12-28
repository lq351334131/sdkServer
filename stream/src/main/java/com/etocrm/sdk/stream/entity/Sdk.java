package com.etocrm.sdk.stream.entity;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.lang.reflect.Array;
import java.util.Date;

@Data
public class Sdk {


    private int year;                     //年份
    private int quarter;                  //季度
    private int month;                    //月份
    private int dayOfWeek;                //一周的第几天
    private int isfirstday;               // 是否新用户
    private String country;               //国籍
    private String tv;                    //当前track发起环境: qpp page event
    private String avatarurl;             //头像地址
    private int accuracy;                 //精确值
    private String language;              //语言
    private String networktype;           //网络类型
    private int verticalaccuracy;         //垂直精度
    private int scene;                    //场景值
    private String pixelratio;            //像素
    private int fontsizesetting;          //字体
    private String dt;                    //分区字段
    private String path;                  // ao范畴 每次打开小程序的所在页
    private String sdkversion;            //sdk版本号
    private String province;              //省份
    private String model;                 //手机型号
    private String brand;
    private double longitude;
    private String uu;
    private Array query;                  // ao范畴 每次打开小程序的所在页 所带的参数 数组形式  但存储采用string
    private String version;
    private String system;
    private String reqIp;                 //真实ip
    private String sharefrom;
    private int altitude;
    private String gender;
    private String sharepath;
    private String city;
    private double latitude;
    private String sharetitle;
    private String platform;
    private int speed;
    private String nickname;
    private int screenwidth;
    private String b;
    private int c;
    private int horizontalaccuracy;
    private String sourcename;
    private String k;
    private String m;
    private String o; // 小程序的openid
    private String p;
    private JSONArray q;
    private JSONArray te;
    private String t;
    private String u;   // 公众号unionid ;
    private String v;
    private String tl;
    private String referrerinfo;
    private int screenheight;
    private int newpage;
    private int depth;
    private int entrypage;
    private int exitpage;
    private int accesspage;
    private int openapp;
    private int pagetime;
    private int apptime;
    private int sharetime;



}

