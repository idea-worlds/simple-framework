package dev.simpleframework.dag;

import lombok.Getter;

import java.util.List;

/**
 * 有向无环图节点
 *
 * @author loyayz
 **/
@Getter
public class DAGNode<T> {
    private final String key;
    private final T value;
    private Consumer<T> visitHandler;

    public DAGNode(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public DAGNode<T> setVisitHandler(Consumer<T> handler) {
        this.visitHandler = handler;
        return this;
    }

    public interface Consumer<V> {
        /**
         * @param current 当前节点
         * @param froms   来源节点 not null
         * @param tos     目标节点 not null
         */
        void accept(V current, List<V> froms, List<V> tos);
    }

}
