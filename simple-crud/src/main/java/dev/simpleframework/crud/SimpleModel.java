package dev.simpleframework.crud;

import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.annotation.DataOperateUser;
import dev.simpleframework.crud.annotation.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public abstract class SimpleModel<T> implements BaseModel<T> {

    @Id(type = Id.Type.SNOWFLAKE)
    @Column(updatable = false)
    private Long id;

    @DataOperateUser
    @Column(updatable = false)
    private Long creator;

    @Column(insertable = false, updatable = false)
    private Date createdTime;

    @Column(insertable = false, updatable = false)
    private Date updatedTime;

}
