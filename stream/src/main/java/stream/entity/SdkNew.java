package stream.entity;

import lombok.Data;


@Data
public class SdkNew {


    //* @param {String} t 时间戳
    private int year;                     //年份
    private int quarter;                  //季度
    private int month;                    //月份
    //private int dayofweek;                //一周的第几天
    //private int dayofmonth;                //一周的第几天

    //@param {String} uu 32位uuid
    private String uu;

    //  @param {Object} u userInfo
    private String unionId;  //// 公众号unionid ;
    private String openId;  //小程序的openid
   // private String mobiePhone;  //手机号

    //  @param {Object} ai 小程序版本信息
    private String appId;
    // @param {String} k appkey
    private String appkey;
    private String enVersion;
    private String version;


    // * @param {Object} ui 微信API-userInfo
    // private String nickname;    //昵称
    //  private String language;    //语言
    // private String gender;      //性别 0：未知、1：男、2：女
    // private String province;    //微信用户注册省份
    // private String city;        //微信用户注册城市
    // private String country;     //微信用户国籍信息
    //private String avatarurl;   //头像地址


    //* @param {Object} ao app-onshow-options
    private String path;                  // 每次打开小程序的所在页
    private int scene;                    //场景值
    private String query;                  // ao范畴 每次打开小程序的所在页 所带的参数 数组形式  但存储采用string

    //* @param {String} p 当前track页面path
    private String visitPath;          //一个track内当前页面所在路径


    //* @param {Object} b 本轮访问标识
    private String back;

    //* @param {String} tv 当前track发起环境：app page share event
    private String tv;
    //* @param {String} tl 当前track事件类型：show hide share error 404 eventId等
    private String tl;

    // * @param {Number} c 本轮track计数
    private int track;


    private  Long t;           //获取时间戳

    private String time;               //时间戳

    private int retry;

    private  String dt;                // 分区字段

    private String tt;

    // private String sourcename;
    private String reqIp;              //真实的ip地址

    // 关于新定义的指标
    //private int depth;
    private int entryPage;
    //private int exitpage;
    //private int accesspage;
    //private int openapp;
    // private int pagetime;
    //private int apptime;
    //private int sharetime;
    private String shareFrom;
    private String shareTitle;
    private String sharePath;
    private int isNewMember;

    //小程序标识事件标识
    private int appShow;
    private int pageShow;
    private int pageShare;
    private int appHide;
    private int pageHide;
    private int eventClick;
    private int getShop;
    private int addToCart;
    private int customOrder;
    private int search;

    //* @param {Array}  te 自定义event的参数   采用string存储
    private String te;

    //* @param {Array}  q 当前track页面路由参数   存储采用string
    private String q;


    //小程序动态列的解析
    private String paramTe;
    private String paramQ;

    // * @param {Object} l 微信API-location
    private double latitude;    //纬度，范围为 -90~90，负数表示南纬
    private double longitude;    //经度，范围为 -180~180，负数表示西经
    // private int speed;        //速度，单位 m/s
    // private int accuracy;     //位置的精确度
    // private int altitude;    //高度，单位 m
    // private int verticalaccuracy;         //垂直精度，单位 m（Android 无法获取，返回 0）
    //private int horizontalaccuracy;       // 水平精度，单位 m

    // * @param {Object} si 微信API-systemInfo
    private String model;                  //    设备型号
    private String brand;                  //  设备品牌
    //private String pixelratio;             //  设备像素比
    // private int screenwidth;               //  屏幕宽度，单位px
    // private int screenheight;              //  屏幕高度，单位px
    //private String version;                //微信版本号
    private String systems;
    private String platForm;               //客户端平台
    //private int fontsizesetting;           //用户字体大小（单位px）
    //private String sdkversion;             //客户端基础库版本


    //* @param {Object} nw 微信API-network
    private String networktype;           //网络类型





    //@param {String} v 本地sdk版本号
    //private String v;

}
