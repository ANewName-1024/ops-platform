# 知识库数据补充方案

## 1. 数据类型设计

### 1.1 知识库分类

| 知识库名称 | 描述 | 文档类型 |
|-----------|------|----------|
| 运维文档 | 系统运维相关文档 | 操作手册、故障处理 |
| 故障排除 | 常见问题与解决方案 | FAQ、故障案例 |
| 技术规范 | 开发规范、编码标准 | 开发指南 |
| 应急响应 | 应急预案、处置流程 | 流程文档 |

### 1.2 文档结构

```json
{
  "id": 1,
  "title": "文档标题",
  "content": "文档内容 (Markdown格式)",
  "type": "markdown",
  "tags": ["标签1", "标签2"],
  "author": "作者",
  "createTime": "创建时间",
  "updateTime": "更新时间"
}
```

## 2. 初始化数据方案

### 2.1 数据库初始化脚本

创建 SQL 初始化脚本 `docs/knowledge-init.sql`:

```sql
-- 知识库基础数据初始化

-- 知识库分类
INSERT INTO knowledge_bases (name, description, document_count, create_time) VALUES 
('运维文档', '系统运维相关文档，包括部署、配置、监控等', 0, NOW()),
('故障排除', '常见问题与解决方案，记录故障处理经验', 0, NOW()),
('技术规范', '开发规范、编码标准、技术选型', 0, NOW()),
('应急响应', '应急预案、处置流程、值班安排', 0, NOW());

-- 文档数据 (示例)
INSERT INTO documents (kb_id, title, content, type, author, create_time) VALUES 
(1, '服务器部署手册', '# 服务器部署手册\n\n## 环境要求\n- JDK 21+\n- PostgreSQL 12+\n- Nginx 1.20+', 'markdown', 'system', NOW()),
(1, 'Nginx配置指南', '# Nginx配置指南\n\n## 基础配置\n...', 'markdown', 'system', NOW()),
(2, '数据库连接超时', '# 数据库连接超时处理\n\n## 问题描述\n应用无法连接数据库...', 'markdown', 'system', NOW());
```

## 3. API 批量导入方案

### 3.1 批量导入接口

在 knowledge-service 添加批量导入 API:

```java
/**
 * 批量导入文档
 * POST /knowledge/import
 */
@PostMapping("/import")
public Map<String, Object> importDocuments(@RequestBody List<Map<String, Object>> documents) {
    int count = 0;
    for (Map<String, Object> doc : documents) {
        Long kbId = ((Number) doc.get("kbId")).longValue();
        // 创建文档
        count++;
    }
    
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("count", count);
    return result;
}
```

### 3.2 前端批量导入

在知识库页面添加导入功能:

```javascript
// 导入 JSON 文件
const handleImport = async (file) => {
  const reader = new FileReader()
  reader.onload = async (e) => {
    const data = JSON.parse(e.target.result)
    await importDocuments(data)
    ElMessage.success('导入成功')
  }
  reader.readAsText(file)
}
```

## 4. 示例数据 JSON

创建 `docs/knowledge-sample.json`:

```json
{
  "knowledgeBases": [
    {
      "name": "运维文档",
      "description": "系统运维相关文档",
      "documents": [
        {
          "title": "服务部署手册",
          "content": "# 服务部署手册\n\n## 前置要求\n- JDK 21+\n- PostgreSQL 12+\n\n## 部署步骤\n\n1. 编译项目\n```bash\nmvn clean package\n```\n\n2. 启动服务\n```bash\njava -jar target/service.jar\n```",
          "type": "markdown"
        },
        {
          "title": "Nginx配置指南",
          "content": "# Nginx配置指南\n\n## 反向代理配置\n\n```nginx\nlocation / {\n    proxy_pass http://localhost:8083;\n}\n```"
        },
        {
          "title": "数据库备份恢复",
          "content": "# 数据库备份恢复\n\n## 备份\n\n```bash\npg_dump -h localhost -U business business_db > backup.sql\n```\n\n## 恢复\n\n```bash\npsql -h localhost -U business business_db < backup.sql\n```"
        }
      ]
    },
    {
      "name": "故障排除",
      "description": "常见问题与解决方案",
      "documents": [
        {
          "title": "数据库连接超时",
          "content": "# 数据库连接超时\n\n## 问题描述\n应用启动时报错: connection timeout\n\n## 原因\n1. 数据库连接数已满\n2. 网络延迟过高\n3. 数据库服务器负载过高\n\n## 解决方案\n1. 重启 PostgreSQL\n2. 检查连接池配置\n3. 优化慢查询"
        },
        {
          "title": "服务启动失败",
          "content": "# 服务启动失败\n\n## 常见原因\n1. 端口被占用\n2. 配置文件错误\n3. 依赖服务未启动\n\n## 排查步骤\n1. 检查端口: `netstat -ano | findstr 8083`\n2. 查看日志: `tail -f logs/service.log`\n3. 检查配置: application.yml"
        }
      ]
    },
    {
      "name": "技术规范",
      "description": "开发规范与最佳实践",
      "documents": [
        {
          "title": "Java编码规范",
          "content": "# Java编码规范\n\n## 命名规范\n- 类名: UpperCamelCase\n- 方法名: lowerCamelCase\n- 常量: UPPER_SNAKE_CASE\n\n## 代码格式\n- 缩进: 4空格\n- 行宽: 120字符"
        },
        {
          "title": "Git提交规范",
          "content": "# Git提交规范\n\n## 提交类型\n- feat: 新功能\n- fix: 修复bug\n- docs: 文档更新\n- refactor: 重构"
        }
      ]
    },
    {
      "name": "应急响应",
      "description": "应急预案与处置流程",
      "documents": [
        {
          "title": "服务故障应急预案",
          "content": "# 服务故障应急预案\n\n## 响应级别\n- P1: 核心服务不可用\n- P2: 部分功能异常\n- P3: 性能下降\n\n## 处置流程\n1. 确认故障\n2. 启动应急预案\n3. 问题定位\n4. 恢复服务\n5. 事后分析"
        }
      ]
    }
  ]
}
```

## 5. 一键初始化脚本

创建 PowerShell 脚本 `scripts/init-knowledge.ps1`:

```powershell
# 知识库初始化脚本

param(
    [string]$KnowledgeBaseUrl = "http://localhost:8092/knowledge"
)

$sampleData = Get-Content "docs/knowledge-sample.json" | ConvertFrom-Json

Write-Host "Initializing Knowledge Base..." -ForegroundColor Cyan

foreach ($kb in $sampleData.knowledgeBases) {
    Write-Host "Creating Knowledge Base: $($kb.name)" -ForegroundColor Yellow
    
    # 创建知识库
    $kbResult = Invoke-RestMethod -Uri "$KnowledgeBaseUrl/bases" -Method Post -ContentType "application/json" -Body (@{
        name = $kb.name
        description = $kb.description
    } | ConvertTo-Json)
    
    $kbId = $kbResult.id
    
    Write-Host "  Created KB ID: $kbId" -ForegroundColor Green
    
    # 导入文档
    foreach ($doc in $kb.documents) {
        Write-Host "  Adding document: $($doc.title)" -ForegroundColor Cyan
        
        Invoke-RestMethod -Uri "$KnowledgeBaseUrl/bases/$kbId/documents" -Method Post -ContentType "application/json" -Body (@{
            title = $doc.title
            content = $doc.content
            type = $doc.type
        } | ConvertTo-Json) | Out-Null
        
        Write-Host "    OK" -ForegroundColor Green
    }
}

Write-Host "`nKnowledge Base initialized successfully!" -ForegroundColor Green
```

## 6. 执行计划

| 步骤 | 操作 | 状态 |
|------|------|------|
| 1 | 创建示例数据 JSON | ✅ |
| 2 | 创建初始化脚本 | ✅ |
| 3 | 准备知识库数据 | 待执行 |
| 4 | 执行初始化 | 待执行 |

是否需要我创建示例数据 JSON 文件并执行初始化？
