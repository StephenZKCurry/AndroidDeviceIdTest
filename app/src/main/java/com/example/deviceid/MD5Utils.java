package com.example.deviceid;

import java.security.MessageDigest;

/**
 * @description: MD5加密不可逆
 * @author: zhukai
 * @date: 2019/12/20 9:54
 */
public class MD5Utils {

    /**
     * MD5进行加密
     *
     * @param
     * @return
     */
    public static String digest(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                int c = b & 0xff; // 负数转换成正数
                String result = Integer.toHexString(c); // 把十进制的数转换成十六进制的书
                if (result.length() < 2) {
                    sb.append(0); // 让十六进制全部都是两位数
                }
                sb.append(result);
            }
            return sb.toString(); // 返回加密后的密文
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
}
