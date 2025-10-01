package top.yinaicheng.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限流配置属性
 * @author yinaicheng
 */
@ConfigurationProperties(prefix = "yinaicheng.limit")
public class LimitProperties {

    /**
     * 是否启用限流功能
     */
    private boolean enabled = true;

    /**
     * 默认限流key前缀
     */
    private String keyPrefix = "limit:";

    /**
     * 默认时间窗口（秒）
     */
    private int defaultPeriod = 60;

    /**
     * 默认限流次数
     */
    private int defaultCount = 100;

    /**
     * 默认限流算法
     */
    private String defaultAlgorithm = "fixed_window";

    /**
     * Redis配置
     */
    private RedisProperties redis = new RedisProperties();

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public int getDefaultPeriod() {
        return defaultPeriod;
    }

    public void setDefaultPeriod(int defaultPeriod) {
        this.defaultPeriod = defaultPeriod;
    }

    public int getDefaultCount() {
        return defaultCount;
    }

    public void setDefaultCount(int defaultCount) {
        this.defaultCount = defaultCount;
    }

    public String getDefaultAlgorithm() {
        return defaultAlgorithm;
    }

    public void setDefaultAlgorithm(String defaultAlgorithm) {
        this.defaultAlgorithm = defaultAlgorithm;
    }

    public RedisProperties getRedis() {
        return redis;
    }

    public void setRedis(RedisProperties redis) {
        this.redis = redis;
    }

    /**
     * Redis配置
     */
    public static class RedisProperties {
        /**
         * Redis服务器地址
         */
        private String host = "localhost";

        /**
         * Redis服务器端口
         */
        private int port = 6379;

        /**
         * Redis密码
         */
        private String password;

        /**
         * Redis数据库索引
         */
        private int database = 0;

        /**
         * 连接超时时间（毫秒）
         */
        private int timeout = 2000;

        // Getters and Setters
        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
}