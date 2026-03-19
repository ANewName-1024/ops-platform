package com.example.ops.monitoring;

/**
 * 指标采集器接口
 * 所有指标采集器需实现此接口
 */
public interface MetricsCollector {
    
    /**
     * 采集指标
     * @param context 指标上下文
     */
    void collect(MetricsContext context);
    
    /**
     * 采集器名称
     * @return 名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 初始化
     */
    default void init() {
        // 可选实现
    }
    
    /**
     * 销毁
     */
    default void destroy() {
        // 可选实现
    }
}
