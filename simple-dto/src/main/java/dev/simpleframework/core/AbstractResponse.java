package dev.simpleframework.core;

import lombok.Data;

import java.io.Serializable;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public abstract class AbstractResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code = "0";
    private String msg = "";
    private T data;

}
