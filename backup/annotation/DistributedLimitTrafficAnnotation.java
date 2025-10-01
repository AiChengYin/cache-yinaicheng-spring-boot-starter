package top.yinaicheng.annotation;
import top.yinaicheng.constant.DistributedLimitTrafficTypeEnum;
import top.yinaicheng.utils.string.StringUtils;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @author yinaicheng
 * @description 分布式限流注解
 */
/*注解的作用目标为方法*/
@Target(ElementType.METHOD)
/*注解会在class字节码文件中存在，在运行时可以通过反射获取到*/
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLimitTrafficAnnotation
{

    /**
     * 描述
     */
    String description() default StringUtils.EMPTY_STRING;

    /**
     * key的前缀，防止key与其他缓存的key一致造成冲突
     */
    String keyPrefix() default StringUtils.EMPTY_STRING;

    /**
     * key
     */
    String key() default StringUtils.EMPTY_STRING;

    /**
     * 给定的时间范围 单位（秒）
     */
    int period() default 60;

    /**
     * 一定时间内最多访问次数
     */
    int count() default 1024;

    /**
     * 限流的类型
     */
    DistributedLimitTrafficTypeEnum distributedLimitTrafficTypeEnum() default DistributedLimitTrafficTypeEnum.CUSTOM_KEY;

}