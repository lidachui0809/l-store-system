package com.ldc.store;

import com.ldc.store.core.constants.RPanConstants;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = RPanConstants.BASE_COMPONENT_SCAN_PATH)
@MapperScan(basePackages = RPanConstants.BASE_COMPONENT_SCAN_PATH+".**.mapper")
public class StoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreApplication.class);
    }
}
