package com.ldc.store.store.schedule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
/* 注意一个细节问题 @ComponentScan注解 只是作用于扫描 需要一些启动注解配合使用 来实现 比如@Configuration @SpringBootApplication */
@ComponentScan(basePackageClasses = ScheduleConfig.class)
public class ScheduleConfig {

    @Bean
    public ThreadPoolTaskScheduler scheduledThreadPoolExecutor(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10);
        //设置守护进程
        threadPoolTaskScheduler.setDaemon(true);
        return threadPoolTaskScheduler;
    }
}
