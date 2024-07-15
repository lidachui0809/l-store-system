package com.ldc.store.modules.user.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ldc.store.common.aspect.annotation.LoginIgnore;
import utils.UserInfoHolder;
import com.ldc.store.core.response.R;
import com.ldc.store.core.utils.IdUtil;
import com.ldc.store.modules.user.context.*;
import com.ldc.store.modules.user.converter.UserConverter;
import com.ldc.store.modules.user.service.IUserService;
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
    private IUserService iUserService;

    @Autowired
    private UserConverter userConverter;


    //注册
    // 用户注册 创建用户存储根目录

    @PostMapping("/register")
    @LoginIgnore
    public R register(@RequestBody @Validated RegisterVO registerVO){
        UserRegisterContext userRegisterContext =
                userConverter.userRegisterPO2UserRegisterContext(registerVO);
        Long userId = iUserService.register(userRegisterContext);
        return R.data(IdUtil.encrypt(userId));
    }


    /* @Validated 这个注解 才会开始验证 */
    @PostMapping("/login")
    @LoginIgnore
    public R login(@RequestBody @Validated LoginVO loginVO){
        UserLoginContext userRegisterContext = userConverter.userLoginPO2UserLoginContext(loginVO);
        BeanUtil.copyProperties(loginVO, userRegisterContext);
        String token = iUserService.login(userRegisterContext);
        return R.data(token);
    }

    @RequestMapping("/loginOut")
    public R logout(){
        iUserService.loginOut(UserInfoHolder.get());
        return R.success();
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
        CheckUsernameContext checkUsernameContext = userConverter.checkUsernamePO2CheckUsernameContext(checkUsernamePO);
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
        CheckAnswerContext checkAnswerContext =userConverter.checkAnswerPO2CheckAnswerContext(checkAnswerPO);
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
        ChangePasswordContext changePasswordContext =
                userConverter.changePasswordPO2ChangePasswordContext(changePasswordPO);
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
        ResetPasswordContext resetPasswordContext =userConverter.resetPasswordPO2ResetPasswordContext(resetPasswordPO);
        iUserService.resetPassword(resetPasswordContext);
        return R.success();
    }


    //检验用户名 检验密保 同时 返回时效token验证 验证通过
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



}
