# simple-crud

基于 MyBatis 动态 SQL 生成的低代码 CRUD 框架。通过注解驱动，零 XML、零 Mapper 接口，即可为实体类自动生成完整的增删改查能力。

## 目录

- [快速开始](#快速开始)
- [模块依赖](#模块依赖)
- [核心注解](#核心注解)
- [模型层次](#模型层次)
- [内置 CRUD 方法](#内置-crud-方法)
- [查询 DSL](#查询-dsl)
- [动态模型](#动态模型)
- [第三方模型](#第三方模型)
- [扩展点](#扩展点)
- [Spring Boot 集成](#spring-boot-集成)
- [FAQ](#faq)
- [架构说明](#架构说明)

---

## 快速开始

**1. 定义实体类**

```java
@Table("user")
public class User extends SimpleModel<User> {
    private String name;
    private Integer age;
}
```

**2. 声明扫描包**

```java
@ModelScan("com.example.model")
@SpringBootApplication
public class App { ... }
```

**3. 直接调用 CRUD 方法**

```java
User user = new User();
user.setName("Alice");
user.setAge(25);

// 新增（id、createdTime、updatedTime 自动填充）
user.insert();

// 按主键查询
User found = new User().findById(user.getId());

// 按条件查询
QueryConditions cond = QueryConditions.and()
        .add("name", "Alice")
        .add("age", ConditionType.greater_equal, 18);
List<User> list = new User().listByConditions(QueryConfig.of(cond));

// 按主键更新
user.setAge(26);
user.updateById();

// 按主键删除
user.deleteById(user.getId());
```

---

## 模块依赖

```xml
<dependency>
    <groupId>dev.simpleframework</groupId>
    <artifactId>simple-crud</artifactId>
</dependency>
```

运行时可选依赖（按需引入）：

| 依赖 | 作用 |
|------|------|
| `mybatis` / `mybatis-spring` | 核心 ORM，必须 |
| `pagehelper` | 分页支持（`pageByConditions`、`findOneByConditions` 优先路径） |
| `spring-boot-autoconfigure` | Spring Boot 自动配置 |
| HikariCP / Druid / DBCP2 / Tomcat JDBC | 连接池 URL 提取（用于方言自动匹配） |

**版本兼容性：**

| 组件 | 最低版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.x |
| MyBatis | 3.5.x |
| MyBatis Spring | 3.x |
| PageHelper | 5.x（可选） |

---

## 核心注解

### `@Table`

声明实体类对应的数据库表。

```java
@Table(name = "t_user", schema = "public")
public class User { ... }
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `name` | 类名转下划线（如 `UserInfo` → `USER_INFO`） | 表名 |
| `schema` | 空 | schema 前缀，最终拼接为 `schema.table` |

---

### `@Id`

标记主键字段，支持四种生成策略。

```java
@Id(type = Id.Type.SNOWFLAKE)
private Long id;
```

| 策略 | 说明 |
|------|------|
| `SNOWFLAKE`（默认） | Snowflake 算法，插入前由框架生成 `Long` ID |
| `UUID32` | 32 位无连字符 UUID 字符串 |
| `UUID36` | 标准 36 位 UUID 字符串 |
| `AUTO_INCREMENT` | 数据库自增，插入后回填 |

未声明 `@Id` 时，框架自动将名为 `id` 的字段作为主键（策略为 `SNOWFLAKE`）。

---

### `@Column`

精细控制字段的 CRUD 行为。

```java
@Column(name = "user_name", insertable = true, updatable = false, selectable = true)
private String name;
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `name` | 字段名转下划线大写 | 数据库列名 |
| `insertable` | `true` | 是否参与 INSERT |
| `updatable` | `true` | 是否参与 UPDATE |
| `selectable` | `true` | 是否参与 SELECT |

---

### `@Condition` / `@Conditions`

在条件类（VO/DTO）字段上声明查询条件，配合 `QueryConditions.fromAnnotation()` 使用。

```java
public class UserQuery {
    @Condition                                      // 等于（默认）
    private Long id;

    @Condition(field = "name", type = ConditionType.like_all)
    private String keyword;

    @Condition(type = ConditionType.greater_equal, defaultValueIfNull = "18")
    private Integer age;

    @Condition(type = ConditionType.in)
    private List<Long> ids;
}

// 使用
UserQuery query = new UserQuery();
query.setKeyword("Alice");
QueryConditions cond = QueryConditions.fromAnnotation(query);
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `field` | Java 字段名 | 数据库字段名 |
| `type` | `equal` | 条件类型，见 [`ConditionType`](#conditiontype) |
| `defaultValueIfNull` | 空 | 字段值为 null 时的默认值（自动转换为字段类型） |

同一字段可叠加多个 `@Condition`（或用 `@Conditions` 容器），每个独立生效。

**`@Conditions` 容器示例：**

```java
// 单字段多条件 — 用 @Conditions 包裹
@Conditions({
    @Condition(field = "user_age", defaultValueIfNull = "1"),
    @Condition(field = "person_age")
})
private Integer age;
```

---

### `@DataOperateDate` / `@DataOperateUser`

自动填充操作时间和操作用户 ID。

```java
@DataOperateDate
@Column(updatable = false)
private Date createdTime;   // insert 时自动填充当前时间

@DataOperateDate
private Date updatedTime;   // insert 和 update 时均自动填充

@DataOperateUser
@Column(updatable = false)
private Long createUser;    // insert 时通过 DataFillStrategy 填充当前用户 ID
```

`@DataOperateUser` 需要自定义 `DataFillStrategy` 提供当前用户 ID，见[扩展点](#扩展点)。

---

### `@ModelMethod`

为模型声明需要注册的 CRUD 方法定义，通常已由 `BaseModel` 接口的 `default` 方法自动完成，高级场景下可自定义。

---

## 模型层次

```
BaseModel<T>
  └── BaseInsert<T>    insert / insertBatch
  └── BaseDelete<T>    deleteById / deleteByIds / deleteByConditions
  └── BaseUpdate<T>    updateById / updateByConditions
  └── BaseQuery<T>     findById / findOneByConditions / listByIds
                       listByConditions / pageByConditions
                       countByConditions / existByConditions
        ↑
  SimpleModel<T>   预置 id / createUser / createdTime / updatedTime
        ↑
  YourEntity       业务字段
```

**推荐**：直接继承 `SimpleModel<T>`，获得主键和时间戳的自动管理。

**自定义**：实现 `BaseModel<T>` 接口，完全自主定义字段，通过 `@Id` / `@Column` 等注解控制行为。

---

## 内置 CRUD 方法

| 方法 | 说明 |
|------|------|
| `insert()` | 新增单条，非 null 字段才参与 INSERT（动态 SQL） |
| `insertBatch(List)` | 批量新增，**所有 insertable 字段**均参与（固定列） |
| `deleteById(id)` | 按主键删除 |
| `deleteByIds(ids)` | 按主键集合批量删除 |
| `deleteByConditions(cond)` | 按条件删除 |
| `updateById()` | 按主键更新，非 null 字段才参与 UPDATE（动态 SQL） |
| `updateByConditions(model, cond)` | 按条件批量更新 |
| `findById(id)` | 按主键查询单条 |
| `findOneByConditions(config)` | 按条件查询第一条。**有 PageHelper 时**用分页 `LIMIT 1` 避免全表扫描；**无 PageHelper 时**全量查询后取首条（大数据量下建议引入 PageHelper） |
| `listByIds(ids)` | 按主键集合查询 |
| `listByConditions(config)` | 按条件查询列表 |
| `pageByConditions(num, size, config)` | 分页查询（需 PageHelper） |
| `countByConditions(cond)` | 按条件统计数量 |
| `existByConditions(cond)` | 按条件判断是否存在 |

> **注意**：`insert` 与 `insertBatch` 的 null 字段处理不同：
> - `insert`：跳过 null 字段（`<if test="field != null">`），利于设置数据库默认值；
> - `insertBatch`：所有 insertable 字段均写入（含 null），适合批量场景。

---

## 查询 DSL

### `QueryConditions`

链式构建 WHERE 子句，支持 AND/OR 组合及嵌套。

```java
// AND 组合（默认）
QueryConditions cond = QueryConditions.and()
        .add("name", "Alice")                              // name = 'Alice'
        .add("age", ConditionType.greater_equal, 18)       // AND age >= 18
        .add("status", ConditionType.in, 1, 2, 3);         // AND status IN (1,2,3)

// OR 嵌套
QueryConditions orCond = QueryConditions.or()
        .add("type", 1)
        .add("type", 2);
cond.add(orCond);   // AND (type = 1 OR type = 2)

// Lambda 字段名
cond.add(User::getName, "Alice");

// 从注解对象构建
cond.addFromAnnotation(userQuery);
```

### `ConditionType`

| 值 | SQL |
|----|-----|
| `equal` | `= ?` |
| `not_equal` | `<> ?` |
| `like_all` | `LIKE '%?%'` |
| `like_left` | `LIKE '%?'` |
| `like_right` | `LIKE '?%'` |
| `greater_than` | `> ?` |
| `greater_equal` | `>= ?` |
| `less_than` | `< ?` |
| `less_equal` | `<= ?` |
| `in` | `IN (...)` |
| `not_in` | `NOT IN (...)` |
| `is_null` | `IS NULL` |
| `not_null` | `IS NOT NULL` |
| `array_contains` | `@> ?`（PostgreSQL 数组） |
| `array_contained_by` | `<@ ?`（PostgreSQL 数组） |
| `array_overlap` | `&& ?`（PostgreSQL 数组） |
| `json_contains` | `@> ?`（PostgreSQL JSONB） |
| `json_contained_by` | `<@ ?`（PostgreSQL JSONB） |
| `json_exist_key` | `? key` |
| `json_exist_key_any` | `?| keys` |
| `json_exist_key_all` | `?& keys` |

> 数组/JSON 条件依赖 PostgreSQL 方言，需通过 `Dialects.registerConditionDialect("postgresql", ...)` 注册。

### `QueryFields`

指定 SELECT 字段（默认取所有 `selectable=true` 的字段）。

```java
QueryFields fields = QueryFields.include("name", "age");  // 只查 name、age
QueryFields fields = QueryFields.exclude("password");      // 排除 password
```

### `QuerySorters`

指定 ORDER BY。

```java
QuerySorters sorter = QuerySorters.of()
        .asc("createdTime")
        .desc("age");
```

### `QueryConfig`

将 Conditions / Fields / Sorters 合并为统一查询配置。

```java
QueryConfig config = QueryConfig.of(conditions, fields, sorter);
```

---

## 动态模型

无需 Java 类，运行时直接构建模型元信息，适合动态表结构场景。

```java
DynamicModelInfo info = new DynamicModelInfo("t_log", DatasourceType.Mybatis)
        .setId("id")
        .addField("content")
        .addField("createdTime", Date.class);

// 注册到全局缓存
DynamicModel.register(info);

// 通过 DynamicModel 执行操作
DynamicModel model = new DynamicModel(info);
model.put("content", "hello");
model.insert();

// 不再使用时注销
DynamicModel.unregister(info);
```

`DynamicModelInfo` 支持运行时调用 `addField` / `removeField` / `removeAllFields` 增删字段

---

## 第三方模型

当实体类因来自外部模块或无法修改等原因而不能继承 `SimpleModel` / 实现 `BaseModel` 时，可通过 `Models` 工具类对该实体类执行 CRUD 操作。

### 1. 注册

在 `@ModelScan` 上通过 `superClass` 指定基类，`operatorClass` 指定操作类：

```java
@ModelScan(
    superClass    = AbstractEntity.class,    // 扫描所有继承 AbstractEntity 的实体类
    operatorClass = Models.class             // 为其注册全套 CRUD SQL
)
@SpringBootApplication
public class App { ... }
```

`operatorClass` 默认为 `ModelOperator.class`（不注册任何 SQL）。框架读取 `operatorClass` 接口层级上的 `@ModelMethod` 注解来决定为实体类注册哪些 SQL。

### 2. 使用

```java
User user = new User();
user.setName("Alice");

// 绑定实体（insert / updateById / updateByConditions 需要字段值）
Models.wrap(user).insert();
Models.wrap(user).updateById();
Models.wrap(user).updateByConditions(QueryConditions.and().add("status", 0));

// 绑定类型（delete / query 只需类型信息）
Models.wrap(User.class).deleteById(id);
Models.wrap(User.class).findById(id);
Models.wrap(User.class).listByConditions(QueryConfig.of(cond));
Models.wrap(User.class).pageByConditions(1, 20, config);

// insertBatch 两种 wrap 均可
Models.wrap(User.class).insertBatch(users);
```

两种 `wrap` 均返回新实例，线程安全。

### 3. 自定义操作类

需要限制访问范围或添加业务方法时，实现 `ModelOperator<T>` 及所需的 `BaseXxx<T>` 接口，参考 `Models` 的实现方式直接调用 `XxxDefinition.exec()`：

```java
// @ModelScan(superClass = X.class, operatorClass = UserOperator.class)
// 只注册查询 SQL，insert/update/delete 操作会因无对应 SQL 而报错
@Component
public class UserOperator implements ModelOperator<User>, BaseQuery<User> {
    private static final Class<User> MODEL = User.class;

    @Override
    public <R extends User> R findById(Object id, QueryFields... queryFields) {
        return FindByIdDefinition.exec(MODEL, id, queryFields);
    }

    @Override
    public <R extends User> List<R> listByConditions(QueryConfig config) {
        return ListByConditionsDefinition.exec(MODEL, config);
    }

    public List<User> findActive() {
        return ListByConditionsDefinition.exec(MODEL,
            QueryConfig.of(QueryConditions.and().add("status", 1)));
    }
}
```

---

## 扩展点

### `DataFillStrategy`：自定义字段自动填充

常见用途：填充当前登录用户 ID。

```java
@Component
public class CurrentUserFillStrategy implements DataFillStrategy {

    @Override
    public Class<?> support() {
        return DataOperateUser.class;   // 作用于 @DataOperateUser 注解的字段
    }

    @Override
    public <R> R get(Object param) {
        return (R) SecurityContext.getCurrentUserId();
    }

    @Override
    public FillType type() {
        return FillType.NULL;   // 仅在字段为 null 时填充
    }
}
```

| `FillType` | 说明 |
|------------|------|
| `NULL` | 字段值为 null 时才填充 |
| `ALWAYS` | 直接替换原有值 |

---

### `FieldCustomizer`：模型字段策略覆盖配置

当实体类无法添加注解时，通过注册 `FieldCustomizer` Spring Bean 在启动时统一声明字段策略：

```java
@Bean
public FieldCustomizer<User> userFieldOptions() {
    return FieldCustomizer.of(User.class)
        .field(User::getId,          f -> f.id(Id.Type.SNOWFLAKE).updatable(false))
        .field(User::getCreatedTime, f -> f.autoFill(DataOperateDate.class).insertable(false).updatable(false))
        .field(User::getUpdatedTime, f -> f.autoFill(DataOperateDate.class))
        .field(User::getCreatorId,   f -> f.name("create_user").autoFill(DataOperateUser.class).updatable(false));
}
```

`FieldOptions` 可链式调用的配置项：

| 方法 | 等价注解 |
|------|---------|
| `name(String)` | `@Column(name=...)` |
| `id(Id.Type)` | `@Id(type=...)` |
| `insertable(boolean)` | `@Column(insertable=...)` |
| `updatable(boolean)` | `@Column(updatable=...)` |
| `selectable(boolean)` | `@Column(selectable=...)` |
| `autoFill(Class<? extends Annotation>)` | `@DataOperateDate` / `@DataOperateUser` |

### `DatasourceProvider`：多数据源支持

```java
@Component
public class SecondaryMybatisProvider implements DatasourceProvider<SqlSession> {

    @Override
    public DatasourceType support() {
        return DatasourceType.Mybatis;
    }

    @Override
    public SqlSession get(String datasourceName) {
        return "secondary".equals(datasourceName) ? secondaryFactory.openSession() : null;
    }

    @Override
    public int order() {
        return 1;   // 比默认实现（order=0）优先级低，仅作补充
    }
}
```

---

### `ConditionDialect`：自定义 SQL 方言

```java
// 注册 PostgreSQL 方言（框架内置，示例仅演示 API）
Dialects.registerConditionDialect("postgresql", new PostgreSQLConditionDialect());
```

方言根据 JDBC URL 中的数据库标识（如 `:postgresql:`）自动匹配。默认使用通用 ANSI SQL 方言。

**列名 SQL 关键字保护**：如果字段名与 SQL 关键字冲突（如 `order`、`group`），可通过以下开关开启列名引号包裹：

```java
// 在应用启动时设置
Dialects.setQuoteColumnNames(true);
```

开启后所有列名会用数据库特定的引号包裹（PG/H2 双引号 `"order"`，MySQL 反引号 `` `order` ``），避免关键字冲突。注意：开启后 `@Column(name)` 必须与 DB 实际列名大小写一致。

---

## Spring Boot 集成

### 自动配置

引入 `spring-boot-autoconfigure` 后，`SimpleCrudAutoConfiguration` 自动完成：

1. 注册 `DefaultSpringMybatisProvider`（从 Spring 容器获取 `SqlSessionFactory`）；
2. 注册 `DefaultDataIdFillStrategy`（Snowflake/UUID）和 `DefaultDataOperateDateFillStrategy`（时间戳）；
3. 收集所有自定义 `DatasourceProvider` / `DataFillStrategy` Bean 并注册；
4. 触发 `ModelRegistrar.register()` 完成所有模型的 MyBatis MappedStatement 注册。

配置类使用 `@Order(HIGHEST_PRECEDENCE)` 确保在其他 Bean 初始化之前完成模型注册。

### `@ModelScan`

```java
@ModelScan(
    value          = "com.example.model",
    superClass     = BaseModel.class,          // 扫描此父类的子类（默认）
    operatorClass  = ModelOperator.class,      // 操作类，默认不注册 SQL；
                                               // 设为 Models.class 注册全套 CRUD
    datasourceType = DatasourceType.Mybatis,
    datasourceName = ""                        // 对应 DatasourceProvider.get(name) 的参数
)
```

多数据源场景可叠加多个 `@ModelScan`（通过 `@ModelScans` 容器）：

```java
@ModelScans({
    @ModelScan(value = "com.example.primary"),
    @ModelScan(value = "com.example.secondary", datasourceName = "secondary")
})
```

---

## FAQ

**Q: 调用 CRUD 方法时报 "Class is not registered"，怎么办？**

A: 检查以下三项：
1. 实体类是否在 `@ModelScan` 扫描路径下
2. 实体类是否实现了 `BaseModel` 接口（或继承 `SimpleModel`）
3. 对于 POJO 模式，`@ModelScan` 的 `operatorClass` 是否设为 `Models.class`

**Q: 分页查询 `pageByConditions` 不生效或返回空？**

A: `pageByConditions` 依赖 PageHelper。确保已引入 `pagehelper` 依赖，且 PageHelper 拦截器已正确配置。在 `@Transactional` 方法内使用时，PageHelper 可能因 ThreadLocal 机制返回空 items——这是已知限制。

**Q: 如何让 `@DataOperateUser` 自动填充当前用户 ID？**

A: 框架不提供默认用户填充策略。需自定义 `DataFillStrategy` 实现，注册为 Spring Bean，从当前请求上下文（如 `SecurityContext`）获取用户 ID。

**Q: `@Column` 的 `name` 属性不填时，列名是什么？**

A: 默认将 Java 字段名转为下划线大写（如 `userName` → `USER_NAME`）。省略 `@Column` 注解效果相同。

**Q: 如何切换数据库方言？**

A: 框架根据 JDBC URL 自动匹配内置方言（`postgresql` / `mysql` / `h2`）。其他数据库需手动注册：
```java
Dialects.registerConditionDialect("oracle", new MyOracleConditionDialect());
```

---

## 架构说明

```
注解层            @Table @Id @Column @Condition @ModelMethod ...
    ↓
模型信息层        ClassModelInfo / DynamicModelInfo
                  └── 字段列表缓存（insert/update/select 三份，按需失效）
    ↓
注册层            ModelRegistrar
                  └── 并行构建 ClassModelInfo（纯计算）
                  └── 串行写入 MyBatis Configuration（非线程安全）
    ↓
方法层            ModelMethodDefinition（register + exec）
                  └── InsertDefinition / UpdateByIdDefinition / ...
    ↓
MyBatis 实现层    MybatisScripts（动态 SQL 生成）
                  MybatisHelper（MappedStatement 注册 / SqlSession 管理 / 分页）
    ↓
方言层            Dialects（JDBC URL → ConditionDialect 路由）
                  PgConditionDialect（PostgreSQL，默认）/ MySqlConditionDialect（MySQL 5.7+）/ H2ConditionDialect（H2）
    ↓
缓存层            ModelCache（ModelInfo / DatasourceProvider / DataFillStrategy）
```

### 关键设计约定

- **动态 SQL**：所有 SQL 在运行时按参数动态生成，而非在注册阶段生成静态字符串，从而支持 null 跳过等特性。
- **可选依赖**：MyBatis、PageHelper、Spring Boot 等均为 `<optional>true</optional>`，框架不强制传递依赖。
- **覆盖优先**：所有 Provider / Strategy 均支持 `order()` 排序，`@ConditionalOnMissingBean` 确保用户自定义实现优先。
- **字段缓存**：`getAllFields()` 及三个派生列表在注册后首次访问时懒加载并缓存，字段变更时自动失效（DynamicModel 场景）。
