package top.yinaicheng.utils;

import java.util.function.Function;

/**
 * 异常值处理工具类
 * @author yinaicheng
 */
public class HandleAbnormalValue {

    /**
     * NULL转空字符串的函数
     */
    public static final Function<String, String> NULL_TO_EMPTY_FUNCTION = (value) -> {
        return value == null ? "" : value;
    };

    /**
     * NULL转指定默认值的函数
     */
    public static final Function<Object, String> NULL_TO_DEFAULT_FUNCTION = (value) -> {
        return value == null ? "default" : String.valueOf(value);
    };

    /**
     * 处理NULL值，转换为空字符串
     * 
     * @param value 待处理的值
     * @return 如果值为null返回空字符串，否则返回原值
     */
    public static String nullToEmpty(String value) {
        return NULL_TO_EMPTY_FUNCTION.apply(value);
    }

    /**
     * 处理NULL值，转换为指定的默认值
     * 
     * @param value 待处理的值
     * @param defaultValue 默认值
     * @return 如果值为null返回默认值，否则返回原值
     */
    public static String nullToDefault(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 处理NULL对象，转换为字符串
     * 
     * @param value 待处理的对象
     * @param defaultValue 默认值
     * @return 如果对象为null返回默认值，否则返回对象的字符串表示
     */
    public static String objectToString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String result = String.valueOf(value);
        return "null".equals(result) ? defaultValue : result;
    }

    /**
     * 安全的字符串转换
     * 
     * @param value 待转换的对象
     * @return 安全的字符串表示
     */
    public static String safeToString(Object value) {
        return objectToString(value, "");
    }

    /**
     * 处理空白字符串
     * 
     * @param value 待处理的字符串
     * @param defaultValue 默认值
     * @return 如果字符串为空或空白返回默认值，否则返回原值
     */
    public static String blankToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 数字安全转换
     * 
     * @param value 待转换的字符串
     * @param defaultValue 默认数值
     * @return 转换后的整数，失败时返回默认值
     */
    public static int safeToInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 长整数安全转换
     * 
     * @param value 待转换的字符串
     * @param defaultValue 默认数值
     * @return 转换后的长整数，失败时返回默认值
     */
    public static long safeToLong(String value, long defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 布尔值安全转换
     * 
     * @param value 待转换的字符串
     * @param defaultValue 默认布尔值
     * @return 转换后的布尔值
     */
    public static boolean safeToBoolean(String value, boolean defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        String trimmedValue = value.trim().toLowerCase();
        if ("true".equals(trimmedValue) || "1".equals(trimmedValue) || "yes".equals(trimmedValue)) {
            return true;
        } else if ("false".equals(trimmedValue) || "0".equals(trimmedValue) || "no".equals(trimmedValue)) {
            return false;
        }
        
        return defaultValue;
    }
}