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
    private Map<String, NodeHelper<T>> nodes;
    private Map<String, List<String>> edges;
    private NodeHelper<T> firstNode;
    private boolean sorted;
    private Supplier<RuntimeException> validHandler;

    public DAG() {
        this.nodes = new LinkedHashMap<>();
        this.edges = new LinkedHashMap<>();
        this.sorted = false;
        this.validHandler = () -> new RuntimeException("不是一个有向无环图");
    }

    /**
     * 添加节点
     */
    public DAG<T> addNode(Node<T> node) {
        this.nodes.put(node.getKey(), new NodeHelper<>(node));
        this.sorted = false;
        return this;
    }

    /**
     * 添加节点
     */
    public DAG<T> addNode(Collection<Node<T>> nodes) {
        nodes.forEach(this::addNode);
        return this;
    }

    /**
     * 添加边（添加完所有节点再添加边）
     *
     * @param fromNodeKey 来源节点
     * @param toNodeKey   目标节点
     */
    public DAG<T> addEdge(String fromNodeKey, String toNodeKey) {
        this.edges.compute(fromNodeKey, (o, tos) -> {
            if (tos == null) {
                tos = new ArrayList<>();
            }
            tos.add(toNodeKey);
            return tos;
        });
        this.sorted = false;
        return this;
    }

    /**
     * 添加边（添加完所有节点再添加边）
     *
     * @param fromNodeKey 来源节点
     * @param toNodeKey   目标节点
     */
    public DAG<T> addEdge(String fromNodeKey, List<String> toNodeKey) {
        toNodeKey.forEach(to -> this.addEdge(fromNodeKey, to));
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
     * @param fromNodeKey 来源节点
     * @param toNodeKey   目标节点
     */
    public DAG<T> removeEdge(String fromNodeKey, String toNodeKey) {
        this.edges.compute(fromNodeKey, (o, tos) -> {
            if (tos == null) {
                return null;
            }
            tos.remove(toNodeKey);
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
     * @param fromNodeKey 来源节点
     * @param toNodeKey   目标节点
     */
    public DAG<T> removeEdge(String fromNodeKey, List<String> toNodeKey) {
        toNodeKey.forEach(to -> this.removeEdge(fromNodeKey, to));
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
     * 获取起始节点
     */
    public Node<T> getStartNode() {
        return this.firstNode == null ? null : this.firstNode.node;
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
    public void visit(Consumer<Node<T>> action) {
        this.sort();
        for (NodeHelper<T> currentNode : this.nodes.values()) {
            List<Node<T>> inNodes = currentNode.inKeys.stream()
                    .map(this.nodes::get)
                    .filter(Objects::nonNull)
                    .map(n -> n.node)
                    .toList();
            List<Node<T>> outNodes = currentNode.outKeys.stream()
                    .map(this.nodes::get)
                    .filter(Objects::nonNull)
                    .map(n -> n.node)
                    .toList();
            Node.Consumer<T> visitHandler = currentNode.node.getVisitHandler();
            if (visitHandler != null) {
                visitHandler.accept(currentNode.node, inNodes, outNodes);
            }
            if (action != null) {
                action.accept(currentNode.node);
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
                NodeHelper<T> toNode = this.nodes.get(toKey);
                node.addNext(toNode);
            });
        });
        this.firstNode = this.nodes.values().stream()
                .filter(node -> node.inKeys.isEmpty())
                .findFirst()
                .orElse(null);
        Deque<NodeHelper<T>> stack = new ArrayDeque<>();
        this.doSort(stack, this.firstNode);
        Map<String, NodeHelper<T>> sortedNodes = new LinkedHashMap<>();
        while (!stack.isEmpty()) {
            NodeHelper<T> node = stack.pop();
            sortedNodes.put(node.node.getKey(), node);
        }
        if (this.nodes.values().stream().anyMatch(node -> !node.visited)) {
            throw this.validHandler.get();
        } else {
            this.nodes = sortedNodes;
        }
        this.sorted = true;
    }

    private void doSort(Deque<NodeHelper<T>> stack, NodeHelper<T> currentNode) {
        if (currentNode == null) {
            return;
        }
        for (String nextKey : currentNode.outKeys) {
            if (currentNode.previousKeys.contains(nextKey)) {
                throw this.validHandler.get();
            }
            NodeHelper<T> nextNode = this.nodes.get(nextKey);
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

    static class NodeHelper<N> {
        String key;
        Node<N> node;
        List<String> inKeys;
        List<String> outKeys;
        Set<String> previousKeys;
        boolean visited;

        NodeHelper(Node<N> node) {
            this.key = node.getKey();
            this.node = node;
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

        void addNext(NodeHelper<N> to) {
            to.inKeys.add(this.key);
            this.outKeys.add(to.key);
        }

    }

}
