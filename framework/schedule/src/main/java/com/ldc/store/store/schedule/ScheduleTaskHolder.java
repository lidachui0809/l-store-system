package com.ldc.store.store.schedule;

import lombok.Data;

import java.util.concurrent.ScheduledFuture;

@Data
//维护定时任务的执行结果 以及任务
public class ScheduleTaskHolder {


    private ScheduleTask scheduleTask;

    /*
    * 定时任务的执行结果
    * */
    private ScheduledFuture future;
}
