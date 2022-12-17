package dev.simpleframework.crud;

import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.annotation.DataOperateDate;
import dev.simpleframework.crud.annotation.DataOperateUser;
import dev.simpleframework.crud.annotation.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class SimpleModel<T> implements BaseModel<T> {

    @Id(type = Id.Type.SNOWFLAKE)
    @Column(updatable = false)
    private Long id;

    @DataOperateUser
    @Column(updatable = false)
    private Long createUser;

    @DataOperateDate
    @Column(updatable = false)
    private Date createdTime;

    @DataOperateDate
    private Date updatedTime;

}
