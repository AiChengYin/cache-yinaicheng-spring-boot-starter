package top.yinaicheng.aspect;
import com.google.common.collect.ImmutableList;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.yinaicheng.annotation.DistributedLimitTrafficAnnotation;
import top.yinaicheng.config.LimitProperties;
import top.yinaicheng.constant.DistributedLimitTrafficTypeEnum;
import top.yinaicheng.exception.SystemException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Optional;

/**
 * 分布式限流Aspect
 * @author yinaicheng
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@Aspect
@Component
@ConditionalOnProperty(prefix = "yinaicheng.limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DistributedLimitTrafficAspect {

    private static final Logger log = LoggerFactory.getLogger(DistributedLimitTrafficAspect.class);

    /**
     * execution表达式常量值
     */
    private static final String CUT_OFF_POINT = "execution(* *(..)) && @annotation(top.yinaicheng.annotation.DistributedLimitTrafficAnnotation)";

    private final RedisTemplate<String, Object> redisTemplate;
    private final LimitProperties limitProperties;

    private static final String UNKNOWN = "unknown";

    @Autowired
    public DistributedLimitTrafficAspect(@Qualifier("limitRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                         LimitProperties limitProperties) {
        this.redisTemplate = redisTemplate;
        this.limitProperties = limitProperties;
    }

    /**
     * 控制层切点，声明公共切入点
     */
    @Pointcut(CUT_OFF_POINT)
    public void distributedLimitTrafficAspect() {
    }

    /**
     * 声明环绕通知，记录请求数据
     */
    @Around("distributedLimitTrafficAspect()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 获取当前方法和参数
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        
        // 如果不存在分布式限流注解，直接返回执行结果
        Optional<DistributedLimitTrafficAnnotation> annotationOptional = 
            Optional.ofNullable(methodSignature.getMethod().getAnnotation(DistributedLimitTrafficAnnotation.class));
        if (!annotationOptional.isPresent()) {
            return proceedingJoinPoint.proceed();
        }
        
        DistributedLimitTrafficAnnotation annotation = annotationOptional.get();
        
        // 检查是否启用
        if (!annotation.enabled() || !limitProperties.isEnabled()) {
            return proceedingJoinPoint.proceed();
        }
        
        DistributedLimitTrafficTypeEnum distributedLimitTrafficTypeEnum = annotation.distributedLimitTrafficTypeEnum();
        String description = annotation.description();
        int limitPeriod = annotation.period();
        int limitCount = annotation.count();
        String algorithm = annotation.algorithm();

        // 根据限流类型获取不同的key，如果不传我们会以方法名作为key
        String key = generateLimitKey(annotation, proceedingJoinPoint, distributedLimitTrafficTypeEnum);
        key = annotation.keyPrefix() + key;
        
        ImmutableList<String> keys = ImmutableList.of(key);
        
        try {
            String luaScript = buildLuaScript(algorithm);
            RedisScript<Number> redisScript = new DefaultRedisScript<>(luaScript, Number.class);
            Number count = (Number) redisTemplate.execute(redisScript, keys, limitCount, limitPeriod);
            
            log.info("分布式限流描述：{}，key为：{}，时间范围：{}，时间范围内最多访问次数：{}，当前访问次数：{}", 
                    description, key, limitPeriod, limitCount, count);
                    
            if (count != null && count.intValue() <= limitCount) {
                return proceedingJoinPoint.proceed();
            } else {
                String message = StringUtils.hasText(annotation.message()) ? 
                    annotation.message() : 
                    MessageFormat.format("{0}秒时间范围内访问数量{1}，限制数量{2}，访问频繁，请稍后访问", 
                            limitPeriod, count != null ? count.intValue() : 0, limitCount);
                throw new SystemException(message);
            }
        } catch (Exception e) {
            if (e instanceof SystemException) {
                throw e;
            }
            log.error("限流处理异常: {}", e.getMessage(), e);
            throw new SystemException("限流处理异常: " + e.getMessage());
        }
    }

    /**
     * 生成限流key
     */
    private String generateLimitKey(DistributedLimitTrafficAnnotation annotation, 
                                   ProceedingJoinPoint proceedingJoinPoint, 
                                   DistributedLimitTrafficTypeEnum typeEnum) {
        String key;
        switch (typeEnum) {
            case REQUESTER_IP:
                key = getIpAddress();
                break;
            case CUSTOM_KEY:
                key = annotation.key();
                // 支持SpEL表达式
                if (StringUtils.hasText(key) && key.contains("#")) {
                    key = parseSpelExpression(key, proceedingJoinPoint);
                }
                break;
            default:
                MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
                key = methodSignature.getMethod().getName().toUpperCase();
        }
        
        return StringUtils.hasText(key) ? key : "default";
    }

    /**
     * 解析SpEL表达式
     */
    private String parseSpelExpression(String expression, ProceedingJoinPoint proceedingJoinPoint) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(expression);
            EvaluationContext context = new StandardEvaluationContext();

            // 设置方法参数
            MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
            Method method = methodSignature.getMethod();
            DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
            String[] parameterNames = discoverer.getParameterNames(method);
            Object[] args = proceedingJoinPoint.getArgs();

            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }

            Object result = exp.getValue(context);
            return result != null ? result.toString() : "null";
        } catch (Exception e) {
            log.warn("SpEL表达式解析失败: {}, 错误: {}", expression, e.getMessage());
            return expression;
        }
    }

    /**
     * 编写Redis Lua限流脚本
     */
    public String buildLuaScript(String algorithm) {
        if ("sliding_window".equals(algorithm)) {
            return buildSlidingWindowScript();
        } else {
            return buildFixedWindowScript();
        }
    }

    /**
     * 固定窗口算法脚本
     */
    private String buildFixedWindowScript() {
        StringBuilder lua = new StringBuilder();
        lua.append("local c");
        lua.append("\nc = redis.call('get',KEYS[1])");
        // 调用不超过最大值，则直接返回
        lua.append("\nif c and tonumber(c) > tonumber(ARGV[1]) then");
        lua.append("\nreturn c;");
        lua.append("\nend");
        // 执行计算器自加
        lua.append("\nc = redis.call('incr',KEYS[1])");
        lua.append("\nif tonumber(c) == 1 then");
        // 从第一次调用开始限流，设置对应键值的过期
        lua.append("\nredis.call('expire',KEYS[1],ARGV[2])");
        lua.append("\nend");
        lua.append("\nreturn c;");
        return lua.toString();
    }

    /**
     * 滑动窗口算法脚本
     */
    private String buildSlidingWindowScript() {
        StringBuilder lua = new StringBuilder();
        lua.append("local key = KEYS[1]");
        lua.append("\nlocal limit = tonumber(ARGV[1])");
        lua.append("\nlocal window = tonumber(ARGV[2])");
        lua.append("\nlocal current = tonumber(redis.call('time')[1])");
        // 删除窗口外的数据
        lua.append("\nredis.call('zremrangebyscore', key, 0, current - window)");
        // 获取当前窗口内的请求数
        lua.append("\nlocal currentRequests = redis.call('zcard', key)");
        lua.append("\nif currentRequests < limit then");
        // 添加当前请求
        lua.append("\nredis.call('zadd', key, current, current)");
        lua.append("\nredis.call('expire', key, window)");
        lua.append("\nreturn currentRequests + 1");
        lua.append("\nelse");
        lua.append("\nreturn currentRequests");
        lua.append("\nend");
        return lua.toString();
    }

    /**
     * 声明异常通知
     */
    @AfterThrowing(value = "distributedLimitTrafficAspect()", throwing = "throwable")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable throwable) {
        log.error(new StringBuffer()
                .append("异常方法：").append(getMethodName((ProceedingJoinPoint) joinPoint))
                .append(" ; ").append("异常信息：").append(throwable.getMessage()).toString());
    }

    /**
     * 获取方法名
     */
    private static String getMethodName(ProceedingJoinPoint proceedingJoinPoint) {
        return proceedingJoinPoint.getSignature().getName();
    }

    /**
     * 获取IP地址
     */
    public String getIpAddress() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            log.warn("获取IP地址失败: {}", e.getMessage());
            return "unknown_ip";
        }
    }
}