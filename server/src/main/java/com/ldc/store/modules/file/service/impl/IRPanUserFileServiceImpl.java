package com.ldc.store.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldc.store.core.constants.RPanConstants;
import com.ldc.store.core.exception.RPanBusinessException;
import com.ldc.store.core.utils.IdUtil;
import com.ldc.store.modules.file.constants.FileConstants;
import com.ldc.store.modules.file.context.CreateFileContext;
import com.ldc.store.modules.file.domain.RPanUserFile;
import com.ldc.store.modules.file.enums.DelFlagEnum;
import com.ldc.store.modules.file.enums.FolderFlagEnum;
import com.ldc.store.modules.file.service.IRPanUserFileService;
import com.ldc.store.modules.file.mapper.RPanUserFileMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author 李Da锤
 * @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Service实现
 * @createDate 2024-07-12 13:37:32
 */
@Service
public class IRPanUserFileServiceImpl extends ServiceImpl<RPanUserFileMapper, RPanUserFile>
        implements IRPanUserFileService {

    @Override
    public Long createFolder(CreateFileContext createFileContext) {
        //将用户根目录信息插入数据表
        return saveFolder(createFileContext.getParentId(),
                createFileContext.getFolderName(), FolderFlagEnum.YES, null, null, createFileContext.getUserId(),
                null);
    }

    @Override
    public RPanUserFile getUserRootFile(Long userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("parent_id", FileConstants.ROOT_PARENT_ID);
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        queryWrapper.eq("folder_flag", FolderFlagEnum.YES.getCode());
        return getOne(queryWrapper);
    }



    private RPanUserFile assembleUserFileBean(Long parentId,
                                              String filename,
                                              FolderFlagEnum folderFlagEnum,
                                              Integer fileType,
                                              Long realFileId,
                                              Long userId,
                                              String fileSizeDesc) {
        RPanUserFile entity = new RPanUserFile();
        entity.setFileId(IdUtil.get());
        entity.setUserId(userId);
        entity.setParentId(parentId);
        entity.setRealFileId(realFileId);
        entity.setFilename(filename);
        entity.setFolderFlag(folderFlagEnum.getCode());
        entity.setFileSizeDesc(fileSizeDesc);
        entity.setFileType(fileType);
        entity.setDelFlag(DelFlagEnum.NO.getCode());
        entity.setCreateUser(userId);
        entity.setCreateTime(new Date());
        entity.setUpdateUser(userId);
        entity.setUpdateTime(new Date());
        return entity;
    }


    private Long saveFolder(Long parentId,
                            String filename,
                            FolderFlagEnum folderFlagEnum,
                            Integer fileType,
                            Long realFileId,
                            Long userId,
                            String fileSizeDesc) {
        RPanUserFile rPanUserFile = assembleUserFileBean(parentId,filename, folderFlagEnum,
                fileType, realFileId, userId,fileSizeDesc);
        handlerDuplicateFileName(rPanUserFile);
        if (!save(rPanUserFile)) {
            throw new RPanBusinessException("创建文件夹失败！");
        }
        return rPanUserFile.getFileId();
    }

    /**
     * 处理重复文件名
     *
     * @param rPanUserFile
     */
    private void handlerDuplicateFileName(RPanUserFile rPanUserFile) {
        String fileFullName = rPanUserFile.getFilename();
        String fileName, fileSuffix;
        //分割文件名与后缀
        int index = fileFullName.lastIndexOf(RPanConstants.POINT_STR);
        if (index == -1) {
            fileName = fileFullName;
            fileSuffix = RPanConstants.EMPTY_STR;
        } else {
            fileName = fileFullName.substring(0, index);
            fileSuffix = fileFullName.replace(fileName, RPanConstants.EMPTY_STR);
        }
        Integer count = getDuplicateFileName(rPanUserFile, fileName);
        if (count == 0)
            return;
        rPanUserFile.setFilename(renameFolderName(fileName, fileSuffix, count));
    }

    private String renameFolderName(String fileName, String fileSuffix, Integer count) {
        return new StringBuilder().append(fileName)
                .append("(").append(count).append(")").append(fileSuffix).toString();
    }


    private Integer getDuplicateFileName(RPanUserFile rPanUserFile, String fileName) {

        //查找数据表中 同目录下的相同类型的文件名
        LambdaQueryWrapper<RPanUserFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getParentId, rPanUserFile.getParentId());
        queryWrapper.eq(RPanUserFile::getFolderFlag, rPanUserFile.getFolderFlag());
        queryWrapper.eq(RPanUserFile::getDelFlag, DelFlagEnum.NO.getCode());
        queryWrapper.eq(RPanUserFile::getUserId, rPanUserFile.getUserId());
        queryWrapper.likeLeft(RPanUserFile::getFilename, fileName);
        return getBaseMapper().selectCount(queryWrapper);
    }

}




