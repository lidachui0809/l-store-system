package com.ldc.store.modules.user.converter;

import com.ldc.store.modules.file.domain.RPanUserFile;
import com.ldc.store.modules.user.context.ChangePasswordContext;
import com.ldc.store.modules.user.context.CheckAnswerContext;
import com.ldc.store.modules.user.context.CheckUsernameContext;
import com.ldc.store.modules.user.context.ResetPasswordContext;
import com.ldc.store.modules.user.context.UserLoginContext;
import com.ldc.store.modules.user.context.UserRegisterContext;
import com.ldc.store.modules.user.domain.RPanUser;
import com.ldc.store.modules.user.vo.ChangePasswordPO;
import com.ldc.store.modules.user.vo.CheckAnswerPO;
import com.ldc.store.modules.user.vo.CheckUsernamePO;
import com.ldc.store.modules.user.vo.LoginVO;
import com.ldc.store.modules.user.vo.RegisterVO;
import com.ldc.store.modules.user.vo.ResetPasswordPO;
import com.ldc.store.modules.user.vo.UserInfoVO;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-16T22:06:41+0800",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 1.8.0_111 (Oracle Corporation)"
)
@Component
public class UserConverterImpl implements UserConverter {

    @Override
    public UserRegisterContext userRegisterPO2UserRegisterContext(RegisterVO userRegisterPO) {
        if ( userRegisterPO == null ) {
            return null;
        }

        UserRegisterContext userRegisterContext = new UserRegisterContext();

        userRegisterContext.setUsername( userRegisterPO.getUsername() );
        userRegisterContext.setPassword( userRegisterPO.getPassword() );
        userRegisterContext.setQuestion( userRegisterPO.getQuestion() );
        userRegisterContext.setAnswer( userRegisterPO.getAnswer() );

        return userRegisterContext;
    }

    @Override
    public RPanUser userRegisterContext2RPanUser(UserRegisterContext userRegisterContext) {
        if ( userRegisterContext == null ) {
            return null;
        }

        RPanUser rPanUser = new RPanUser();

        rPanUser.setUsername( userRegisterContext.getUsername() );
        rPanUser.setQuestion( userRegisterContext.getQuestion() );
        rPanUser.setAnswer( userRegisterContext.getAnswer() );

        return rPanUser;
    }

    @Override
    public UserLoginContext userLoginPO2UserLoginContext(LoginVO userLoginPO) {
        if ( userLoginPO == null ) {
            return null;
        }

        UserLoginContext userLoginContext = new UserLoginContext();

        userLoginContext.setUsername( userLoginPO.getUsername() );
        userLoginContext.setPassword( userLoginPO.getPassword() );

        return userLoginContext;
    }

    @Override
    public CheckUsernameContext checkUsernamePO2CheckUsernameContext(CheckUsernamePO checkUsernamePO) {
        if ( checkUsernamePO == null ) {
            return null;
        }

        CheckUsernameContext checkUsernameContext = new CheckUsernameContext();

        checkUsernameContext.setUsername( checkUsernamePO.getUsername() );

        return checkUsernameContext;
    }

    @Override
    public CheckAnswerContext checkAnswerPO2CheckAnswerContext(CheckAnswerPO checkAnswerPO) {
        if ( checkAnswerPO == null ) {
            return null;
        }

        CheckAnswerContext checkAnswerContext = new CheckAnswerContext();

        checkAnswerContext.setUsername( checkAnswerPO.getUsername() );
        checkAnswerContext.setQuestion( checkAnswerPO.getQuestion() );
        checkAnswerContext.setAnswer( checkAnswerPO.getAnswer() );

        return checkAnswerContext;
    }

    @Override
    public ResetPasswordContext resetPasswordPO2ResetPasswordContext(ResetPasswordPO resetPasswordPO) {
        if ( resetPasswordPO == null ) {
            return null;
        }

        ResetPasswordContext resetPasswordContext = new ResetPasswordContext();

        resetPasswordContext.setUsername( resetPasswordPO.getUsername() );
        resetPasswordContext.setPassword( resetPasswordPO.getPassword() );
        resetPasswordContext.setToken( resetPasswordPO.getToken() );

        return resetPasswordContext;
    }

    @Override
    public ChangePasswordContext changePasswordPO2ChangePasswordContext(ChangePasswordPO changePasswordPO) {
        if ( changePasswordPO == null ) {
            return null;
        }

        ChangePasswordContext changePasswordContext = new ChangePasswordContext();

        changePasswordContext.setOldPassword( changePasswordPO.getOldPassword() );
        changePasswordContext.setNewPassword( changePasswordPO.getNewPassword() );

        changePasswordContext.setUserId( com.ldc.store.common.utils.UserInfoHolder.get() );

        return changePasswordContext;
    }

    @Override
    public UserInfoVO assembleUserInfoVO(RPanUser panUser, RPanUserFile userFile) {
        if ( panUser == null && userFile == null ) {
            return null;
        }

        UserInfoVO userInfoVO = new UserInfoVO();

        if ( panUser != null ) {
            userInfoVO.setUsername( panUser.getUsername() );
        }
        if ( userFile != null ) {
            userInfoVO.setRootFileId( userFile.getFileId() );
            userInfoVO.setRootFilename( userFile.getFilename() );
        }

        return userInfoVO;
    }
}
