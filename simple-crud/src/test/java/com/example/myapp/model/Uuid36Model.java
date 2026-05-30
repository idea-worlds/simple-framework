package com.example.myapp.model;

import dev.simpleframework.crud.BaseModel;
import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.annotation.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "t_uuid36")
public class Uuid36Model implements BaseModel<Uuid36Model> {
    @Id(type = Id.Type.UUID36)
    @Column(name = "id", updatable = false)
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "age")
    private Integer age;
}
