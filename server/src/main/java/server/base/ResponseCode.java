package server.base;

/**
 * @Author qi.li
 * @create 2020/9/1 17:20
 */
public enum ResponseCode {

    /*------Base Code------*/
    OK(200, "ok"),
    SUCCESS_INSERT_REDIS_STREAM(0,"success insert redis stream"),
    Fail(-1, "fail"),
    Kafka_Connect_ExCeption(-2,"redis connnect exception"),
    Failed_to_Parse_Parameters(-3,"Failed to parse parameters"),

    Cannot_Insert_Boolean_Filter(-4,"can not insert redis boolan filter"),
    Cannot_Insert_Redis_Stream(-5,"can not insert redis stresm"),
    Redis_Connect_ExCeption(-6,"redis connnect exception"),

    MissParam(-100, "parameter is error"),
    FILE_NOTFOUND_ERROR(1000, "file notfound error"),
    IO_EXCEPTION(1001, "IO transport exception"),
    JEDIS_EXCEPTION(1002, "jedis connect exception"),
    CANT_FIND_RECORD(5, "cant find record"),
    TOO_MANY_RECORD(6, "too many record"),
    CANT_FIND_DEVICE(7,"cant find device");


    private int code;
    private String msg;

    ResponseCode(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public static ResponseCode getResponsecodeByCode(int code){
        for (ResponseCode responseCode : ResponseCode.values()){
            if (code == responseCode.getCode()) return responseCode;
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMsg(){
        return msg;
    }


}
