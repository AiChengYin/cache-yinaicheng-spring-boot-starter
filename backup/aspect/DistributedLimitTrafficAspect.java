package top.yinaicheng.aspect;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.yinaicheng.annotation.DistributedLimitTrafficAnnotation;
import top.yinaicheng.constant.DistributedLimitTrafficTypeEnum;
import top.yinaicheng.exception.SystemException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Optional;
/**
 * @author yinaicheng
 * @description 分布式限流Aspect
 */
@Order(Ordered.LOWEST_PRECEDENCE) // 可以使用@Order注解修饰Aspect类，值越小，优先级越高
@Aspect
@Component
@Getter
@Setter
public class DistributedLimitTrafficAspect
{

    /*日志*/
    private static final Logger log = LoggerFactory.getLogger(DistributedLimitTrafficAspect.class);

    /**execution表达式常量值*/
    private static final String CUT_OFF_POINT="execution(* *(..)) && @annotation(top.yinaicheng.annotation.DistributedLimitTrafficAnnotation)";

    private final RedisTemplate redisTemplate;

    private static final String UNKNOWN = "unknown";

    @Autowired
    public DistributedLimitTrafficAspect(@Qualifier("data_governance_redis") RedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;
    }

    /**
     * 控制层切点，声明公共切入点
     */
    @Pointcut(CUT_OFF_POINT)
    public void distributedLimitTrafficAspect(){}

    /**
     * 声明环绕通知，记录请求数据
     */
    @Around("distributedLimitTrafficAspect()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable /* ProceedingJoinPoint可以放行，JoinPoint不可以放行 */
    {
        /*获取当前方法和参数*/
        MethodSignature methodSignature=(MethodSignature)proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        /*如果不存在分布式限流注解，直接返回执行结果*/
        Optional<DistributedLimitTrafficAnnotation> distributedLimitTrafficAnnotationOptional=Optional.ofNullable(methodSignature.getMethod().getAnnotation(DistributedLimitTrafficAnnotation.class));
        if(!distributedLimitTrafficAnnotationOptional.isPresent()){
            return proceedingJoinPoint.proceed();
        }
        DistributedLimitTrafficAnnotation distributedLimitTrafficAnnotation=distributedLimitTrafficAnnotationOptional.get();
        DistributedLimitTrafficTypeEnum distributedLimitTrafficTypeEnum=distributedLimitTrafficAnnotation.distributedLimitTrafficTypeEnum();
        String description=distributedLimitTrafficAnnotation.description();
        int limitPeriod=distributedLimitTrafficAnnotation.period();
        int limitCount=distributedLimitTrafficAnnotation.count();

        /**
         * 根据限流类型获取不同的key ,如果不传我们会以方法名作为key
         */
        String key;
        switch (distributedLimitTrafficTypeEnum) {
            case REQUESTER_IP:
                key = getIpAddress();
                break;
            case CUSTOM_KEY:
                key = distributedLimitTrafficAnnotation.key();
                break;
            default:
                key = StringUtils.upperCase(method.getName());
        }
        key=distributedLimitTrafficAnnotation.keyPrefix().concat(key);
        ImmutableList<String> keys = ImmutableList.of(key);
        try {
            String luaScript = buildLuaScript();
            RedisScript<Number> redisScript = new DefaultRedisScript<>(luaScript, Number.class);
            Number count = (Number) redisTemplate.execute(redisScript, keys, limitCount, limitPeriod);
            log.info("分布式限流描述：{}，key为：{},时间范围：{}，时间范围内最多访问次数：{}",description,key,limitPeriod,limitCount);
            if (count != null && count.intValue() <= limitCount) {
                return proceedingJoinPoint.proceed();
            } else {
                throw new SystemException(MessageFormat.format("{0}秒时间范围内访问数量{1}，限制数量{2}，访问频繁，请稍后访问",limitPeriod,count.intValue(),limitCount));
            }
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw new RuntimeException(e.getLocalizedMessage());
            }
            throw new RuntimeException("server exception");
        }

    }

    /**
     * @author yinaicheng
     * @description 编写Redis Lua限流脚本
     */
    public String buildLuaScript() {
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
     * 声明异常通知
     */
    @AfterThrowing(value="distributedLimitTrafficAspect()",throwing="throwable")
    public void doAfterThrowing(JoinPoint joinPoint,Throwable throwable)
    {
        log.error(new StringBuffer().append("异常方法：").append(getMethodName((ProceedingJoinPoint)joinPoint)).append(" ; ").append("异常信息：").append(throwable.getMessage()).toString());
    }

    /**
     * 获取方法名
     */
    private static String getMethodName(ProceedingJoinPoint proceedingJoinPoint){
        return proceedingJoinPoint.getSignature().getName();
    }

    /**
     * @author fu
     * @description 获取id地址
     * @date 2020/4/8 13:24
     */
    public String getIpAddress() {
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
    }

}