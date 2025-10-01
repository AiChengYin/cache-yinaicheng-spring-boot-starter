package top.yinaicheng.constant;

/**
 * 分布式限流类型枚举
 * @author yinaicheng
 */
public enum DistributedLimitTrafficTypeEnum {

    /**
     * 根据请求者IP进行限流
     */
    REQUESTER_IP("requester_ip", "根据请求者IP进行限流"),

    /**
     * 根据自定义key进行限流
     */
    CUSTOM_KEY("custom_key", "根据自定义key进行限流");

    private final String code;
    private final String description;

    DistributedLimitTrafficTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}