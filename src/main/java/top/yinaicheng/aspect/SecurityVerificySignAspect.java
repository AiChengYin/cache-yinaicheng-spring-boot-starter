package top.yinaicheng.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.yinaicheng.annotation.SecurityVerificySignAnnotation;
import top.yinaicheng.config.SecurityProperties;
import top.yinaicheng.exception.UnAuthorizedException;
import top.yinaicheng.utils.HandleAbnormalValue;
import top.yinaicheng.utils.encrypt.MD5Utils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 安全验签Aspect
 * @author yinaicheng
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@Aspect
@Component
@ConditionalOnProperty(prefix = "yinaicheng.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityVerificySignAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecurityProperties securityProperties;

    @Autowired
    public SecurityVerificySignAspect(@Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                      SecurityProperties securityProperties) {
        this.redisTemplate = redisTemplate;
        this.securityProperties = securityProperties;
    }

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(SecurityVerificySignAspect.class);

    /**
     * execution表达式常量值
     */
    private static final String CUT_OFF_POINT = "execution(* *(..)) && @annotation(top.yinaicheng.annotation.SecurityVerificySignAnnotation)";

    /**
     * 控制层切点，声明公共切入点
     */
    @Pointcut(CUT_OFF_POINT)
    public void controllerAspect() {
    }

    /**
     * 声明环绕通知，记录请求数据
     */
    @Around("controllerAspect()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 获取当前方法和参数
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        
        // 如果不存在验签注解，直接返回执行结果
        Optional<SecurityVerificySignAnnotation> annotationOptional = 
            Optional.ofNullable(methodSignature.getMethod().getAnnotation(SecurityVerificySignAnnotation.class));
        if (!annotationOptional.isPresent()) {
            return proceedingJoinPoint.proceed();
        }

        SecurityVerificySignAnnotation annotation = annotationOptional.get();
        
        // 检查是否启用
        if (!annotation.enabled() || !securityProperties.isEnabled()) {
            return proceedingJoinPoint.proceed();
        }

        Map<String, String> needConfirmSignMap = getNeedConfirmSignMap(annotation);
        if (!needConfirmSignMap.isEmpty()) {
            // 获取时间戳
            String timestamp = needConfirmSignMap.get(annotation.timestampField());
            // 获取随机值
            String nonce = needConfirmSignMap.get(annotation.nonceField());
            // 获取签名
            String sign = needConfirmSignMap.get(annotation.signField());

            // 校验nonce
            if (annotation.enableNonce() && securityProperties.isEnableNonce()) {
                checkNonce(nonce, annotation);
            }
            
            // 校验时间戳
            if (annotation.enableTimestamp() && securityProperties.isEnableTimestamp()) {
                checkTimestamp(timestamp, annotation);
            }
            
            // 对nonce和timestamp进行验签
            if (annotation.enableSign() && securityProperties.isEnableSign()) {
                Map<String, Object> signMap = new HashMap<>(1);
                signMap.put("context", 
                    HandleAbnormalValue.NULL_TO_EMPTY_FUNCTION.apply(nonce) +
                    HandleAbnormalValue.NULL_TO_EMPTY_FUNCTION.apply(timestamp));
                checkSign(sign(getMapSignText(signMap), annotation), 
                         HandleAbnormalValue.NULL_TO_EMPTY_FUNCTION.apply(sign), annotation);
            }
            
            logger.info("验签通过");
            return proceedingJoinPoint.proceed();
        }
        
        // 请求头没有值，请求非法
        throw new UnAuthorizedException("REQUEST_ILLEGAL", annotation.message());
    }

    /**
     * 校验时间戳
     */
    private void checkTimestamp(String timestamp, SecurityVerificySignAnnotation annotation) {
        if (StringUtils.hasText(timestamp)) {
            // 获取开始时间
            Long beginTime = getCurrentTime();
            long validityPeriod = annotation.timestampValidityPeriod();
            if (validityPeriod <= 0) {
                validityPeriod = securityProperties.getDefaultTimestampValidityPeriod();
            }
            
            // http请求限制为指定时间内的请求
            if (beginTime - Long.parseLong(timestamp) > validityPeriod) {
                throw new UnAuthorizedException("TIMESTAMP_ILLEGAL", "时间戳已过期");
            }
        } else {
            throw new UnAuthorizedException("TIMESTAMP_IS_EMPTY", "时间戳不能为空");
        }
    }

    /**
     * 校验Sign
     */
    private void checkSign(String backEndSign, String frontEndSign, SecurityVerificySignAnnotation annotation) {
        if (StringUtils.isEmpty(backEndSign) || StringUtils.isEmpty(frontEndSign) || !backEndSign.equals(frontEndSign)) {
            throw new UnAuthorizedException("SIGN_ILLEGAL", annotation.message());
        }
    }

    /**
     * 校验nonce
     */
    private void checkNonce(String nonce, SecurityVerificySignAnnotation annotation) {
        if (StringUtils.isEmpty(nonce)) {
            throw new UnAuthorizedException("NOUNCE_IS_EMPTY", "随机数不能为空");
        }
        
        Object value = redisTemplate.opsForValue().get(nonce);
        if (value == null) {
            int validityPeriod = annotation.nonceValidityPeriod();
            if (validityPeriod <= 0) {
                validityPeriod = securityProperties.getDefaultNonceValidityPeriod();
            }
            redisTemplate.opsForValue().set(nonce, nonce, validityPeriod, TimeUnit.MINUTES);
        } else {
            // 如果从redis中找到nonce，nonce重复
            logger.error("Repeated nonce = {}", nonce);
            throw new UnAuthorizedException("NOUNCE_ILLEGAL", "随机数重复");
        }
    }

    /**
     * 获取当前时间，从1970-01-01T00:00:00Z至今的毫秒数
     */
    private Long getCurrentTime() {
        return Instant.now().toEpochMilli();
    }

    /**
     * 从请求头中获取Nonce、Timestamp、Sign
     */
    private Map<String, String> getNeedConfirmSignMap(SecurityVerificySignAnnotation annotation) {
        HttpServletRequest httpServletRequest;
        try {
            httpServletRequest = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("获取请求信息失败，原因是{}", exception.getMessage());
            throw new RuntimeException("GET_REQUEST_CONTEXT_ERROR");
        }
        
        Map<String, String> needConfirmRequestHeaderInfo;
        try {
            // 需要验签的请求头信息
            needConfirmRequestHeaderInfo = new HashMap<>(3);
            // 随机值，一段时间内不允许重复
            needConfirmRequestHeaderInfo.put(annotation.nonceField(), 
                httpServletRequest.getHeader(annotation.nonceField()));
            // 时间戳
            needConfirmRequestHeaderInfo.put(annotation.timestampField(), 
                httpServletRequest.getHeader(annotation.timestampField()));
            // 获取前端生成的签名，需要和后端进行比对
            needConfirmRequestHeaderInfo.put(annotation.signField(), 
                httpServletRequest.getHeader(annotation.signField()));
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("获取请求头信息失败，原因是{}", exception.getMessage());
            throw new UnAuthorizedException("REQUEST_HEADER_ILLEGAL", "请求头信息不合法");
        }
        return needConfirmRequestHeaderInfo;
    }

    /**
     * 组装成map字符串
     */
    private String getMapSignText(Map<String, Object> map) {
        TreeMap<String, Object> treeMap = new TreeMap<>(map);
        StringBuilder buffer = new StringBuilder();
        Iterator<Map.Entry<String, Object>> iterator;
        for (iterator = treeMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Object> entry = iterator.next();
            buffer.append(entry.getKey());
            buffer.append("=");
            buffer.append(entry.getValue().toString());
            buffer.append("&");
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        return buffer.toString();
    }

    /**
     * 签名方法
     */
    private String sign(String data, SecurityVerificySignAnnotation annotation) {
        String privateKey = securityProperties.getPrivateKey();
        data = data + privateKey;
        
        String algorithm = annotation.signAlgorithm();
        if (StringUtils.isEmpty(algorithm)) {
            algorithm = securityProperties.getDefaultSignAlgorithm();
        }
        
        switch (algorithm.toUpperCase()) {
            case "MD5":
            default:
                return MD5Utils.createSign(data);
        }
    }
}