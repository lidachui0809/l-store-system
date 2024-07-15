package com.ldc.store.common.config;

import com.ldc.store.core.constants.RPanConstants;
import io.swagger.models.auth.In;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "store.server")
public class IStoreSystemConfig {

    /**
     * 分片默认超时时间
     */
    private Integer chunkExpirationTime= RPanConstants.ONE_INT;

}
