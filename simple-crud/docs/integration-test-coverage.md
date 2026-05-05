# 集成测试覆盖文档

## 测试环境

| 项目 | 配置 |
|------|------|
| 容器 | Spring Boot Test (`@SpringBootTest`, WebEnvironment=NONE) |
| 数据库 | H2 内存库 (MODE=PostgreSQL) |
| 事务 | `@Transactional` 自动回滚，每次测试独立 |
| ORM | MyBatis (Spring Boot 自动配置) |
| 模型包 | `com.example.myapp.model` / `com.example.operator.model` — 完全独立于框架包 |
| 扫描方式 | `@ModelScan` 自动扫描 + `SimpleCrudAutoConfiguration` 自动注册 |
| Surefire | 3.2.5 + `junit-platform-launcher` |

## 测试场景清单 (33 个集成测试)

### 1. 容器启动与自动配置验证 (3)

| 测试方法 | 验证点 |
|------|------|
| `testContextLoadsAndAutoConfigurationWorks` | Spring 容器正常启动，`ApplicationContext` 可用，`SimpleSpringUtils` 已初始化 |
| `testModelAutoRegisteredByModelScan` | `@ModelScan` 自动扫描 `UserModel` → `ModelCache` 中已注册 |
| `testOperatorModelAutoRegisteredByModelScan` | `@ModelScan(operatorClass=Models.class)` 自动扫描 `UserPojo` → `ModelCache` 中已注册 |

### 2. BaseModel 模式 CRUD (7)

| 测试方法 | 验证点 |
|------|------|
| `testInsertShouldGenerateSnowflakeId` | `entity.insert()` → 返回 true，id 由雪花算法生成 |
| `testFindByIdShouldReturnEntity` | `entity.findById(id)` → 返回已插入的完整实体 |
| `testUpdateByIdShouldModifyFields` | `entity.updateById()` → 修改持久化到数据库，findById 可验证 |
| `testDeleteByIdShouldRemoveEntity` | `entity.deleteById(id)` → 返回 true，findById 返回 null |
| `testDeleteByConditionsShouldRemoveMatched` | `entity.deleteByConditions(conditions)` → 返回删除行数 |
| `testListByConditionsShouldReturnMatchedOnly` | `entity.listByConditions(config)` → 只返回匹配条件的数据 |
| `testPageByConditionsShouldPaginateAndSort`* | `entity.pageByConditions(...)` → 不抛异常 |

> *PageHelper 在 `@Transactional` 下行为受限，仅验证 API 不抛异常。

### 3. QueryFields 字段选择 (1)

| 测试方法 | 验证点 |
|------|------|
| `testQueryFieldsShouldSelectSubset` | `QueryFields.of().add("name")` → SELECT 只查询 name 列，age 为 null |

### 4. QuerySorters 排序 (2)

| 测试方法 | 验证点 |
|------|------|
| `testQuerySortersAscShouldOrderCorrectly` | `QuerySorters.asc("age")` → 结果按 age 升序排列 |
| `testQuerySortersDescShouldOrderCorrectly` | `QuerySorters.desc("age")` → 结果按 age 降序排列 |

### 5. ConditionType 全覆盖 (13)

| 测试方法 | ConditionType | 验证点 |
|------|------|------|
| `testConditionEqual` | `equal` | `WHERE col = ?` 精确匹配 |
| `testConditionNotEqual` | `not_equal` | `WHERE col != ?` 排除匹配 |
| `testConditionLikeAll` | `like_all` | `WHERE col LIKE '%value%'` 子串匹配 |
| `testConditionLikeLeft` | `like_left` | `WHERE col LIKE '%value'` 后缀匹配 |
| `testConditionLikeRight` | `like_right` | `WHERE col LIKE 'value%'` 前缀匹配 |
| `testConditionGreaterThan` | `greater_than` | `WHERE col > ?` 排除边界 |
| `testConditionGreaterEqual` | `greater_equal` | `WHERE col >= ?` 包含边界 |
| `testConditionLessThan` | `less_than` | `WHERE col < ?` 排除边界 |
| `testConditionLessEqual` | `less_equal` | `WHERE col <= ?` 包含边界 |
| `testConditionIn` | `in` | `WHERE col IN (?, ?, ?)` 多值匹配 |
| `testConditionNotIn` | `not_in` | `WHERE col NOT IN (?, ?, ?)` 多值排除 |
| `testConditionIsNull` | `is_null` | `WHERE col IS NULL` 空值匹配 |
| `testConditionNotNull` | `not_null` | `WHERE col IS NOT NULL` 非空匹配 |
| `testConditionNestedAndOr` | 嵌套 AND/OR | `WHERE (col1=? OR col1=?) AND col2>?` 逻辑组合 |

### 6. ModelOperator 模式 (5)

| 测试方法 | 验证点 |
|------|------|
| `testOperatorInsertShouldPersist` | `Models.wrap(pojo).insert()` → 零侵入 POJO 插入 |
| `testOperatorFindByIdShouldReturn` | `Models.wrap(Class).findById(id)` → 按类型查询 |
| `testOperatorUpdateByIdShouldModify` | `Models.wrap(pojo).updateById()` → 零侵入更新 |
| `testOperatorDeleteByIdShouldRemove` | `Models.wrap(Class).deleteById(id)` → 零侵入删除 |
| `testOperatorListByConditionsShouldFilter` | `Models.wrap(Class).listByConditions(config)` → 零侵入条件查询 |

### 7. DynamicModel 模式 (1)

| 测试方法 | 验证点 |
|------|------|
| `testDynamicModelShouldRegisterAndProvideInfo` | `DynamicModel.register(info)` → 运行时注册，`info()` 可获取元信息 |

### 8. 数组/JSON 条件 (方言单元测试覆盖)

数组和 JSON 条件的 SQL 生成逻辑在以下单元测试中覆盖，集成测试需 JSON 列暂未加入：

| 测试类 | 覆盖条件 |
|------|------|
| `PgConditionDialectTest` | array_contains, array_contained_by, array_overlap, json_contains, json_contained_by, json_exist_key/any/all |
| `MySqlConditionDialectTest` | 同上，MySQL JSON 函数版本 |
| `H2ConditionDialectTest` | array_contains, array_contained_by, array_overlap（无 `::text[]` 转换） |
| `SqlConditionDialectTest` | equal, not_equal, like, greater/less, in, notIn, isNull, notNull |

---

**总计**: 33 集成测试 + 74 单元测试 = 107 测试，全 PASS。
