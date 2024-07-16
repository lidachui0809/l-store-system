package com.ldc.store.engine.core;


import com.ldc.store.engine.core.context.*;

import java.io.IOException;

/**
 * 文件存储引擎模块的顶级接口
 * 该接口定义所有需要向外暴露给业务层面的相关文件操作的功能
 * 业务方只能调用该接口的方法，而不能直接使用具体的实现方案去做业务调用
 */
public interface StoreEngine {

    /**
     * 存储物理文件
     *
     * @param context
     * @throws IOException
     */
    void store(StoreFileContext context) throws IOException;

    /**
     * 删除物理文件
     *
     * @param context
     * @throws IOException
     */
    void delete(DeleteRealFileContext context) throws IOException;

    void storeChunk(StoreFileChunkContext context) throws IOException;


    /**
     * 合并文件分片
     *
     * @param context
     * @throws IOException
     */
    void mergeFile(MergeFileContext context) throws IOException;

    void readFile(ReadFileContext context) throws IOException;

}
