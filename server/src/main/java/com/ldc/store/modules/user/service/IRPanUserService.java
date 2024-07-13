package com.ldc.store.modules.user.service;

import com.ldc.store.core.response.R;
import com.ldc.store.modules.user.context.*;
import com.ldc.store.modules.user.domain.RPanUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldc.store.modules.user.vo.UserInfoVO;
import lombok.extern.java.Log;

/**
* @author 李Da锤
* @description 针对表【r_pan_user(用户信息表)】的数据库操作Service
* @createDate 2024-07-12 13:32:33
*/
public interface IRPanUserService extends IService<RPanUser> {

    R register(UserRegisterContext userRegisterContext);

    R login(UserLoginContext userRegisterContext);

    R loginOut(Long userId);

    String checkUsername(CheckUsernameContext checkUsernameContext);

    String checkAnswer(CheckAnswerContext checkAnswerContext);

    void resetPassword(ResetPasswordContext resetPasswordContext);

    void changePassword(ChangePasswordContext changePasswordContext);

    UserInfoVO info(Long aLong);
}
