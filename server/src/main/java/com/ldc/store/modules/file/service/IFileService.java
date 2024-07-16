package com.ldc.store.modules.file.service;

import com.ldc.store.modules.file.context.*;
import com.ldc.store.modules.file.domain.RPanFile;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 李Da锤
* @description 针对表【r_pan_file(物理文件信息表)】的数据库操作Service
* @createDate 2024-07-12 13:37:32
*/
public interface IFileService extends IService<RPanFile> {


    List<RPanFile> queryListFilesById(QueryRealFileListContext context);

    void saveFileInCD(SaveChunkFileContext context);

    void mergeChunkFile(SaveChunkFileMergeContext saveChunkFileMergeContext);
}
