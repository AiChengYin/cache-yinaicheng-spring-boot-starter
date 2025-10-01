package top.yinaicheng.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存配置属性
 * @author yinaicheng
 */
@ConfigurationProperties(prefix = "yinaicheng.cache")
public class CacheProperties {

    /**
     * 是否启用缓存功能
     */
    private boolean enabled = true;

    /**
     * 默认缓存过期时间（分钟）
     */
    private int defaultExpiration = 30;

    /**
     * 缓存key前缀
     */
    private String keyPrefix = "cache:";

    /**
     * 是否允许缓存空值
     */
    private boolean cacheNullValues = false;

    /**
     * 缓存最大容量
     */
    private long maxCapacity = 10000;

    /**
     * 本地缓存配置
     */
    private LocalCacheProperties local = new LocalCacheProperties();

    /**
     * Redis缓存配置
     */
    private RedisCacheProperties redis = new RedisCacheProperties();

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDefaultExpiration() {
        return defaultExpiration;
    }

    public void setDefaultExpiration(int defaultExpiration) {
        this.defaultExpiration = defaultExpiration;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public boolean isCacheNullValues() {
        return cacheNullValues;
    }

    public void setCacheNullValues(boolean cacheNullValues) {
        this.cacheNullValues = cacheNullValues;
    }

    public long getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(long maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public LocalCacheProperties getLocal() {
        return local;
    }

    public void setLocal(LocalCacheProperties local) {
        this.local = local;
    }

    public RedisCacheProperties getRedis() {
        return redis;
    }

    public void setRedis(RedisCacheProperties redis) {
        this.redis = redis;
    }

    /**
     * 本地缓存配置
     */
    public static class LocalCacheProperties {
        /**
         * 是否启用本地缓存
         */
        private boolean enabled = true;

        /**
         * 本地缓存最大条目数
         */
        private long maxSize = 1000;

        /**
         * 本地缓存过期时间（分钟）
         */
        private int expireAfterWrite = 10;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public int getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(int expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }
    }

    /**
     * Redis缓存配置
     */
    public static class RedisCacheProperties {
        /**
         * 是否启用Redis缓存
         */
        private boolean enabled = true;

        /**
         * Redis数据库索引
         */
        private int database = 0;

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
         * 连接超时时间（毫秒）
         */
        private int timeout = 2000;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

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

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
}