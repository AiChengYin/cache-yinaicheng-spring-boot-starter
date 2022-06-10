package top.yinaicheng.config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.yinaicheng.service.HelloService;
/**
 * 配置类，基于Java代码的bean配置
 * @author yinaicheng
 */
@Configuration
@EnableConfigurationProperties(HelloProperties.class)
public class HelloServiceAutoConfiguration {

    private HelloProperties helloProperties;

    /**
     * 通过构造方法注入配置属性对象HelloProperties
     */
    public HelloServiceAutoConfiguration(HelloProperties helloProperties) {
        this.helloProperties = helloProperties;
    }

    /**
     * 实例化HelloService并载入Spring IoC容器
     */
    @Bean
    @ConditionalOnMissingBean // 仅仅在当前上下文中不存在某个Bean时，才会实例化这个Bean
    public HelloService helloService(){
        return new HelloService(helloProperties.getName(),helloProperties.getAddress());
    }
}