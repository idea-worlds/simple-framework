package dev.simpleframework.token.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Getter
@Setter
public class InvalidRoleException extends SimpleTokenException {
    private String role;
    private Boolean has;

    public InvalidRoleException() {
    }

    public InvalidRoleException(String role, Boolean has) {
        super(has ? "Has role " : "Not role" + role);
        this.role = role;
        this.has = has;
    }

}
