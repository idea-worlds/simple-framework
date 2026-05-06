package com.example.multids.model;

import dev.simpleframework.crud.BaseModel;
import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.annotation.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "user_info")
public class SecondDsUserModel implements BaseModel<SecondDsUserModel> {
    @Id
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private Integer age;
}
