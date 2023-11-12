package dev.simpleframework.token.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Getter
@Setter
public class NotPermissionException extends SimpleTokenException {
    private String permission;

    public NotPermissionException() {
    }

    public NotPermissionException(String permission) {
        super("Not permission: " + permission);
        this.permission = permission;
    }

}
