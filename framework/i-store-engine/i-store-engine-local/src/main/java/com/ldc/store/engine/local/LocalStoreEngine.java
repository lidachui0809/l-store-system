package com.ldc.store.engine.local;

import com.ldc.store.core.utils.FileUtils;
import com.ldc.store.engine.core.AbstractStoreEngine;
import com.ldc.store.engine.core.context.*;
import com.ldc.store.engine.local.config.LocalStoreEngineConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;


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

    @Override
    protected void doMergeFile(MergeFileContext context) throws IOException {
        String realPath= FileUtils.generateStoreFileRealPath(config.getBasePath(),context.getFilename());
        FileUtils.createFile(new File(realPath));
        File file = new File(realPath);
        for (String chunkRealPath : context.getRealPathList()) {
            //将分片文件拼接依次拼接
            FileUtils.appendWrite(file.toPath(),Paths.get(chunkRealPath));
        }
        context.setRealPath(realPath);
    }

    @Override
    protected void doReadFile(ReadFileContext context) throws IOException {
        File file = new File(context.getRealPath());
        FileUtils.writeFile2OutputStream(new FileInputStream(file), context.getOutputStream(), file.length());
//        FileChannel channel =new FileInputStream(file).getChannel();
//        WritableByteChannel writableByteChannel = Channels.newChannel(context.getOutputStream());
//        //使用零拷贝 将fileInput流转移到目标输出流中
//        channel.transferTo(0,file.length(),writableByteChannel);
//        channel.close();
//        writableByteChannel.close();
//        context.getOutputStream().flush();
//        context.getOutputStream().close();
//
    }
}
