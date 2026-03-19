package com.example.ops.monitoring;

import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * 数据库指标采集器
 * 采集数据库连接池，性能等指标
 */
@Component
public class DatabaseMetricsCollector implements MetricsCollector {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void collect(MetricsContext context) {
        if (dataSource instanceof HikariDataSource) {
            collectHikariMetrics(context, (HikariDataSource) dataSource);
        }
        
        collectDatabaseInfo(context);
    }
    
    /**
     * 采集 HikariCP 连接池指标
     */
    private void collectHikariMetrics(MetricsContext context, HikariDataSource hikariDS) {
        try {
            HikariConfigMXBean configBean = hikariDS.getHikariConfigMXBean();
            
            if (configBean != null) {
                // 连接池配置指标
                context.addMetric("db.pool.maxSize", configBean.getMaximumPoolSize());
                context.addMetric("db.pool.minIdle", configBean.getMinimumIdle());
                context.addMetric("db.pool.connectionTimeout", configBean.getConnectionTimeout());
                context.addMetric("db.pool.idleTimeout", configBean.getIdleTimeout());
                context.addMetric("db.pool.maxLifetime", configBean.getMaxLifetime());
                context.addMetric("db.pool.validationTimeout", configBean.getValidationTimeout());
            }
            
            context.addMetric("db.pool.configured", 1.0);
            
        } catch (Exception e) {
            // 忽略采集错误
        }
    }
    
    /**
     * 采集数据库基本信息
     */
    private void collectDatabaseInfo(MetricsContext context) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            context.addTag("db.type", metaData.getDatabaseProductName());
            context.addTag("db.version", metaData.getDatabaseProductVersion());
            context.addMetric("db.autocommit", conn.getAutoCommit() ? 1.0 : 0.0);
            context.addMetric("db.readonly", conn.isReadOnly() ? 1.0 : 0.0);
            context.addMetric("db.isolation", (double) conn.getTransactionIsolation());
            
        } catch (SQLException e) {
            // 忽略采集错误
        }
    }
}
