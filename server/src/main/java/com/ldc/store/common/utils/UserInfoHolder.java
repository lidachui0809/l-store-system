package com.ldc.store.common.utils;

import com.ldc.store.core.constants.RPanConstants;

import java.util.Objects;

public class UserInfoHolder {


    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID
     *
     * @param userId
     */
    public static void set(Long userId) {
        threadLocal.set(userId);
    }

    /**
     * 获取当前线程的用户ID
     *
     * @return
     */
    public static Long get() {
        Long userId = threadLocal.get();
        if (Objects.isNull(userId)) {
            return RPanConstants.ZERO_LONG;
        }
        return userId;
    }


}
