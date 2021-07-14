package server.base;

import java.util.List;

public class TrackLogVO {
    /// <summary>
    /// 小程序用户授权信息
    /// </summary>
    public WechatUserInfoReq ui;

    /// <summary>
    /// 微信系统信息
    /// </summary>
    public WechtSystemInfoReq si;

    /// <summary>
    /// 网络信息
    /// </summary>
    public NetworkInfoReq nw;

    /// <summary>
    /// 地理位置
    /// </summary>
    public WechatLocationInfo l;

    /// <summary>
    /// app-onshow-options
    /// 用户进入小程序app事件信息
    /// </summary>
    public AppOnShowOptionInfo ao;

    /// <summary>
    /// 当前访问页面path
    /// </summary>
    public String p;

    /// <summary>
    /// 来源信息
    /// </summary>
    public String SourceName;
    /// <summary>
    /// 当前访问页面的路由参数
    /// </summary>
    public List<MyTrackQueryPair> q;

    /// <summary>
    /// 用户上报用户关键信息
    /// </summary>
    public UploadWechatUserInfo u;
    /// <summary>
    /// 当前track发起环境：app(show hide error 404) page（show load hide share） event(自定义eventid或"") share(暂不使用)
    /// 上报用户信息时为：tv:event, tl为""
    /// </summary>
    public String tv;

    /// <summary>
    /// 当前track事件类型：show load hide share error 404 以及自定义的事件eventid等
    /// </summary>
    public String tl;
    /// <summary>
    /// 自定义event的参数
    /// </summary>
    public List<MyTrackQueryPair> te;

    /// <summary>
    /// 备用参数
    /// </summary>
    public String b;

    public String ReqIP;
}


/// <summary>
/// 小程序用户授权信息
/// </summary>
class WechatUserInfoReq {
    public String nickName;
    public int gender;
    public String language;
    public String city;
    public String province;
    public String country;
    public String avatarUrl;
}

/// <summary>
/// 用户手机系统信息
/// </summary>
class WechtSystemInfoReq {
    /// <summary>
    /// 设备型号
    /// </summary>
    public String model;
    public String brand;
    /// <summary>
    /// 设备像素比
    /// </summary>
    public String pixelRatio;
    public int screenWidth;
    public int screenHeight;
    public String language;
    /// <summary>
    /// 微信版本号
    /// </summary>
    public String version;
    /// <summary>
    /// 操作系统及版本
    /// </summary>
    public String system;
    /// <summary>
    /// 客户端平台
    /// </summary>
    public String platform;
    /// <summary>
    /// 用户字体大小（单位px
    /// </summary>
    public int fontSizeSetting;
    /// <summary>
    /// 客户端基础库版本
    /// </summary>
    public String SDKVersion;
}

/// <summary>
/// 网络信息
/// </summary>
class NetworkInfoReq {
    public String networkType;
}

/// <summary>
/// 用户进入小程序app事件信息
/// "path": "pages/memberCenter/index/index","query": {"scene": "invoice","trackcode": "123",xxx:xxx},"scene": 1001
/// </summary>
class AppOnShowOptionInfo {
    public String path;
    public List<MyTrackQueryPair> query;
    public int scene;
    public AppReferrerInfo referrerinfo;
}

class AppReferrerInfo {
    public String appId;
    //public object extraData ;
}

class ReferrerExtraDataInfo {

}

/// <summary>
/// 用户地理位置信息
/// </summary>
class WechatLocationInfo {
    /// <summary>
    /// 纬度，范围为 -90~90，负数表示南纬
    /// </summary>
    public double latitude;
    /// <summary>
    /// 经度，范围为 -180~180，负数表示西经
    /// </summary>
    public double longitude;
    /// <summary>
    /// 速度，单位 m/s
    /// </summary>
    public String speed;
    /// <summary>
    /// 位置的精确度
    /// </summary>
    public String accuracy;
    /// <summary>
    /// 高度，单位 m
    /// </summary>
    public String altitude;
    /// <summary>
    /// 垂直精度，单位 m（Android 无法获取，返回 0）
    /// </summary>
    public String verticalAccuracy;
    /// <summary>
    /// 水平精度，单位 m
    /// </summary>
    public String horizontalAccuracy;
}

/// <summary>
/// 上报的用户微信信息
/// </summary>
class UploadWechatUserInfo {
    /// <summary>
    /// openid
    /// </summary>
    public String o;
    /// <summary>
    /// unionid
    /// </summary>
    public String u;
    /// <summary>
    /// mobile
    /// </summary>
    public String m;
}

class MyTrackQueryPair {
    public String k;
    public String v;
    /// <summary>
    /// 字段 v 的数据类型
    /// </summary>
    public String t;
}
