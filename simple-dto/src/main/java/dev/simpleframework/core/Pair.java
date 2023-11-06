package dev.simpleframework.core;

import lombok.Data;

import java.io.Serializable;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class Pair<L, R> implements Serializable {
    private static final long serialVersionUID = 1L;

    private L left;
    private R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }
}
