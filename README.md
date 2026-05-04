# RAG Demo

Spring Boot + LangChain4j RAG platform demo

## 包含内容

- 文章里的核心 `pom.xml`、`application.yml`、SQL 和主要 Java 类
- 问答链路：`Query Rewrite -> Retrieval -> Rerank -> Context Assemble -> LLM Generate`
- 样例知识库初始化：启动时自动导入一份售后规则文档
- PostgreSQL + PGVector / Redis 的本地运行配置
- Kafka 摄入类保留，但默认不自动启动监听器，避免本地演示时强依赖 Kafka

## 前置要求

- Java 17
- Maven 3.9+
- Docker
- DashScope API Key

## 启动基础设施

```bash
docker compose up -d
```

## 配置环境变量

```bash
export DASHSCOPE_API_KEY=your_api_key
export DB_PASSWORD=change_me
```

## 启动应用

```bash
mvn spring-boot:run
```

首次启动会自动执行：

- `schema.sql` 建表
- 将 `src/main/resources/sample-data/after-sale-policy.md` 切块
- 生成向量并写入 `knowledge_document` / `knowledge_chunk`

## 测试接口

```bash
curl -X POST 'http://localhost:8080/api/v1/rag/ask' \
  -H 'Content-Type: application/json' \
  -d '{
    "tenantId": "merchant-1001",
    "userId": "cs-001",
    "knowledgeBaseId": "after-sale-kb",
    "question": "已签收商品还能申请七天无理由退货吗？",
    "datasetVersion": "2026.03"
  }'
```

## 说明

- 为了让示例真正闭环运行，我补了一层 JDBC 写库逻辑，把文章里的“切块与向量写入服务”接到了自定义表结构上。
- Kafka 相关生产者/消费者类按文章保留，默认通过 `rag.ingest.kafka-enabled=false` 关闭监听器。
- 当前工作区没有安装 Java 和 Maven，所以我已经完成代码落地，但没法在本机直接编译验证。
