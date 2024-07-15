package com.ldc.store.modules.file.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldc.store.common.config.IStoreSystemConfig;
import com.ldc.store.core.exception.RPanBusinessException;
import com.ldc.store.engine.core.StoreEngine;
import com.ldc.store.engine.core.context.StoreFileChunkContext;
import com.ldc.store.modules.file.context.ChunkUploadFileContext;
import com.ldc.store.modules.file.context.SaveFileContext;
import com.ldc.store.modules.file.convert.FileConverter;
import com.ldc.store.modules.file.domain.RPanFileChunk;
import com.ldc.store.modules.file.enums.MergeFlagEnum;
import com.ldc.store.modules.file.service.IFileChunkService;
import com.ldc.store.modules.file.mapper.RPanFileChunkMapper;
import com.ldc.store.modules.file.service.IFileService;
import com.ldc.store.modules.file.vo.ChunkFileUploadResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

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
    /**
     * 保存文件分片 保存文件分片信息 通过存储引擎保存文件到磁盘
     * @param context
     * @return
     */
    @Override
    public ChunkFileUploadResultVO chunkFileUpload(ChunkUploadFileContext context) {
        doStoreChunkFileInCD(context);
        doSaveChunkFileRecord(context);
        doCheckMergeCondition(context);
        ChunkFileUploadResultVO vo
                = new ChunkFileUploadResultVO();
        vo.setMergeFlag(context.getMergeFlagEnum().getCode());
        return null;
    }

    /**
     * 检查文件分片是否上传完整
     * @param context
     */
    private void doCheckMergeCondition(ChunkUploadFileContext context) {
        LambdaQueryWrapper<RPanFileChunk> queryWrapper
                = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanFileChunk::getIdentifier,context.getIdentifier());
        int count = count(queryWrapper);
        if(count==context.getTotalChunks()){
            context.setMergeFlagEnum(MergeFlagEnum.READY);
        }
    }

    /**
     * 将文件分片信息 保存在数据库
     * @param context
     */
    private void doSaveChunkFileRecord(ChunkUploadFileContext context) {

        RPanFileChunk fileChunk=new RPanFileChunk();
        fileChunk.setChunkNumber(context.getChunkNumber());
        fileChunk.setIdentifier(context.getIdentifier());
        //设置过期时间为一天
        fileChunk.setExpirationTime(DateUtil.offsetDay(new Date(),
                config.getChunkExpirationTime()));
        fileChunk.setRealPath(context.getRealPath());
        fileChunk.setCreateUser(context.getUserId());
        fileChunk.setCreateTime(new Date());
        if (!save(fileChunk)) {
            throw  new RPanBusinessException("文件分片信息保存失败！");
        }
    }

    /**
     * 将文件分片数据保存再本地磁盘
     * @param context
     */
    private void doStoreChunkFileInCD(ChunkUploadFileContext context) {
        SaveFileContext saveFileContext=fileConverter.chunkUploadFileContext2SaveFileContext(context);
        saveChunkFileInCD(saveFileContext);
        context.setRealPath(saveFileContext.getRealPath());
    }

    private void saveChunkFileInCD(SaveFileContext saveFileContext) {
        try{
            StoreFileChunkContext storeFileChunkContext =
                    fileConverter.saveFileContext2StoreFileChunkContext(saveFileContext);
            storeFileChunkContext.setInputStream(saveFileContext.getFile().getInputStream());
            storeEngine.storeChunk(storeFileChunkContext);
            saveFileContext.setRealPath(storeFileChunkContext.getRealPath());
        }catch (IOException ioException){
            throw new RPanBusinessException("文件分片保存失败");

        }
    }
}




