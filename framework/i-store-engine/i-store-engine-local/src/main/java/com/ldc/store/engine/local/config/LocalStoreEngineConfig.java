package com.ldc.store.engine.local.config;

import com.ldc.store.core.utils.FileUtils;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "store.engine.local")
@Configuration
@Data
public class LocalStoreEngineConfig {

    /* 文件存储根目录 */
    private String basePath= FileUtils.generateDefaultStoreFileRealPath();
    /* 文件分片根目录存储地址 */
    private String chunkBasePath= FileUtils.generateDefaultStoreFileChunkRealPath();
}
