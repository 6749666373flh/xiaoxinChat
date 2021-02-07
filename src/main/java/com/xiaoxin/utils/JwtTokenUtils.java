package com.xiaoxin.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtTokenUtils {

    public static final String TOKEN_HEADER = "TOKEN";
    /**
     * 密钥key
     */
    private static final String SECRET = "jwtsecurit";

    /**
     * JWT的发行人
     */
    private static final String ISS = "fan";

    /**
     * 自定义用户信息
     */
    private static final String ROLE_CLAIMS = "rol";

    /**
     * 过期时间是3600秒，既是1个小时
     */
    public static final long EXPIRATION = 604800L * 1000;

    /**
     * 选择了记住我之后的过期时间为7天
     */
    // public static final long EXPIRATION_REMEMBER = 604800L * 1000;


    public static String createToken(String username)  {


        return Jwts.builder().signWith(SignatureAlgorithm.HS512, SECRET) // 加密算法
                .setIssuer(ISS) // jwt发行人
                .setSubject(username) // jwt面向的用户
                .setIssuedAt(new Date()) // jwt发行人
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) // token过期时间
                .compact();
    }

    /**
     * 从token获取用户名
     * @param token
     * @return
     */
    public static String getUsername(String token)  {
        return getTokenBody(token).getSubject();
    }

    /**
     * 是否已过期
     *
     * @param token
     * @return
     */
    public static boolean isExpiration(String token)  {
        return getTokenBody(token).getExpiration().before(new Date());
    }

    private static Claims getTokenBody(String token)  {
        return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
    }

    /**
     * 验证token
     *
     * @param token
     * @return
     */
    public static boolean validateToken(String token)  {
        return isExpiration(token) == false;
    }

}