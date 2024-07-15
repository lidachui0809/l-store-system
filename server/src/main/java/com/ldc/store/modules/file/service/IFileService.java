package com.ldc.store.modules.file.service;

import com.ldc.store.modules.file.context.ChunkUploadFileContext;
import com.ldc.store.modules.file.context.QueryRealFileListContext;
import com.ldc.store.modules.file.context.SaveFileContext;
import com.ldc.store.modules.file.context.UploadFileContext;
import com.ldc.store.modules.file.domain.RPanFile;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldc.store.modules.file.vo.ChunkFileUploadResultVO;

import java.util.List;

/**
* @author 李Da锤
* @description 针对表【r_pan_file(物理文件信息表)】的数据库操作Service
* @createDate 2024-07-12 13:37:32
*/
public interface IFileService extends IService<RPanFile> {


    List<RPanFile> queryListFilesById(QueryRealFileListContext context);

    void saveFileInCD(SaveFileContext context);
}
