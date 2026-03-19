package com.example.ops.monitoring;

import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JVM 指标采集器
 * 采集 JVM 内存、GC、线程等指标
 */
@Component
public class JvmMetricsCollector implements MetricsCollector {
    
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    @Override
    public void collect(MetricsContext context) {
        collectMemoryMetrics(context);
        collectThreadMetrics(context);
        collectGcMetrics(context);
        collectRuntimeMetrics(context);
    }
    
    /**
     * 采集内存指标
     */
    private void collectMemoryMetrics(MetricsContext context) {
        // 堆内存
        Map<String, Long> heap = new HashMap<>();
        heap.put("used", memoryMXBean.getHeapMemoryUsage().getUsed());
        heap.put("max", memoryMXBean.getHeapMemoryUsage().getMax());
        heap.put("committed", memoryMXBean.getHeapMemoryUsage().getCommitted());
        
        context.addMetric("jvm.memory.heap.used", heap.get("used") / 1024.0 / 1024.0);
        context.addMetric("jvm.memory.heap.max", heap.get("max") / 1024.0 / 1024.0);
        context.addMetric("jvm.memory.heap.usage", 
            heap.get("max") > 0 ? (double) heap.get("used") / heap.get("max") * 100 : 0);
        
        // 非堆内存
        Map<String, Long> nonHeap = new HashMap<>();
        nonHeap.put("used", memoryMXBean.getNonHeapMemoryUsage().getUsed());
        nonHeap.put("max", memoryMXBean.getNonHeapMemoryUsage().getMax());
        
        context.addMetric("jvm.memory.nonheap.used", nonHeap.get("used") / 1024.0 / 1024.0);
    }
    
    /**
     * 采集线程指标
     */
    private void collectThreadMetrics(MetricsContext context) {
        context.addMetric("jvm.threads.count", threadMXBean.getThreadCount());
        context.addMetric("jvm.threads.daemon", threadMXBean.getDaemonThreadCount());
        context.addMetric("jvm.threads.peak", threadMXBean.getPeakThreadCount());
        context.addMetric("jvm.threads.totalStarted", threadMXBean.getTotalStartedThreadCount());
    }
    
    /**
     * 采集 GC 指标
     */
    private void collectGcMetrics(MetricsContext context) {
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            String gcName = gcBean.getName().replaceAll("\\s+", "_");
            
            context.addMetric("jvm.gc." + gcName + ".count", gcBean.getCollectionCount());
            context.addMetric("jvm.gc." + gcName + ".time", gcBean.getCollectionTime());
        }
    }
    
    /**
     * 采集运行时指标
     */
    private void collectRuntimeMetrics(MetricsContext context) {
        Runtime runtime = Runtime.getRuntime();
        
        context.addMetric("jvm.runtime.availableProcessors", runtime.availableProcessors());
        context.addMetric("jvm.runtime.freeMemory", runtime.freeMemory() / 1024.0 / 1024.0);
        context.addMetric("jvm.runtime.totalMemory", runtime.totalMemory() / 1024.0 / 1024.0);
        context.addMetric("jvm.runtime.maxMemory", runtime.maxMemory() / 1024.0 / 1024.0);
    }
}
