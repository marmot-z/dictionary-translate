package priv.zxw.dictranslate.demo.entity;

/**
 * enum description
 *
 * @author zhangxunwei
 * @date 2021/7/20
 */
public enum DemoEnum {
    FIRSET(1, "第一个"),
    SECOND(1, "第一个"),
    THREE(1, "第一个");

    private int code;
    private String value;

    DemoEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static DemoEnum get(Integer code) {
        for (DemoEnum value : DemoEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("无此枚举");
    }
}
