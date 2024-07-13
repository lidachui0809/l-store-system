package com.ldc.store.store.schedule;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class ScheduleManager {

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private Map<String,ScheduleTaskHolder> cache=new ConcurrentHashMap<>();

    /* cron表达式是一个用于时间表达的表达式 类似于正则表达式  * 表示给区间内的任意时间 - 表示某个区间
    *  Seconds Minutes Hours DayofMonth Month DayofWeek
    *  就比如: 0-40 * * * * *  表示在每时每刻的第0-40秒都会执行一次任务
    *         * * * 2-10 8 *  2-5 表示在每年的8月2-10号的周二到周五的任意时刻执行 【必须同时满足,在8月2-10号之间,且当天是周二到周五之间】
    *  */
    public String startTask(ScheduleTask task,String cron){
        ScheduledFuture<?> future
                = threadPoolTaskScheduler.schedule(task, new CronTrigger(cron));
        String key= RandomUtil.simpleUUID();
        ScheduleTaskHolder scheduleTaskHolder = new ScheduleTaskHolder();
        scheduleTaskHolder.setFuture(future);
        scheduleTaskHolder.setScheduleTask(task);
        cache.put(key,scheduleTaskHolder);
        return key;
    }

    public void stopTask(String key){
        if (StrUtil.isEmpty(key)) {
            return;
        }
        ScheduleTaskHolder scheduleTaskHolder = cache.get(key);
        if(ObjectUtil.isNull(scheduleTaskHolder)){
            return;
        }
        ScheduledFuture future = scheduleTaskHolder.getFuture();
        future.cancel(true);
    }

    public void resetTaskTime(String key,String cron){
        if (StrUtil.isEmpty(key)) {
            return;
        }
        ScheduleTaskHolder scheduleTaskHolder = cache.get(key);
        if(ObjectUtil.isNull(scheduleTaskHolder)){
            return;
        }
        scheduleTaskHolder.getFuture().cancel(true);
        startTask(scheduleTaskHolder.getScheduleTask(), cron);
    }

}
