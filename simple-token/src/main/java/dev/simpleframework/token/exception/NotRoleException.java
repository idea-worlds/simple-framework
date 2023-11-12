package dev.simpleframework.token.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Getter
@Setter
public class NotRoleException extends SimpleTokenException {
    private String role;

    public NotRoleException() {
    }

    public NotRoleException(String role) {
        super("Not role: " + role);
        this.role = role;
    }

}
