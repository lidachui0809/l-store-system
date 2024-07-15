package com.ldc.store.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldc.store.core.constants.RPanConstants;
import com.ldc.store.core.exception.RPanBusinessException;
import com.ldc.store.core.response.R;
import com.ldc.store.core.utils.FileUtils;
import com.ldc.store.core.utils.IdUtil;
import com.ldc.store.modules.file.constants.FileConstants;
import com.ldc.store.modules.file.context.*;
import com.ldc.store.modules.file.convert.FileConverter;
import com.ldc.store.modules.file.domain.RPanFile;
import com.ldc.store.modules.file.domain.RPanUserFile;
import com.ldc.store.modules.file.enums.DelFlagEnum;
import com.ldc.store.modules.file.enums.FileTypeEnum;
import com.ldc.store.modules.file.enums.FolderFlagEnum;
import com.ldc.store.modules.file.event.DeleteFileEvent;
import com.ldc.store.modules.file.service.IFileService;
import com.ldc.store.modules.file.service.IUserFileService;
import com.ldc.store.modules.file.mapper.RPanUserFileMapper;
import com.ldc.store.modules.file.vo.UserFileResultVO;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 李Da锤
 * @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Service实现
 * @createDate 2024-07-12 13:37:32
 */
@Service
public class IUserFileServiceImpl extends ServiceImpl<RPanUserFileMapper, RPanUserFile>
        implements IUserFileService, ApplicationContextAware {


    private ApplicationContext applicationContext;

    @Autowired
    private IFileService iFileService;

    @Autowired
    private FileConverter fileConverter;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public Long createFolder(CreateFileContext createFileContext) {
        //将用户根目录信息插入数据表
        return saveUserFile(createFileContext.getParentId(),
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


    @Override
    public List<UserFileResultVO> selectFolderList(QueryFileListContext queryFileListContext) {
        //根据parentId userId fileType查找文件
        return baseMapper.queryFileList(queryFileListContext);
    }

    @Override
    public R updateFileName(UpdateFilenameContext context) {
        //重命名 判断file_id合法性 判断重命名的字符是否符合规则 检验是否有权限 检验是否存在同级相同文件名
        checkReNameSuccessfully(context);
        doReName(context);
        return R.success();
    }

    @Override
    public void deleteFiles(DeleteFileContext context) {
        //判断 传递的fileId是否合法 用户是否有权限 执行删除 同时发布删除事件
        checkFileDeleteCondition(context);
        doDeleteFile(context);
        deleteAfterPublishEvent(context);
    }

    @Override
    public boolean secUpload(SecUploadFileContext context) {
        //根据文件标识 查找对应文件 如果有 则插入文件上传记录 如果不存在 秒传失败
        QueryRealFileListContext queryRealFileListContext = new QueryRealFileListContext();
        queryRealFileListContext.setUserId(context.getUserId());
        queryRealFileListContext.setIdentifier(context.getIdentifier());
        List<RPanFile> rPanFileList=iFileService.queryListFilesById(queryRealFileListContext);
        if (rPanFileList.isEmpty()) {
            return false;
        }
        RPanFile entity =rPanFileList.get(0);
        //找到文件
        saveUserFile(
                context.getParentId(), context.getFilename(), FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(context.getFilename()))
                , entity.getFileId(), context.getUserId(), entity.getFileSizeDesc());
        return true;
    }

    /**
     * 文件上传 保存文件以及文件实体信息 创建用户文件关系记录
     * @param context
     */
    @Override
    @Transactional
    public void upload(UploadFileContext context) {
        //保存文件到磁盘 这里需要返回文件的真实物理地址
        saveRealFile(context);
        //保存用户文件关系记录
        saveUserFile(
                context.getParentId(),
                context.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(context.getFilename())),
                context.getRecord().getFileId(),
                context.getUserId(),
                FileUtils.byteCountToDisplaySize(context.getTotalSize())
        );
    }




    /**
     * 保存文件到磁盘 同时返回文件的fileId和realPath
     * @param context
     */
    private void saveRealFile(UploadFileContext context) {
        SaveFileContext saveFileContext=fileConverter.uploadFileContext2SaveFileContext(context) ;
        iFileService.saveFileInCD(saveFileContext);
        context.setRecord(saveFileContext.getRecord());
    }


    private void deleteAfterPublishEvent(DeleteFileContext context) {
        //发布删除事件 这里基于spring的发布订阅实现的
        //TODO 后期使用mq代替
        applicationContext.publishEvent(new DeleteFileEvent(this, context));
    }

    private void doDeleteFile(DeleteFileContext context) {
        LambdaQueryWrapper<RPanUserFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RPanUserFile::getFileId, context.getFileIdList());
        queryWrapper.apply("del_flag={0}", DelFlagEnum.YES.getCode());
        if (!update(queryWrapper)) {
            throw new RPanBusinessException("删除失败！");
        }
    }

    private void checkFileDeleteCondition(DeleteFileContext context) {
        List<Long> fileIdList = context.getFileIdList();

        List<RPanUserFile> rPanUserFiles = listByIds(fileIdList);
        if (rPanUserFiles.size() != fileIdList.size()) {
            throw new RPanBusinessException("存在不合法的文件记录");
        }
        //检查fileId是否合法 set会自动去重
        Set<Long> fileIdSet = rPanUserFiles.stream().map(RPanUserFile::getFileId).collect(Collectors.toSet());
        if (fileIdSet.size() != fileIdList.size()) {
            throw new RPanBusinessException("存在不合法的文件记录");
        }
        //检查是否是当前用户的文件
        Set<Long> userIdSet = rPanUserFiles.stream().map(RPanUserFile::getUserId).collect(Collectors.toSet());
        if (userIdSet.size() != 1) {
            throw new RPanBusinessException("存在不合法的文件记录");
        }
        Long dbUserId = userIdSet.stream().findFirst().get();
        if (!Objects.equals(dbUserId, context.getUserId())) {
            throw new RPanBusinessException("当前登录用户没有删除该文件的权限");
        }
    }

    private void doReName(UpdateFilenameContext context) {


        if (baseMapper.updateById(context.getEntity()) <= 0) {
            throw new RPanBusinessException("重命名失败！");
        }
    }

    private void checkReNameSuccessfully(UpdateFilenameContext context) {
        RPanUserFile entity = baseMapper.selectById(context.getFileId());
        if (Objects.isNull(entity)) throw new RPanBusinessException("未找到对应文件");
        if (!Objects.equals(entity.getUserId(), context.getUserId()))
            throw new RPanBusinessException("暂无权限修改非本人文件文件名！");
        //查看指定根目录下 是否存在同名的文件名
        LambdaQueryWrapper<RPanUserFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getParentId, entity.getParentId());
        queryWrapper.eq(RPanUserFile::getFilename, entity.getFilename());
        queryWrapper.eq(RPanUserFile::getFileType, entity.getFileType());
        int count = baseMapper.selectCount(queryWrapper);
        if (count > 0) throw new RPanBusinessException("存在同名文件,换一个吧！");
        entity.setFilename(context.getNewFilename());
        entity.setUpdateTime(new Date());
        entity.setUpdateUser(context.getUserId());
        context.setEntity(entity);
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


    /**
     * 保存用户文件信息
     */
    private Long saveUserFile(Long parentId,
                              String filename,
                              FolderFlagEnum folderFlagEnum,
                              Integer fileType,
                              Long realFileId,
                              Long userId,
                              String fileSizeDesc) {
        RPanUserFile rPanUserFile = assembleUserFileBean(parentId, filename, folderFlagEnum,
                fileType, realFileId, userId, fileSizeDesc);
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




