package com.ldc.store.modules.file.convert;

import com.ldc.store.engine.core.context.MergeFileContext;
import com.ldc.store.engine.core.context.StoreFileChunkContext;
import com.ldc.store.modules.file.context.ChunkFileMergeContext;
import com.ldc.store.modules.file.context.ChunkUploadFileContext;
import com.ldc.store.modules.file.context.CreateFileContext;
import com.ldc.store.modules.file.context.DeleteFileContext;
import com.ldc.store.modules.file.context.FileCopyContext;
import com.ldc.store.modules.file.context.FileTransferContext;
import com.ldc.store.modules.file.context.QueryFileChunkContext;
import com.ldc.store.modules.file.context.SaveChunkFileContext;
import com.ldc.store.modules.file.context.SaveChunkFileMergeContext;
import com.ldc.store.modules.file.context.SecUploadFileContext;
import com.ldc.store.modules.file.context.UpdateFilenameContext;
import com.ldc.store.modules.file.context.UploadFileContext;
import com.ldc.store.modules.file.domain.RPanUserFile;
import com.ldc.store.modules.file.vo.ChunkFileMergeVO;
import com.ldc.store.modules.file.vo.ChunkUploadFileVO;
import com.ldc.store.modules.file.vo.DeleteFileVO;
import com.ldc.store.modules.file.vo.FolderTreeNodeVO;
import com.ldc.store.modules.file.vo.QueryFileChunksVO;
import com.ldc.store.modules.file.vo.SecUploadFileVO;
import com.ldc.store.modules.file.vo.UpdateFilenameVO;
import com.ldc.store.modules.file.vo.UploadFileVO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-16T22:06:40+0800",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 1.8.0_111 (Oracle Corporation)"
)
@Component
public class FileConverterImpl implements FileConverter {

    @Override
    public CreateFileContext create2CreateFileContext(String parentId, String folderName) {
        if ( parentId == null && folderName == null ) {
            return null;
        }

        CreateFileContext createFileContext = new CreateFileContext();

        createFileContext.setFolderName( folderName );
        createFileContext.setUserId( com.ldc.store.common.utils.UserInfoHolder.get() );
        createFileContext.setParentId( com.ldc.store.core.utils.IdUtil.decrypt(parentId) );

        return createFileContext;
    }

    @Override
    public UpdateFilenameContext updateFileNameVO2UpdateFilenameContext(UpdateFilenameVO updateFilenameVO) {
        if ( updateFilenameVO == null ) {
            return null;
        }

        UpdateFilenameContext updateFilenameContext = new UpdateFilenameContext();

        updateFilenameContext.setNewFilename( updateFilenameVO.getNewFilename() );

        updateFilenameContext.setUserId( com.ldc.store.common.utils.UserInfoHolder.get() );
        updateFilenameContext.setFileId( com.ldc.store.core.utils.IdUtil.decrypt(updateFilenameVO.getFileId()) );

        return updateFilenameContext;
    }

    @Override
    public DeleteFileContext deleteFilePO2DeleteFileContext(DeleteFileVO deleteFileVO) {
        if ( deleteFileVO == null ) {
            return null;
        }

        DeleteFileContext deleteFileContext = new DeleteFileContext();

        deleteFileContext.setUserId( com.ldc.store.common.utils.UserInfoHolder.get() );

        return deleteFileContext;
    }

    @Override
    public SecUploadFileContext secUploadFileVO2SecUploadFileContext(SecUploadFileVO secUploadFileVO) {
        if ( secUploadFileVO == null ) {
            return null;
        }

        SecUploadFileContext secUploadFileContext = new SecUploadFileContext();

        secUploadFileContext.setFilename( secUploadFileVO.getFilename() );
        secUploadFileContext.setIdentifier( secUploadFileVO.getIdentifier() );

        secUploadFileContext.setUserId( com.ldc.store.common.utils.UserInfoHolder.get() );
        secUploadFileContext.setParentId( com.ldc.store.core.utils.IdUtil.decrypt(secUploadFileVO.getParentId()) );

        return secUploadFileContext;
    }

    @Override
    public UploadFileContext uploadFileVO2UploadFileContext(UploadFileVO uploadFileVO) {
        if ( uploadFileVO == null ) {
            return null;
        }

        UploadFileContext uploadFileContext = new UploadFileContext();

        uploadFileContext.setFilename( uploadFileVO.getFilename() );
        uploadFileContext.setIdentifier( uploadFileVO.getIdentifier() );
        uploadFileContext.setTotalSize( uploadFileVO.getTotalSize() );
        uploadFileContext.setFile( uploadFileVO.getFile() );

        uploadFileContext.setUserId( com.ldc.store.common.utils.UserInfoHolder.get() );
        uploadFileContext.setParentId( com.ldc.store.core.utils.IdUtil.decrypt(uploadFileVO.getParentId()) );

        return uploadFileContext;
    }

    @Override
    public SaveChunkFileContext uploadFileContext2SaveFileContext(UploadFileContext context) {
        if ( context == null ) {
            return null;
        }

        SaveChunkFileContext saveChunkFileContext = new SaveChunkFileContext();

        saveChunkFileContext.setFilename( context.getFilename() );
        saveChunkFileContext.setIdentifier( context.getIdentifier() );
        saveChunkFileContext.setTotalSize( context.getTotalSize() );
        saveChunkFileContext.setFile( context.getFile() );
        saveChunkFileContext.setUserId( context.getUserId() );

        return saveChunkFileContext;
    }

    @Override
    public ChunkUploadFileContext chunkUploadFileVO2ChunkUploadFileContext(ChunkUploadFileVO chunkUploadFileVO) {
        if ( chunkUploadFileVO == null ) {
            return null;
        }

        ChunkUploadFileContext chunkUploadFileContext = new ChunkUploadFileContext();

        chunkUploadFileContext.setFilename( chunkUploadFileVO.getFilename() );
        chunkUploadFileContext.setIdentifier( chunkUploadFileVO.getIdentifier() );
        chunkUploadFileContext.setTotalChunks( chunkUploadFileVO.getTotalChunks() );
        chunkUploadFileContext.setChunkNumber( chunkUploadFileVO.getChunkNumber() );
        chunkUploadFileContext.setCurrentChunkSize( chunkUploadFileVO.getCurrentChunkSize() );
        chunkUploadFileContext.setTotalSize( chunkUploadFileVO.getTotalSize() );
        chunkUploadFileContext.setFile( chunkUploadFileVO.getFile() );

        chunkUploadFileContext.setUserId( com.ldc.store.common.utils.UserInfoHolder.get() );

        return chunkUploadFileContext;
    }

    @Override
    public SaveChunkFileContext chunkUploadFileContext2SaveFileContext(ChunkUploadFileContext context) {
        if ( context == null ) {
            return null;
        }

        SaveChunkFileContext saveChunkFileContext = new SaveChunkFileContext();

        saveChunkFileContext.setFilename( context.getFilename() );
        saveChunkFileContext.setIdentifier( context.getIdentifier() );
        saveChunkFileContext.setTotalSize( context.getTotalSize() );
        saveChunkFileContext.setFile( context.getFile() );
        saveChunkFileContext.setUserId( context.getUserId() );
        saveChunkFileContext.setTotalChunks( context.getTotalChunks() );
        saveChunkFileContext.setChunkNumber( context.getChunkNumber() );
        saveChunkFileContext.setCurrentChunkSize( context.getCurrentChunkSize() );
        saveChunkFileContext.setRealPath( context.getRealPath() );

        return saveChunkFileContext;
    }

    @Override
    public StoreFileChunkContext saveFileContext2StoreFileChunkContext(SaveChunkFileContext saveChunkFileContext) {
        if ( saveChunkFileContext == null ) {
            return null;
        }

        StoreFileChunkContext storeFileChunkContext = new StoreFileChunkContext();

        storeFileChunkContext.setFilename( saveChunkFileContext.getFilename() );
        storeFileChunkContext.setIdentifier( saveChunkFileContext.getIdentifier() );
        storeFileChunkContext.setTotalSize( saveChunkFileContext.getTotalSize() );
        storeFileChunkContext.setTotalChunks( saveChunkFileContext.getTotalChunks() );
        storeFileChunkContext.setChunkNumber( saveChunkFileContext.getChunkNumber() );
        storeFileChunkContext.setCurrentChunkSize( saveChunkFileContext.getCurrentChunkSize() );
        storeFileChunkContext.setUserId( saveChunkFileContext.getUserId() );

        return storeFileChunkContext;
    }

    @Override
    public QueryFileChunkContext queryFileChunksVO2QueryFileChunksVO(QueryFileChunksVO queryFileChunksVO) {
        if ( queryFileChunksVO == null ) {
            return null;
        }

        QueryFileChunkContext queryFileChunkContext = new QueryFileChunkContext();

        queryFileChunkContext.setIdentifier( queryFileChunksVO.getIdentifier() );

        queryFileChunkContext.setUserId( com.ldc.store.common.utils.UserInfoHolder.get() );

        return queryFileChunkContext;
    }

    @Override
    public ChunkFileMergeContext chunkFileMergeVO2ChunkFileMergeContext(ChunkFileMergeVO chunkFileMergeVO) {
        if ( chunkFileMergeVO == null ) {
            return null;
        }

        ChunkFileMergeContext chunkFileMergeContext = new ChunkFileMergeContext();

        chunkFileMergeContext.setFilename( chunkFileMergeVO.getFilename() );
        chunkFileMergeContext.setIdentifier( chunkFileMergeVO.getIdentifier() );
        chunkFileMergeContext.setTotalSize( chunkFileMergeVO.getTotalSize() );

        chunkFileMergeContext.setUserId( com.ldc.store.common.utils.UserInfoHolder.get() );
        chunkFileMergeContext.setParentId( com.ldc.store.core.utils.IdUtil.decrypt(chunkFileMergeVO.getParentId()) );

        return chunkFileMergeContext;
    }

    @Override
    public SaveChunkFileMergeContext chunkFileMergeContext2SaveChunkFileMergeContext(ChunkFileMergeContext context) {
        if ( context == null ) {
            return null;
        }

        SaveChunkFileMergeContext saveChunkFileMergeContext = new SaveChunkFileMergeContext();

        saveChunkFileMergeContext.setFilename( context.getFilename() );
        saveChunkFileMergeContext.setIdentifier( context.getIdentifier() );
        saveChunkFileMergeContext.setTotalSize( context.getTotalSize() );
        saveChunkFileMergeContext.setParentId( context.getParentId() );
        saveChunkFileMergeContext.setUserId( context.getUserId() );
        saveChunkFileMergeContext.setRecord( context.getRecord() );

        return saveChunkFileMergeContext;
    }

    @Override
    public MergeFileContext chunkFileMergeContext2MergeFileContext(SaveChunkFileMergeContext context) {
        if ( context == null ) {
            return null;
        }

        MergeFileContext mergeFileContext = new MergeFileContext();

        mergeFileContext.setFilename( context.getFilename() );
        mergeFileContext.setIdentifier( context.getIdentifier() );
        mergeFileContext.setUserId( context.getUserId() );
        List<String> list = context.getRealPathList();
        if ( list != null ) {
            mergeFileContext.setRealPathList( new ArrayList<String>( list ) );
        }

        return mergeFileContext;
    }

    @Override
    public FolderTreeNodeVO panUserFiles2FolderTreeNodeVO(RPanUserFile record) {
        if ( record == null ) {
            return null;
        }

        FolderTreeNodeVO folderTreeNodeVO = new FolderTreeNodeVO();

        folderTreeNodeVO.setLabel( record.getFilename() );
        folderTreeNodeVO.setId( record.getFileId() );
        folderTreeNodeVO.setParentId( record.getParentId() );

        folderTreeNodeVO.setChildren( com.google.common.collect.Lists.newArrayList() );

        return folderTreeNodeVO;
    }

    @Override
    public FileTransferContext fileCopyContext2FileTransferContext(FileCopyContext fileCopyContext) {
        if ( fileCopyContext == null ) {
            return null;
        }

        FileTransferContext fileTransferContext = new FileTransferContext();

        List<Long> list = fileCopyContext.getCopyFileId();
        if ( list != null ) {
            fileTransferContext.setTransferFileId( new ArrayList<Long>( list ) );
        }
        fileTransferContext.setUserId( fileCopyContext.getUserId() );
        fileTransferContext.setTargetParentId( fileCopyContext.getTargetParentId() );
        List<RPanUserFile> list1 = fileCopyContext.getPrepareFiles();
        if ( list1 != null ) {
            fileTransferContext.setPrepareFiles( new ArrayList<RPanUserFile>( list1 ) );
        }

        return fileTransferContext;
    }
}
