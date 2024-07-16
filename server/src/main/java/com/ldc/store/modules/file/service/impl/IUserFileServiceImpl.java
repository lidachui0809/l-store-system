package com.ldc.store.modules.file.service.impl;

import cn.hutool.core.util.CharsetUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldc.store.common.utils.HttpUtil;
import com.ldc.store.core.constants.RPanConstants;
import com.ldc.store.core.exception.RPanBusinessException;
import com.ldc.store.core.response.R;
import com.ldc.store.core.utils.FileUtils;
import com.ldc.store.core.utils.IdUtil;
import com.ldc.store.engine.core.StoreEngine;
import com.ldc.store.engine.core.context.ReadFileContext;
import com.ldc.store.modules.file.constants.FileConstants;
import com.ldc.store.modules.file.context.*;
import com.ldc.store.modules.file.convert.FileConverter;
import com.ldc.store.modules.file.domain.RPanFile;
import com.ldc.store.modules.file.domain.RPanUserFile;
import com.ldc.store.modules.file.enums.DelFlagEnum;
import com.ldc.store.modules.file.enums.FileTypeEnum;
import com.ldc.store.modules.file.enums.FolderFlagEnum;
import com.ldc.store.modules.file.event.DeleteFileEvent;
import com.ldc.store.modules.file.event.UserSearchEvent;
import com.ldc.store.modules.file.service.IFileService;
import com.ldc.store.modules.file.service.IUserFileService;
import com.ldc.store.modules.file.mapper.RPanUserFileMapper;
import com.ldc.store.modules.file.vo.BreadcrumbsVO;
import com.ldc.store.modules.file.vo.FileSearchResultVO;
import com.ldc.store.modules.file.vo.FolderTreeNodeVO;
import com.ldc.store.modules.file.vo.UserFileResultVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
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

    @Autowired
    private StoreEngine storeEngine;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public Long createFolder(CreateFileContext createFileContext) {
        //将用户根目录信息插入数据表
        return saveUserFileRecord(createFileContext.getParentId(),
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
        List<RPanFile> rPanFileList = iFileService.queryListFilesById(queryRealFileListContext);
        if (rPanFileList.isEmpty()) {
            return false;
        }
        RPanFile entity = rPanFileList.get(0);
        //找到文件
        saveUserFileRecord(
                context.getParentId(), context.getFilename(), FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(context.getFilename()))
                , entity.getFileId(), context.getUserId(), entity.getFileSizeDesc());
        return true;
    }

    /**
     * 文件上传 保存文件以及文件实体信息 创建用户文件关系记录
     *
     * @param context
     */
    @Override
    @Transactional
    public void upload(UploadFileContext context) {
        //保存文件到磁盘 这里需要返回文件的真实物理地址
        saveRealFile(context);
        //保存用户文件关系记录
        saveUserFileRecord(
                context.getParentId(),
                context.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(context.getFilename())),
                context.getRecord().getFileId(),
                context.getUserId(),
                FileUtils.byteCountToDisplaySize(context.getTotalSize())
        );
    }

    @Override
    public void saveMergeAfterFileRecord(ChunkFileMergeContext context) {
        saveUserFileRecord(context.getParentId(), context.getFilename(), FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(context.getFilename())),
                context.getRecord().getFileId(), context.getUserId(), FileUtils.byteCountToDisplaySize(context.getTotalSize()));
    }

    /**
     * 检查文件是否存在
     * 解决文件下载跨域问题
     * 获得文件真实存储路劲 存储引擎加载文件输出流 写入response中
     * @param context
     */
    @Override
    public void downloadFile(FileDownloadContext context) {
        RPanUserFile userFileRecord = getById(context.getFileId());
        checkFileCondition(context.getUserId(), userFileRecord);
        doDownload(context, userFileRecord);
    }

    /**
     * 文件预览 和下载差不多 只是修改了一下请求头参数
     * @param context
     */

    @Override
    public void previewFile(FilePreviewContext context) {
        RPanUserFile userFileRecord = getById(context.getFileId());
        checkFileCondition(context.getUserId(),userFileRecord);
        doPreview(context,userFileRecord);
    }

    /**
     * 获得用户的文件夹树 为了提高效率 直接一次性将用户相关的文件夹查出 在内存中构建文件夹树
     * @param queryFolderTreeContext
     * @return
     */
    @Override
    public List<FolderTreeNodeVO> getFolderTree(QueryFolderTreeContext queryFolderTreeContext) {
        List<FolderTreeNodeVO> folderTreeNodeVOList= queryUserFolder(queryFolderTreeContext);
        folderTreeNodeVOList=assembleFolderTreeNode(folderTreeNodeVOList);
        return folderTreeNodeVOList;
    }

    /**
     * 文件批量转移
     *
     * 1. 父文件id是否是文件夹
     * 2. 被转移文件不能是目标文件的子文件
     * 3. 目标文件不能包含再被转移文件中
     * @param fileTransferContext
     */
    @Override
    public void transferFile(FileTransferContext fileTransferContext) {
        doCheckTransferConditions(fileTransferContext);
        doTransferFile(fileTransferContext);
    }

    /**
     * 批量复制文件
     * @param fileTransferContext
     */
    @Override
    public void copyFiles(FileCopyContext fileTransferContext) {
        doCheckCopyConditions(fileTransferContext);
        doCopy(fileTransferContext);
    }

    @Override
    public List<FileSearchResultVO> search(FileSearchContext context) {
        List<FileSearchResultVO> result = doSearch(context);
        fillParentFilename(result);
        afterSearch(context);
        return result;
    }

    /**
     * 文件夹面包屑
     * @param context
     * @return
     */
    @Override
    public List<BreadcrumbsVO> getBreadcrumbs(BreadcrumbsContext context) {
        List<RPanUserFile> userAllFolder = getUserAllFolder(context.getUserId());
        Map<Long, BreadcrumbsVO> fileIdBreadcrumbsVOMap = userAllFolder.stream().map(BreadcrumbsVO::transfer)
                .collect(Collectors.toMap(BreadcrumbsVO::getId, item -> item));
        Long fileId = context.getFileId();
        List<BreadcrumbsVO> breadcrumbsVOList=new LinkedList<>();
        BreadcrumbsVO breadcrumbsVO ;
        //根据目标fileId 依此往上递归查找 直至根目录
        do{
            breadcrumbsVO=fileIdBreadcrumbsVOMap.get(fileId);
            if(Objects.nonNull(breadcrumbsVO)){
                fileId=breadcrumbsVO.getId();
                breadcrumbsVOList.add(breadcrumbsVO);
            }
        }while (Objects.nonNull(breadcrumbsVO));
        return breadcrumbsVOList;
    }


    /**
     * 获得user的所有文件夹
     * @param userId
     * @return
     */
    private List<RPanUserFile> getUserAllFolder(Long userId){
       return list(new LambdaQueryWrapper<RPanUserFile>()
                .eq(RPanUserFile::getUserId,userId)
                .eq(RPanUserFile::getFolderFlag,FolderFlagEnum.YES.getCode())
                .eq(RPanUserFile::getDelFlag,DelFlagEnum.NO.getCode()));
    }

    /**
     * 填充文件列表的父文件名称
     *
     * @param result
     */
    private void fillParentFilename(List<FileSearchResultVO> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        List<Long> parentIdList = result.stream().map(FileSearchResultVO::getParentId).collect(Collectors.toList());
        List<RPanUserFile> parentRecords = listByIds(parentIdList);
        Map<Long, String> fileId2filenameMap = parentRecords.stream().collect(Collectors.toMap(RPanUserFile::getFileId, RPanUserFile::getFilename));
        result.stream().forEach(vo -> vo.setParentFilename(fileId2filenameMap.get(vo.getParentId())));
    }

    /**
     * 搜索文件列表
     *
     * @param context
     * @return
     */
    private List<FileSearchResultVO> doSearch(FileSearchContext context) {
        return baseMapper.searchFile(context);
    }


    /**
     * 搜索的后置操作
     * <p>
     * 1、发布文件搜索的事件
     *
     * @param context
     */
    private void afterSearch(FileSearchContext context) {
        UserSearchEvent event = new UserSearchEvent(this, context.getKeyword(), context.getUserId());
        applicationContext.publishEvent(event);
    }

    private void doCopy(FileCopyContext fileCopyContext) {
        List<RPanUserFile> prepareFiles = fileCopyContext.getPrepareFiles();
        List<RPanUserFile> userAllFilesRecords = fileCopyContext.getUserAllFilesRecords();
        Map<Long, List<RPanUserFile>> parentIdMap = 
                userAllFilesRecords.parallelStream().collect(Collectors.groupingBy(RPanUserFile::getParentId));
        //递归的方式获得目标文件的所有子目录
        for (RPanUserFile prepareFile : prepareFiles) {
            getFileChildren(prepareFiles,prepareFile.getFileId(),parentIdMap);
        }
        //更新复制后文件的id 以及parentId
        prepareFiles.forEach(userFile -> {
            userFile.setFileId(IdUtil.get());
            userFile.setUserId(fileCopyContext.getUserId());
            userFile.setUpdateUser(fileCopyContext.getUserId());
            userFile.setUpdateTime(new Date());
            userFile.setParentId(fileCopyContext.getTargetParentId());
        });
        if (!saveBatch(prepareFiles,prepareFiles.size())) {
            throw new RPanBusinessException("文件复制失败！");
        }
    }


    private void doCheckCopyConditions(FileCopyContext fileCopyContext) {
        if (!checkIsFolder(fileCopyContext.getTargetParentId())) {
            throw new RPanBusinessException("目标父文件必须是文件夹！");
        }
        List<Long> unavailableFileId=new ArrayList<>();
        List<RPanUserFile> prepareFile=new ArrayList<>();
        List<RPanUserFile> userAllFiles = 
                getUnAvailableFileIds(fileCopyContext.getUserId(), fileCopyContext.getCopyFileId(), unavailableFileId, prepareFile);
        if(unavailableFileId.contains(fileCopyContext.getTargetParentId())) {
            throw new RPanBusinessException("目标父文件不可以是被移动的文件或其子文件！");
        }
        fileCopyContext.setPrepareFiles(prepareFile);
        fileCopyContext.setUserAllFilesRecords(userAllFiles);
    }

    private boolean checkIsFolder(Long parentId) {
        RPanUserFile targetFolder = getById(parentId);
        if(FolderFlagEnum.NO.getCode().equals(targetFolder.getFolderFlag()))
            return false;
        return true;    
    }

    private void doTransferFile(FileTransferContext context) {
        context.getPrepareFiles().forEach(rPanUserFile -> {
            rPanUserFile.setParentId(context.getTargetParentId());
            rPanUserFile.setUserId(context.getUserId());
            rPanUserFile.setUpdateUser(context.getUserId());
            rPanUserFile.setUpdateTime(new Date());
        });
        updateBatchById(context.getPrepareFiles());
    }

    /**
     * 为节省每次数据库的查找 直接将用户的所有文件全部找出
     * 并将结果按照parentId进行分组 最后遍历被转移的文件
     * 递归查找出其所有子文件的id 并保存添加 最后判断是否存在目标文件夹id
     * @param fileTransferContext
     */
    private void doCheckTransferConditions(FileTransferContext fileTransferContext) {
        if (!checkIsFolder(fileTransferContext.getTargetParentId())) {
            throw new RPanBusinessException("目标父文件必须是文件夹！");
        }
        List<Long> unavailableFileId=new ArrayList<>();
        List<RPanUserFile> prepareFile=new ArrayList<>();
        getUnAvailableFileIds(fileTransferContext.getUserId(),fileTransferContext.getTransferFileId()
                , unavailableFileId,prepareFile);
        fileTransferContext.setPrepareFiles(prepareFile);
        if(unavailableFileId.contains(fileTransferContext.getTargetParentId()))
            throw new RPanBusinessException("目标父文件不可以是被移动的文件或其子文件！");
    }

    private List<RPanUserFile> getUnAvailableFileIds(Long userId,List<Long> targetFileIds,
                                       List<Long> unavailableFileId,List<RPanUserFile> targetFile) {
        List<RPanUserFile> userAllFiles=baseMapper.selectList(new LambdaQueryWrapper<RPanUserFile>()
                .eq(RPanUserFile::getUserId, userId)
                .eq(RPanUserFile::getDelFlag,DelFlagEnum.NO.getCode()));
        List<RPanUserFile> prepareFile=userAllFiles.parallelStream()
                .filter(rPanUserFile -> targetFileIds.contains(rPanUserFile.getUserId()))
                .collect(Collectors.toList());
        Map<Long, List<RPanUserFile>> parentIdMap =
                userAllFiles.parallelStream().collect(Collectors.groupingBy(RPanUserFile::getParentId));

        ArrayList<RPanUserFile> unavailableFile = new ArrayList<>();
        for (RPanUserFile file : prepareFile) {
            unavailableFileId.add(file.getFileId());
            getFileChildren(unavailableFile,file.getFileId(),parentIdMap);
        }
        List<Long> unavailableFileIdRe =
                unavailableFile.stream().map(RPanUserFile::getFileId).collect(Collectors.toList());
        unavailableFileId.addAll(unavailableFileIdRe);
        targetFile.addAll(prepareFile);
        return userAllFiles;
    }

    /**
     * 获得目标文件的子文件
     * @param resultList
     * @param targetFileId
     * @param parentIdMap
     */
    private void getFileChildren(List<RPanUserFile> resultList, Long targetFileId, Map<Long, List<RPanUserFile>> parentIdMap){
        List<RPanUserFile> fileChildren = parentIdMap.get(targetFileId);
        if(CollectionUtils.isEmpty(fileChildren))
            return;
        for (RPanUserFile fileChild : fileChildren) {
            resultList.add(fileChild);
            //只有文件夹才需要判断
            if(FolderFlagEnum.NO.getCode().equals(fileChild.getFolderFlag()))
                continue;
            getFileChildren(resultList,fileChild.getFileId(),parentIdMap);
        }
    }


    //转移的文件夹不是包含目标文件夹 同时不能是目标文件夹的子文件夹
    private List<FolderTreeNodeVO> assembleFolderTreeNode(List<FolderTreeNodeVO> folderTreeNodeVOList) {
        //使用parentId进行分组
        Map<Long, List<FolderTreeNodeVO>> parentFolderMap =
                folderTreeNodeVOList.stream().collect(Collectors.groupingBy(FolderTreeNodeVO::getParentId));
        //遍历所有文件夹节点 将子节点添加到到父节点中
        for (FolderTreeNodeVO node : folderTreeNodeVOList) {
            List<FolderTreeNodeVO> childrenNodes = parentFolderMap.get(node.getId());
            if(CollectionUtils.isNotEmpty(childrenNodes))
                node.getChildren().addAll(childrenNodes);
        }
        return folderTreeNodeVOList.stream().filter(item->Objects.equals(item.getParentId(),FileConstants.ROOT_PARENT_ID))
                .collect(Collectors.toList());
    }

    private List<FolderTreeNodeVO> queryUserFolder(QueryFolderTreeContext context) {
        List<RPanUserFile> panUserFiles = baseMapper.selectList(new LambdaQueryWrapper<RPanUserFile>()
                .eq(RPanUserFile::getUserId, context.getUserId())
                .eq(RPanUserFile::getDelFlag, DelFlagEnum.NO.getCode()));
        if(panUserFiles.isEmpty())
            return Collections.emptyList();
        return panUserFiles.stream().map(fileConverter::panUserFiles2FolderTreeNodeVO).collect(Collectors.toList());
    }

    /** 私有方法区域
     *
     */


    private void doPreview(FilePreviewContext context, RPanUserFile userFileRecord) {
        RPanFile realFile = iFileService.getOne(new LambdaQueryWrapper<RPanFile>()
                .eq(RPanFile::getFileId, userFileRecord.getRealFileId())
                .eq(RPanFile::getCreateUser, context.getUserId()));
        if (Objects.isNull(realFile)) throw new RPanBusinessException("未找到指定文件物理地址信息！");
        addCommonResponseHeader(context.getResponse(), realFile.getFilePreviewContentType());
        writeRealFile2OutputStream(context.getResponse(),realFile.getRealPath());
    }

    private void doDownload(FileDownloadContext context, RPanUserFile userFileRecord) {
        RPanFile realFile = iFileService.getOne(new LambdaQueryWrapper<RPanFile>()
                .eq(RPanFile::getFileId, userFileRecord.getRealFileId())
                .eq(RPanFile::getCreateUser, context.getUserId()));
        if (Objects.isNull(realFile)) throw new RPanBusinessException("未找到指定文件物理地址信息！");
        //添加文件标识响应头
        addCommonResponseHeader(context.getResponse(), MediaType.APPLICATION_OCTET_STREAM_VALUE);
        //添加下载文件的属性信息
        context.getResponse().addHeader(FileConstants.CONTENT_DISPOSITION,FileConstants.ATTACHMENT_FILENAME
                +new String(userFileRecord.getFilename().getBytes(StandardCharsets.UTF_8), CharsetUtil.CHARSET_UTF_8));
        writeRealFile2OutputStream(context.getResponse(),realFile.getRealPath());
    }

    private void writeRealFile2OutputStream(HttpServletResponse response, String realPath) {
        //TODO 委托给文件存储进行文件的写入
        ReadFileContext readFileContext = new ReadFileContext();
        readFileContext.setRealPath(realPath);
        try{
            readFileContext.setOutputStream(response.getOutputStream());
            storeEngine.readFile(readFileContext);
        }catch (IOException e){
            e.printStackTrace();
            throw  new RPanBusinessException("文件下载失败！");
        }
    }

    private void addCommonResponseHeader(HttpServletResponse response, String contentTypeValue) {
        response.reset();
        HttpUtil.addCorsResponseHeaders(response);
        response.addHeader("Content-Type", contentTypeValue);
        response.setContentType(contentTypeValue);
    }

    private void checkFileCondition(Long userId, RPanUserFile userFile) {
        if (Objects.isNull(userFile))
            throw new RPanBusinessException("未找到指定文件！");
        if (!userFile.getUserId().equals(userId))
            throw new RPanBusinessException("暂无权限下载该文件！");
        if(FolderFlagEnum.YES.getCode().equals(userFile.getFileType()))
            throw new RPanBusinessException("文件夹暂不支持下载！");
    }

    /**
     * 保存用户文件记录信息
     */
    private Long saveUserFileRecord(Long parentId,
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
            throw new RPanBusinessException("创建文件记录失败！");
        }
        return rPanUserFile.getFileId();
    }


    /**
     * 保存文件到磁盘 同时返回文件的fileId和realPath
     *
     * @param context
     */
    private void saveRealFile(UploadFileContext context) {
        SaveChunkFileContext saveChunkFileContext = fileConverter.uploadFileContext2SaveFileContext(context);
        iFileService.saveFileInCD(saveChunkFileContext);
        context.setRecord(saveChunkFileContext.getRecord());
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




