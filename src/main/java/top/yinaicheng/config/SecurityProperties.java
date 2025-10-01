package top.yinaicheng.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全验签配置属性
 * @author yinaicheng
 */
@ConfigurationProperties(prefix = "yinaicheng.security")
public class SecurityProperties {

    /**
     * 是否启用安全验签功能
     */
    private boolean enabled = true;

    /**
     * 签名密钥
     */
    private String privateKey = "default_secret_key";

    /**
     * 默认时间戳有效期（毫秒）
     */
    private long defaultTimestampValidityPeriod = 3000L;

    /**
     * 默认随机数有效期（分钟）
     */
    private int defaultNonceValidityPeriod = 3;

    /**
     * 默认签名算法
     */
    private String defaultSignAlgorithm = "MD5";

    /**
     * 是否启用时间戳验证
     */
    private boolean enableTimestamp = true;

    /**
     * 是否启用随机数验证
     */
    private boolean enableNonce = true;

    /**
     * 是否启用签名验证
     */
    private boolean enableSign = true;

    /**
     * Redis配置（用于存储nonce）
     */
    private RedisProperties redis = new RedisProperties();

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public long getDefaultTimestampValidityPeriod() {
        return defaultTimestampValidityPeriod;
    }

    public void setDefaultTimestampValidityPeriod(long defaultTimestampValidityPeriod) {
        this.defaultTimestampValidityPeriod = defaultTimestampValidityPeriod;
    }

    public int getDefaultNonceValidityPeriod() {
        return defaultNonceValidityPeriod;
    }

    public void setDefaultNonceValidityPeriod(int defaultNonceValidityPeriod) {
        this.defaultNonceValidityPeriod = defaultNonceValidityPeriod;
    }

    public String getDefaultSignAlgorithm() {
        return defaultSignAlgorithm;
    }

    public void setDefaultSignAlgorithm(String defaultSignAlgorithm) {
        this.defaultSignAlgorithm = defaultSignAlgorithm;
    }

    public boolean isEnableTimestamp() {
        return enableTimestamp;
    }

    public void setEnableTimestamp(boolean enableTimestamp) {
        this.enableTimestamp = enableTimestamp;
    }

    public boolean isEnableNonce() {
        return enableNonce;
    }

    public void setEnableNonce(boolean enableNonce) {
        this.enableNonce = enableNonce;
    }

    public boolean isEnableSign() {
        return enableSign;
    }

    public void setEnableSign(boolean enableSign) {
        this.enableSign = enableSign;
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