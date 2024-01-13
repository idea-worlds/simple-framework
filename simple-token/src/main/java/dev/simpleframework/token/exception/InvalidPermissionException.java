package dev.simpleframework.token.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Getter
@Setter
public class InvalidPermissionException extends SimpleTokenException {
    private String permission;
    private Boolean has;

    public InvalidPermissionException() {
    }

    public InvalidPermissionException(String message) {
        super(message);
    }

    public InvalidPermissionException(String permission, Boolean has) {
        super(has ? "Has permission " : "Not permission" + permission);
        this.permission = permission;
        this.has = has;
    }

}
