package com.ldc.store.modules.file.mapper;

import com.ldc.store.modules.file.context.QueryFileListContext;
import com.ldc.store.modules.file.domain.RPanUserFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ldc.store.modules.file.vo.UserFileResultVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 李Da锤
* @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Mapper
* @createDate 2024-07-12 13:37:32
* @Entity com.ldc.store.modules.file.domain.RPanUserFile
*/
public interface RPanUserFileMapper extends BaseMapper<RPanUserFile> {

    List<UserFileResultVO> queryFileList(@Param("param") QueryFileListContext queryFileListContext);
}




