package top.yinaicheng.aspect;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import top.yinaicheng.utils.number.NumberUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.yinaicheng.annotation.CacheAnnotation;
import top.yinaicheng.constant.CachedOperationTypeEnum;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static top.yinaicheng.utils.string.StringUtils.CONVERT_OBJECT_TO_STRING;
/**
 * 系统缓存Aspect
 * @author yinaicheng
 */
@Order(Ordered.HIGHEST_PRECEDENCE) // 可以使用@Order注解修饰Aspect类，值越小，优先级越高
@Aspect
@Component
public class CacheAspect
{

    private static final Logger logger = LoggerFactory.getLogger(CacheAspect.class);

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 关闭缓存切面功能标志，true开启，false关闭，默认开启
     */
    @Value("${redis.cacheAspect.turnOnCacheSign}")
    private Boolean turnOnCacheSign;

    @Autowired
    public CacheAspect(@Qualifier("data_governance_redis") RedisTemplate redisTemplate)
    {
        /*使用GenericFastJsonRedisSerializer：替换默认序列化*/
        GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();
        /*设置默认的Serialize，包含 keySerializer & valueSerializer*/
        redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);
        /*单独设置keySerializer*/
        redisTemplate.setKeySerializer(fastJsonRedisSerializer);
        /*单独设置valueSerializer*/
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        this.redisTemplate=redisTemplate;

    }

    /**
     * 直接获取redis缓存中的值，如果从redis中未获取该值，则从数据库或其他渠道获取数据，再存储到redis中
     */
    @Around("@annotation(top.yinaicheng.annotation.CacheAnnotation)")
    public Object getCacheValue(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        /*缓存切面功能不开启，直接查询数据库*/
        if(!turnOnCacheSign){
            return proceedingJoinPoint.proceed();
        }

        try
        {
            /*反射技术：从注解来看，读取key的生成规则*/
            /*获取当前方法和参数*/
            MethodSignature methodSignature=(MethodSignature)proceedingJoinPoint.getSignature();
            /*返回一个Method对象，它表示的是此Class对象所代表的类的指定公共成员方法*/
            Method method;
            try{
                method=proceedingJoinPoint.getTarget().getClass().getMethod(methodSignature.getName(),methodSignature.getMethod().getParameterTypes());
            }
            catch(NoSuchMethodException e) {
                logger.error("获取Method对象对象异常，原因是{}",e.getMessage());
                return proceedingJoinPoint.proceed();
            }
            /*获取该方法的缓存注解*/
            CacheAnnotation cacheAnnotation=method.getAnnotation(CacheAnnotation.class);

            /*获取需要操作的缓存key*/
            List<String> cacheKeyList=getNeedToOperateCacheKey(proceedingJoinPoint,method);

            CachedOperationTypeEnum cachedOperationTypeEnum=cacheAnnotation.cacheOperateType();
            Object value;
            switch (cachedOperationTypeEnum){
                case DELETE_CACHE_BY_KEY_PREFIX:
                    value= deleteCacheByKeyPrefixOperate(proceedingJoinPoint,cacheKeyList);
                    break;
                case DELETE_CACHE_BY_KEY:
                    value= deleteCacheByKeyOperate(proceedingJoinPoint,cacheKeyList);
                    break;
                case QUERY_CACHE:
                default:
                    value=queryCacheOperate(proceedingJoinPoint,cacheKeyList,cacheAnnotation);
                    break;
            }
            return value;
        }
        catch(Throwable throwable)
        {
            logger.error("从缓存中或者执行程序获取值时出错，具体原因是：{}",throwable.getMessage());
            throwable.printStackTrace();
            return proceedingJoinPoint.proceed();
        }
    }

    private Object deleteCacheByKeyPrefixOperate(ProceedingJoinPoint proceedingJoinPoint, List<String> cacheKeyList) throws Throwable {
        /*如果缓存key列表为空，则没有必要进行redis操作*/
        if(CollectionUtils.isEmpty(cacheKeyList)){
            return proceedingJoinPoint.proceed();
        }
        Set<String> remoteCachekeys=redisTemplate.opsForSet().union(cacheKeyList.get(0),cacheKeyList).stream().map(element->{
            if(Optional.ofNullable(element).isPresent()){
                return String.valueOf(element);
            }
            return null;
        }).collect(Collectors.toSet());

        /*获取所有的key*/
        List<String> needRemoveCachekeyList=Stream.of(remoteCachekeys).flatMap(Collection::stream).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(needRemoveCachekeyList)){
            redisTemplate.delete(needRemoveCachekeyList);
        }
        logger.info("redis中{}被清空",cacheKeyList);
        return proceedingJoinPoint.proceed();
    }

    private Object deleteCacheByKeyOperate(ProceedingJoinPoint proceedingJoinPoint, List<String> cacheKeyList) throws Throwable {
        /*如果缓存key列表为空，则没有必要进行redis操作*/
        if(CollectionUtils.isEmpty(cacheKeyList)){
            return proceedingJoinPoint.proceed();
        }
        redisTemplate.delete(cacheKeyList);
        logger.info("redis中{}被清空",cacheKeyList);
        return proceedingJoinPoint.proceed();
    }

    private Object queryCacheOperate(ProceedingJoinPoint proceedingJoinPoint,List<String> cacheKeyList,CacheAnnotation cacheAnnotation) throws Throwable {
        /*反射技术：从注解来看，读取key的生成规则*/
        /*获取当前方法和参数*/
        MethodSignature methodSignature=(MethodSignature)proceedingJoinPoint.getSignature();
        String methodName=proceedingJoinPoint.getTarget().getClass().getName().concat(".").concat(methodSignature.getName());
        /*通过key来命中缓存，如果缓存中没有，则查询数据库，然后放入缓存*/
        /*从缓存中获取值*/
        Object value;
        /*如果缓存key列表为空，则没有必要进行redis操作*/
        if(CollectionUtils.isEmpty(cacheKeyList)){
            return proceedingJoinPoint.proceed();
        }
        String cacheKey=cacheKeyList.get(0);
        try{
            value=redisTemplate.opsForValue().get(cacheKey);
        }
        catch (Exception exception){
            logger.error("通过key:{}获取redis对应的value出错，原因是{}",cacheKey,exception.getMessage());
            exception.printStackTrace();
            return proceedingJoinPoint.proceed();
        }
        /*如果缓存中的值存在，直接返回缓存中的值*/
        if(Optional.ofNullable(value).isPresent())
        {
            logger.info("通过key:{}从缓存中获取值了，方法名：{}",cacheKey,methodName);
            return value;
        }
        /*如果缓存中的值不存在*/
        logger.info("通过key:{}没有从缓存中获取值，方法名：{}",cacheKey,methodName);
        /*获取缓存时长*/
        int duration=cacheAnnotation.duration();
        duration=NumberUtils.GENERATE_INTEGER_VALUE_FUNCTION.apply(new int[]{duration,duration<<1});
        String operateCacheKeyPrefix=cacheAnnotation.operateCacheKeyPrefix();
        /*从数据库中查到值*/
        value=proceedingJoinPoint.proceed();
        /*往Redis里面存数据*/
        try{
            redisTemplate.opsForValue().set(cacheKey, value,duration,TimeUnit.MINUTES);
            /*因为调用的Redis服务不支持通过模糊key匹配进行批量删除，因此需要通过要操作的缓存key前缀找到key列表，再进行批量删除*/
            if(StringUtils.isNotEmpty(operateCacheKeyPrefix) && Boolean.TRUE.equals(cacheAnnotation.judgeSpel())){
                redisTemplate.opsForSet().add(operateCacheKeyPrefix,cacheKey);
            }
        }
        catch (Exception exception){
            logger.error("通过key:{}往redis插入对应的value出错，原因是{}",cacheKey,exception.getMessage());
            exception.printStackTrace();
        }
        return value;
    }

    private List<String> getNeedToOperateCacheKey(ProceedingJoinPoint proceedingJoinPoint,Method method){
        /*获取该方法的缓存注解*/
        CacheAnnotation cacheAnnotation=method.getAnnotation(CacheAnnotation.class);
        String key;
        /*获取该注解上的值，即缓存的key*/
        String[] cacheKeyArray=cacheAnnotation.operateCacheKey();
        /*获取缓存key前缀值*/
        String operateCacheKeyPrefix=cacheAnnotation.operateCacheKeyPrefix();
        /*根据key的规则，通过springEL表达式进行解析*/
        boolean judgeSpel=cacheAnnotation.judgeSpel();
        List<String> keyList= Lists.newArrayList();
        switch(String.valueOf(judgeSpel))
        {
            /*使用spel表达式*/
            case "true":
                ExpressionParser expressionParser=new SpelExpressionParser();
                /*设置解析上下文（有哪些占位符，以及每种占位符的值）*/
                EvaluationContext evaluationContext=new StandardEvaluationContext();
                /*spring的参数解析*/
                DefaultParameterNameDiscoverer defaultParameterNameDiscoverer=new DefaultParameterNameDiscoverer();
                /*获取方法参数名*/
                String[] parameterNames=Optional.ofNullable(defaultParameterNameDiscoverer.getParameterNames(method)).orElse(new String[0]);
                /*所有参数值*/
                Object[] args=proceedingJoinPoint.getArgs();
                for(int i = 0; i< parameterNames.length; i++){
                    evaluationContext.setVariable(parameterNames[i],Optional.ofNullable(args[i]).isPresent()?String.valueOf(args[i]):null);
                }
                for(String cacheKey:cacheKeyArray){
                    /*创建SPEL解析器*/
                    /*解析SPEL表达式*/
                    Expression expression=expressionParser.parseExpression(cacheKey);
                    /*获取缓存值的key*/
                    keyList.add(operateCacheKeyPrefix.concat(CONVERT_OBJECT_TO_STRING.apply(expression.getValue(evaluationContext),"String").replaceAll(" ", StringUtils.EMPTY).replaceAll("\\s*",StringUtils.EMPTY)));
                }
                break;
            /*不使用spel表达式*/
            case "false":
            default:
                for(String cacheKey:cacheKeyArray){
                    /*获取缓存值的key*/
                    keyList.add(operateCacheKeyPrefix.concat(cacheKey.replaceAll(" ", StringUtils.EMPTY).replaceAll("\\s*",StringUtils.EMPTY)));
                }
                break;
        }
        return keyList;
    }

}