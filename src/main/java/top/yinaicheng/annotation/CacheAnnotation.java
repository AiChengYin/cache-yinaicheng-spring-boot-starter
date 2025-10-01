package top.yinaicheng.annotation;

import top.yinaicheng.constant.CachedOperationTypeEnum;

import java.lang.annotation.*;

/**
 * 自定义缓存注解
 * @author yinaicheng
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CacheAnnotation {

    /**
     * 要操作的缓存key前缀，只针对查询缓存业务有效
     */
    String operateCacheKeyPrefix() default "";

    /**
     * 要操作的缓存key，key的规则，可以使用SpringEl表达式，可以使用方法执行一些参数
     */
    String[] operateCacheKey() default "";

    /**
     * 判断是否要使用spel表达式，默认为spel表达式
     */
    boolean judgeSpel() default true;

    /**
     * 缓存时长（以分钟为单位），默认1分钟
     */
    int duration() default 1;

    /**
     * 缓存操作类别：增删改查，与具体业务操作相关
     */
    CachedOperationTypeEnum cacheOperateType() default CachedOperationTypeEnum.QUERY_CACHE;

    /**
     * 缓存名称，用于区分不同的缓存区域
     */
    String cacheName() default "default";

    /**
     * 是否允许缓存空值
     */
    boolean cacheNull() default false;

    /**
     * 缓存条件，支持SpEL表达式
     */
    String condition() default "";

    /**
     * 排除缓存条件，支持SpEL表达式
     */
    String unless() default "";

    /**
     * 缓存同步，用于防止缓存击穿
     */
    boolean sync() default false;
}