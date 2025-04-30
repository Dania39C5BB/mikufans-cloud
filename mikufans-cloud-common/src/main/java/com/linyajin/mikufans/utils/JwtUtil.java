package com.linyajin.mikufans.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;
import java.util.Base64;

public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * 将自定义短密钥扩展为 32 字节的安全密钥
     */
    private static byte[] extendKey(String shortKey) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        return sha256.digest(shortKey.getBytes(StandardCharsets.UTF_8)); // 固定输出 32 字节
    }

    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        //将字符串密钥转换为字节数组
        byte[] keyBytes = null;
        try {
            keyBytes = extendKey(secretKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        // 自动生成 256 位（32 字节）的密钥
//        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        // 生成JWT的过期时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        log.info("expire time: {}", expMillis);
        Date exp = new Date(expMillis);

        // 设置jwt的body
        JwtBuilder builder = Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .claims(claims)
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(key)
                // 设置过期时间
                .expiration(exp);

        return builder.compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        byte[] keyBytes = null;
        try {
            keyBytes  = extendKey(secretKey); // 得到DefaultJwtParser
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        Claims claims = Jwts.parser()
                // 设置签名的秘钥
                .verifyWith(key)
                .build()
                // 设置需要解析的jwt
                .parseSignedClaims(token)
                .getPayload();
        return claims;
    }

}
