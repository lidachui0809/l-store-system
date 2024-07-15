package com.ldc.store.modules.file.convert;

import com.ldc.store.engine.core.context.StoreFileChunkContext;
import com.ldc.store.modules.file.context.*;
import com.ldc.store.modules.file.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileConverter {


    @Mapping(target = "userId" ,expression = "java(com.ldc.store.common.utils.UserInfoHolder.get())")
    @Mapping(target = "parentId" ,expression = "java(com.ldc.store.core.utils.IdUtil.decrypt(parentId))")
    CreateFileContext create2CreateFileContext(String parentId,String folderName);

    /* 这里的fileId被加密了 需要经过解密后 再赋值 */
    @Mapping(target = "userId" ,expression = "java(com.ldc.store.common.utils.UserInfoHolder.get())")
    @Mapping(target = "fileId" ,expression = "java(com.ldc.store.core.utils.IdUtil.decrypt(updateFilenameVO.getFileId()))")
    UpdateFilenameContext updateFileNameVO2UpdateFilenameContext(UpdateFilenameVO updateFilenameVO);

    @Mapping(target = "userId" ,expression = "java(com.ldc.store.common.utils.UserInfoHolder.get())")
    DeleteFileContext deleteFilePO2DeleteFileContext(DeleteFileVO deleteFileVO);

    @Mapping(target = "userId" ,expression = "java(com.ldc.store.common.utils.UserInfoHolder.get())")
    @Mapping(target = "parentId" ,expression = "java(com.ldc.store.core.utils.IdUtil.decrypt(secUploadFileVO.getParentId()))")
    SecUploadFileContext secUploadFileVO2SecUploadFileContext(SecUploadFileVO secUploadFileVO);

    @Mapping(target = "userId" ,expression = "java(com.ldc.store.common.utils.UserInfoHolder.get())")
    @Mapping(target = "parentId" ,expression = "java(com.ldc.store.core.utils.IdUtil.decrypt(uploadFileVO.getParentId()))")
    UploadFileContext uploadFileVO2UploadFileContext(UploadFileVO uploadFileVO);

    /* 这里的record是需要SaveFileContext 业务逻辑获得  */
    @Mapping(target = "record" ,ignore = true)
    SaveFileContext uploadFileContext2SaveFileContext(UploadFileContext context);

    @Mapping(target = "userId" ,expression = "java(com.ldc.store.common.utils.UserInfoHolder.get())")
    ChunkUploadFileContext chunkUploadFileVO2ChunkUploadFileContext(ChunkUploadFileVO chunkUploadFileVO);

    SaveFileContext chunkUploadFileContext2SaveFileContext(ChunkUploadFileContext context);

    @Mapping(target = "realPath",ignore = true)
    StoreFileChunkContext saveFileContext2StoreFileChunkContext(SaveFileContext saveFileContext);
}
