package com.example.ops.alerting;

/**
 * 告警类型
 */
public enum AlertType {
    THRESHOLD,   // 阈值告警
    TREND,       // 趋势告警
    COMPARISON,  // 对比告警
    CONSTANT      // 持续告警
}
