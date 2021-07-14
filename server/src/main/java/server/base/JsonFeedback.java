package server.base;

/**
 * @Author qi.li
 * @create 2020/9/1 17:21
 */
public class JsonFeedback {

    private int code;

    private String msg;

    private Object data;

    private Object other;

    public JsonFeedback() {
        super();
        this.setCode(ResponseCode.OK);
    }

    public JsonFeedback(ResponseCode code) {
        super();
        this.setCode(code);
    }

    public JsonFeedback(Object data) {
        super();
        this.setCode(ResponseCode.OK);
        this.setData(data);
    }

    public JsonFeedback(ResponseCode code, Object data, Object other) {
        super();
        this.setCode(code);
        this.setData(data);
        this.setOther(other);
    }
    public JsonFeedback(int code, String msg) {
        super();
        this.setCode(code);
        this.setMsg(msg);
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setCode(ResponseCode code) {
        this.code = code.getCode();
        this.msg = code.getMsg();
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getOther() {
        return other;
    }

    public void setOther(Object other) {
        this.other = other;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
