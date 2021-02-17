package com.xiaoxin.aop;


import com.xiaoxin.utils.MyJSONResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class LoginAspectController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Pointcut("@annotation(com.xiaoxin.aop.LoginTrack)")
    public void access() {
    }

    @Around("access()")
    public Object doBefore(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        Object[] args = pjp.getArgs();

        Long count = redisTemplate.opsForValue().increment(args[0],1);

        if(count > 1){
            return MyJSONResult.errorMsg("超过最大登录次数");
        }else{
            return pjp.proceed();
        }

    }

    @Around("access() && @annotation(loginTrack)")
    public Object doAround(ProceedingJoinPoint pjp, LoginTrack loginTrack) throws Throwable {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        String remoteHost = request.getRemoteHost();
        int remotePort = request.getRemotePort();

        String key = remoteHost + ":" + remotePort;
        Long count = redisTemplate.opsForValue().increment(key,1);
        if(count == 1) redisTemplate.expire(key, loginTrack.time(), TimeUnit.SECONDS);

        if (count > loginTrack.count()) {
            return MyJSONResult.errorMsg("访问次数过于频繁,锁定"+loginTrack.time()/60+"分钟");
        }else{
            return pjp.proceed();
        }
    }
}
