package java.com.ldc.store.engine.oss;


import com.ldc.store.engine.core.AbstractStoreEngine;
import com.ldc.store.engine.core.context.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public  class OSSStoreEngine extends AbstractStoreEngine {


    @Override
    protected void doStore(StoreFileContext context) throws IOException {

    }

    @Override
    protected void doStoreChunk(StoreFileChunkContext context) throws IOException {

    }

    @Override
    protected void doDelete(DeleteRealFileContext context) throws IOException {

    }

    @Override
    protected void doMergeFile(MergeFileContext context) throws IOException {

    }

    @Override
    protected void doReadFile(ReadFileContext context) throws IOException {

    }
}
