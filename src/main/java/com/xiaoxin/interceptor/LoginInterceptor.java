package com.xiaoxin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoxin.utils.JwtTokenUtils;
import com.xiaoxin.utils.MyJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Objects;

@Component
public class LoginInterceptor implements HandlerInterceptor {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头里面获取token,因为每次都会在请求头里面携带token
        String token = request.getHeader("TOKEN");
        // 校验token
        if (StringUtils.isNotBlank(token)) {
            // 根据 token 拿到用户名
            String username = JwtTokenUtils.getUsername(token);
            // 再根据获取到的用户名 从redis中获取数据
            String sourceToken = redisTemplate.opsForValue().get(username);
            // 如果能够获取到数据，说明token未过期
            if (StringUtils.isNotBlank(sourceToken)) {
                // 如果两个token相等，代表 token有效
                if (Objects.equals(token, sourceToken)) {
                    // 请求放行
                    return true;
                }
            }
        }
        //从请求头中获取不到token说明未登录，阻止该请求访问资源
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        PrintWriter pw = response.getWriter();
        pw.println(objectMapper.writeValueAsString(MyJSONResult.errorMsg("用户未登录!!!!!")));
        return false;
    }
	
}