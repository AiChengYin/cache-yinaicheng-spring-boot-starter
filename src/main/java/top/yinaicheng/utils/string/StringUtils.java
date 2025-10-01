package top.yinaicheng.utils.string;

import java.util.function.BiFunction;

/**
 * 字符串工具类
 * @author yinaicheng
 */
public class StringUtils {

    /**
     * 空字符串常量
     */
    public static final String EMPTY_STRING = "";

    /**
     * 空白字符串常量
     */
    public static final String BLANK_STRING = " ";

    /**
     * 默认分隔符
     */
    public static final String DEFAULT_SEPARATOR = ",";

    /**
     * 将对象转换为字符串的函数
     */
    public static final BiFunction<Object, String, String> CONVERT_OBJECT_TO_STRING = (object, defaultValue) -> {
        if (object == null) {
            return defaultValue;
        }
        String result = String.valueOf(object);
        return result.equals("null") ? defaultValue : result;
    };

    /**
     * 检查字符串是否为空
     * 
     * @param str 待检查的字符串
     * @return 如果字符串为null或空字符串，返回true
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 检查字符串是否不为空
     * 
     * @param str 待检查的字符串
     * @return 如果字符串不为null且不为空字符串，返回true
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 检查字符串是否为空白
     * 
     * @param str 待检查的字符串
     * @return 如果字符串为null、空字符串或只包含空白字符，返回true
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查字符串是否不为空白
     * 
     * @param str 待检查的字符串
     * @return 如果字符串不为null、不为空字符串且不只包含空白字符，返回true
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 去除字符串前后的空白字符
     * 
     * @param str 待处理的字符串
     * @return 去除前后空白字符后的字符串，如果输入为null则返回null
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 安全的去除字符串前后的空白字符
     * 
     * @param str 待处理的字符串
     * @return 去除前后空白字符后的字符串，如果输入为null则返回空字符串
     */
    public static String trimToEmpty(String str) {
        return str == null ? EMPTY_STRING : str.trim();
    }

    /**
     * 字符串连接
     * 
     * @param separator 分隔符
     * @param elements 要连接的元素
     * @return 连接后的字符串
     */
    public static String join(String separator, String... elements) {
        if (elements == null || elements.length == 0) {
            return EMPTY_STRING;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            if (elements[i] != null) {
                sb.append(elements[i]);
            }
        }
        return sb.toString();
    }

    /**
     * 默认值处理
     * 
     * @param str 原字符串
     * @param defaultStr 默认值
     * @return 如果原字符串为空，返回默认值，否则返回原字符串
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * 默认值处理（空白字符）
     * 
     * @param str 原字符串
     * @param defaultStr 默认值
     * @return 如果原字符串为空白，返回默认值，否则返回原字符串
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }
}