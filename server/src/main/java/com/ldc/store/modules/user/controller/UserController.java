package com.ldc.store.modules.user.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ldc.store.common.aspect.annotation.LoginIgnore;
import com.ldc.store.common.utils.UserInfoHolder;
import com.ldc.store.core.response.R;
import com.ldc.store.modules.user.context.*;
import com.ldc.store.modules.user.service.IRPanUserService;
import com.ldc.store.modules.user.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api(tags = "用户模块")
public class UserController {


    @Autowired
    private IRPanUserService iUserService;


    //注册
    // 用户注册 创建用户存储根目录

    @PostMapping("/register")
    @LoginIgnore
    public R register(@RequestBody @Validated RegisterVO registerVO){
        UserRegisterContext userRegisterContext = new UserRegisterContext();
        BeanUtil.copyProperties(registerVO, userRegisterContext);
        return iUserService.register(userRegisterContext);
    }


    /* @Validated 这个注解 才会开始验证 */
    @PostMapping("/login")
    @LoginIgnore
    public R login(@RequestBody @Validated LoginVO loginVO){
        UserLoginContext userRegisterContext = new UserLoginContext();
        BeanUtil.copyProperties(loginVO, userRegisterContext);
        return iUserService.login(userRegisterContext);
    }

    @RequestMapping("/loginOut")
    public R logout(){
        return iUserService.loginOut(UserInfoHolder.get());
    }

    @ApiOperation(
            value = "用户忘记密码-校验用户名",
            notes = "该接口提供了用户忘记密码-校验用户名的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("username/check")
    public R checkUsername(@Validated @RequestBody CheckUsernamePO checkUsernamePO) {
        CheckUsernameContext checkUsernameContext = new CheckUsernameContext();
        BeanUtil.copyProperties(checkUsernamePO,checkUsernameContext);
        String question = iUserService.checkUsername(checkUsernameContext);
        return R.data(question);
    }


    @ApiOperation(
            value = "用户忘记密码-校验密保答案",
            notes = "该接口提供了用户忘记密码-校验密保答案的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("answer/check")
    public R checkAnswer(@Validated @RequestBody CheckAnswerPO checkAnswerPO) {
        CheckAnswerContext checkAnswerContext =new CheckAnswerContext();
        BeanUtil.copyProperties(checkAnswerPO,checkAnswerContext);
        String token = iUserService.checkAnswer(checkAnswerContext);
        return R.data(token);
    }

    @ApiOperation(
            value = "用户在线修改密码",
            notes = "该接口提供了用户在线修改密码的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("password/change")
    public R changePassword(@Validated @RequestBody ChangePasswordPO changePasswordPO) {
        ChangePasswordContext changePasswordContext = new ChangePasswordContext();
        BeanUtil.copyProperties(changePasswordPO,changePasswordContext);
        changePasswordContext.setUserId(UserInfoHolder.get());
        iUserService.changePassword(changePasswordContext);
        return R.success();
    }


    @ApiOperation(
            value = "用户忘记密码-重置新密码",
            notes = "该接口提供了用户忘记密码-重置新密码的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("password/reset")
    @LoginIgnore
    public R resetPassword(@Validated @RequestBody ResetPasswordPO resetPasswordPO) {
        ResetPasswordContext resetPasswordContext = new ResetPasswordContext();
        BeanUtil.copyProperties(resetPasswordPO,resetPasswordContext);
        iUserService.resetPassword(resetPasswordContext);
        return R.success();
    }

    @ApiOperation(
            value = "查询登录用户的基本信息",
            notes = "该接口提供了查询登录用户的基本信息的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/")
    public R<UserInfoVO> info() {
        UserInfoVO userInfoVO = iUserService.info(UserInfoHolder.get());
        return R.data(userInfoVO);
    }


    //检验用户名 检验密保 同时 返回时效token验证 验证通过
}
