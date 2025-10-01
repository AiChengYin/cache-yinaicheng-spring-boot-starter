package top.yinaicheng.utils.encrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密工具类
 * @author yinaicheng
 */
public class MD5Utils {

    private static final String MD5_ALGORITHM = "MD5";
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 对字符串进行MD5加密
     * 
     * @param data 待加密的字符串
     * @return MD5加密后的字符串（32位小写）
     */
    public static String createSign(String data) {
        if (data == null) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            byte[] bytes = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 对字符串进行MD5加密（32位大写）
     * 
     * @param data 待加密的字符串
     * @return MD5加密后的字符串（32位大写）
     */
    public static String createSignUpperCase(String data) {
        String result = createSign(data);
        return result != null ? result.toUpperCase() : null;
    }

    /**
     * 验证字符串的MD5值
     * 
     * @param data 原始字符串
     * @param md5 MD5值
     * @return 如果匹配返回true，否则返回false
     */
    public static boolean verify(String data, String md5) {
        if (data == null || md5 == null) {
            return false;
        }
        
        String calculatedMd5 = createSign(data);
        return md5.equalsIgnoreCase(calculatedMd5);
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX_DIGITS[(b >>> 4) & 0x0f]);
            sb.append(HEX_DIGITS[b & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * 对文件内容进行MD5加密
     * 
     * @param fileBytes 文件字节数组
     * @return MD5加密后的字符串
     */
    public static String createFileSign(byte[] fileBytes) {
        if (fileBytes == null) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            byte[] bytes = md.digest(fileBytes);
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 计算多个字符串连接后的MD5值
     * 
     * @param data 字符串数组
     * @return MD5加密后的字符串
     */
    public static String createMultiSign(String... data) {
        if (data == null || data.length == 0) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (String str : data) {
            if (str != null) {
                sb.append(str);
            }
        }
        
        return createSign(sb.toString());
    }

    /**
     * 加盐MD5加密
     * 
     * @param data 待加密的字符串
     * @param salt 盐值
     * @return 加盐MD5加密后的字符串
     */
    public static String createSignWithSalt(String data, String salt) {
        if (data == null) {
            return null;
        }
        
        String saltedData = data + (salt != null ? salt : "");
        return createSign(saltedData);
    }
}