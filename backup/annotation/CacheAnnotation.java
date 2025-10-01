package top.yinaicheng.annotation;
import top.yinaicheng.constant.CachedOperationTypeEnum;

import java.lang.annotation.*;
/**
 * 自定义缓存注解
 * @author yinaicheng
 */
/*注解的作用目标为可以用在所有使用Type的地方*/
@Target({ElementType.TYPE, ElementType.METHOD})
/*注解会在class字节码文件中存在，在运行时可以通过反射获取到*/
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CacheAnnotation
{

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

}
