package com.ldc.store.modules.user.mapper;

import com.ldc.store.modules.user.domain.RPanUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 李Da锤
* @description 针对表【r_pan_user(用户信息表)】的数据库操作Mapper
* @createDate 2024-07-12 13:32:33
* @Entity com.ldc.store.modules.user.domain.RPanUser
*/
public interface RPanUserMapper extends BaseMapper<RPanUser> {

    String selectQuestionByUsername(String username);
}




