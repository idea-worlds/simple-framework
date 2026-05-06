package com.example.myapp.model;

import dev.simpleframework.crud.BaseModel;
import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.annotation.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "sys_user_auto")
public class AutoIncrementModel implements BaseModel<AutoIncrementModel> {
    @Id(type = Id.Type.AUTO_INCREMENT)
    @Column(name = "id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private Integer age;
}
