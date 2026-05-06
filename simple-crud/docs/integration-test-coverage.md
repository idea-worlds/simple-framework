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

## 测试场景清单 (57 个集成测试)

### 1. 容器启动与自动配置验证 (3)

| 测试方法 | 验证点 |
|------|------|
| `testContextLoadsAndAutoConfigurationWorks` | Spring 容器正常启动，`ApplicationContext` 可用，`SimpleSpringUtils` 已初始化 |
| `testModelAutoRegisteredByModelScan` | `@ModelScan` 自动扫描 `UserModel` → `ModelCache` 中已注册 |
| `testOperatorModelAutoRegisteredByModelScan` | `@ModelScan(operatorClass=Models.class)` 自动扫描 `UserPojo` → `ModelCache` 中已注册 |

### 2. BaseModel 模式 CRUD (18)

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
| `testConditionNestedAndOr` | 嵌套 AND/OR (2层) | `WHERE (col1=? OR col1=?) AND col2>?` 逻辑组合 |

### 5b. 复杂条件嵌套 (5 集成 + 5 单元)

在 `QueryConditionsTest` 中验证数据结构构建，在 `BaseModelCrudIntegrationTest` 中验证真实 SQL 执行。

| 测试方法 | 层级 | 条件结构 | 验证点 |
|------|:---:|------|------|
| `testThreeLevelNesting_shouldBuildCorrectTree` (unit) | 3 | `AND(name, OR(AND(a=1)))` | 树深度正确，叶子条件在正确层级 |
| `testMultipleParallelSubConditions_shouldPreserveOrder` (unit) | 2 | `AND(OR(a), OR(b))` | 多个平行子条件各自独立 |
| `testOrRootWithAndChildren_shouldBuildCorrectly` (unit) | 2 | `OR(AND(a,b), AND(a,b))` | OR 根 + AND 子条件反向构建 |
| `testSameFieldNameAcrossLevels_shouldDedupKeys` (unit) | 2 | 父+子同字段 `name` | `flushFieldKey` 跨层级 key 去重 |
| `testEmptySubCondition_shouldStillBeAddedToList` (unit) | 1 | `AND(a=1, empty OR)` | 空子条件行为（SQL 层跳过） |
| `testThreeLevelNestingShouldFilterCorrectly` | 3 | `name='T' AND (age=10 OR (age=20 AND email='x'))` | 三级递归过滤正确 |
| `testOrRootWithAndChildrenShouldMatchEitherGroup` | 2 | `(A AND 10) OR (B AND 20)` | OR 包裹多个 AND 组 |
| `testMultipleParallelOrGroupsShouldIntersect` | 2 | `(X OR Y) AND (10 OR 20)` | 多组 OR 交集过滤 |
| `testMixedFieldsAndSubConditionsShouldAllApply` | 2 | `name='M' AND age>5 AND OR(email1,email2)` | fields+subConditions 混合拼接 |

### 6. ModelOperator 模式 (9)

| 测试方法 | 验证点 |
|------|------|
| `testOperatorInsertShouldPersist` | `Models.wrap(pojo).insert()` → 零侵入 POJO 插入 |
| `testOperatorFindByIdShouldReturn` | `Models.wrap(Class).findById(id)` → 按类型查询 |
| `testOperatorUpdateByIdShouldModify` | `Models.wrap(pojo).updateById()` → 零侵入更新 |
| `testOperatorDeleteByIdShouldRemove` | `Models.wrap(Class).deleteById(id)` → 零侵入删除 |
| `testOperatorListByConditionsShouldFilter` | `Models.wrap(Class).listByConditions(config)` → 零侵入条件查询 |

### 7. DynamicModel 模式 (7)

| 测试方法 | 验证点 |
|------|------|
| `testRegisterShouldStoreModelInfo` | 运行时注册 → info() 返回元信息 |
| `testInsertShouldPersistMapData` | insert() → 雪花 ID 自动填充 |
| `testFindByIdShouldReturnInsertedData` | findById → Map 值一致 |
| `testUpdateByIdShouldOnlyModifyNonNullFields` | updateById → null 不覆盖 |
| `testListByConditionsShouldFilter` | 条件查询 → 过滤正确 |
| `testDeleteByIdShouldRemoveOnlyTarget` | 按 id 删除 → 目标行被删 |
| `testRemoveRegisteredShouldCleanUp` | 注销 → info() 抛异常 |

### 8. DynamicModel count / page (2)

| 测试方法 | 验证点 |
|------|------|
| `testCountByConditionsShouldReturnCount` | countByConditions → 计数正确 |
| `testPageByConditionsShouldNotThrow` | pageByConditions → API 不抛异常 |

### 9. @Condition 注解查询 (9)

| 测试方法 | 验证点 |
|------|------|
| `testFromAnnotationShouldFilterByConditions` | 多条件注解 → listByConditions 过滤正确 |
| `testFromAnnotationWithDefaultValueIfNull` | defaultValueIfNull → 默认值生效 |
| `testFromAnnotationWithMultipleConditionsOnSameField` | @Conditions 容器 → 矛盾条件返回 0 |
| `testFromAnnotationWithInCondition` | in 类型 + List 值 → WHERE IN 过滤 |
| `testFromAnnotationWithLikeAllCondition` | like_all + field 映射 → 模糊匹配 |
| `testFromAnnotationWithIsNullCondition` | is_null → 无需值，匹配 null 行 |
| `testFromAnnotationWithNullFieldShouldSkipCondition` | 字段 null 无 default → 条件被跳过 |
| `testFromAnnotationWithFieldMapping` | field 属性 → Java 字段名映射到 DB 列 |
| `testFromAnnotationWithStringDefaultValue` | 字符串 defaultValueIfNull 生效 |

### 10. DataFill 自动填充 (4)

| 测试方法 | 验证点 |
|------|------|
| `testInsertShouldAutoFillTimeFields` | @DataOperateDate → insert 时 createdTime/updatedTime 非 null |
| `testUpdateShouldRefreshUpdatedTimeOnly` | updateById → updatedTime 刷新，createdTime 不变 |
| `testInsertShouldAutoGenerateSnowflakeId` | @Id(SNOWFLAKE) → id 递增 |
| `testCreateUserShouldBeNullWithoutCustomStrategy` | @DataOperateUser → 无自定义策略时为 null |

### 11. FieldCustomizer 字段策略覆盖 (3 新增)

| 测试方法 | 验证点 |
|------|------|
| `testBaseModelSelectableFalseShouldNotReturnField` | selectable=false → findById 字段为 null |
| `testBaseModelNameOverrideShouldMapToDifferentColumn` | name("name2") → 列名映射变更生效 |
| `testOperatorSelectableFalseShouldNotReturnField` | Operator 路径 + selectable=false → 生效 |

### 12. @Id 主键策略 (9)

验证四种主键生成策略在 insert 和 insertBatch 下的行为。

| 测试方法 | 策略 | 操作 | 验证点 |
|------|:---:|:---:|------|
| `testSnowflakeInsertShouldGenerateLongId` | SNOWFLAKE | insert | Long id > 0 |
| `testSnowflakeInsertBatchShouldGenerateDistinctIds` | SNOWFLAKE | insertBatch | 3 个 id 递增唯一 |
| `testUuid32InsertShouldGenerate32CharHex` | UUID32 | insert | 32 位无连字符 hex |
| `testUuid32InsertBatchShouldGenerateUniqueIds` | UUID32 | insertBatch | 多个 id 不重复 |
| `testUuid36InsertShouldGenerate36CharUuid` | UUID36 | insert | 36 位标准 UUID 格式 |
| `testUuid36InsertBatchShouldGenerateUniqueIds` | UUID36 | insertBatch | 多个 UUID 不重复 |
| `testAutoIncrementInsertShouldGenerateDbId` | AUTO_INCREMENT | insert | id 由 DB 生成并回填 |
| `testAutoIncrementInsertShouldIncreaseSequentially` | AUTO_INCREMENT | insert | id 递增 |
| `testAutoIncrementInsertBatchShouldBackFillIds` | AUTO_INCREMENT | insertBatch | 3 个 id 递增且全部回填 |

> insertBatch AUTO_INCREMENT 通过逐条 `session.insert` 处理，每条独立使用 `Jdbc3KeyGenerator` 回填主键。

### 13. 多数据源 (5)

第二数据源（H2 `seconddb`）+ 独立 `@ModelScan(datasourceName="second")`。

| 测试方法 | 验证点 |
|------|------|
| `testSecondDsModelShouldBeRegistered` | 第二数据源模型已注册，datasourceName="second" |
| `testSecondDsInsertAndFind` | insert + findById 在第二数据源正确执行 |
| `testSecondDsUpdateAndDelete` | updateById + deleteById 在第二数据源正确执行 |
| `testSecondDsListByConditions` | listByConditions 在第二数据源过滤正确 |
| `testTwoDatasourcesAreIsolated` | 两个数据源各自独立，数据不交叉 |

### 14. 数组/JSON 条件 (方言单元测试覆盖)

数组和 JSON 条件的 SQL 生成逻辑在以下单元测试中覆盖，集成测试需 JSON 列暂未加入：

| 测试类 | 覆盖条件 |
|------|------|
| `PgConditionDialectTest` | array_contains, array_contained_by, array_overlap, json_contains, json_contained_by, json_exist_key/any/all |
| `MySqlConditionDialectTest` | 同上，MySQL JSON 函数版本 |
| `H2ConditionDialectTest` | array_contains, array_contained_by, array_overlap |
| `OracleConditionDialectTest` | LIKE（|| 拼接）、数组/JSON（JSON_EXISTS 函数） |
| `H2ConditionDialectTest` | array_contains, array_contained_by, array_overlap（无 `::text[]` 转换） |
| `SqlConditionDialectTest` | equal, not_equal, like, greater/less, in, notIn, isNull, notNull |

---

**总计**: 97 集成测试 + 89 单元测试 = 186 测试，全 PASS。
