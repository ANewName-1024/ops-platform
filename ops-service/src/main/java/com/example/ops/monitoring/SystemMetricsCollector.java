package com.example.ops.monitoring;

import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 系统指标采集器
 * 采集系统 CPU、内存、负载等指标
 */
@Component
public class SystemMetricsCollector implements MetricsCollector {
    
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    
    @Override
    public void collect(MetricsContext context) {
        collectSystemMetrics(context);
        collectCpuMetrics(context);
    }
    
    /**
     * 采集系统指标
     */
    private void collectSystemMetrics(MetricsContext context) {
        context.addMetric("system.cpu.count", osBean.getAvailableProcessors());
        
        try {
            context.addTag("host.name", InetAddress.getLocalHost().getHostName());
            context.addTag("host.address", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            // ignore
        }
    }
    
    /**
     * 采集 CPU 指标
     */
    private void collectCpuMetrics(MetricsContext context) {
        // 系统负载
        double loadAverage = osBean.getSystemLoadAverage();
        if (loadAverage > 0) {
            context.addMetric("system.load.average.1m", loadAverage);
        }
    }
}
