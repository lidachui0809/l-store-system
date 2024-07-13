package com.ldc.store.modules.user.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class LoginVO {

    @NotNull(message = "用户名不可以为空")
    @Pattern(regexp = "^[\\da-zA-Z]{6,12}$",message = "用户名只能是数子和字符组成的6-12位的字符")
    private String username;

    @NotNull(message = "密码不可以为空")
    @Pattern(regexp = "^[\\da-zA-Z]{6,18}$",message = "密码只能是数子和字符组成的6-18位的字符")
    private String password;
}
