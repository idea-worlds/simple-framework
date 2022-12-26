package dev.simpleframework.crud.test;

import dev.simpleframework.crud.annotation.Condition;
import dev.simpleframework.crud.annotation.Conditions;
import dev.simpleframework.crud.core.ConditionType;
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
        QueryConditions conditions = QueryConditions.of().addFromAnnotation(data);
        Assertions.assertEquals(7, conditions.getConditionData().size());

        for (Map.Entry<String, List<QueryConditions.ConditionInfo>> entry : conditions.getConditions().entrySet()) {
            String fieldName = entry.getKey();
            QueryConditions.ConditionInfo condition = entry.getValue().get(0);
            if ("id".equals(fieldName)) {
                Assertions.assertEquals(ConditionType.equal, condition.getType());
                Assertions.assertEquals(data.getId(), condition.getValue());
            } else if ("ids".equals(fieldName)) {
                Assertions.assertEquals(ConditionType.equal, condition.getType());
                Assertions.assertEquals(data.getId(), condition.getValue());
            } else if ("name".equals(fieldName)) {
                Assertions.assertEquals(2, entry.getValue().size());
                for (QueryConditions.ConditionInfo info : entry.getValue()) {
                    Assertions.assertNull(info.getValue());
                }
            } else if ("age".equals(fieldName)) {
                Assertions.assertEquals(ConditionType.great_equal, condition.getType());
                Assertions.assertNull(condition.getValue());
            } else if ("user_age".equals(fieldName)) {
                Assertions.assertEquals(ConditionType.equal, condition.getType());
                Assertions.assertEquals(1, condition.getValue());
            } else if ("person_age".equals(fieldName)) {
                Assertions.assertEquals(ConditionType.equal, condition.getType());
                Assertions.assertNull(condition.getValue());
            } else if ("birthDay".equals(fieldName)) {
                Assertions.assertEquals(ConditionType.not_equal, condition.getType());
                Assertions.assertEquals(new Date(1000000), condition.getValue());
            } else {
                throw new IllegalArgumentException("");
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
