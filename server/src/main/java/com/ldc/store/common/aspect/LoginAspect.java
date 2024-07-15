package com.ldc.store.common.aspect;

import com.ldc.store.cache.core.constants.CacheConstants;
import com.ldc.store.common.aspect.annotation.LoginIgnore;
import utils.UserInfoHolder;
import com.ldc.store.core.response.R;
import com.ldc.store.core.response.ResponseCode;
import com.ldc.store.core.utils.JwtUtil;
import com.ldc.store.modules.user.constants.UserConstants;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

//基于aop 对方法进行切面处理 登录验证
@Aspect
@Component
@Slf4j
public class LoginAspect {


    public static final String AUTH_IN_PARAM_KEY="authorization";
    public static final String AUTH_IN_HEADER_KEY="Authorization";

    //切入点 表达式 匹配com.ldc.store.modules子包的controller包的所有类的所有方法 .. 标识全部匹配 类似于 *
    public static final String PointCut="execution(* com.ldc.store.modules.*.controller..*(..))";


    @Pointcut(value = PointCut)
    public void loginCut(){}

    @Autowired
    private CacheManager cacheManager;

    //将方法进行环切
    @Around("loginCut()")
    public Object checkLoginStatus(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取方法签名信息 获得注解信息 判断是否存在LoginIgnore注解 获得请求头或者请求参数token 进行鉴权
        if(needCheckLoginStatus(joinPoint)){
            return joinPoint.proceed();
        }
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        if(!checkAndSaveUserStatus(request)){
            log.error("拦截请求 url:{}",request.getRequestURL());
            return R.fail(ResponseCode.NEED_LOGIN);
        }
        return joinPoint.proceed();
    }

    private boolean checkAndSaveUserStatus(HttpServletRequest request) {
        String authorization = request.getParameter(AUTH_IN_PARAM_KEY);
        //获得请求中的token
        if(Objects.isNull(authorization)){
            authorization=request.getHeader(AUTH_IN_HEADER_KEY);
        }
        if(authorization==null){
            log.error("请求不存在token ");
            return false;
        }
        // 解析请求token 获得缓存中token
        Object userIdObj = JwtUtil.analyzeToken(authorization, UserConstants.USER_Jwt_KEY);
        if(Objects.isNull(userIdObj)){
            return false;
        }
        Long userId=Long.valueOf(userIdObj.toString());
        Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
        Object cacheUserId  = cache.get(UserConstants.USER_LOGIN_TOKEN_PREFIX + userId).get();
        if(Objects.isNull(cacheUserId)){
            log.error("缓存中未找到对应token！");
            return false;
        }
        if(!Objects.equals(cacheUserId.toString(),userId.toString())){
            log.error("用户签名不正确！");
            return false;
        }
        //保存用户id再当前线程中
        UserInfoHolder.set(userId);
        return true;
    }


    private boolean needCheckLoginStatus(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().isAnnotationPresent(LoginIgnore.class);
    }


}
