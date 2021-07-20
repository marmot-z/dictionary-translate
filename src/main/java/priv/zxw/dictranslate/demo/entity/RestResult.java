package priv.zxw.dictranslate.demo.entity;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2021/7/20
 */
public class RestResult<T> {
    private T data;
    private String message;
    private int code;

    public RestResult(T data) {
        this.data = data;
        this.code = 200;
        this.message = "";
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
