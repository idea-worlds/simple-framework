package com.example.myapp.model;

import dev.simpleframework.crud.SimpleModel;
import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.annotation.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "sys_user")
public class DateModel extends SimpleModel<DateModel> {
    @Column(name = "name")
    private String name;
}
