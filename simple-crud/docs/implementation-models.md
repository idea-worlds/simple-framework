# simple-crud 模型实现原理：BaseModel、DynamicModel 与 ModelOperator

## 1. 架构概览

simple-crud 提供三种模型接入方式，覆盖从「静态编译期」到「动态运行时」再到「零侵入操作器」的全场景：

| 模型 | 实体类型 | 注册时机 | 侵入性 | 适用场景 |
|------|---------|---------|--------|---------|
| BaseModel | POJO 类 | 编译期（`@ModelScan` 扫描） | 需实现接口 | 标准 CRUD，实体类可控 |
| DynamicModel | `Map<String,Object>` | 运行时（代码注册） | 无 | 动态表结构、多租户、临时查询 |
| ModelOperator | 任意 POJO | 编译期（`@ModelScan` + `operatorClass`） | 无 | 外部实体类、不可修改的 POJO |

### 核心分层

三种模型在上层 API 形态不同，但在元信息层统一为 `ModelInfo`/`ModelField` 接口，在注册层和执行层共享同一套基础设施。

```
┌──────────────────────────────────────────────────┐
│                    使用层                         │
│  BaseModel / DynamicModel / Models.wrap()        │
├──────────────────────────────────────────────────┤
│           元信息层 (ModelInfo / ModelField)        │
│  ClassModelInfo / DynamicModelInfo                │
│  ClassModelField / DynamicModelField              │
├──────────────────────────────────────────────────┤
│         注册层 (ModelRegistrar / ModelCache)       │
│  @ModelScan → ModelScannerRegistrar               │
│  → ModelRegistrar → ModelCache                    │
├──────────────────────────────────────────────────┤
│        执行层 (Definition / MybatisImpl)           │
│  InsertDefinition → MybatisInsertMethod           │
│  → MybatisHelper → MyBatis Configuration          │
└──────────────────────────────────────────────────┘
```

### 速查：何时用哪种模型

| 场景 | 推荐模型 |
|------|---------|
| 标准业务实体，可以修改源码 | `implements BaseModel<T>`（也可 `extends SimpleModel<T>` 复用公共字段） |
| 只需要部分 CRUD 能力 | 自定义实体超类接口，选择性继承 `BaseXxx<T>`（如只读场景仅继承 `BaseQuery<T>`） |
| 实体来自外部 JAR，不可修改 | `@ModelScan(superClass=..., operatorClass=Models.class)` |
| 表结构在运行时才能确定 | `DynamicModel` |
| 需要自定义 CRUD 逻辑或访问控制 | 自定义 `ModelOperator` 实现 |

---

## 2. BaseModel — 注解驱动的静态模型

`BaseModel<T>` 是框架最核心的模型入口。一个 POJO 类实现此接口后，即可通过 `entity.insert()`、`entity.updateById()` 等方式直接调用 CRUD 操作。

### 2.1 接口继承链

BaseModel 的能力来自四层接口组合。每层又由更细粒度的「单体 method interface」聚合而成。

**组合层（四大能力接口）：**

```java
// BaseInsert、BaseDelete、BaseUpdate、BaseQuery 是组合接口
public interface BaseInsert<T> extends Insert<T>, InsertBatch<T> {}
public interface BaseDelete<T> extends DeleteById<T>, DeleteByIds<T>, DeleteByConditions<T> {}
public interface BaseUpdate<T> extends UpdateById<T>, UpdateByConditions<T> {}
public interface BaseQuery<T> extends FindById<T>, FindOneByConditions<T>,
    ListByIds<T>, ListByConditions<T>, PageByConditions<T>, CountByConditions<T>, ExistByConditions<T> {}

// BaseModel 聚合全部四种能力
public interface BaseModel<T> extends BaseInsert<T>, BaseDelete<T>, BaseUpdate<T>, BaseQuery<T> {}
```

**单体 method interface 层：**

每个单体接口定义一种操作，并通过 `@ModelMethod` 注解绑定对应的 SQL 注册实现：

```java
@ModelMethod(InsertDefinition.class)
public interface Insert<T> {
    boolean insert();
}

@ModelMethod(FindByIdDefinition.class)
public interface FindById<T> {
    <R extends T> R findById(Object id, QueryFields... queryFields);
}
```

这 12 个单体接口是框架的基础操作原子，组合接口只做聚合不做逻辑。

**`@ModelMethod` → `ModelMethodDefinition` 绑定机制：**

```java
// 接口上的注解指向对应的 Definition 类
@ModelMethod(InsertDefinition.class)
public interface Insert<T> { ... }

// Definition 类实现 ModelMethodDefinition，负责注册和执行
public class InsertDefinition implements ModelMethodDefinition {
    @Override
    public void register(ModelInfo<?> info) {
        MybatisInsertMethod.register(info);   // 向 MyBatis 注册 MappedStatement
    }

    public static <T> boolean exec(T model) {
        return MybatisInsertMethod.exec(model); // 运行时执行
    }
}
```

链路：`@ModelMethod(InsertDefinition.class)` → 扫描阶段调用 `InsertDefinition.register(info)` → 内部调用 `MybatisHelper.addMappedStatement()` 注册到 MyBatis Configuration → 运行时 `InsertDefinition.exec(model)` → 执行 SQL。

### 2.2 SimpleModel — 参考实现

`SimpleModel<T>` 是框架提供的参考 POJO 实现，展示了注解的标准用法：

```java
@Getter @Setter @ToString
public class SimpleModel<T> implements BaseModel<T> {

    @Id(type = Id.Type.SNOWFLAKE)
    @Column(updatable = false)
    private Long id;

    @DataOperateUser
    @Column(updatable = false)
    private Long createUser;

    @DataOperateDate
    @Column(updatable = false)
    private Date createdTime;

    @DataOperateDate
    private Date updatedTime;
}
```

四个字段的注解策略：

| 字段 | 注解 | insertable | updatable | 自动填充策略 |
|------|------|:---:|:---:|------|
| `id` | `@Id(SNOWFLAKE)` + `@Column(updatable=false)` | ✓ | ✗ | `DefaultDataIdFillStrategy` 生成雪花 ID |
| `createUser` | `@DataOperateUser` + `@Column(updatable=false)` | ✓ | ✗ | 需自定义 `DataFillStrategy` 实现 |
| `createdTime` | `@DataOperateDate` + `@Column(updatable=false)` | ✓ | ✗ | `DefaultDataOperateDateFillStrategy` 填充当前时间 |
| `updatedTime` | `@DataOperateDate` | ✓ | ✓ | `DefaultDataOperateDateFillStrategy`，每次更新自动刷新 |

`@Column(updatable = false)` 表示该字段不会出现在 UPDATE SET 子句中。`@DataOperateDate` 和 `@DataOperateUser` 是标记注解，框架通过 `DataFillStrategy` 接口查找对应的填充实现。

### 2.3 元信息构建：ClassModelInfo

当模型类被 `@ModelScan` 扫描到后，框架通过 `ClassModelInfo` 将其转换为运行时可用的元信息对象。构建过程如下：

**Step 1 — 解析模型名（`obtainModelName()`）：**

```java
// ClassModelInfo 构造器中
String obtainModelName(Class<?> clazz) {
    Table table = clazz.getAnnotation(Table.class);
    if (table != null && Strings.isNotBlank(table.name())) {
        // @Table 的 schema 作为前缀
        return (table.schema() + "." + table.name()).trim();
    }
    // 默认：驼峰转下划线大写
    return Strings.camelToUnderline(clazz.getSimpleName()).toUpperCase();
}
```

**Step 2 — 确定主键字段（`obtainModelIdFieldName()`）：**

优先查找标注了 `@Id` 的字段，若没有则默认使用名为 `id` 的字段。

**Step 3 — 反射扫描所有字段（`obtainFields()`）：**

遍历类的所有字段（排除 static 和 transient），为每个字段创建 `ClassModelField`。

```java
for (Field field : clazz.getDeclaredFields()) {
    int mod = field.getModifiers();
    if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) continue;
    ClassModelField<?> modelField = new ClassModelField<>(info, field);
    fields.add(modelField);
}
```

**Step 4 — 构建字段配置（`ClassModelField.buildConfig()`）：**

```java
// ClassModelField 构造时读取注解
void buildConfig() {
    Column column = field.getAnnotation(Column.class);
    if (column != null) {
        this.column = column.name();          // 列名
        this.insertable = column.insertable(); // 可插入
        this.updatable = column.updatable();   // 可更新
        this.selectable = column.selectable(); // 可查询
    }
    // 检查自动填充注解
    Id id = field.getAnnotation(Id.class);
    if (id != null) this.fillStrategy = ModelCache.getFillStrategy(Id.class);
    DataOperateDate dod = field.getAnnotation(DataOperateDate.class);
    if (dod != null) this.fillStrategy = ModelCache.getFillStrategy(DataOperateDate.class);
    // ... DataOperateUser 同理
}
```

### 2.4 注册链路

从 `@ModelScan` 到最终 MyBatis MappedStatement 注册的完整链路：

```
应用启动
  │
  ▼
@ModelScan(basePackages = "com.example")
  │  @Import(ModelScannerRegistrar.class)
  ▼
ModelScannerRegistrar.registerBeanDefinitions()
  │  ClassPathScanningCandidateComponentProvider
  │  过滤器：AssignableTypeFilter(BaseModel) 或 AnnotationTypeFilter(@Table)
  ▼
ModelRegistrar.newRegistrar(dsType, dsName, operatorClass).add(modelClass)
  │  暂存到 CopyOnWriteArrayList，等待批量注册
  ▼
SimpleCrudAutoConfiguration.afterPropertiesSet()
  │  → ModelRegistrar.register()
  │      ├── [并行] 每个 modelClass → new ClassModelInfo<>(modelClass)
  │      ├── [串行] 扫描 @ModelMethod → Definition.register(info)
  │      │         └── MybatisHelper.addMappedStatement()
  │      │               └── configuration.addMappedStatement(ms)
  │      └── ModelCache.registerInfo(info)
  ▼
注册完毕，模型可被使用
```

关键点：
- 元信息构建（`ClassModelInfo`）是**并行**的，因为各模型类之间无依赖
- MyBatis SQL 注册是**串行**的，因为 `Configuration.addMappedStatement()` 不是线程安全的
- 整个注册在 `InitializingBean.afterPropertiesSet()` 中完成，确保在所有用户 Bean 初始化之前

### 2.5 运行时执行

以 `insert()` 为例，从 Java 方法调用到 SQL 执行的全链路：

```
entity.insert()
  │  Insert 接口的 default 方法:
  │  default boolean insert() {
  │      return InsertDefinition.exec(this);  // this 即 entity 自身
  │  }
  ▼
InsertDefinition.exec(entity)
  │  MybatisInsertMethod.exec(model)
  ▼
MybatisInsertMethod.exec(model)
  │  ├── 遍历 insertable 字段，执行 DataFillStrategy 自动填充
  │  ├── 生成 SQL 脚本:
  │  │   <script>
  │  │     INSERT INTO table_name
  │  │     (<foreach collection="insertFields">column</foreach>)
  │  │     VALUES
  │  │     (<foreach collection="insertFields">#{field}</foreach>)
  │  │   </script>
  │  │
  │  └── MybatisHelper.exec(dsType, dsName, sqlSession → {
  │          sqlSession.insert(statementId, model);
  │      })
  ▼
MyBatis → JDBC → 数据库
```

关键点：`Insert` 接口的 default 方法中 `this` 就是 entity 自身（因为实体类 `implements BaseModel<T>` → `BaseInsert<T>` → `Insert<T>`），因此 `entity.insert()` 直接委托给 `InsertDefinition.exec(entity)`，中间不经过 `Models`。

`Models.wrap(entity).insert()` 走的是另一个路径：`Models.insert()` 调用 `InsertDefinition.exec(requireEntity())`，最终殊途同归，都到 `InsertDefinition.exec()`。

自动填充在 MyBatis 参数绑定之前执行：遍历所有可插入字段，若字段的 `fillStrategy != null` 且满足填充条件（`FillType.ALWAYS` 或 `FillType.NULL && value == null`），则调用 `strategy.get(param)` 填充值。

---

## 3. DynamicModel — 运行时动态模型

### 3.1 设计动机

`ClassModelInfo` 依赖编译期存在的 POJO 类和注解。当表结构在运行时才能确定（如用户自定义字段、多租户动态列、ETL 临时表），就需要一种「无需编译期实体类」的模型机制。`DynamicModel` 以 `Map<String, Object>` 作为实体载体，通过代码在运行时动态构建字段和注册 SQL。

### 3.2 DynamicModel 类结构

```java
public class DynamicModel implements BaseModel<Map<String, Object>> {

    private static final Map<String, DynamicModelInfo> INFOS = new ConcurrentHashMap<>();

    private String modelName;
    private DynamicModelInfo info;

    // 静态工厂方法
    public static DynamicModel of(String modelName) { ... }
    public static DynamicModel of(DynamicModelInfo info) { ... }

    // 注册 / 注销
    public static void register(DynamicModelInfo info) { ... }
    public static void removeRegistered(String modelName) { ... }

    // 实例方法
    public DynamicModel register() { ... }
    public DynamicModelInfo info() { ... }
}
```

核心设计：
- 实现 `BaseModel<Map<String, Object>>`，实体类型是 `Map`，而非 POJO
- 全局静态 `INFOS`（`ConcurrentHashMap`）管理所有已注册的动态模型信息
- `info()` 方法懒加载——首次调用时从 `INFOS` 按 `modelName` 查找

### 3.3 DynamicModelInfo — 运行时字段构建

```java
DynamicModelInfo info = new DynamicModelInfo("user_custom");
info.addField("name", "name_col");              // 字段名 + 列名，默认 String 类型
info.addField("age", "age_col", Integer.class); // 指定字段类型
info.addField("email", "email_col");

// 也可链式定义
DynamicModel.register(info);
```

与 `ClassModelInfo` 的关键差异：

| 维度 | ClassModelInfo | DynamicModelInfo |
|------|---------------|-----------------|
| 实体类型 | POJO 类 | `Map<String,Object>` |
| 字段定义方式 | 编译期（Java 字段 + 注解） | 运行时（`addField()` / `removeField()`） |
| 值存取方式 | 反射（`Field.get/set`） | `Map.get/put` |
| 类型安全 | 编译期检查 | 运行时校验 |
| `methodNamespace()` | 类的全限定名（稳定） | `modelName + "_" + hashCode()`（每次 new 不同） |
| `dynamic()` 标记 | `false` | `true` |
| 字段可变性 | 不可变（反射扫描后固定） | 可随时增删 |

`methodNamespace()` 使用 `hashCode()` 的后果：重新 `new DynamicModelInfo("same_name")` 会生成不同的 statementId，旧 SQL 注册仍然残留在 MyBatis Configuration 中。因此生产环境应复用同一个 `DynamicModelInfo` 实例。

### 3.4 DynamicModelField — Map 值存取

```java
public class DynamicModelField extends AbstractModelField<Map<String, Object>> {

    @Override
    public Object getValue(Map<String, Object> model) {
        return model.get(this.fieldName);
    }

    @Override
    public void setValue(Map<String, Object> model, Object value) {
        if (value != null && this.fieldType != null
                && !this.fieldType.isInstance(value)) {
            throw new ModelExecuteException(
                "Field type mismatch: expected " + this.fieldType.getName()
                + ", got " + value.getClass().getName());
        }
        model.put(this.fieldName, value);
    }
}
```

与 `ClassModelField` 的反射存取相比，`DynamicModelField` 有两个关键差异：
- **数据载体**：`DynamicModel` 继承 `HashMap<String, Object>`，自身就是 Map，`getValue/setValue` 直接通过 `model.get/put` 操作字段值
- **类型校验**：因缺少编译期类型检查，`setValue` 时增加了运行时类型校验

### 3.5 注册生命周期

```java
// 1. 构建模型信息
DynamicModelInfo info = new DynamicModelInfo("report_2024");
info.addField("name", "name");
info.addField("value", "value", BigDecimal.class);

// 2. 注册 — 写入 INFOS 并注册 MyBatis SQL
DynamicModel.register(info);
// 等效：DynamicModel.of(info).register();

// 3. 使用 — 与 BaseModel 子类完全一致的 API
DynamicModel.of("report_2024").insert();
DynamicModel.of("report_2024").updateById();
DynamicModel.of("report_2024").listByConditions(config);

// 4. 注销
DynamicModel.removeRegistered("report_2024");
```

**注册时的 MyBatis SQL 注册**：

`DynamicModel.register(info)` 除了写入 INFOS，还会读取 `DynamicModel.class` 接口层级上的 `@ModelMethod` 注解（与 `BaseModel` 子类相同的注解），为每个 CRUD 方法调用 `ModelMethodDefinition.register(info)`，进而通过 `MybatisHelper.addMappedStatement()` 注册到 MyBatis Configuration。因此动态模型与 ClassModel 共享同一套 SQL 生成和执行基础设施。

**insert/update 的值存取**：

`DynamicModel` 继承 `HashMap<String, Object>`，自身就是数据载体。insert/update 时字段值通过 `put/get` 直接操作自身：

```java
var model = DynamicModel.of("report_2024");
model.put("name", "value");  // 设置字段值
model.insert();               // Map 中的值被写入数据库
```

注意事项：
- `removeRegistered()` 仅从 `INFOS` 中移除引用，**已注册到 MyBatis Configuration 的 MappedStatement 不会被清除**。

---

## 4. ModelOperator — 操作器分离模式

### 4.1 设计动机

`BaseModel` 方式要求实体类实现接口，即「实体 = 数据载体 + CRUD 行为」。当 POJO 来自外部 JAR 或框架约束不允许修改时，这种耦合就构成了障碍。

`ModelOperator` 将「数据定义」与「操作行为」解耦：POJO 只负责数据结构（注解仍是可选的），CRUD 能力通过独立的 Operator 类注入。这个模式也叫「零侵入接入」。

### 4.2 ModelOperator 标记接口

```java
/**
 * 模型操作标记接口。
 *
 * Models 是框架提供的标准实现，通过静态工厂方法 Models.wrap(Object) /
 * Models.wrap(Class) 创建实例，无需继承或实现本接口。
 *
 * 如需自定义操作类（如限制访问范围、添加业务方法），可实现本接口并配合
 * BaseInsert、BaseDelete、BaseUpdate、BaseQuery 接口。
 *
 * @ModelScan 的 operatorClass 属性必须是本接口的子类型。
 */
public interface ModelOperator<T> {}
```

纯标记接口，无任何方法。其作用是在 `@ModelScan` 中作为类型约束：

```java
// operatorClass 决定为扫描到的 POJO 注册哪些 SQL
@ModelScan(
    superClass   = AbstractEntity.class,  // 扫描目标 POJO
    operatorClass = Models.class          // 用 Models 作为操作器（注册全部 12 种 CRUD）
)
```

框架在注册阶段读取 `operatorClass` 接口层级上的 `@ModelMethod` 注解：
- `operatorClass = Models.class`：`Models` 实现了 `BaseInsert/BaseDelete/BaseUpdate/BaseQuery`，其接口链上标注了全部 12 个 `@ModelMethod` → 注册全部 CRUD SQL
- `operatorClass = ModelOperator.class`（默认值）：无任何 `@ModelMethod` → 只构建 ModelInfo，不注册 SQL

### 4.3 Models<T> — 标准操作器

`Models<T>` 是框架提供的 `final` 标准实现，线程安全，零业务逻辑，纯代理层。

**双入口设计：**

```java
public final class Models<T> implements ModelOperator<T>,
       BaseInsert<T>, BaseDelete<T>, BaseUpdate<T>, BaseQuery<T> {

    private final Class<T> modelClass;
    private final T entity;

    private Models(Class<T> modelClass, T entity) { ... }

    // 入口 1：绑定实体（拥有字段值，用于 insert/update）
    public static <T> Models<T> wrap(T entity) {
        Objects.requireNonNull(entity);
        return new Models<>((Class<T>) entity.getClass(), entity);
    }

    // 入口 2：绑定类型（不持有字段值，用于 delete/query）
    public static <T> Models<T> wrap(Class<T> modelClass) {
        Objects.requireNonNull(modelClass);
        return new Models<>(modelClass, null);
    }

    // entity-bound：需要 entity != null
    public boolean insert()           { return InsertDefinition.exec(requireEntity()); }
    public boolean updateById()       { return UpdateByIdDefinition.exec(requireEntity()); }
    public int updateByConditions(QueryConditions conditions) {
        return UpdateByConditionsDefinition.exec(requireEntity(), conditions);
    }

    // class-bound：entity 可以为 null
    public boolean deleteById(Object id) { return DeleteByIdDefinition.exec(this.modelClass, id); }
    public <R extends T> R findById(Object id, QueryFields... fields) {
        return FindByIdDefinition.exec(this.modelClass, id, fields);
    }
    // ... 其余 7 个查询方法类似

    private T requireEntity() {
        if (this.entity == null)
            throw new ModelExecuteException(modelClass,
                "This operation requires an entity instance. "
                + "Use Models.wrap(T entity) instead of Models.wrap(Class<T>).");
        return this.entity;
    }
}
```

设计要点：
- `wrap(entity)` 和 `wrap(Class)` 每次返回**新实例**，无共享状态，天然线程安全
- `requireEntity()` 在 insert/update 时做守卫：若用 `wrap(Class)` 创建却调用 `insert()`，直接抛异常
- 所有 CRUD 方法直接委托 `XxxDefinition.exec()`，`Models` 自身无任何字段映射或 SQL 拼接逻辑

**`findOneByConditions` 的特殊处理：**

```java
public <R extends T> R findOneByConditions(QueryConfig config) {
    List<R> list;
    if (Constants.pageHelperPresent) {
        // PageHelper 可用时，用分页 LIMIT 1 避免全表扫描
        Page<R> page = PageByConditionsDefinition.exec(this.modelClass, 1, 1, false, config);
        list = page.getItems();
    } else {
        list = ListByConditionsDefinition.exec(this.modelClass, config);
    }
    return list == null || list.isEmpty() ? null : list.get(0);
}
```

### 4.4 自定义 Operator

当需要限制操作范围或添加业务方法时，自定义 `ModelOperator` 实现：

```java
// 只为 User 开放查询能力，不暴露写操作
@Component
public class UserOperator implements ModelOperator<User>, BaseQuery<User> {
    private static final Class<User> MODEL = User.class;

    @Override
    public <R extends User> R findById(Object id, QueryFields... fields) {
        return FindByIdDefinition.exec(MODEL, id, fields);
    }

    @Override
    public <R extends User> List<R> listByConditions(QueryConfig config) {
        return ListByConditionsDefinition.exec(MODEL, config);
    }

    // 可以不实现 BaseInsert/BaseUpdate/BaseDelete — 写操作自然不可用

    // 自定义业务方法
    public List<User> findActiveUsers() {
        return ListByConditionsDefinition.exec(MODEL,
            QueryConfig.of(QueryConditions.and().add("status", 1)));
    }
}

// 使用方式
@ModelScan(superClass = User.class, operatorClass = UserOperator.class)
```

此时 `operatorClass = UserOperator.class` 的接口层级上只有 `BaseQuery` 的 `@ModelMethod`，框架只为 `User` 注册查询 SQL。`Models.wrap(User.class).deleteById(1L)` 会因无对应 MappedStatement 而报错 — 访问限制由 SQL 注册范围自然形成，无需额外的权限检查层。

### 4.5 FieldCustomizer 声明式字段覆盖

当 POJO 完全不可修改（来自外部 JAR），甚至无法添加注解时，通过 `FieldCustomizer` Spring Bean 在启动时声明式覆盖字段配置：

```java
@Bean
public FieldCustomizer<User> userFieldOptions() {
    return FieldCustomizer.of(User.class)
        .field(User::getId,          f -> f.id(Id.Type.SNOWFLAKE).updatable(false))
        .field(User::getCreatedTime, f -> f.autoFill(DataOperateDate.class).updatable(false))
        .field(User::getUpdatedTime, f -> f.autoFill(DataOperateDate.class))
        .field(User::getCreatorId,   f -> f.name("create_user").autoFill(DataOperateUser.class));
}
```

`FieldOptions` 提供链式 API：

| 方法 | 等价注解 |
|------|---------|
| `name(String)` | `@Column(name=...)` |
| `id(Id.Type)` | `@Id(type=...)` |
| `insertable(boolean)` | `@Column(insertable=...)` |
| `updatable(boolean)` | `@Column(updatable=...)` |
| `selectable(boolean)` | `@Column(selectable=...)` |
| `autoFill(Class<? extends Annotation>)` | `@DataOperateDate` / `@DataOperateUser` |

**执行时机**：`SimpleCrudAutoConfiguration.afterPropertiesSet()` 中，`ModelRegistrar.register()` 执行完毕后，统一调用所有 `FieldCustomizer` Bean 的 `apply()` 方法。这确保覆盖发生在模型注册之后、业务调用之前。

```java
// AbstractModelInfo.changeFieldOptions()
public void changeFieldOptions(String fieldName, FieldOptions config) {
    ModelField<?> field = this.fields.get(fieldName);
    if (field != null) {
        field.config(config);  // 非 null 值覆盖，null 值保持原配置不变
        this.fieldChanged = true;
        this.getInsertFields(); // 刷新缓存
        this.getUpdateFields();
    }
}
```

**Lambda 解析原理**：`User::getId` 通过 `SerializedFunction` 序列化后，从序列化方法名（`getId`）反解出字段名（`id`），无需字符串硬编码，享受 IDE 重构安全。

---

## 5. 核心机制深入

### 5.1 ModelCache — 全局注册中心

`ModelCache` 是框架运行时的中心注册表，维护三个维度的缓存：

```java
// 1. 模型信息：模型类 → ModelInfo
private static final Map<Class<?>, ModelInfo<?>> INFOS = new ConcurrentHashMap<>();

// 2. 数据源提供者：数据源类型 → DatasourceProvider
private static final Map<DatasourceType, DatasourceProvider<?>> PROVIDERS = new ConcurrentHashMap<>();

// 3. 填充策略：注解类型 → DataFillStrategy
private static final Map<Class<?>, DataFillStrategy> FILL_STRATEGY = new ConcurrentHashMap<>();
```

**注册优先级（按 `order()` 排序）**：当有新提供者注册时，仅在其 `order()` 小于已注册者的 `order()` 时才覆盖。`order()` 越小优先级越高，默认为 `Integer.MAX_VALUE`。

```java
public static void registerProvider(DatasourceProvider<?> provider) {
    PROVIDERS.merge(provider.support(), provider, (old, nw) ->
        nw.order() < old.order() ? nw : old);
}
```

**代理类处理**：`info(Object modelOrClass)` 通过 `Classes.getUserClass()` 自动解 CGLIB/Spring 代理，确保 CGLIB 代理对象也能正确找到 ModelInfo。

### 5.2 MybatisHelper — 动态 MappedStatement 注册

这是框架最底层的机制 — 在 MyBatis 启动完成后，运行时动态注册新的 MappedStatement。

```java
public static void addMappedStatement(
    DatasourceType dsType, String dsName,
    Class<?> modelClass, String statementId,
    BiFunction<Configuration, Object, String> sqlProvider,
    SqlCommandType sqlCommandType, ModelInfo<?> info
) {
    exec(dsType, dsName, sqlSession -> {
        Configuration config = sqlSession.getConfiguration();

        // 防止重复注册
        if (config.hasStatement(statementId, false)) return null;

        // 构建延迟求值 SqlSource
        SqlSource sqlSource = param -> {
            String script = sqlProvider.apply(config, param);
            return config.getDefaultScriptingLanguageInstance()
                .createSqlSource(config, script, param.getClass());
        };

        // INSERT 且主键自增时启用 KeyGenerator
        boolean autoGenerateKeys = sqlCommandType == SqlCommandType.INSERT
            && info.id() != null && !info.id().insertable();
        KeyGenerator keyGenerator = autoGenerateKeys
            ? new Jdbc3KeyGenerator() : new NoKeyGenerator();

        MappedStatement ms = new MappedStatement.Builder(
                config, statementId, sqlSource, sqlCommandType)
            .keyGenerator(keyGenerator)
            .resultMaps(resultMaps)  // SELECT 时附加 ResultMapping
            .build();
        config.addMappedStatement(ms);
        return null;
    });
}
```

**延迟 SQL 生成**：`sqlProvider` 是 `BiFunction<Configuration, Object, String>`，**每次 SQL 执行时才调用**。传入的实际参数对象被用于生成包含绑定值占位符的 MyBatis XML 脚本，然后动态编译为 `SqlSource`。这种设计实现了「一个 statementId，多次执行可有不同 SQL」（如动态列、动态条件）。

**通用执行模板**：

```java
public static <R> R exec(DatasourceType dsType, String dsName,
                         Function<SqlSession, R> function) {
    DatasourceProvider<SqlSession> provider = ModelCache.getProvider(dsType);
    SqlSession sqlSession = provider.get(dsName);
    try {
        return function.apply(sqlSession);
    } finally {
        if (provider.closeable(dsName)) sqlSession.close();
    }
}
```

### 5.3 MybatisScripts — 动态 SQL 脚本生成

`MybatisScripts` 是框架的 SQL 生成引擎，负责将 `ModelInfo` + 运行时参数转换为 MyBatis 可执行的 XML 脚本。

**条件脚本（`conditionScript()`）：**

递归解析 `QueryConditions` 树，生成 `<where>` 子句：

```
QueryConditions.and()
  .add("name", "Zhang")
  .add(QueryConditions.or()
    .add("age", greater_than, 18)
    .add("status", 1))
  .add("deleted", equal, 0)

        ↓

<script>
  <where>
    AND name = #{name}
    AND (
      OR age &gt; #{age}
      OR status = #{status}
    )
    AND deleted = #{deleted}
  </where>
</script>
```

**单条件解析（`resolveCondition()`）：**

```java
static String resolveCondition(Configuration config, QueryConditionField field,
                                boolean xml, Map<String, Object> paramMap) {
    ConditionType type = field.type();
    switch (type) {
        case is_null:
        case not_null:
            // 不需要值绑定
            return type.expression(field.name(), "NULL");
        case in:
        case not_in:
            // 生成 <foreach> 子句
            return type.expression(field.name(),
                MybatisScripts.foreach(field.key(), "item", ",", null));
        default:
            // 标准绑定：#{key}
            return type.expression(field.name(), "#{" + field.key() + "}");
    }
}
```

**运算符方言适配（`ConditionDialect`）：**

不同数据库对同一运算符的 SQL 语法不同。框架通过 `ConditionDialect` 接口 + 数据库特定子类处理（`PgConditionDialect` 默认，`MySqlConditionDialect` MySQL 5.7+）：

```java
// PgConditionDialect 中
public String arrayContains(ModelField<?> field, String value, boolean xml) {
    String op = "@>";
    return xml ? field.columnName() + " <![CDATA[ " + op + " ]]> " + value
               : field.columnName() + " " + op + " " + value;
}
```

框架已内置并自动注册三种方言（通过 JDBC URL 子协议 `postgresql` / `mysql` / `h2` 匹配）。若使用其他数据库，需手动调用 `Dialects.registerConditionDialect()` 注册自定义实现：

```java
// 注册自定义方言（如 H2、Oracle 等）
Dialects.registerConditionDialect("h2", new MyCustomConditionDialect());
```

当 `xml = true` 时，`>`、`<`、`>=`、`<=` 等运算符用 `<![CDATA[...]]>` 包裹以通过 XML 解析。

### 5.4 完整生命周期

从应用启动到 SQL 执行的全链路时序图：

```
┌─ 启动阶段 ────────────────────────────────────────────┐
│                                                       │
│  SimpleCrudAutoConfiguration.afterPropertiesSet()     │
│  │                                                    │
│  ├─ 1. setDatasourceProvider()                        │
│  │     ModelCache.registerProvider(                   │
│  │       new DefaultSpringMybatisProvider())           │
│  │                                                    │
│  ├─ 2. setDataFillStrategy()                          │
│  │     ModelCache.registerFillStrategy(               │
│  │       new DefaultDataIdFillStrategy())              │
│  │     ModelCache.registerFillStrategy(               │
│  │       new DefaultDataOperateDateFillStrategy())     │
│  │                                                    │
│  └─ 3. ModelRegistrar.register()                      │
│        │                                              │
│        ├─ [并行] ClassModelInfo 构建                   │
│        │   ├─ obtainModelName() → 解析 @Table         │
│        │   ├─ obtainFields()   → 反射遍历字段          │
│        │   └─ buildConfig()    → @Column/@Id 配置     │
│        │                                              │
│        ├─ [串行] @ModelMethod 注册                     │
│        │   对每个标注 @ModelMethod 的接口:              │
│        │   ModelMethodDefinition.register(info)       │
│        │     └─ MybatisHelper.addMappedStatement()    │
│        │           └─ config.addMappedStatement(ms)   │
│        │                                              │
│        └─ ModelCache.registerInfo(info)               │
│                                                       │
│  4. applyFieldCustomizers() — FieldCustomizer.apply │
└───────────────────────────────────────────────────────┘

┌─ 运行时（BaseModel 路径：entity.insert()）────────────┐
│                                                       │
│  entity.insert()                                      │
│  │                                                    │
│  ├─ Insert.insert() default 方法                       │
│  │  default boolean insert() {                        │
│  │      return InsertDefinition.exec(this);           │
│  │  }                                                 │
│  │                                                    │
│  ├─ → InsertDefinition.exec(entity)                   │
│  │                                                    │
│  └─ → MybatisInsertMethod.exec(model)                 │
│       │                                               │
│       ├─ 自动填充：遍历 insertable 字段                 │
│       │   fillStrategy.get(param) 填充 id/time/user    │
│       │                                               │
│       ├─ 生成 SQL 脚本（MybatisScripts）                │
│       │   INSERT INTO t_user (id, name, ...)           │
│       │   VALUES (#{id}, #{name}, ...)                 │
│       │                                               │
│       └─ MybatisHelper.exec(dsType, dsName,            │
│               sqlSession -> {                          │
│                 sqlSession.insert(stmtId, model)        │
│               })                                      │
│           │                                           │
│           └─ JDBC → 数据库                              │
│                                                       │
│  Models.wrap(user).insert() 走另一路径:                 │
│  Models.insert() → InsertDefinition.exec(requireEntity)│
│  → 殊途同归，最终都到 InsertDefinition.exec()           │
└───────────────────────────────────────────────────────┘
```

---

## 附录 A：核心类关系图

```
                          CRUD 能力接口层
══════════════════════════════════════════════════════
  Insert<T>     DeleteById<T>    UpdateById<T>      FindById<T>
  InsertBatch<T> DeleteByIds<T>   UpdateByConds<T>   FindOneByConds<T>
    └── BaseInsert<T>  └── BaseDelete<T>  └── BaseUpdate<T>   ListByConds<T> ...
                                                         └── BaseQuery<T>
                                                               │
                              ┌────────────────────────────────┘
                              │
                         BaseModel<T>
                         ▲          ▲
                         │          │
                  SimpleModel<T>  DynamicModel
                  (POJO 实现)    (Map<String,Object> 实现)

══════════════════════════════════════════════════════
                    操作器分离层
══════════════════════════════════════════════════════
  ModelOperator<T> (标记接口)
        ▲
        │
  Models<T> (final, 标准操作器)
  implements ModelOperator, BaseInsert, BaseDelete, BaseUpdate, BaseQuery

══════════════════════════════════════════════════════
                    元信息层
══════════════════════════════════════════════════════
  ModelInfo<T> ◄── AbstractModelInfo<T>
                      ▲              ▲
                      │              │
              ClassModelInfo<T>  DynamicModelInfo

  ModelField<T> ◄── AbstractModelField<T>
                      ▲              ▲
                      │              │
              ClassModelField<T>  DynamicModelField

══════════════════════════════════════════════════════
                  注册与执行层
══════════════════════════════════════════════════════
  ModelCache           ModelRegistrar
  (三级缓存)             (批量注册)
      │                     │
      └──────┬──────────────┘
             │
      MybatisHelper       MybatisScripts
  (动态 MappedStatement)  (SQL 脚本生成)
```

---

## 附录 B：注解速查表

| 注解 | 作用域 | 核心属性 | 处理时机 | 处理器 |
|------|:---:|------|------|------|
| `@Table` | TYPE | `name`, `schema` | `ClassModelInfo` 构造时 | `obtainModelName()` |
| `@Id` | FIELD | `type`（SNOWFLAKE/UUID32/UUID36/AUTO_INCREMENT） | `ClassModelField` 构造时 | `DefaultDataIdFillStrategy` |
| `@Column` | FIELD | `name`, `insertable`, `updatable`, `selectable` | `ClassModelField` 构造时 | `buildConfig()` |
| `@Condition` | FIELD | `field`, `type`, `defaultValueIfNull` | 调用 `QueryConditions.fromAnnotation()` 时 | 反射扫描 + 缓存 |
| `@Conditions` | FIELD | `value`（`@Condition[]`） | 同上 | 容器注解，Java 编译器自动处理 |
| `@DataOperateDate` | FIELD | — | `ClassModelField` 构造时 | `DefaultDataOperateDateFillStrategy` |
| `@DataOperateUser` | FIELD | — | `ClassModelField` 构造时 | 需自定义 `DataFillStrategy` |
| `@ModelMethod` | TYPE | `value`（`Class<? extends ModelMethodDefinition>`） | `ModelRegistrar.register()` 时 | `Definition.register(info)` |
| `@ModelScan` | TYPE | `basePackages`, `superClass`, `operatorClass`, `datasourceType`, `datasourceName` | Spring 容器启动时 | `ModelScannerRegistrar` |
| `@ModelScans` | TYPE | `value`（`@ModelScan[]`） | 同上 | 容器注解 |

