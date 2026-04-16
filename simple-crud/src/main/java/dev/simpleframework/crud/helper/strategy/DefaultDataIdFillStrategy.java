package dev.simpleframework.crud.helper.strategy;

import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.helper.DataFillStrategy;
import dev.simpleframework.util.Snowflake;
import dev.simpleframework.util.Strings;

import java.lang.annotation.Annotation;

@SuppressWarnings("unchecked")
public class DefaultDataIdFillStrategy implements DataFillStrategy {

    @Override
    public <R> R get(Object param) {
        if (param instanceof Id.Type) {
            Object result = null;
            switch ((Id.Type) param) {
                case SNOWFLAKE:
                    result = Snowflake.DEFAULT.nextId();
                    break;
                case UUID32:
                    result = Strings.uuid32();
                    break;
                case UUID36:
                    result = Strings.uuid();
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
