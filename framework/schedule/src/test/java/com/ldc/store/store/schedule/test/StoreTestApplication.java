package com.ldc.store.store.schedule.test;

import com.ldc.store.store.schedule.ScheduleConfig;
import com.ldc.store.store.schedule.ScheduleManager;
import com.ldc.store.store.schedule.ScheduleTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
/* 这个注解是用于指定spring的上下文容器 通常用于测试类 */
/*  */
@ContextConfiguration(classes = ScheduleConfig.class)
@RunWith(SpringRunner.class)
@Slf4j
public class StoreTestApplication {

    @Autowired
    private ScheduleManager scheduleManager;

    @Test
    public void test() throws InterruptedException {
        log.info("info {}",scheduleManager);
        String key = scheduleManager.startTask(new ScheduleTask() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public void run() {
                System.out.println("任务执行....");
            }
        }, "0/5 * * * * *");
        //设置每5秒执行一次任务
        Thread.sleep(10*1000);
        scheduleManager.resetTaskTime(key,"0/1 * * * * *");
        Thread.sleep(5*1000);
        scheduleManager.stopTask(key);
        Thread.sleep(10*1000);
    }

}
