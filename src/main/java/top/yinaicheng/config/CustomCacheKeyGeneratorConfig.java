package top.yinaicheng.config;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 覆盖SpringCache相关配置，自定义密钥生成器
 * 从 CachingConfigurerSupport 扩展有助于向拦截器注册声明的 KeyGenerator
 * 使用：@Cacheable("test", keyGenerator = "customCacheKeyGenerator")
 * @author yinaicheng
 */
@EnableCaching
@Configuration
public class CustomCacheKeyGeneratorConfig extends CachingConfigurerSupport {
    
    /**
     * 解决注解：Cacheable 没有指定key时，会将key生成为 ${value}:SimpleKey []
     * 举例：@Cacheable(value = "test") ->  test:SimpleKey []
     */
    @Bean("customCacheKeyGenerator")
    @Override
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }

    /**
     * 自定义缓存key生成器
     */
    public static class CustomKeyGenerator implements KeyGenerator {
        
        @Override
        public Object generate(Object target, Method method, Object... params) {
            StringBuilder key = new StringBuilder();
            
            // 添加类名
            key.append(target.getClass().getSimpleName()).append(":");
            
            // 添加方法名
            key.append(method.getName());
            
            // 添加参数
            if (params != null && params.length > 0) {
                String paramStr = Arrays.stream(params)
                    .map(param -> param != null ? param.toString() : "null")
                    .collect(Collectors.joining(","));
                key.append(":").append(paramStr);
            }
            
            return key.toString();
        }
    }
}
