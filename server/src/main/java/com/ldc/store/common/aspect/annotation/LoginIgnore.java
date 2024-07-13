package com.ldc.store.common.aspect.annotation;

import lombok.Data;

import java.lang.annotation.*;

/**
 * 标记不需要登录验证的方法
 */
@Documented
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LoginIgnore {
}
