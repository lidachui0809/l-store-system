package com.ldc.store.modules.user.context;


import com.ldc.store.modules.user.domain.RPanUser;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户在线修改密码上下文信息实体
 */
@Data
public class ChangePasswordContext implements Serializable {

    /**
     * 当前登录的用户ID
     */
    private Long userId;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 当前登录用户的实体信息
     */
    private RPanUser entity;

}
