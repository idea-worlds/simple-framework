package dev.simpleframework.dag;

import lombok.Getter;

import java.util.List;

/**
 * 有向无环图顶点
 *
 * @author loyayz
 **/
@Getter
public class Vertex<T> {
    private final String key;
    private final T value;
    private Consumer<T> visitHandler;

    public Vertex(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public Vertex<T> setVisitHandler(Consumer<T> handler) {
        this.visitHandler = handler;
        return this;
    }

    void visit(List<Vertex<T>> inVertex, List<Vertex<T>> outVertex) {
        if (this.visitHandler == null) {
            return;
        }
        this.visitHandler.accept(this, inVertex, outVertex);
    }

    public interface Consumer<C> {
        void accept(Vertex<C> current, List<Vertex<C>> froms, List<Vertex<C>> tos);
    }

}
