package dev.simpleframework.crud.helper.strategy;

import dev.simpleframework.crud.annotation.DataOperateDate;
import dev.simpleframework.crud.helper.DataFillStrategy;

import java.util.Date;

@SuppressWarnings("unchecked")
public class DefaultDataOperateDateFillStrategy implements DataFillStrategy {

    @Override
    public <R> R get(Object param) {
        return (R) new Date();
    }

    @Override
    public Class<?> support() {
        return DataOperateDate.class;
    }

    @Override
    public FillType type() {
        return FillType.NULL;
    }

}
