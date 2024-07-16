package com.ldc.store.modules.file.convert;

import com.ldc.store.engine.core.context.MergeFileContext;
import com.ldc.store.engine.core.context.StoreFileChunkContext;
import com.ldc.store.modules.file.context.*;
import com.ldc.store.modules.file.domain.RPanUserFile;
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
    SaveChunkFileContext uploadFileContext2SaveFileContext(UploadFileContext context);

    @Mapping(target = "userId" ,expression = "java(com.ldc.store.common.utils.UserInfoHolder.get())")
    ChunkUploadFileContext chunkUploadFileVO2ChunkUploadFileContext(ChunkUploadFileVO chunkUploadFileVO);

    SaveChunkFileContext chunkUploadFileContext2SaveFileContext(ChunkUploadFileContext context);

    @Mapping(target = "realPath",ignore = true)
    StoreFileChunkContext saveFileContext2StoreFileChunkContext(SaveChunkFileContext saveChunkFileContext);

    @Mapping(target = "userId" ,expression = "java(com.ldc.store.common.utils.UserInfoHolder.get())")
    QueryFileChunkContext queryFileChunksVO2QueryFileChunksVO(QueryFileChunksVO queryFileChunksVO);


    @Mapping(target = "userId" ,expression = "java(com.ldc.store.common.utils.UserInfoHolder.get())")
    @Mapping(target = "parentId" ,expression = "java(com.ldc.store.core.utils.IdUtil.decrypt(chunkFileMergeVO.getParentId()))")
    ChunkFileMergeContext chunkFileMergeVO2ChunkFileMergeContext(ChunkFileMergeVO chunkFileMergeVO);

    SaveChunkFileMergeContext chunkFileMergeContext2SaveChunkFileMergeContext(ChunkFileMergeContext context);

    MergeFileContext chunkFileMergeContext2MergeFileContext(SaveChunkFileMergeContext context);

    @Mapping(target = "label",source = "record.filename")
    @Mapping(target = "id", source = "record.fileId")
    @Mapping(target = "children", expression = "java(com.google.common.collect.Lists.newArrayList())")
    FolderTreeNodeVO panUserFiles2FolderTreeNodeVO(RPanUserFile record);

    @Mapping(target = "transferFileId",source = "copyFileId")
    FileTransferContext fileCopyContext2FileTransferContext(FileCopyContext fileCopyContext);
}
