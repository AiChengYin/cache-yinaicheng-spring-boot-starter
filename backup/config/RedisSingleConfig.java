package top.yinaicheng.config;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
/**
 * Redis单点配置
 */
@Configuration
@PropertySource("classpath:application.properties") // 指定配置文件
@ConfigurationProperties("redis.single.config") // 前缀=spring.datasource.druid，会在配置文件中寻找spring.datasource.druid.*的配置项
@Getter
@Setter
@Slf4j
public class RedisSingleConfig
{
  
  private String host; /*服务器地址*/
  
  private Integer port; /*端口号*/
  
  private String password; /*访问密码*/
  
  @Bean(name="redisConnectionFactory")
  public RedisConnectionFactory connectionFactory(){
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(host);
    redisStandaloneConfiguration.setPort(port);
    redisStandaloneConfiguration.setPassword(password);
    JedisConnectionFactory jedisConnectionFactory=new JedisConnectionFactory(redisStandaloneConfiguration);
    jedisConnectionFactory.afterPropertiesSet();
    return jedisConnectionFactory;
  }
  /**
   * 主要是配置Redis的序列化规则，替换默认的jdkSerializer
   * value的序列化规则用GenericFastJsonRedisSerializer：
   */
  @Bean(name="data_governance_redis")
  public RedisTemplate<Object,Object> redisTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory connectionFactory)
  {
    RedisTemplate<Object,Object> redisTemplate=new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);
    /*使用GenericFastJsonRedisSerializer：替换默认序列化*/
    GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();
    redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);//设置默认的Serialize，包含 keySerializer & valueSerializer
    redisTemplate.setKeySerializer(fastJsonRedisSerializer);//单独设置keySerializer
    redisTemplate.setValueSerializer(fastJsonRedisSerializer);//单独设置valueSerializer
    return redisTemplate;
  }
  
}