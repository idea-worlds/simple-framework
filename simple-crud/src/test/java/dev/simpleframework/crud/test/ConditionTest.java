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
import java.util.List;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class ConditionTest {

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
                Assertions.assertEquals(ConditionType.equal, type);
                Assertions.assertEquals(data.getId(), value);
            } else if ("ids".equals(name)) {
                Assertions.assertEquals(ConditionType.equal, type);
                Assertions.assertEquals(data.getId(), value);
            } else if ("name".equals(name)) {
                Assertions.assertEquals(2, conditions.getFieldSize().get(name));
                Assertions.assertNull(value);
            } else if ("age".equals(name)) {
                Assertions.assertEquals(ConditionType.great_equal, type);
                Assertions.assertNull(value);
            } else if ("user_age".equals(name)) {
                Assertions.assertEquals(ConditionType.equal, type);
                Assertions.assertEquals(1, value);
            } else if ("person_age".equals(name)) {
                Assertions.assertEquals(ConditionType.equal, type);
                Assertions.assertNull(value);
            } else if ("birthDay".equals(name)) {
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
        @Condition(type = ConditionType.great_equal)
        @Conditions({@Condition(field = "user_age", defaultValueIfNull = "1"), @Condition(field = "person_age"),})
        private Integer age;
        @Condition(type = ConditionType.not_equal, defaultValueIfNull = "1000000")
        private Date birthDay;
    }

}
