# java_exp

独立的 Spring Boot 3 商品接口综合学习项目，使用 Java 17。

## 两种运行模式

| 模式 | 数据库 | 缓存 | 搜索 | 用途 |
|---|---|---|---|---|
| 默认模式 | H2 内存数据库 | JVM 内存缓存 | JVM 内存索引 | 不安装中间件也能学习和测试 |
| `infra` Profile | MySQL 8 | Redis 7 | Elasticsearch 8 | 学习真实基础设施接入 |

默认模式的数据在进程退出后消失。`infra` 模式的数据由 Docker Volume 保存。

## 默认启动

```bash
cd /home/wangyanan.1314/code/java_exp
mvn spring-boot:run
```

服务地址：`http://127.0.0.1:8082`

H2 控制台：`http://127.0.0.1:8082/h2-console`

H2 JDBC URL：

```text
jdbc:h2:mem:java_exp
```

用户名为 `sa`，密码为空。

## MySQL、Redis、Elasticsearch 模式

本机安装 Docker 后执行：

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=infra
```

默认连接参数：

```text
MySQL:        localhost:3306/java_exp  java_exp/java_exp
Redis:       localhost:6379
Elasticsearch: http://localhost:9200
```

生产环境应通过 `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`、`REDIS_HOST`、
`REDIS_PORT`、`ELASTICSEARCH_URIS` 环境变量覆盖默认值。

## 请求链路

```text
HTTP Request
  -> RequestIdFilter
  -> ProductController
  -> Jakarta Validation
  -> ProductService + AOP timing
  -> @Transactional
  -> ProductRepository
  -> H2 / MySQL
  -> Spring Cache (memory / Redis)
  -> AFTER_COMMIT event
  -> memory index / Elasticsearch
```

数据库是商品真相源。Redis 只缓存单个商品查询，Elasticsearch 只负责关键词搜索，
两者都不能替代 MySQL。

## 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/products` | 创建商品 |
| GET | `/api/products/{id}` | 查询单个商品，使用缓存 |
| GET | `/api/products` | 查询列表，可传 `category` |
| GET | `/api/products/page?page=0&size=10` | 分页、排序查询 |
| GET | `/api/products/search?keyword=hotpot` | 内存或 ES 关键词搜索 |
| GET | `/api/products/price-above?minPrice=100` | 原生 SQL 查询 |
| PUT | `/api/products/{id}` | 更新商品并刷新缓存、索引 |
| DELETE | `/api/products/{id}` | 删除商品并清理缓存、索引 |
| GET | `/actuator/health` | 健康检查 |
| GET | `/actuator/metrics` | 运行指标 |
| GET | `/actuator/caches` | 缓存信息 |

## 创建与查询

```bash
curl -i -X POST http://127.0.0.1:8082/api/products \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Hotpot Set",
    "category": "Group Meal",
    "price": 128.00,
    "description": "A hotpot meal for two people"
  }'

curl 'http://127.0.0.1:8082/api/products/page?page=0&size=10&sort=price,desc'
curl 'http://127.0.0.1:8082/api/products/search?keyword=Hotpot'
```

每个响应都包含 `X-Request-Id`，也可以由调用方传入该 Header，便于关联日志。

## Spring Boot 知识点

| 知识点 | 代码入口 |
|---|---|
| 启动、自动配置、组件扫描 | `JavaExpApplication.java` |
| MVC、REST、HTTP 状态码 | `ProductController.java` |
| DTO 与参数校验 | `ProductRequest.java`、`ProductResponse.java` |
| JPA Entity 与生命周期 | `Product.java` |
| Repository 动态代理、派生查询、原生 SQL | `ProductRepository.java` |
| Service、依赖注入、事务 | `ProductService.java` |
| Spring Cache、Redis TTL | `CacheConfig.java` |
| Profile 与环境配置 | `application.properties`、`application-infra.properties` |
| 事务提交后事件 | `ProductChangedEvent.java`、`ProductIndexListener.java` |
| Elasticsearch 与接口隔离 | `ProductSearchGateway.java` 及其两个实现 |
| Filter、MDC、请求追踪 | `RequestIdFilter.java` |
| AOP | `ServiceTimingAspect.java` |
| 全局异常处理 | `ApiExceptionHandler.java` |
| Actuator | `/actuator/*` 和配置文件 |
| MockMvc 集成测试 | `ProductControllerTest.java` |

建议按表格顺序阅读，每次只看一个文件。

Security、消息队列、定时任务、数据库迁移和分布式事务属于重要后端专题，
但不适合硬塞进一个商品 CRUD；应在掌握本项目后分别做独立示例。

## 测试

```bash
mvn test
```
