package top.yinaicheng.config;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        /*暂时返回空字符串，暂不处理*/
        return (target, method, objects) -> "";
    }

}
