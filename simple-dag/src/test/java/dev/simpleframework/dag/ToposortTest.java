package dev.simpleframework.dag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author loyayz
 **/
public class ToposortTest {

    @Test
    public void test() {
        Vertex.Consumer<String> visitHandler = (current, froms, tos) -> {
            String toKeys = tos.stream().map(Vertex::getKey).collect(Collectors.joining(","));
            System.out.println("  " + current.getKey() + " --> " + toKeys);
        };
        DAG<String> dag = new DAG<String>()
                .addVertex(new StringVertex("2").setVisitHandler(visitHandler))
                .addVertex(new StringVertex("3").setVisitHandler(visitHandler))
                .addVertex(new StringVertex("9").setVisitHandler(visitHandler))
                .addVertex(new StringVertex("5").setVisitHandler(visitHandler))
                .addVertex(new StringVertex("8").setVisitHandler(visitHandler))
                .addVertex(new StringVertex("4").setVisitHandler(visitHandler))
                .addVertex(new StringVertex("1").setVisitHandler(visitHandler))
                .addVertex(new StringVertex("7").setVisitHandler(visitHandler))
                .addVertex(new StringVertex("6").setVisitHandler(visitHandler))
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

    public static class StringVertex extends Vertex<String> {

        public StringVertex(String key) {
            super(key, key);
        }

    }

}
