package com.ldc.store.modules.user.context;

import com.ldc.store.modules.user.domain.RPanUser;
import lombok.Data;

/**
 * user的上下文对象 持有用户的所有信息
 */
@Data
public class UserRegisterContext {

    private String username;
    private String password;
    private String question;

    private String answer;
    private RPanUser entity;
}
