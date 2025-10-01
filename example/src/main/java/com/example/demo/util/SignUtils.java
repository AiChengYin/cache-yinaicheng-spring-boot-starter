package com.example.demo.util;

import top.yinaicheng.utils.encrypt.MD5Utils;

import java.util.*;

/**
 * 签名工具类 - 用于客户端生成签名
 * @author yinaicheng
 */
public class SignUtils {

    /**
     * 生成签名
     * @param nonce 随机数
     * @param timestamp 时间戳
     * @param privateKey 私钥
     * @return 签名
     */
    public static String generateSign(String nonce, String timestamp, String privateKey) {
        Map<String, Object> signMap = new HashMap<>();
        signMap.put("context", nonce + timestamp);
        
        String data = getMapSignText(signMap) + privateKey;
        return MD5Utils.createSign(data);
    }

    /**
     * 组装成map字符串
     */
    private static String getMapSignText(Map<String, Object> map) {
        TreeMap<String, Object> treeMap = new TreeMap<>(map);
        StringBuilder buffer = new StringBuilder();
        Iterator<Map.Entry<String, Object>> iterator;
        for (iterator = treeMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Object> entry = iterator.next();
            buffer.append(entry.getKey());
            buffer.append("=");
            buffer.append(entry.getValue().toString());
            buffer.append("&");
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        return buffer.toString();
    }

    /**
     * 生成随机数
     */
    public static String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成时间戳
     */
    public static String generateTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static void main(String[] args) {
        // 示例：生成签名
        String nonce = generateNonce();
        String timestamp = generateTimestamp();
        String privateKey = "demo_secret_key_123456";
        String sign = generateSign(nonce, timestamp, privateKey);
        
        System.out.println("nonce: " + nonce);
        System.out.println("timestamp: " + timestamp);
        System.out.println("sign: " + sign);
        
        // 模拟HTTP请求头
        System.out.println("\n==== HTTP请求头示例 ====");
        System.out.println("nonce: " + nonce);
        System.out.println("timestamp: " + timestamp);
        System.out.println("sign: " + sign);
        
        // curl 命令示例
        System.out.println("\n==== curl命令示例 ====");
        System.out.println("curl -X POST http://localhost:8080/api/users/create \\");
        System.out.println("  -H \"Content-Type: application/json\" \\");
        System.out.println("  -H \"nonce: " + nonce + "\" \\");
        System.out.println("  -H \"timestamp: " + timestamp + "\" \\");
        System.out.println("  -H \"sign: " + sign + "\" \\");
        System.out.println("  -d '{\"name\":\"测试用户\",\"email\":\"test@example.com\",\"age\":25}'");
    }
}