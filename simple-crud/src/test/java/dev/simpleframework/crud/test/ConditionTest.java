package dev.simpleframework.crud.test;

import dev.simpleframework.crud.annotation.Condition;
import dev.simpleframework.crud.annotation.Conditions;
import dev.simpleframework.crud.core.ConditionType;
import dev.simpleframework.crud.core.QueryConditionField;
import dev.simpleframework.crud.core.QueryConditions;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * @author loyayz (loyayz@foxmail.com)
 *
 * @Condition 注解解析测试。
 * 验证 @Condition 和 @Conditions 容器注解的各种用法：
 * 字段映射、条件类型、默认值、同名字段去重、多注解等。
 */
public class ConditionTest {

    /**
     * 场景：用户 POJO 上声明 @Condition 和 @Conditions 注解，
     *       通过 QueryConditions.fromAnnotation() 解析为条件列表。
     * 验证点：
     *   - @Condition 基本映射：字段名 → 条件名，类型默认 equal
     *   - @Condition(field="x") 指定不同字段名
     *   - @Conditions 容器支持同一字段多个条件
     *   - defaultValueIfNull 在字段值为 null 时生效
     *   - 合计 7 个条件被解析
     */
    @Test
    public void testAnnotation() {
        ConditionData data = new ConditionData();
        data.setId(1L);
        QueryConditions conditions = QueryConditions.and().addFromAnnotation(data);
        Assertions.assertEquals(7, conditions.getConditionData().size());

        for (QueryConditionField field : conditions.getFields()) {
            String name = field.getName();
            ConditionType type = field.getType();
            Object value = field.getValue();
            if ("id".equals(name)) {
                // @Condition 默认：字段名=条件名，type=equal，值来自实体字段
                Assertions.assertEquals(ConditionType.equal, type);
                Assertions.assertEquals(data.getId(), value);
            } else if ("ids".equals(name)) {
                // @Condition(field="ids") 重定向字段名，值仍来自原字段
                Assertions.assertEquals(ConditionType.equal, type);
                Assertions.assertEquals(data.getId(), value);
            } else if ("name".equals(name)) {
                // @Condition + @Condition 同名字段去重，fieldSize=2
                Assertions.assertEquals(2, conditions.getFieldSize().get(name));
                Assertions.assertNull(value);
            } else if ("age".equals(name)) {
                // @Condition(type=greater_equal)
                Assertions.assertEquals(ConditionType.greater_equal, type);
                Assertions.assertNull(value);
            } else if ("user_age".equals(name)) {
                // @Conditions + defaultValueIfNull="1"
                Assertions.assertEquals(ConditionType.equal, type);
                Assertions.assertEquals(1, value);
            } else if ("person_age".equals(name)) {
                // @Conditions 第二个条件，字段值为 null 无默认值
                Assertions.assertEquals(ConditionType.equal, type);
                Assertions.assertNull(value);
            } else if ("birthDay".equals(name)) {
                // @Condition(type=not_equal, defaultValueIfNull="1000000")
                Assertions.assertEquals(ConditionType.not_equal, type);
                Assertions.assertEquals(new Date(1000000), value);
            } else {
                throw new IllegalArgumentException(field.toString());
            }
        }
    }

    @Data
    public static class ConditionData {
        @Condition
        private Long id;
        @Condition(field = "name")
        private String userName;
        @Condition(field = "name", type = ConditionType.not_equal)
        private String personName;
        @Condition(type = ConditionType.greater_equal)
        @Conditions({@Condition(field = "user_age", defaultValueIfNull = "1"), @Condition(field = "person_age"),})
        private Integer age;
        @Condition(type = ConditionType.not_equal, defaultValueIfNull = "1000000")
        private Date birthDay;
    }

}
