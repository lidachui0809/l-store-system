package com.ldc.store.modules.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(value = "用户注册信息")
public class RegisterVO {

    @NotNull(message = "用户名不可以为空")
    @Pattern(regexp = "^[\\da-zA-Z]{6,12}$",message = "用户名只能是数子和字符组成的6-12位的字符")
    private String username;

    @NotNull(message = "密码不可以为空")
    @Pattern(regexp = "^[\\da-zA-Z]{6,18}$",message = "密码只能是数子和字符组成的6-18位的字符")
    private String password;

    @ApiModelProperty(value = "密码问题", required = true)
    @NotBlank(message = "密保问题不能为空")
    @Length(max = 100, message = "密保问题不能超过100个字符")
    private String question;

    @ApiModelProperty(value = "密码答案", required = true)
    @NotBlank(message = "密保答案不能为空")
    @Length(max = 100, message = "密保答案不能超过100个字符")
    private String answer;
}
