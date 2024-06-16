package dev.simpleframework.dag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

/**
 * @author loyayz
 **/
public class ToposortTest {

    @Test
    public void test() {
        Node.Consumer<String> visitHandler = (current, froms, tos) -> {
            String toKeys = tos.stream().map(Node::getKey).collect(Collectors.joining(","));
            System.out.println("  " + current.getKey() + " --> " + toKeys);
        };
        DAG<String> dag = new DAG<String>()
                .addNode(new StringNode("2").setVisitHandler(visitHandler))
                .addNode(new StringNode("3").setVisitHandler(visitHandler))
                .addNode(new StringNode("9").setVisitHandler(visitHandler))
                .addNode(new StringNode("5").setVisitHandler(visitHandler))
                .addNode(new StringNode("8").setVisitHandler(visitHandler))
                .addNode(new StringNode("4").setVisitHandler(visitHandler))
                .addNode(new StringNode("1").setVisitHandler(visitHandler))
                .addNode(new StringNode("7").setVisitHandler(visitHandler))
                .addNode(new StringNode("6").setVisitHandler(visitHandler))
                .addEdge("1", "2")
                .addEdge("2", "3")
                .addEdge("3", "4")
                .addEdge("4", "5")
                .addEdge("5", "6")
                .addEdge("7", "8")
                .addEdge("8", "9");
        Assertions.assertFalse(dag.check());
        dag.addEdge("6", "7");
        Assertions.assertTrue(dag.check());
        System.out.println("========================");
        dag.visit();
        System.out.println("========================");

        dag.cleanEdge();
        Assertions.assertFalse(dag.check());

        /**
         * 1 →→→→ 3 →→→→          →→→→ 9
         * ↓      ↑      ↓      ↑
         * ↓      ↑      ↓      ↑
         *   →→→→ 4 →→→→ 2 →→→→ 5 →→→→ 6
         *                      ↓      ↑
         *                      ↓      ↑
         *                      7 →→→→ 8
         */
        dag.addEdge("1", "3")
                .addEdge("1", "4")
                .addEdge("2", "5")
                .addEdge("3", "2")
                .addEdge("4", "2")
                .addEdge("4", "3")
                .addEdge("5", "6")
                .addEdge("5", "7")
                .addEdge("5", "9")
                .addEdge("7", "8")
                .addEdge("8", "6");
        Assertions.assertTrue(dag.check());
        dag.visit();
    }

    public static class StringNode extends Node<String> {

        public StringNode(String key) {
            super(key, key);
        }

    }

}
