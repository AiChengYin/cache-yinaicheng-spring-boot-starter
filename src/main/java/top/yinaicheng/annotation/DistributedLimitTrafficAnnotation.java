package top.yinaicheng.annotation;

import top.yinaicheng.constant.DistributedLimitTrafficTypeEnum;
import top.yinaicheng.utils.string.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式限流注解
 * @author yinaicheng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLimitTrafficAnnotation {

    /**
     * 描述
     */
    String description() default StringUtils.EMPTY_STRING;

    /**
     * key的前缀，防止key与其他缓存的key一致造成冲突
     */
    String keyPrefix() default "limit:";

    /**
     * key，支持SpEL表达式
     */
    String key() default "";

    /**
     * 给定的时间范围 单位（秒）
     */
    int period() default 60;

    /**
     * 一定时间内最多访问次数
     */
    int count() default 100;

    /**
     * 限流的类型
     */
    DistributedLimitTrafficTypeEnum distributedLimitTrafficTypeEnum() default DistributedLimitTrafficTypeEnum.CUSTOM_KEY;

    /**
     * 限流失败时的提示消息
     */
    String message() default "访问过于频繁，请稍后再试";

    /**
     * 是否启用限流
     */
    boolean enabled() default true;

    /**
     * 限流算法类型：fixed_window（固定窗口）、sliding_window（滑动窗口）
     */
    String algorithm() default "fixed_window";
}