# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 构建

- 编译、测试、打包时，必须优先使用当前 JetBrains IDEA 工程配置，不要使用终端默认的 `java`、`javac`、`mvn`
- 项目 JDK 以 IDEA 配置为准：`.idea/misc.xml` 中配置的是 `temurin-17`，构建时必须使用 Java 17
- Maven 可执行文件路径 和 user settings 文件路径 都必须从 `.idea/workspace.xml` 读取，不得假定默认值：
    - Maven 路径：从 `MavenImportPreferences` → `customMavenHome` 读取；若未设则从 IDEA 安装目录下的 Maven 插件目录解析
    - user settings：从 `MavenImportPreferences` → `userSettingsFile` 读取
- **执行 `mvn` 命令时必须显式传入 `-s <userSettingsFile>` 参数**，否则会走到默认 `settings.xml`（可能指向不可达的内部仓库）
- 如果命令行环境中的 `JAVA_HOME`、`java`、`javac`、`mvn` 与上述配置不一致，执行构建前必须显式切换到 IDEA 对应配置
- 若出现 `record`、`switch ->` 等语法报错，优先检查是否误用了 JDK 8，而不是先改源码

```bash
# 执行 mvn 命令时必须带 -s 参数指定 settings 文件，同时设置 JAVA_HOME
# 示例：JAVA_HOME=<jdk17> <mvn> -s <userSettingsFile> compile -pl simple-crud

# 构建所有模块
mvn -s $SETTINGS_XML clean install

# 跳过测试
mvn -s $SETTINGS_XML install -DskipTests

# 构建指定模块的测试
mvn -s $SETTINGS_XML test -pl simple-crud

# 运行单个测试类
mvn -s $SETTINGS_XML test -pl simple-core -Dtest=JsonsTest

# 发布构建（生成 sources、javadoc、GPG 签名产物）
mvn -s $SETTINGS_XML clean install -Prelease
```

需要 Java 17。编译器全局启用 `-Xpkginfo:always` 标志。

## 模块架构

```
simple-framework (父 POM)
├── simple-dependencies   — BOM：集中管理依赖版本
├── simple-core           — 基础工具类和响应类型
├── simple-crud           — 基于注解的 MyBatis CRUD 框架
├── simple-token          — 认证、会话与权限管理
└── simple-dag            — 基于 DAG 的流式执行引擎
```

依赖关系：`simple-dag → simple-core`，`simple-crud → simple-core`（可选）。`simple-token` 独立无内部依赖。

所有模块的包根路径为 `dev.simpleframework.*`。

## simple-core

提供框架内共用的基础原语：
- **响应封装**：`CommonResponse`、`ListResponse`、`PageResponse` — 标准 REST API 响应信封，包含 code/message/data。
- **分页**：`PageRequest` / `PageData`。
- **Snowflake ID**：`Snowflake.DEFAULT` — 分布式唯一 ID 生成器，可通过系统属性 `simple.snowflake.*` 配置。
- **工具类**：`Jsons`（Jackson 封装）、`Strings`、`Threads`、`Clock`、`Classes`（反射）、`Functions`。
- **Spring 桥接**：`SimpleSpringUtils`。

## simple-crud

基于动态 MyBatis SQL 生成的低代码 CRUD 框架。核心概念：

**注解驱动一切：**
- `@Table` — 将类映射到数据库表。
- `@Id` — 标记主键字段。
- `@Column` — 控制每个字段的 insert/update/select 行为。
- `@Condition` / `@Conditions` — 在类上声明可复用的查询条件。
- `@DataOperateDate` / `@DataOperateUser` — 自动填充时间戳和操作用户 ID。
- `@ModelMethod` — 声明模型需要暴露的 CRUD 方法。

**模型层级：** `BaseModel<T>`（接口）→ `SimpleModel<T>`（包含 id、createdTime、updatedTime、createUser 的抽象类）。

**12 个内置操作：** `insert`、`insertBatch`、`deleteById`、`deleteByIds`、`deleteByConditions`、`updateById`、`updateByConditions`、`findById`、`findOneByConditions`、`listByIds`、`listByConditions`、`pageByConditions`、`countByConditions`。

**查询 DSL：** `QueryConditions` — 链式构建 WHERE 子句；`QueryFields` 用于 SELECT 字段；`QuerySorters` 用于 ORDER BY。

**扩展点：**
- `DatasourceProvider<T>` — 可插拔数据源解析
- `DataFillStrategy` — insert/update 前自定义字段填充。
- `ConditionDialect` — 按数据库类型适配 SQL 方言。

Spring Boot 自动配置通过 `SimpleCrudAutoConfiguration` 注册默认实现，模型通过 `@ModelScan` 扫描。

## simple-token

完整的认证框架，入口为 `SimpleTokens` 静态门面。

**核心概念：**
- `SessionInfo` — 包含 loginId 和过期时间的活跃 token。
- `SessionStore` — 可插拔存储：`DefaultSessionStore`（内存）或 `SpringRedisDefaultSessionStore`（Redis）。
- `SessionManager` — 会话的增删改查。
- 领域操作对象：`SessionLogin`、`SessionLogout`、`SessionKick`、`SessionRefresh` — 均通过 `.exec()` 执行。

**权限模型：**
- `PermissionManager` — 实现此接口以提供当前用户的角色/权限。
- 注解：`@TokenCheckPermission`、`@TokenCheckRole`（配合 AND/OR `@CheckMode`）。
- `PathPermission` — URL 路径 → 权限映射。

**HTTP 集成：** `SimpleTokenSpringServletFilter` — Servlet 过滤器。`ContextManager` 持有当前请求上下文（`Context` → `ContextRequest` / `ContextResponse` / `ContextCookie`）。

**用户/账号模型：** `UserManager` + `UserQuery` 接口用于用户查找；`UserAccountPasswordValidator` 用于自定义密码校验。

**异常体系：** `InvalidTokenException`、`LoginUserNotFoundException`、`LoginPasswordInvalidException`、`InvalidPermissionException`、`InvalidRoleException`。

Spring Boot 自动配置位于 `simple-token-spring-boot-autoconfigure`。

## simple-dag

面向流水线和任务型工作负载的 DAG 执行引擎。

**图结构：** `DAG<T>` — 添加节点/边，校验无环性，提供拓扑排序及起始/终止节点发现。

**执行引擎：**
- `PipelineEngine` — 数据驱动；Job 发出 `JobRecord`，通过 Disruptor 环形缓冲区自动流向下游。
- `TaskEngine` — 控制驱动；Job 之间显式传递数据。

**Job 层级：** `Job<T>` → `AbstractJob<T>`。流水线场景：`BaseSourcePipelineJob`（生产者）、`BaseTargetPipelineJob`（消费者），以及中间转换 Job。

**过滤与转换：** 20+ 个可组合的 `FilterAction` 实现（`IsNull`、`IsEquals`、`Contains`、`GreaterThan` 等），`ExpressionFilter`（Aviator 表达式），以及 `TransAction` 转换（常量、字典、环境变量、表达式、函数）。

**结果：** 每个 Job 产生 `JobResult`（状态、指标），整体执行产生 `EngineResult`。支持异步执行、超时策略和运行时快照。

## 协作约定

- 开始实现任何新功能前，必须先向用户确认，获得明确同意后再动手。
- **代码与文档同步更新**：测试通过后、`git add` 前，必须暂停并自问：这次改动影响了哪些文档？对照以下 checklist 逐个检查：
  - 改注解/接口/扩展点/行为 → `simple-crud/README.md` + `simple-crud/docs/implementation-models.md`
  - 改测试（新增/修改/删除）→ `simple-crud/docs/integration-test-coverage.md`
  - 改其他模块同样逻辑，对照模块名找对应文档
  检查完、文档改完之后，再 `git add`。不在 commit 前检查——commit 太快没有停顿；
  在 `git add` 前检查——那是代码写完、测试通过后的自然停顿点。

## 关键约定

- 所有可选依赖（`spring-boot`、`mybatis`、`jackson` 等）在 POM 中标记为 `<optional>true</optional>`，避免传递依赖泄漏。
- 全框架采用 `@ConditionalOnMissingBean` 模式，始终允许用户覆盖默认实现。
- 需要提前初始化的 Spring Bean 使用 `@Order(Ordered.HIGHEST_PRECEDENCE)`。
- 广泛使用 Lombok（`@Data`、`@Getter`、`@Setter`、`@Slf4j`）。
- 通过 `flatten-maven-plugin` 实现 CI 友好版本管理，父 POM 中的 `revision` 属性统一控制版本号。
