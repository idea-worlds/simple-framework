package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public record ExceptionResponse(int status, String errCode, String errMsg) {
}
