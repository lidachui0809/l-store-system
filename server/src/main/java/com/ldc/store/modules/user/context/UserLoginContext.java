package com.ldc.store.modules.user.context;

import lombok.Data;

@Data
public class UserLoginContext {

    private String username;
    private String password;
    private Long userId;
    private String accessToken;
}
