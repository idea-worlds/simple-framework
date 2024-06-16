package dev.simpleframework.dag;

import lombok.Getter;

import java.util.List;

/**
 * 有向无环图节点
 *
 * @author loyayz
 **/
@Getter
public class Node<T> {
    private final String key;
    private final T value;
    private Consumer<T> visitHandler;

    public Node(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public Node<T> setVisitHandler(Consumer<T> handler) {
        this.visitHandler = handler;
        return this;
    }

    public interface Consumer<C> {
        /**
         * @param current 当前节点
         * @param froms   来源节点 not null
         * @param tos     目标节点 not null
         */
        void accept(Node<C> current, List<Node<C>> froms, List<Node<C>> tos);
    }

}
