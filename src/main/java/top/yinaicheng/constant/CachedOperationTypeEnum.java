package top.yinaicheng.constant;

/**
 * 缓存操作类型枚举
 * @author yinaicheng
 */
public enum CachedOperationTypeEnum {

    /**
     * 查询缓存
     */
    QUERY_CACHE("query_cache", "查询缓存"),

    /**
     * 通过key删除缓存
     */
    DELETE_CACHE_BY_KEY("delete_cache_by_key", "通过key删除缓存"),

    /**
     * 通过key前缀删除缓存
     */
    DELETE_CACHE_BY_KEY_PREFIX("delete_cache_by_key_prefix", "通过key前缀删除缓存");

    private final String code;
    private final String description;

    CachedOperationTypeEnum(String code, String description) {
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