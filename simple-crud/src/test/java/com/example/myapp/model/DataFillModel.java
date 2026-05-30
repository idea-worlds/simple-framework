package com.example.myapp.model;

import dev.simpleframework.crud.SimpleModel;
import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.annotation.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "t_data_fill")
public class DataFillModel extends SimpleModel<DataFillModel> {
    @Column(name = "name")
    private String name;
}
