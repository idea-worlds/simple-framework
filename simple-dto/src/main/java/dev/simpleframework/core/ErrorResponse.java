package dev.simpleframework.core;

import lombok.Data;

import java.io.Serializable;

@Data
public class ErrorResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String errCode;
    private String errMsg;

    public static ErrorResponse of(String code, String msg) {
        ErrorResponse response = new ErrorResponse();
        response.errCode = code;
        response.errMsg = msg;
        return response;
    }

}
