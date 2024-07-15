package com.ldc.store.modules.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldc.store.cache.core.constants.CacheConstants;
import com.ldc.store.core.exception.RPanBusinessException;
import com.ldc.store.core.response.ResponseCode;
import com.ldc.store.core.utils.IdUtil;
import com.ldc.store.core.utils.JwtUtil;
import com.ldc.store.core.utils.PasswordUtil;
import com.ldc.store.modules.file.constants.FileConstants;
import com.ldc.store.modules.file.context.CreateFileContext;
import com.ldc.store.modules.file.domain.RPanUserFile;
import com.ldc.store.modules.file.service.IUserFileService;
import com.ldc.store.modules.user.constants.UserConstants;
import com.ldc.store.modules.user.context.*;
import com.ldc.store.modules.user.converter.UserConverter;
import com.ldc.store.modules.user.domain.RPanUser;
import com.ldc.store.modules.user.service.IUserService;
import com.ldc.store.modules.user.mapper.RPanUserMapper;
import com.ldc.store.modules.user.vo.UserInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
* @author 李Da锤
* @description 针对表【r_pan_user(用户信息表)】的数据库操作Service实现
* @createDate 2024-07-12 13:32:33
*/
@Service
public class IUserServiceImpl extends ServiceImpl<RPanUserMapper, RPanUser>
    implements IUserService {


    @Autowired
    private IUserFileService iUserFileService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserConverter userConverter;

    @Override
    public Long register(UserRegisterContext userRegisterContext) {
        //存入数据库
        doRegister(userRegisterContext);
        //创建根目录
        createRootFolder(userRegisterContext);
        //返回加密后的用户id
        return userRegisterContext.getEntity().getUserId();
    }

    @Override
    public String login(UserLoginContext userLoginContext) {
        //返回 token
        if(!checkAndSaveUserInfo(userLoginContext)){
            throw  new RPanBusinessException("用户名或密码错误！");
        }
        //生成token
        generateAccessToken(userLoginContext);
        //添加存入缓存中
        saveTokenInCache(userLoginContext);
        return userLoginContext.getAccessToken();
    }

    @Override
    public void loginOut(Long userId) {
        //移除缓存中的数据
       cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME)
                .evict(UserConstants.USER_LOGIN_TOKEN_PREFIX + userId);

    }


    @Override
    public String checkUsername(CheckUsernameContext checkUsernameContext) {
        String question = baseMapper.selectQuestionByUsername(checkUsernameContext.getUsername());
        if (StringUtils.isBlank(question)) {
            throw new RPanBusinessException("没有此用户");
        }
        return question;
    }

    /**
     * 用户忘记密码-校验密保答案
     *
     * @param checkAnswerContext
     * @return
     */
    @Override
    public String checkAnswer(CheckAnswerContext checkAnswerContext) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", checkAnswerContext.getUsername());
        queryWrapper.eq("question", checkAnswerContext.getQuestion());
        queryWrapper.eq("answer", checkAnswerContext.getAnswer());
        int count = count(queryWrapper);

        if (count == 0) {
            throw new RPanBusinessException("密保答案错误");
        }

        return generateCheckAnswerToken(checkAnswerContext);
    }



    /**
     * 重置用户密码
     * 1、校验token是不是有效
     * 2、重置密码
     *
     * @param resetPasswordContext
     */
    @Override
    public void resetPassword(ResetPasswordContext resetPasswordContext) {
        checkForgetPasswordToken(resetPasswordContext);
        checkAndResetUserPassword(resetPasswordContext);
    }

    /**
     * 在线修改密码
     * 1、校验旧密码
     * 2、重置新密码
     * 3、退出当前的登录状态
     *
     * @param changePasswordContext
     */
    @Override
    public void changePassword(ChangePasswordContext changePasswordContext) {
        checkOldPassword(changePasswordContext);
        doChangePassword(changePasswordContext);
        exitLoginStatus(changePasswordContext);
    }

    private void exitLoginStatus(ChangePasswordContext changePasswordContext) {
        loginOut(changePasswordContext.getUserId());
    }

    private void doChangePassword(ChangePasswordContext changePasswordContext) {
        String newPassword = changePasswordContext.getNewPassword();
        RPanUser entity = changePasswordContext.getEntity();
        String salt = entity.getSalt();

        String encNewPassword = PasswordUtil.encryptPassword(salt, newPassword);

        entity.setPassword(encNewPassword);

        if (!updateById(entity)) {
            throw new RPanBusinessException("修改用户密码失败");
        }
    }

    private void checkOldPassword(ChangePasswordContext changePasswordContext) {
        Long userId = changePasswordContext.getUserId();
        String oldPassword = changePasswordContext.getOldPassword();

        RPanUser entity = getById(userId);
        if (Objects.isNull(entity)) {
            throw new RPanBusinessException("用户信息不存在");
        }
        changePasswordContext.setEntity(entity);

        String encOldPassword = PasswordUtil.encryptPassword(entity.getSalt(), oldPassword);
        String dbOldPassword = entity.getPassword();
        if (!Objects.equals(encOldPassword, dbOldPassword)) {
            throw new RPanBusinessException("旧密码不正确");
        }
    }

    private void checkAndResetUserPassword(ResetPasswordContext resetPasswordContext) {
        String username = resetPasswordContext.getUsername();
        String password = resetPasswordContext.getPassword();
        RPanUser entity = getRPanUserByUsername(username);
        if (Objects.isNull(entity)) {
            throw new RPanBusinessException("用户信息不存在");
        }

        String newDbPassword = PasswordUtil.encryptPassword(entity.getSalt(), password);
        entity.setPassword(newDbPassword);
        entity.setUpdateTime(new Date());

        if (!updateById(entity)) {
            throw new RPanBusinessException("重置用户密码失败");
        }
    }
    private RPanUser getRPanUserByUsername(String username) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }


    private void checkForgetPasswordToken(ResetPasswordContext resetPasswordContext) {
        String token = resetPasswordContext.getToken();
        Object value = JwtUtil.analyzeToken(token, UserConstants.FORGET_USERNAME);
        if (Objects.isNull(value)) {
            throw new RPanBusinessException(ResponseCode.TOKEN_EXPIRE);
        }
        String tokenUsername = String.valueOf(value);
        if (!Objects.equals(tokenUsername, resetPasswordContext.getUsername())) {
            throw new RPanBusinessException("token错误");
        }
    }


    /**
     * 查询在线用户的基本信息
     * 1、查询用户的基本信息实体
     * 2、查询用户的根文件夹信息
     * 3、拼装VO对象返回
     *
     * @param userId
     * @return
     */
    @Override
    public UserInfoVO info(Long userId) {
        RPanUser entity = getById(userId);
        if (Objects.isNull(entity)) {
            throw new RPanBusinessException("用户信息查询失败");
        }
        RPanUserFile rPanUserFile = getUserRootFileInfo(userId);
        if (Objects.isNull(rPanUserFile)) {
            throw new RPanBusinessException("查询用户根文件夹信息失败");
        }
        return userConverter.assembleUserInfoVO(entity,rPanUserFile);
    }


    /**private method********************************************/


    /**
     * 获取用户根文件夹信息实体
     *
     * @param userId
     * @return
     */
    private RPanUserFile getUserRootFileInfo(Long userId) {
        return iUserFileService.getUserRootFile(userId);
    }

    private String generateCheckAnswerToken(CheckAnswerContext checkAnswerContext) {
        String token = JwtUtil.generateToken(checkAnswerContext.getUsername(), UserConstants.FORGET_USERNAME, checkAnswerContext.getUsername(), UserConstants.FIVE_MINUTES_LONG);
        return token;
    }



    private void generateAccessToken(UserLoginContext userLoginContext) {
        String accessToken = JwtUtil.generateToken(userLoginContext.getUserId().toString(), UserConstants.USER_Jwt_KEY,
                UserConstants.FORGET_USERNAME, UserConstants.ONE_DAY_LONG);
        userLoginContext.setAccessToken(accessToken);
    }

    private boolean checkAndSaveUserInfo(UserLoginContext userLoginContext) {
        LambdaQueryWrapper<RPanUser> queryWrapper =
                new LambdaQueryWrapper<RPanUser>().eq(RPanUser::getUsername, userLoginContext.getUsername());
        RPanUser user = getOne(queryWrapper);
        if(Objects.isNull(user))
            return false;
        userLoginContext.setUserId(user.getUserId());
        String salt = user.getSalt();
        String rePwd=PasswordUtil.encryptPassword(salt,userLoginContext.getPassword());
        return Objects.equals(rePwd, user.getPassword());
    }

    private void saveTokenInCache(UserLoginContext userLoginContext) {
        if(StrUtil.isEmpty(userLoginContext.getAccessToken())){
          throw new RPanBusinessException("用户签名生成失败！");
        }
        //说明: cacheManager 是spring为缓存提供的一个规范 使用它可以实现可插拔式的添加自定义的缓存框架
        Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
        cache.put(UserConstants.USER_LOGIN_TOKEN_PREFIX +userLoginContext.getUserId(),
                userLoginContext.getAccessToken());
    }


    private void createRootFolder(UserRegisterContext userRegisterContext) {
        CreateFileContext createFileContext = new CreateFileContext();
        createFileContext.setUserId(userRegisterContext.getEntity().getUserId());
        createFileContext.setFolderName(FileConstants.ROOT_FOLDER_NAME);
        createFileContext.setParentId(FileConstants.ROOT_PARENT_ID);
        iUserFileService.createFolder(createFileContext);
    }

    private void doRegister(UserRegisterContext userRegisterContext) {
        RPanUser user=userConverter.userRegisterContext2RPanUser(userRegisterContext);
//        BeanUtil.copyProperties(userRegisterContext,user);
        user.setSalt(PasswordUtil.getSalt());
        String password = PasswordUtil.encryptPassword(user.getSalt(), user.getPassword());
        user.setPassword(password);
        user.setUserId(IdUtil.get());
        userRegisterContext.setEntity(user);
        try{
            if(!save(user)){
                throw new RPanBusinessException("注册失败！");
            }
        }catch (DuplicateKeyException duplicateKeyException){
            throw new RPanBusinessException("用户名存在！");
        }
    }
}




