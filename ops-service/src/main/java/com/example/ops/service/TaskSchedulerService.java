package com.example.ops.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * 任务调度服务
 * 支持定时、延迟、周期任务
 */
@Service
public class TaskSchedulerService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    /**
     * 延迟任务
     */
    public ScheduledFuture<?> scheduleDelayTask(Runnable task, long delayMs) {
        return scheduler.schedule(task, delayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 周期任务 (固定频率)
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * 周期任务 (固定延迟)
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    /**
     * Cron 任务 (简化实现)
     */
    public ScheduledFuture<?> scheduleCronTask(Runnable task, String cronExpression) {
        // 简化：每5分钟执行一次，实际生产应解析cron表达式
        return scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.MINUTES);
    }

    /**
     * 取消任务
     */
    public boolean cancelTask(ScheduledFuture<?> future) {
        return future.cancel(false);
    }

    /**
     * 关闭调度器
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
