# 集成测试覆盖文档

## 测试环境

| 项目 | 配置 |
|------|------|
| 容器 | Spring Boot Test (`@SpringBootTest`, WebEnvironment=NONE) |
| 数据库 | H2 内存库 (MODE=PostgreSQL) |
| 事务 | `@Transactional` 自动回滚，分页测试用 `NOT_SUPPORTED` + 手动清理 |
| ORM | MyBatis (Spring Boot 自动配置) + PageHelper (`pagehelper-spring-boot-starter`) |
| 模型包 | `com.example.myapp.model` / `com.example.operator.model` / `com.example.multids.model` |
| 扫描方式 | `@ModelScan` 自动扫描 + `SimpleCrudAutoConfiguration` 自动注册 |

## 测试场景清单

### 1. 容器启动与自动配置验证

| 测试类 | 测试方法 | 验证点 |
|--------|---------|------|
| `ContainerAutoConfigurationTest` | `testContextLoadsAndAutoConfigurationWorks` | Spring 容器正常启动，`SimpleSpringUtils` 已初始化 |
| | `testBaseModelAutoRegisteredByModelScan` | `UserModel` → `t_user` 已注册 |
| | `testAllBaseModelsAutoRegistered` | 全部 BaseModel 子类（Uuid32/Uuid36/AutoIncrement/DataFill）已注册 |
| | `testOperatorModelAutoRegisteredByModelScan` | `UserPojo` → `t_operator` 已注册 |

### 2. BaseModel CRUD

| 测试类 | 测试方法 | 验证点 |
|--------|---------|------|
| `BaseModelCrudTest` | `testInsertShouldGenerateSnowflakeIdAndPersist` | insert() → id > 0 |
| | `testFindByIdShouldReturnCompleteEntity` | findById → 完整实体，独立实例查询 |
| | `testUpdateByIdShouldOnlyModifyNonNullFields` | updateById → null 字段不覆盖 |
| | `testUpdateByIdWithOnlyIdShouldThrowException` | 全 null 字段 → SET 为空 → 异常 |
| | `testDeleteByIdShouldRemoveOnlyTarget` | deleteById → 只删目标行 |
| | `testDeleteByIdsShouldRemoveOnlyTargets` | deleteByIds → 批量删除指定行 |
| | `testDeleteByConditionsShouldRemoveOnlyMatched` | deleteByConditions → 条件删除 |
| | `testInsertBatchShouldPersistAll` | insertBatch → 全部持久化 |
| | `testListByIdsShouldReturnOnlyRequested` | listByIds → 按 ID 列表返回 |
| | `testCountByConditionsShouldReturnCorrectCount` | countByConditions → 计数正确 |
| | `testExistByConditionsShouldReturnTrueAndFalse` | existByConditions → 存在/不存在 |
| | `testUpdateByConditionsShouldModifyOnlyMatched` | updateByConditions → 条件更新 |
| | `testFindOneByConditionsShouldReturnSingle` | findOneByConditions → 返回单条 |
| | `testFindOneByConditionsShouldReturnNullWhenNoMatch` | 无匹配 → null |

### 3. BaseModel 查询 — 条件 / 排序 / 分页 / 字段

| 测试类 | 测试方法 | 验证点 |
|--------|---------|------|
| `BaseModelQueryTest` | `testListByConditionsShouldReturnMatchedOnly` | 基本条件过滤 |
| | `testQueryFieldsShouldSelectSubset` | QueryFields → 只查指定列 |
| | `testQuerySortersAscShouldOrderCorrectly` | asc 排序 |
| | `testQuerySortersDescShouldOrderCorrectly` | desc 排序 |
| | `testConditionEqual` | ConditionType.equal |
| | `testConditionNotEqual` | not_equal |
| | `testConditionLikeAll` | like_all |
| | `testConditionLikeLeft` | like_left |
| | `testConditionLikeRight` | like_right |
| | `testConditionGreaterThan` | greater_than |
| | `testConditionGreaterEqual` | greater_equal |
| | `testConditionLessThan` | less_than |
| | `testConditionLessEqual` | less_equal |
| | `testConditionIn` | in |
| | `testConditionNotIn` | not_in |
| | `testConditionIsNull` | is_null |
| | `testConditionNotNull` | not_null |
| | `testConditionNestedAndOr` | 嵌套 AND/OR |
| | `testPageByConditionsShouldReturnMatchedPage` | 分页元数据正确 |
| | `testPageByConditionsShouldReturnCorrectPage` | 分页多页验证 |
| | `testPageByConditionsWithSortingShouldReturnSortedPage` | 分页 + 排序 |
| | `testThreeLevelNestingShouldFilterCorrectly` | 三级嵌套 |
| | `testOrRootWithAndChildrenShouldMatchEitherGroup` | OR 根 + AND 子 |
| | `testMultipleParallelOrGroupsShouldIntersect` | 多个平行 OR |
| | `testMixedFieldsAndSubConditionsShouldAllApply` | fields + subConditions 混合 |

### 4. @Condition 注解查询

| 测试类 | 测试方法 | 验证点 |
|--------|---------|------|
| `ConditionAnnotationTest` | `testFromAnnotationShouldFilterByConditions` | 多条件注解 → 过滤正确 |
| | `testFromAnnotationWithDefaultValueIfNull` | defaultValueIfNull → 默认值生效 |
| | `testFromAnnotationWithMultipleConditionsOnSameField` | 多注解 → 区间过滤 |
| | `testFromAnnotationWithInCondition` | in + List 值 |
| | `testFromAnnotationWithLikeAllCondition` | like_all + field 映射 |
| | `testFromAnnotationWithIsNullCondition` | is_null → 匹配 null 行 |
| | `testFromAnnotationWithNullFieldShouldSkipCondition` | null 字段无默认值 → 跳过 |
| | `testFromAnnotationWithFieldMapping` | field 属性 → 列名映射 |
| | `testFromAnnotationWithStringDefaultValue` | String defaultValueIfNull |

### 5. ModelOperator 模式

| 测试类 | 测试方法 | 验证点 |
|--------|---------|------|
| `ModelOperatorTest` | `testInsertShouldPersistAndGenerateSnowflakeId` | Models.wrap(pojo).insert() |
| | `testFindByIdShouldReturnCompleteEntity` | Models.wrap(Class).findById(id) |
| | `testUpdateByIdShouldOnlyModifyNonNullFields` | null 字段不覆盖 |
| | `testDeleteByIdShouldRemoveOnlyTarget` | 删除目标行 |
| | `testListByConditionsShouldFilterResults` | 条件过滤 |
| | `testUpdateByConditionsShouldModifyMatched` | 条件更新 |
| | `testDeleteByIdsShouldRemoveTargets` | 批量删除 |
| | `testCountByConditionsShouldReturnCount` | 计数 |
| | `testPageByConditionsShouldReturnSortedPage` | 分页 + 排序 |

### 6. DynamicModel 模式

| 测试类 | 测试方法 | 验证点 |
|--------|---------|------|
| `DynamicModelTest` | `testRegisterShouldStoreModelInfo` | 注册 → info() 返回元信息 |
| | `testInsertShouldPersistMapData` | insert() → 雪花 ID 自动填充 |
| | `testFindByIdShouldReturnInsertedData` | findById → Map 值一致 |
| | `testUpdateByIdShouldOnlyModifyNonNullFields` | null 不覆盖 |
| | `testListByConditionsShouldFilter` | 条件过滤 |
| | `testDeleteByIdShouldRemoveOnlyTarget` | 删除目标行 |
| | `testRemoveRegisteredShouldCleanUp` | 注销 → info() 抛异常 |
| | `testCountByConditionsShouldReturnCount` | 计数正确 |
| | `testPageByConditionsShouldReturnSortedPage` | 分页 + 排序 |

### 7. @Id 主键策略

| 测试类 | 测试方法 | 策略 | 验证点 |
|--------|---------|:---:|------|
| `IdStrategyTest` | `testSnowflakeInsertShouldGenerateLongId` | SNOWFLAKE | Long id > 0 |
| | `testSnowflakeInsertBatchShouldGenerateDistinctIds` | SNOWFLAKE | id 递增唯一 |
| | `testUuid32InsertShouldGenerate32CharHex` | UUID32 | 32 位无连字符 hex |
| | `testUuid32InsertBatchShouldGenerateUniqueIds` | UUID32 | 多个 id 不重复 |
| | `testUuid36InsertShouldGenerate36CharUuid` | UUID36 | 36 位标准 UUID |
| | `testUuid36InsertBatchShouldGenerateUniqueIds` | UUID36 | 多个 UUID 不重复 |
| | `testAutoIncrementInsertShouldGenerateDbId` | AUTO_INCREMENT | DB 生成并回填 |
| | `testAutoIncrementInsertShouldIncreaseSequentially` | AUTO_INCREMENT | 递增 |
| | `testAutoIncrementInsertBatchShouldBackFillIds` | AUTO_INCREMENT | id 递增 + 全部回填 |

### 8. DataFill 自动填充

| 测试类 | 测试方法 | 验证点 |
|--------|---------|------|
| `DataFillTest` | `testInsertShouldAutoFillTimeFields` | @DataOperateDate → createdTime/updatedTime 填充 |
| | `testUpdateShouldRefreshUpdatedTimeOnly` | updateById → updatedTime 刷新，createdTime 不变 |
| | `testInsertShouldAutoGenerateSnowflakeId` | @Id(SNOWFLAKE) → id 递增 |
| | `testCreateUserShouldBeNullWithoutCustomStrategy` | @DataOperateUser → 无自定义策略时为 null |
| | `testInsertBatchShouldAlsoFillTimeFields` | insertBatch → 自动填充也生效 |

### 9. FieldCustomizer 字段策略

| 测试类 | 测试方法 | 验证点 |
|--------|---------|------|
| `FieldCustomizerTest` | `testBaseModelInsertableFalseShouldSkipField` | insertable=false → 字段不插入 |
| | `testOperatorInsertableFalseShouldSkipField` | Operator 路径 insertable=false |
| | `testBaseModelUpdatableFalseShouldRetainOriginal` | updatable=false → 原值保留 |
| | `testOperatorUpdatableFalseShouldRetainOriginal` | Operator 路径 updatable=false |
| | `testBaseModelSelectableFalseShouldNotReturnField` | selectable=false → 字段为 null |
| | `testOperatorSelectableFalseShouldNotReturnField` | Operator 路径 selectable=false |
| | `testBaseModelNameOverrideShouldMapToDifferentColumn` | name("x") → 列名映射变更 |
| | `testDynamicFieldCustomizerViaInfoApi` | DynamicModel→info() 可用 |

### 10. 多数据源

| 测试类 | 测试方法 | 验证点 |
|--------|---------|------|
| `MultiDatasourceTest` | `testSecondDsModelShouldBeRegistered` | 第二数据源模型已注册 |
| | `testSecondDsInsertAndFind` | insert + findById |
| | `testSecondDsUpdateAndDelete` | updateById + deleteById |
| | `testSecondDsListByConditions` | listByConditions 过滤 |
| | `testTwoDatasourcesAreIsolated` | 主/第二数据源隔离 |

---

## 测试模型

| 模型 | 映射表 | 用途 |
|------|-------|------|
| `UserModel` | `t_user` | BaseModel CRUD + 查询 |
| `Uuid32Model` | `t_uuid32` | UUID32 主键策略 |
| `Uuid36Model` | `t_uuid36` | UUID36 主键策略 |
| `AutoIncrementModel` | `t_auto_inc` | AUTO_INCREMENT 主键策略 |
| `DataFillModel` | `t_data_fill` | 自动填充 (SimpleModel) |
| `UserPojo` | `t_operator` | ModelOperator 零侵入 |
| `SecondDsUserModel` | `t_second` | 第二数据源 |
| 动态 `DynamicModel` | `t_dynamic` | 运行时动态模型 |

每张表仅被一个模型使用，避免意外数据污染。

## 单元测试覆盖

| 测试类 | 覆盖范围 |
|--------|---------|
| `ConditionTest` | @Condition 注解解析 |
| `QueryConditionsTest` | AND/OR 工厂、add() 多态推断、同名字段去重、嵌套 |
| `SqlConditionDialectTest` | ANSI SQL 条件生成 |
| `H2ConditionDialectTest` | H2 数组条件 |
| `MySqlConditionDialectTest` | MySQL JSON/数组条件 |
| `PgConditionDialectTest` | PostgreSQL 数组/JSON 条件 |
| `OracleConditionDialectTest` | Oracle JSON_EXISTS / LIKE |
