package top.yinaicheng.aspect;
import cn.hutool.core.map.MapUtil;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.yinaicheng.annotation.SecurityVerificySignAnnotation;
import top.yinaicheng.exception.UnAuthorizedException;
import top.yinaicheng.utils.HandleAbnormalValue;
import top.yinaicheng.utils.encrypt.MD5Utils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
/**
 * @author yinaicheng
 * @description 安全验签
 * @date 2021-12-02
 */
@Order(Ordered.LOWEST_PRECEDENCE) // 可以使用@Order注解修饰Aspect类，值越小，优先级越高
@Aspect
@Component
@Getter
@Setter
@Configuration
@PropertySource("classpath:application.properties") /*指定配置文件*/
@ConfigurationProperties("security.verificy.sign") /*前缀=securityVerificySign，会在配置文件中寻找securityVerificySign.*的配置项*/
public class SecurityVerificySignAspect
{
    
    private final RedisTemplate redisTemplate;
    
    @Autowired
    public SecurityVerificySignAspect(@Qualifier("data_governance_redis") RedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;
    }

    /**
     * 密钥
     */
    private String privateKey;

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(SecurityVerificySignAspect.class);

    /**
     * execution表达式常量值
     */
    private static final String CUT_OFF_POINT="execution(* *(..)) && @annotation(top.yinaicheng.annotation.SecurityVerificySignAnnotation)";

    /**
    * 控制层切点，声明公共切入点
    */
    @Pointcut(CUT_OFF_POINT)
    public void controllerAspect(){}

    /**
    * 声明环绕通知，记录请求数据
    */
    @Around("controllerAspect()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable /* ProceedingJoinPoint可以放行，JoinPoint不可以放行 */
    {
        /*获取当前方法和参数*/
        MethodSignature methodSignature=(MethodSignature)proceedingJoinPoint.getSignature();
        /*如果不存在验签注解，直接返回执行结果*/
        Optional<SecurityVerificySignAnnotation> securityVerificySignAnnotationOptional=Optional.ofNullable(methodSignature.getMethod().getAnnotation(SecurityVerificySignAnnotation.class));
        if(!securityVerificySignAnnotationOptional.isPresent()){
            return proceedingJoinPoint.proceed();
        }
        Map<String,String> needConfirmSignMap=getNeedConfirmSignMap();
        if(MapUtil.isNotEmpty(needConfirmSignMap)){
            /*获取时间戳*/
            String timestamp=needConfirmSignMap.get("timestamp");
            /*获取随机值*/
            String nounce=needConfirmSignMap.get("nounce");
            /*校验nounce*/
            checkNounce(nounce);
            /*校验时间戳*/
            checkTimestamp(timestamp);
            Map<String,Object> signMap=new HashMap<>(1);
            signMap.put("context",HandleAbnormalValue.NULL_TO_EMPTY_FUNCTION.apply(nounce).concat(HandleAbnormalValue.NULL_TO_EMPTY_FUNCTION.apply(timestamp)));
            /*对nounce和timestamp进行验签*/
            checkSign(sign(getMapSignText(signMap)),HandleAbnormalValue.NULL_TO_EMPTY_FUNCTION.apply(needConfirmSignMap.get("sign")));
            logger.info("验签通过");
            return proceedingJoinPoint.proceed();
        }
        /*请求头没有值，请求非法*/
        throw new UnAuthorizedException("REQUEST_ILLEGAL");
    }

    /**
     * 校验时间戳
     */
    private void checkTimestamp(String timestamp){
        if(!StringUtils.isEmpty(timestamp)){
            /*获取开始时间*/
            Long beginTime=getCurrentTime();
            /*http请求限制为3秒内的请求*/
            if (beginTime-Long.parseLong(timestamp)>3000){
                throw new UnAuthorizedException("TIMESTAMP_ILLEGAL");
            }
        }
        else{
            throw new UnAuthorizedException("TIMESTAMP_IS_EMPTY");
        }
    }

    /**
     * 校验Sign
     */
    private void checkSign(String backEndSign,String frontEndSign){
        if (StringUtils.isEmpty(backEndSign) || StringUtils.isEmpty(frontEndSign) || !backEndSign.equals(frontEndSign)){
            throw new UnAuthorizedException("SIGN_ILLEGAL");
        }
    }

    /**
     * 校验nounce
     */
    private void checkNounce(String nounce){
        Object value=redisTemplate.opsForValue().get(nounce);
        if (value == null){
            redisTemplate.opsForValue().set(nounce,nounce,3, TimeUnit.MINUTES);
        }
        /*如果从redis中找到nounce，nounce重复*/
        else {
            logger.error("Repeated nounce = {}",nounce);
            throw new UnAuthorizedException("NOUNCE_ILLEGAL");
        }
    }

    /**
    * 获取当前时间，从1970-01-01T00:00:00Z至今的毫秒数
    */
    private Long getCurrentTime(){
        return Instant.now().toEpochMilli();
    }

    /**
     * 从请求头中获取Nounce、Timestamp、Sign
     */
    private static Map<String,String> getNeedConfirmSignMap(){
        HttpServletRequest httpServletRequest;
        try{
            httpServletRequest=((ServletRequestAttributes)Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        }
        catch (Exception exception){
            exception.printStackTrace();
            logger.error("获取请求信息失败，原因是{}",exception.getMessage());
            throw new RuntimeException("GET_REQUEST_CONTEXT_ERROR");
        }
        Map<String,String> needConfirmRequestHeaderInfo;
        try{
            /*需要验签的请求头信息*/
            needConfirmRequestHeaderInfo=new HashMap<>(3);
            /*随机值，一段时间内不允许重复*/
            needConfirmRequestHeaderInfo.put("nounce",httpServletRequest.getHeader("nounce"));
            /*时间戳*/
            needConfirmRequestHeaderInfo.put("timestamp",httpServletRequest.getHeader("timestamp"));
            /*获取前端生成的签名，需要和后端进行比对*/
            needConfirmRequestHeaderInfo.put("sign",httpServletRequest.getHeader("sign"));
        }
        catch (Exception exception){
            exception.printStackTrace();
            logger.error("获取请求头信息失败，原因是{}",exception.getMessage());
            throw new UnAuthorizedException("REQUEST_HEADER_ILLEGAL");
        }
        return needConfirmRequestHeaderInfo;
    }

    /**
     * 组装成map字符串
     */
    private String getMapSignText(Map<String, Object> map) {
        TreeMap<String, Object> treeMap = new TreeMap(map);
        StringBuilder buffer = new StringBuilder();
        Iterator<Map.Entry<String,Object>> iterator;
        for(iterator=treeMap.entrySet().iterator();iterator.hasNext();) {
            Map.Entry<String, Object> entry = iterator.next();
            buffer.append(entry.getKey());
            buffer.append("=");
            buffer.append(entry.getValue().toString());
            buffer.append("&");
        }
        if(buffer.length() > 0){
            buffer.deleteCharAt(buffer.length() - 1);
        }
        return buffer.toString();
    }

    private String sign(String data){
        data = data+privateKey;
        return MD5Utils.createSign(data);
    }

}
