package com.ldc.store.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldc.store.core.exception.RPanBusinessException;
import com.ldc.store.core.utils.FileUtils;
import com.ldc.store.core.utils.IdUtil;
import com.ldc.store.engine.core.StoreEngine;
import com.ldc.store.engine.core.context.DeleteRealFileContext;
import com.ldc.store.engine.core.context.StoreFileContext;
import com.ldc.store.modules.file.context.ChunkUploadFileContext;
import com.ldc.store.modules.file.context.DeleteFileContext;
import com.ldc.store.modules.file.context.QueryRealFileListContext;
import com.ldc.store.modules.file.context.SaveFileContext;
import com.ldc.store.modules.file.domain.RPanFile;
import com.ldc.store.modules.file.event.DeleteFileEvent;
import com.ldc.store.modules.file.service.IFileService;
import com.ldc.store.modules.file.mapper.RPanFileMapper;
import com.ldc.store.modules.file.vo.ChunkFileUploadResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
* @author 李Da锤
* @description 针对表【r_pan_file(物理文件信息表)】的数据库操作Service实现
* @createDate 2024-07-12 13:37:32
*/
@Service
public class IFileServiceImpl extends ServiceImpl<RPanFileMapper, RPanFile>
    implements IFileService, ApplicationContextAware {



    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    private StoreEngine storeEngine;

    @Override
    public List<RPanFile> queryListFilesById(QueryRealFileListContext context) {
        LambdaQueryWrapper<RPanFile> queryWrapper
                = new LambdaQueryWrapper<RPanFile>();
        queryWrapper.eq(Objects.nonNull(context.getUserId()), RPanFile::getCreateUser, context.getUserId());
        queryWrapper.eq(RPanFile::getIdentifier, context.getIdentifier());
        return  baseMapper.selectList(queryWrapper);
    }

    /**
     * 调用存储引擎 来保存文件到磁盘
     * @param context
     */
    @Override
    public void saveFileInCD(SaveFileContext context) {
        //保存文件到磁盘
        saveMultipartFile(context);
        //保存文件记录
        RPanFile rPanFile = saveRealFileRecord(context.getFilename(), context.getRealPath(),
                context.getTotalSize(), context.getIdentifier(), context.getUserId());
        context.setRecord(rPanFile);
    }


    private RPanFile saveRealFileRecord(String filename, String realPath, Long totalSize, String identifier, Long userId) {
        RPanFile rPanFile = assembleRPanFile(filename, realPath, totalSize, identifier, userId);
        // 如果文件信息保存失败 需要回滚文件保存操作 调用文件引擎删除已保存的文件
        if(!save(rPanFile)){
            try {
                DeleteRealFileContext deleteFileContext = new DeleteRealFileContext();
                storeEngine.delete(deleteFileContext);
            } catch (IOException e) {
                //如果系统删除失败 则需要人为干预 删除 这里将删除事件进行广播  并写入日志
                //TODO 后续将改成mq进行处理
                // applicationContext.publishEvent(new ErrorLogEvent(this,msg));
                throw new RPanBusinessException("文件删除失败！");
            }
        }
        return rPanFile;
    }

    private void saveMultipartFile(SaveFileContext context) {
        try{
            StoreFileContext storeFileContext = new StoreFileContext();
            storeFileContext.setTotalSize(context.getTotalSize());
            storeFileContext.setInputStream(context.getFile().getInputStream());
            storeFileContext.setFilename(context.getFilename());
            //存储到磁盘之后 会返回realPath
            storeEngine.store(storeFileContext);
            context.setRealPath(storeFileContext.getRealPath());
        }catch (IOException e){
            throw new RPanBusinessException("文件保存失败！");
        }

    }

    private RPanFile assembleRPanFile(String filename, String realPath, Long totalSize,
                                  String identifier, Long userId) {
        RPanFile rPanFile=new RPanFile();
        rPanFile.setFileId(IdUtil.get());
        rPanFile.setFilename(filename);
        rPanFile.setRealPath(realPath);
        rPanFile.setFileSize(String.valueOf(totalSize));
        rPanFile.setIdentifier(identifier);
        rPanFile.setCreateUser(userId);
        rPanFile.setCreateTime(new Date());
        rPanFile.setFileSuffix(FileUtils.getFileSuffix(filename));
        rPanFile.setFileSizeDesc(FileUtils.byteCountToDisplaySize(totalSize));
        return rPanFile;
    }
}




