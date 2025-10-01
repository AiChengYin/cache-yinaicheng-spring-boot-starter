package top.yinaicheng.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 安全验签注解
 * @author yinaicheng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityVerificySignAnnotation {

    /**
     * 是否启用时间戳验证
     */
    boolean enableTimestamp() default true;

    /**
     * 时间戳有效期（毫秒），默认3秒
     */
    long timestampValidityPeriod() default 3000L;

    /**
     * 是否启用随机数验证
     */
    boolean enableNonce() default true;

    /**
     * 随机数有效期（分钟），默认3分钟
     */
    int nonceValidityPeriod() default 3;

    /**
     * 签名算法，默认MD5
     */
    String signAlgorithm() default "MD5";

    /**
     * 是否启用签名验证
     */
    boolean enableSign() default true;

    /**
     * 验证失败时的提示消息
     */
    String message() default "签名验证失败";

    /**
     * 自定义签名字段名称
     */
    String signField() default "sign";

    /**
     * 自定义时间戳字段名称
     */
    String timestampField() default "timestamp";

    /**
     * 自定义随机数字段名称
     */
    String nonceField() default "nonce";

    /**
     * 是否启用验签功能
     */
    boolean enabled() default true;
}