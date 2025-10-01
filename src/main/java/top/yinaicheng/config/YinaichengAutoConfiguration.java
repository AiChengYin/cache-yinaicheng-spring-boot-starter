package top.yinaicheng.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Yinaicheng Starter 自动配置类
 * @author yinaicheng
 */
@Configuration
@EnableConfigurationProperties({
    CacheProperties.class,
    LimitProperties.class,
    SecurityProperties.class
})
@ComponentScan(basePackages = "top.yinaicheng")
@Import({
    RedisConfig.class,
    CustomCacheKeyGeneratorConfig.class
})
@ConditionalOnProperty(prefix = "yinaicheng", name = "enabled", havingValue = "true", matchIfMissing = true)
public class YinaichengAutoConfiguration {

    // 自动配置主类，主要用于组织和管理其他配置类

}