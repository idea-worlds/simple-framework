package dev.simpleframework.crud;

import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.core.ModelIdStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public abstract class SimpleModel<T> implements BaseModel<T> {

    @Id(strategy = ModelIdStrategy.SNOWFLAKE)
    private Long id;
    @Column(insertable = false, updatable = false)
    private Date created;
    @Column(insertable = false, updatable = false)
    private Date updated;

}
