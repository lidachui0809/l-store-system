package com.ldc.store.modules.user.service;

import com.ldc.store.modules.user.context.*;
import com.ldc.store.modules.user.domain.RPanUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldc.store.modules.user.vo.UserInfoVO;

/**
* @author 李Da锤
* @description 针对表【r_pan_user(用户信息表)】的数据库操作Service
* @createDate 2024-07-12 13:32:33
*/
public interface IUserService extends IService<RPanUser> {

    Long register(UserRegisterContext userRegisterContext);

    String login(UserLoginContext userRegisterContext);

    void loginOut(Long userId);

    String checkUsername(CheckUsernameContext checkUsernameContext);

    String checkAnswer(CheckAnswerContext checkAnswerContext);

    void resetPassword(ResetPasswordContext resetPasswordContext);

    void changePassword(ChangePasswordContext changePasswordContext);

    UserInfoVO info(Long aLong);
}
