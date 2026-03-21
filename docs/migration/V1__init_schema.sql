-- =====================================================
-- OPS 平台数据库设计 - 按重构方案
-- 数据库: business_db (PostgreSQL 8.137.116.121:8432)
-- =====================================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    nickname VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 2. 知识库表
CREATE TABLE IF NOT EXISTS knowledge_bases (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT REFERENCES sys_users(id),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 3. 文档表
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    kb_id BIGINT REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(20) DEFAULT 'TEXT',
    status VARCHAR(20) DEFAULT 'PROCESSING',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 4. 对话会话表
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES sys_users(id),
    channel VARCHAR(20) DEFAULT 'WEB',
    title VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 5. 对话消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 6. 工作流定义表
CREATE TABLE IF NOT EXISTS workflow_definitions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    definition JSONB,
    version INTEGER DEFAULT 1,
    status VARCHAR(20) DEFAULT 'DRAFT',
    owner_id BIGINT REFERENCES sys_users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 7. 工作流实例表
CREATE TABLE IF NOT EXISTS workflow_instances (
    id BIGSERIAL PRIMARY KEY,
    definition_id BIGINT REFERENCES workflow_definitions(id),
    current_node VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING',
    input_data JSONB,
    output_data JSONB,
    started_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
);

-- 8. 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES sys_users(id),
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(20) DEFAULT 'INFO',
    channel VARCHAR(20) DEFAULT 'SYSTEM',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 9. 任务调度表
CREATE TABLE IF NOT EXISTS scheduled_tasks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    cron_expression VARCHAR(50),
    script_content TEXT,
    target_host VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    next_run_time TIMESTAMP
);

-- 10. 任务执行记录表
CREATE TABLE IF NOT EXISTS task_executions (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT REFERENCES scheduled_tasks(id),
    status VARCHAR(20) DEFAULT 'RUNNING',
    start_time TIMESTAMP DEFAULT NOW(),
    end_time TIMESTAMP,
    result TEXT,
    logs TEXT
);

-- 11. 证书管理表
CREATE TABLE IF NOT EXISTS certificates (
    id BIGSERIAL PRIMARY KEY,
    domain VARCHAR(200) NOT NULL,
    issuer VARCHAR(100),
    serial_number VARCHAR(100),
    issue_date DATE,
    expire_date DATE,
    certificate_content TEXT,
    private_key TEXT,
    status VARCHAR(20) DEFAULT 'VALID',
    created_at TIMESTAMP DEFAULT NOW()
);

-- 12. 系统配置表
CREATE TABLE IF NOT EXISTS system_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    config_type VARCHAR(20) DEFAULT 'STRING',
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 13. 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES sys_users(id),
    operation VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id BIGINT,
    details JSONB,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_kb_owner ON knowledge_bases(owner_id);
CREATE INDEX IF NOT EXISTS idx_docs_kb ON documents(kb_id);
CREATE INDEX IF NOT EXISTS idx_chat_session_user ON chat_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_task_executions_task ON task_executions(task_id);
CREATE INDEX IF NOT EXISTS idx_operation_logs_user ON operation_logs(user_id);

-- 插入默认管理员用户 (密码: Admin@123456)
INSERT INTO sys_users (username, password_hash, email, nickname, role, status)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye/U.N4.5F.HQW5R.HGmh5fCWzpLJDW9i', 'admin@example.com', '管理员', 'ADMIN', 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

-- 插入默认系统配置
INSERT INTO system_configs (config_key, config_value, config_type, description)
VALUES 
    ('system.name', 'OPS Platform', 'STRING', '系统名称'),
    ('system.version', '2.2.0', 'STRING', '系统版本'),
    ('jwt.secret', 'OpsPlatformJWTSecretKey2024ChangeInProduction', 'STRING', 'JWT密钥'),
    ('jwt.expiration', '86400000', 'NUMBER', 'JWT过期时间(毫秒)'),
    ('upload.max.size', '10485760', 'NUMBER', '上传文件大小限制'),
    ('notification.email.enabled', 'false', 'BOOLEAN', '邮件通知是否启用'),
    ('notification.feishu.enabled', 'false', 'BOOLEAN', '飞书通知是否启用')
ON CONFLICT (config_key) DO NOTHING;
