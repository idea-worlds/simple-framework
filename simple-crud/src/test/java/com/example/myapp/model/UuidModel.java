package com.example.myapp.model;

import dev.simpleframework.crud.BaseModel;
import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.annotation.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "sys_user_uuid")
public class UuidModel implements BaseModel<UuidModel> {
    @Id(type = Id.Type.UUID32)
    @Column(name = "id", updatable = false)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private Integer age;
}
