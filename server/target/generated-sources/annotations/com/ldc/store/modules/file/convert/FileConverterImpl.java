package com.ldc.store.modules.file.convert;

import com.ldc.store.engine.core.context.StoreFileChunkContext;
import com.ldc.store.modules.file.context.ChunkUploadFileContext;
import com.ldc.store.modules.file.context.CreateFileContext;
import com.ldc.store.modules.file.context.DeleteFileContext;
import com.ldc.store.modules.file.context.SaveFileContext;
import com.ldc.store.modules.file.context.SecUploadFileContext;
import com.ldc.store.modules.file.context.UpdateFilenameContext;
import com.ldc.store.modules.file.context.UploadFileContext;
import com.ldc.store.modules.file.vo.ChunkUploadFileVO;
import com.ldc.store.modules.file.vo.DeleteFileVO;
import com.ldc.store.modules.file.vo.SecUploadFileVO;
import com.ldc.store.modules.file.vo.UpdateFilenameVO;
import com.ldc.store.modules.file.vo.UploadFileVO;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-15T13:15:35+0800",
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
    public SaveFileContext uploadFileContext2SaveFileContext(UploadFileContext context) {
        if ( context == null ) {
            return null;
        }

        SaveFileContext saveFileContext = new SaveFileContext();

        saveFileContext.setFilename( context.getFilename() );
        saveFileContext.setIdentifier( context.getIdentifier() );
        saveFileContext.setTotalSize( context.getTotalSize() );
        saveFileContext.setFile( context.getFile() );
        saveFileContext.setUserId( context.getUserId() );

        return saveFileContext;
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
    public SaveFileContext chunkUploadFileContext2SaveFileContext(ChunkUploadFileContext context) {
        if ( context == null ) {
            return null;
        }

        SaveFileContext saveFileContext = new SaveFileContext();

        saveFileContext.setFilename( context.getFilename() );
        saveFileContext.setIdentifier( context.getIdentifier() );
        saveFileContext.setTotalSize( context.getTotalSize() );
        saveFileContext.setFile( context.getFile() );
        saveFileContext.setUserId( context.getUserId() );
        saveFileContext.setRealPath( context.getRealPath() );

        return saveFileContext;
    }

    @Override
    public StoreFileChunkContext saveFileContext2StoreFileChunkContext(SaveFileContext saveFileContext) {
        if ( saveFileContext == null ) {
            return null;
        }

        StoreFileChunkContext storeFileChunkContext = new StoreFileChunkContext();

        storeFileChunkContext.setFilename( saveFileContext.getFilename() );
        storeFileChunkContext.setIdentifier( saveFileContext.getIdentifier() );
        storeFileChunkContext.setTotalSize( saveFileContext.getTotalSize() );
        storeFileChunkContext.setUserId( saveFileContext.getUserId() );

        return storeFileChunkContext;
    }
}
