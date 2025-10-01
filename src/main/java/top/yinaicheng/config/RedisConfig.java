package top.yinaicheng.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * @author yinaicheng
 */
@Configuration
@EnableConfigurationProperties({CacheProperties.class, LimitProperties.class, SecurityProperties.class})
@ConditionalOnProperty(prefix = "yinaicheng", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    /**
     * 缓存专用的Redis连接工厂
     */
    @Bean(name = "cacheRedisConnectionFactory")
    @ConditionalOnProperty(prefix = "yinaicheng.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedisConnectionFactory cacheRedisConnectionFactory(CacheProperties cacheProperties) {
        CacheProperties.RedisCacheProperties redis = cacheProperties.getRedis();
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redis.getHost());
        config.setPort(redis.getPort());
        config.setDatabase(redis.getDatabase());
        if (redis.getPassword() != null && !redis.getPassword().isEmpty()) {
            config.setPassword(redis.getPassword());
        }
        
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
        jedisConnectionFactory.afterPropertiesSet();
        return jedisConnectionFactory;
    }

    /**
     * 限流专用的Redis连接工厂
     */
    @Bean(name = "limitRedisConnectionFactory")
    @ConditionalOnProperty(prefix = "yinaicheng.limit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedisConnectionFactory limitRedisConnectionFactory(LimitProperties limitProperties) {
        LimitProperties.RedisProperties redis = limitProperties.getRedis();
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redis.getHost());
        config.setPort(redis.getPort());
        config.setDatabase(redis.getDatabase());
        if (redis.getPassword() != null && !redis.getPassword().isEmpty()) {
            config.setPassword(redis.getPassword());
        }
        
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
        jedisConnectionFactory.afterPropertiesSet();
        return jedisConnectionFactory;
    }

    /**
     * 安全验签专用的Redis连接工厂
     */
    @Bean(name = "securityRedisConnectionFactory")
    @ConditionalOnProperty(prefix = "yinaicheng.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedisConnectionFactory securityRedisConnectionFactory(SecurityProperties securityProperties) {
        SecurityProperties.RedisProperties redis = securityProperties.getRedis();
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redis.getHost());
        config.setPort(redis.getPort());
        config.setDatabase(redis.getDatabase());
        if (redis.getPassword() != null && !redis.getPassword().isEmpty()) {
            config.setPassword(redis.getPassword());
        }
        
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
        jedisConnectionFactory.afterPropertiesSet();
        return jedisConnectionFactory;
    }

    /**
     * 缓存专用的RedisTemplate
     */
    @Bean(name = "cacheRedisTemplate")
    @ConditionalOnProperty(prefix = "yinaicheng.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedisTemplate<String, Object> cacheRedisTemplate(RedisConnectionFactory cacheRedisConnectionFactory) {
        return createRedisTemplate(cacheRedisConnectionFactory);
    }

    /**
     * 限流专用的RedisTemplate
     */
    @Bean(name = "limitRedisTemplate")
    @ConditionalOnProperty(prefix = "yinaicheng.limit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedisTemplate<String, Object> limitRedisTemplate(RedisConnectionFactory limitRedisConnectionFactory) {
        return createRedisTemplate(limitRedisConnectionFactory);
    }

    /**
     * 安全验签专用的RedisTemplate
     */
    @Bean(name = "securityRedisTemplate")
    @ConditionalOnProperty(prefix = "yinaicheng.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedisTemplate<String, Object> securityRedisTemplate(RedisConnectionFactory securityRedisConnectionFactory) {
        return createRedisTemplate(securityRedisConnectionFactory);
    }

    /**
     * 创建通用的RedisTemplate
     */
    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        redisTemplate.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 默认的RedisTemplate（向后兼容）
     */
    @Bean(name = "data_governance_redis")
    @ConditionalOnMissingBean(name = "data_governance_redis")
    public RedisTemplate<String, Object> dataGovernanceRedis(CacheProperties cacheProperties) {
        CacheProperties.RedisCacheProperties redis = cacheProperties.getRedis();
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redis.getHost());
        config.setPort(redis.getPort());
        config.setDatabase(redis.getDatabase());
        if (redis.getPassword() != null && !redis.getPassword().isEmpty()) {
            config.setPassword(redis.getPassword());
        }
        
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
        jedisConnectionFactory.afterPropertiesSet();
        
        return createRedisTemplate(jedisConnectionFactory);
    }
}