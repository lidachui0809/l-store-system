package com.ldc.store.modules.file.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldc.store.common.config.IStoreSystemConfig;
import com.ldc.store.core.exception.RPanBusinessException;
import com.ldc.store.engine.core.StoreEngine;
import com.ldc.store.engine.core.context.StoreFileChunkContext;
import com.ldc.store.modules.file.context.*;
import com.ldc.store.modules.file.convert.FileConverter;
import com.ldc.store.modules.file.domain.RPanFileChunk;
import com.ldc.store.modules.file.enums.MergeFlagEnum;
import com.ldc.store.modules.file.service.IFileChunkService;
import com.ldc.store.modules.file.mapper.RPanFileChunkMapper;
import com.ldc.store.modules.file.service.IFileService;
import com.ldc.store.modules.file.service.IUserFileService;
import com.ldc.store.modules.file.vo.ChunkFileUploadResultVO;
import com.ldc.store.modules.file.vo.FileChunksVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 李Da锤
 * @description 针对表【r_pan_file_chunk(文件分片信息表)】的数据库操作Service实现
 * @createDate 2024-07-12 13:37:32
 */
@Service
public class IFileChunkServiceImpl extends ServiceImpl<RPanFileChunkMapper, RPanFileChunk>
        implements IFileChunkService {


    @Autowired
    private StoreEngine storeEngine;

    @Autowired
    private FileConverter fileConverter;


    @Autowired
    private IStoreSystemConfig config;

    @Autowired
    private IUserFileService iUserFileService;

    @Autowired
    private IFileService iFileService;


    /**
     * 保存文件分片 保存文件分片信息 通过存储引擎保存文件到磁盘
     *
     * @param context
     * @return
     */
    @Override
    public synchronized ChunkFileUploadResultVO chunkFileUpload(ChunkUploadFileContext context) {
        doStoreChunkFileInCD(context);
        doSaveChunkFileRecord(context);
        doCheckMergeCondition(context);
        ChunkFileUploadResultVO vo
                = new ChunkFileUploadResultVO();
        vo.setMergeFlag(context.getMergeFlagEnum().getCode());
        return vo;
    }

    /**
     * 获得文件分片信息
     *
     * @param context
     * @return
     */
    @Override
    public FileChunksVO getFileChunksIInfo(QueryFileChunkContext context) {
        LambdaQueryWrapper<RPanFileChunk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(RPanFileChunk::getChunkNumber);
        queryWrapper.eq(RPanFileChunk::getIdentifier, context.getIdentifier());
        queryWrapper.eq(RPanFileChunk::getCreateUser, context.getUserId());
        //检查是否已经超时过期
        queryWrapper.gt(RPanFileChunk::getExpirationTime, new Date());
        List<Integer> fileChunkNumbers =
                listObjs(queryWrapper, (value) -> Integer.valueOf(value.toString()));
        return new FileChunksVO(fileChunkNumbers);
    }

    /**
     * 分片文件合并
     * <p>
     * 查询文件所有分片信息 得到分片文件的具体保存的地址 交给存储引擎进行合并处理 得到合并后的文件地址
     * 删除该文件分片信息 保存用户文件记录信息
     *
     * @param context
     */
    @Override
    public void mergeChunkFile(ChunkFileMergeContext context) {
        doMergeChunkFileAndSaveFile(context);
        doStoreMergeAfterFileRecord(context);
    }

    private void doStoreMergeAfterFileRecord(ChunkFileMergeContext context) {
        //保存用户文件记录
        iUserFileService.saveMergeAfterFileRecord(context);
    }


    private void doMergeChunkFileAndSaveFile(ChunkFileMergeContext context) {
        LambdaQueryWrapper<RPanFileChunk> lambdaQuery = new LambdaQueryWrapper<>();
        lambdaQuery.eq(RPanFileChunk::getIdentifier, context.getIdentifier());
        lambdaQuery.eq(RPanFileChunk::getCreateUser, context.getUserId());
        lambdaQuery.ge(RPanFileChunk::getExpirationTime, new Date());
        //需要以分片号升序
        lambdaQuery.orderByAsc(RPanFileChunk::getChunkNumber);

        List<RPanFileChunk> fileChunks = list(lambdaQuery);
        if (fileChunks.isEmpty()) {
            throw new RPanBusinessException("文件分片不存在！");
        }
        //得到所有分片存储路径
        List<String> chunkPaths
                = fileChunks.stream().map(RPanFileChunk::getRealPath).collect(Collectors.toList());
        SaveChunkFileMergeContext saveChunkFileMergeContext
                = fileConverter.chunkFileMergeContext2SaveChunkFileMergeContext(context);
        saveChunkFileMergeContext.setRealPathList(chunkPaths);
        iFileService.mergeChunkFile(saveChunkFileMergeContext);
        context.setRecord(saveChunkFileMergeContext.getRecord());
        //删除分片数据
        List<Long> chunkFileIds = fileChunks.stream().map(RPanFileChunk::getId).collect(Collectors.toList());
        baseMapper.deleteBatchIds(chunkFileIds);
    }

    /**
     * 检查文件分片是否上传完整
     *
     * @param context
     */
    private  void doCheckMergeCondition(ChunkUploadFileContext context) {
        LambdaQueryWrapper<RPanFileChunk> queryWrapper
                = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanFileChunk::getIdentifier, context.getIdentifier());
        int count = count(queryWrapper);
        if (count == context.getTotalChunks()) {
            context.setMergeFlagEnum(MergeFlagEnum.READY);
        }
    }

    /**
     * 将文件分片信息 保存在数据库
     *
     * @param context
     */
    private void doSaveChunkFileRecord(ChunkUploadFileContext context) {

        RPanFileChunk fileChunk = new RPanFileChunk();
        fileChunk.setChunkNumber(context.getChunkNumber());
        fileChunk.setIdentifier(context.getIdentifier());
        //设置过期时间为一天
        fileChunk.setExpirationTime(DateUtil.offsetDay(new Date(),
                config.getChunkExpirationTime()));
        fileChunk.setRealPath(context.getRealPath());
        fileChunk.setCreateUser(context.getUserId());
        fileChunk.setCreateTime(new Date());
        if (!save(fileChunk)) {
            throw new RPanBusinessException("文件分片信息保存失败！");
        }
    }

    /**
     * 将文件分片数据保存再本地磁盘
     *
     * @param context
     */
    private void doStoreChunkFileInCD(ChunkUploadFileContext context) {
        SaveChunkFileContext saveChunkFileContext = fileConverter.chunkUploadFileContext2SaveFileContext(context);
        saveChunkFileInCD(saveChunkFileContext);
        context.setRealPath(saveChunkFileContext.getRealPath());
    }

    private void saveChunkFileInCD(SaveChunkFileContext saveChunkFileContext) {
        try {
            StoreFileChunkContext storeFileChunkContext =
                    fileConverter.saveFileContext2StoreFileChunkContext(saveChunkFileContext);
            storeFileChunkContext.setInputStream(saveChunkFileContext.getFile().getInputStream());
            storeEngine.storeChunk(storeFileChunkContext);
            saveChunkFileContext.setRealPath(storeFileChunkContext.getRealPath());
        } catch (IOException ioException) {
            throw new RPanBusinessException("文件分片保存失败");

        }
    }
}




