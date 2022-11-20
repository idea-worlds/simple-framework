package dev.simpleframework.crud.strategy;

import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.util.Snowflake;

import java.lang.annotation.Annotation;
import java.util.UUID;

public class DefaultIdFillStrategy implements DataFillStrategy {

    @Override
    @SuppressWarnings("unchecked")
    public <R> R get(Object param) {
        if (param instanceof Id.Type) {
            Object result = null;
            switch ((Id.Type) param) {
                case SNOWFLAKE:
                    result = Snowflake.DEFAULT.nextId();
                    break;
                case UUID32:
                    result = UUID.randomUUID().toString().replace("-", "");
                    break;
                case UUID36:
                    result = UUID.randomUUID().toString();
                    break;
                case AUTO_INCREMENT:
                    break;
                default:
            }
            return (R) result;
        }
        return null;
    }

    @Override
    public Class<?> support() {
        return Id.class;
    }

    @Override
    public Object toParam(Annotation annotation) {
        if (annotation instanceof Id) {
            return ((Id) annotation).type();
        }
        return null;
    }

    @Override
    public FillType type() {
        return FillType.NULL;
    }

}
