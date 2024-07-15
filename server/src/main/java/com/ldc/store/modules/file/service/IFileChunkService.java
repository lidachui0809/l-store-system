package com.ldc.store.modules.file.service;

import com.ldc.store.modules.file.context.ChunkUploadFileContext;
import com.ldc.store.modules.file.domain.RPanFileChunk;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldc.store.modules.file.vo.ChunkFileUploadResultVO;

/**
* @author 李Da锤
* @description 针对表【r_pan_file_chunk(文件分片信息表)】的数据库操作Service
* @createDate 2024-07-12 13:37:32
*/
public interface IFileChunkService extends IService<RPanFileChunk> {

    ChunkFileUploadResultVO chunkFileUpload(ChunkUploadFileContext context);
}
