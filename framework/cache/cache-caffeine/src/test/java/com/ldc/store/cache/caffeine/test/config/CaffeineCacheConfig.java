package com.ldc.store.cache.caffeine.test.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.ldc.store.cache.core.constants.CacheConstants;
import com.ldc.store.core.constants.RPanConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableCaching
@ComponentScan(value = RPanConstants.BASE_COMPONENT_SCAN_PATH + ".cache.caffeine.test")
public class CaffeineCacheConfig {

    @Autowired
    private CaffeineCacheProperties properties;

    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheConstants.R_PAN_CACHE_NAME);
        cacheManager.setAllowNullValues(properties.getAllowNullValue());
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .initialCapacity(properties.getInitCacheCapacity())
                .maximumSize(properties.getMaxCacheCapacity());
        cacheManager.setCaffeine(caffeineBuilder);
        return cacheManager;
    }

}
