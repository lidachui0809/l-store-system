package com.ldc.store.engine.local;

import com.ldc.store.core.utils.FileUtils;
import com.ldc.store.engine.core.AbstractStoreEngine;
import com.ldc.store.engine.core.context.DeleteRealFileContext;
import com.ldc.store.engine.core.context.StoreFileChunkContext;
import com.ldc.store.engine.core.context.StoreFileContext;
import com.ldc.store.engine.local.config.LocalStoreEngineConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;


@Component
public  class LocalStoreEngine extends AbstractStoreEngine {

    @Autowired
    private LocalStoreEngineConfig config;


    @Override
    protected void doStore(StoreFileContext context) throws IOException {

        String realPath= FileUtils.generateStoreFileRealPath(config.getBasePath(),context.getFilename());
        FileUtils.writeStream2File(context.getInputStream(), new File(realPath),context.getTotalSize());
        context.setRealPath(realPath);
    }

    @Override
    protected void doStoreChunk(StoreFileChunkContext context) throws IOException {
        String realChunkPath=FileUtils.generateStoreFileChunkRealPath(config.getChunkBasePath()
                ,context.getIdentifier(), context.getChunkNumber());
        FileUtils.writeStream2File(context.getInputStream(), new File(realChunkPath), context.getTotalSize());
        context.setRealPath(realChunkPath);
    }

    @Override
    protected void doDelete(DeleteRealFileContext context) throws IOException {
        FileUtils.deleteFiles(context.getRealFilePathList());
    }
}
