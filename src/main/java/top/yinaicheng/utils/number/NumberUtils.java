package top.yinaicheng.utils.number;

import java.util.Random;
import java.util.function.Function;

/**
 * 数字工具类
 * @author yinaicheng
 */
public class NumberUtils {

    private static final Random RANDOM = new Random();

    /**
     * 生成整数值的函数，基于输入数组生成随机数
     */
    public static final Function<int[], Integer> GENERATE_INTEGER_VALUE_FUNCTION = (array) -> {
        if (array == null || array.length == 0) {
            return 0;
        }
        if (array.length == 1) {
            return array[0];
        }
        
        int min = array[0];
        int max = array[1];
        
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        
        return min + RANDOM.nextInt(max - min + 1);
    };

    /**
     * 检查字符串是否为数字
     * 
     * @param str 待检查的字符串
     * @return 如果字符串表示数字，返回true
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 检查字符串是否为整数
     * 
     * @param str 待检查的字符串
     * @return 如果字符串表示整数，返回true
     */
    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 安全地将字符串转换为整数
     * 
     * @param str 待转换的字符串
     * @param defaultValue 默认值
     * @return 转换后的整数，如果转换失败则返回默认值
     */
    public static int toInt(String str, int defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 安全地将字符串转换为长整数
     * 
     * @param str 待转换的字符串
     * @param defaultValue 默认值
     * @return 转换后的长整数，如果转换失败则返回默认值
     */
    public static long toLong(String str, long defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 安全地将字符串转换为双精度浮点数
     * 
     * @param str 待转换的字符串
     * @param defaultValue 默认值
     * @return 转换后的双精度浮点数，如果转换失败则返回默认值
     */
    public static double toDouble(String str, double defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 生成指定范围内的随机整数
     * 
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机整数
     */
    public static int randomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("最小值不能大于最大值");
        }
        return min + RANDOM.nextInt(max - min + 1);
    }

    /**
     * 生成指定范围内的随机长整数
     * 
     * @param min 最小值（包含）
     * @param max 最大值（不包含）
     * @return 随机长整数
     */
    public static long randomLong(long min, long max) {
        if (min >= max) {
            throw new IllegalArgumentException("最小值必须小于最大值");
        }
        return min + (long) (RANDOM.nextDouble() * (max - min));
    }

    /**
     * 比较两个数字
     * 
     * @param a 第一个数字
     * @param b 第二个数字
     * @return 如果a>b返回1，a<b返回-1，a=b返回0
     */
    public static int compare(Number a, Number b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        
        double doubleA = a.doubleValue();
        double doubleB = b.doubleValue();
        
        return Double.compare(doubleA, doubleB);
    }

    /**
     * 获取两个数字中的最大值
     * 
     * @param a 第一个数字
     * @param b 第二个数字
     * @return 最大值
     */
    public static Number max(Number a, Number b) {
        return compare(a, b) >= 0 ? a : b;
    }

    /**
     * 获取两个数字中的最小值
     * 
     * @param a 第一个数字
     * @param b 第二个数字
     * @return 最小值
     */
    public static Number min(Number a, Number b) {
        return compare(a, b) <= 0 ? a : b;
    }
}