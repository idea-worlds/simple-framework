package dev.simpleframework.dag;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 有向无环图
 *
 * @author loyayz
 **/
public class DAG<T> {
    private Map<String, Node<T>> nodes;
    private Map<String, List<String>> edges;
    private Node<T> first;
    private boolean sorted;
    private Supplier<RuntimeException> validHandler;

    public DAG() {
        this.nodes = new LinkedHashMap<>();
        this.edges = new LinkedHashMap<>();
        this.sorted = false;
        this.validHandler = () -> new RuntimeException("不是一个有向无环图");
    }

    /**
     * 添加顶点
     */
    public DAG<T> addVertex(Vertex<T> vertex) {
        this.nodes.put(vertex.getKey(), new Node<>(vertex));
        this.sorted = false;
        return this;
    }

    /**
     * 添加顶点
     */
    public DAG<T> addVertex(Collection<Vertex<T>> vertex) {
        vertex.forEach(this::addVertex);
        return this;
    }

    /**
     * 添加边（添加完所有顶点再添加边）
     *
     * @param fromVertexKey 来源顶点
     * @param toVertexKey   目标顶点
     */
    public DAG<T> addEdge(String fromVertexKey, String toVertexKey) {
        this.edges.compute(fromVertexKey, (o, tos) -> {
            if (tos == null) {
                tos = new ArrayList<>();
            }
            tos.add(toVertexKey);
            return tos;
        });
        this.sorted = false;
        return this;
    }

    /**
     * 添加边（添加完所有顶点再添加边）
     *
     * @param fromVertexKey 来源顶点
     * @param toVertexKey   目标顶点
     */
    public DAG<T> addEdge(String fromVertexKey, List<String> toVertexKey) {
        toVertexKey.forEach(to -> this.addEdge(fromVertexKey, to));
        return this;
    }

    /**
     * 删除所有边
     */
    public DAG<T> cleanEdge() {
        this.edges.clear();
        this.sorted = false;
        return this;
    }

    /**
     * 删除边
     *
     * @param fromVertexKey 来源顶点
     * @param toVertexKey   目标顶点
     */
    public DAG<T> removeEdge(String fromVertexKey, String toVertexKey) {
        this.edges.compute(fromVertexKey, (o, tos) -> {
            if (tos == null) {
                return null;
            }
            tos.remove(toVertexKey);
            if (tos.isEmpty()) {
                return null;
            }
            return tos;
        });
        this.sorted = false;
        return this;
    }

    /**
     * 删除边
     *
     * @param fromVertexKey 来源顶点
     * @param toVertexKey   目标顶点
     */
    public DAG<T> removeEdge(String fromVertexKey, List<String> toVertexKey) {
        toVertexKey.forEach(to -> this.removeEdge(fromVertexKey, to));
        return this;
    }

    /**
     * 设置校验回调
     */
    public DAG<T> validHandler(Supplier<RuntimeException> handler) {
        this.validHandler = handler;
        return this;
    }

    /**
     * 获取起始顶点
     */
    public Vertex<T> getStartVertex() {
        return this.first == null ? null : this.first.vertex;
    }

    /**
     * 是否为有向无环图
     */
    public boolean check() {
        try {
            this.sort();
        } catch (Exception ignore) {
        }
        return this.sorted;
    }

    /**
     * 校验是否为有向无环图
     */
    public void valid() {
        if (this.check()) {
            return;
        }
        throw this.validHandler.get();
    }

    /**
     * 拓扑排序后按顺序访问
     */
    public void visit() {
        this.visit(null);
    }

    /**
     * 拓扑排序后按顺序访问
     */
    public void visit(Consumer<Vertex<T>> action) {
        this.sort();
        for (Node<T> node : this.nodes.values()) {
            Vertex<T> currentVertex = node.vertex;
            List<Vertex<T>> inVertex = node.inKeys.stream()
                    .map(this.nodes::get)
                    .filter(Objects::nonNull)
                    .map(n -> n.vertex)
                    .toList();
            List<Vertex<T>> outVertex = node.outKeys.stream()
                    .map(this.nodes::get)
                    .filter(Objects::nonNull)
                    .map(n -> n.vertex)
                    .toList();
            currentVertex.visit(inVertex, outVertex);
            if (action != null) {
                action.accept(currentVertex);
            }
        }
    }

    /**
     * 拓扑排序
     */
    public synchronized void sort() {
        if (this.nodes.isEmpty() || this.sorted) {
            return;
        }
        this.nodes.forEach((key, node) -> node.reset());
        this.nodes.forEach((key, node) -> {
            this.edges.getOrDefault(key, Collections.emptyList()).forEach(toKey -> {
                Node<T> toNode = this.nodes.get(toKey);
                node.addNext(toNode);
            });
        });
        this.first = this.nodes.values().stream()
                .filter(node -> node.inKeys.isEmpty())
                .findFirst()
                .orElse(null);
        Deque<Node<T>> stack = new ArrayDeque<>();
        this.doSort(stack, this.first);
        Map<String, Node<T>> sortedNodes = new LinkedHashMap<>();
        while (!stack.isEmpty()) {
            Node<T> node = stack.pop();
            Vertex<T> vertex = node.vertex;
            sortedNodes.put(vertex.getKey(), node);
        }
        if (this.nodes.values().stream().anyMatch(node -> !node.visited)) {
            throw this.validHandler.get();
        } else {
            this.nodes = sortedNodes;
        }
        this.sorted = true;
    }

    private void doSort(Deque<Node<T>> stack, Node<T> currentNode) {
        if (currentNode == null) {
            return;
        }
        for (String nextKey : currentNode.outKeys) {
            if (currentNode.previousKeys.contains(nextKey)) {
                throw this.validHandler.get();
            }
            Node<T> nextNode = this.nodes.get(nextKey);
            nextNode.previousKeys.addAll(currentNode.previousKeys);
            nextNode.previousKeys.add(currentNode.key);
            if (!nextNode.visited) {
                this.doSort(stack, nextNode);
                nextNode.visited = true;
            }
        }
        currentNode.visited = true;
        stack.push(currentNode);
    }

    static class Node<N> {
        String key;
        Vertex<N> vertex;
        List<String> inKeys;
        List<String> outKeys;
        Set<String> previousKeys;
        boolean visited;

        Node(Vertex<N> vertex) {
            this.key = vertex.getKey();
            this.vertex = vertex;
            this.inKeys = new ArrayList<>();
            this.outKeys = new ArrayList<>();
            this.previousKeys = new HashSet<>();
            this.visited = false;
        }

        void reset() {
            this.inKeys.clear();
            this.outKeys.clear();
            this.previousKeys.clear();
            this.visited = false;
        }

        void addNext(Node<N> to) {
            to.inKeys.add(this.key);
            this.outKeys.add(to.key);
        }

    }

}
