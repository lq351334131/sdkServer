package server.base;

/**
 * @Author qi.li
 * @Date 2021/1/18 14:56
 */
public class Constants {

    /*------微信小程序的逻辑------*/
    public static final String APP = "app";
    public static final String PAGE = "page";
    public static final String SHOW = "show";
    public static final String HIDE = "hide";
    public static final String SHARE = "share";
    public static final String EVENT = "event";
    public static final String CLICK = "click";


    //电商事件tv='event'
    public static final String GETSHOP="GetShop";
    public static final String ADDTOCART="add_to_cart";
    public static final String CUSTOMORDER="custom_order";
    public static final String SEARCH="search";


    /*------微信小程序分享事件------*/
    public static final String SHAREFROM = "from";
    public static final String SHARETITLE = "title";
    public static final String SHAREPATH = "path";

    /*------微信小程序注册新会员------*/
    public static final String  INTO_MEMBER = "入会入口页面";
    //public static final String  DOWNLOAD_ = "壁纸下载";








    /*------关于REDIS布隆过滤器------*/
    // 布隆过滤器key1
    public static final String USER_ID_BIT_SET = "user_id_strhash_bloomfilter";
    // 初始化集合长度
    public static final int length = Integer.MAX_VALUE;
    // 准备hash计算次数
    public static final int HASH_LENGTH = 5;
    /**
     * 准备自定义哈希算法需要用到的质数，因为一条数据需要hash计算5次 且5次的结果要不一样
     */
    public static int[] primeNums = new int[]{17, 19, 29, 31, 37};
    public static final int MAXLEN = 150;

    /*------关于ip地址调用------*/
    public static final String IP_URL = "http://iploc.market.alicloudapi.com/v3/ip";




}
