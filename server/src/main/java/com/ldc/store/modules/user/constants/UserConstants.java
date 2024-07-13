package com.ldc.store.modules.user.constants;

/**
 * 用户模块的常量类
 */
public interface UserConstants {

    /**
     * 登录用户的用户ID的key值
     */
    String USER_Jwt_KEY = "LOGIN_USER_KEY";

    /**
     * 用户登录缓存前缀
     */
    String USER_LOGIN_TOKEN_PREFIX = "user:login:token:";

    /**
     * 用户忘记密码-重置密码临时token的key
     */
    String FORGET_USERNAME = "FORGET_USERNAME";

    /**
     * 一天的毫秒值
     */
    Long ONE_DAY_LONG = 24L * 60L * 60L * 1000L;

    /**
     * 五分钟的毫秒值
     */
    Long FIVE_MINUTES_LONG = 5L * 60L * 1000L;


}
