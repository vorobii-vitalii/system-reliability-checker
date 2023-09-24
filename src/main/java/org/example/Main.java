package org.example;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class Main {
    private static final AtomicInteger nodeId = new AtomicInteger(1);
    public static final String LAB_1 = "Lab1";

    public static void main(String[] args) throws IOException {
        int elements = 5;
//        int elements = 3;
        Map<Integer, List<Integer>> adjMatrix = Map.of(
            Graph.START, List.of(0, 2),
            0, List.of(1),
            1, List.of(3, 4),
            2, List.of(3, 4),
            3, List.of(Graph.END),
            4, List.of(Graph.END)
        );
//        Map<Integer, List<Integer>> adjMatrix = Map.of(
//                Graph.START, List.of(0),
//                0, List.of(1, 2),
//                1, List.of(Graph.END),
//                2, List.of(Graph.END)
//        );
//        λАЗ1=5∙10-4
//        год-1
//                , λАЗ2=4∙10-4
//        год-1
//                ,
//                λАЗ3=3∙10-4
//        год-1
//                , λАЗ4=2.5∙10-4
//        год-1
//                , λАЗ5=5∙10-4
//        год-1
//                , λПЗ1=5∙10-4
//        год-1
//                , λПЗ2=4∙10-4
//        год-1
//                , λПЗ3=3∙10-4
//        год-1
//                , λПЗ4=2∙10-4
//        год-1
//                , λПЗ5=1∙10-4
//        год-1
//
        Map<Integer, List<Alpha>> failAlphasByElement = Map.of(
                0, List.of(new Alpha(new BigDecimal("0.0005"), "a1az"), new Alpha(new BigDecimal("0.0005"), "a1pz")),
                1, List.of(new Alpha(new BigDecimal("0.0004"), "a2az")),
                2, List.of(new Alpha(new BigDecimal("0.0003"), "a3az"), new Alpha(new BigDecimal("0.0003"), "a1pz")),
                3, List.of(new Alpha(new BigDecimal("0.00025"), "a4az")),
                4, List.of(new Alpha(new BigDecimal("0.0005"), "a5az"))
        );

//        Map<Integer, List<Alpha>> failAlphasByElement = Map.of(
//                0, List.of(new Alpha(new BigDecimal("0.0005"), "a1az")),
//                1, List.of(new Alpha(new BigDecimal("0.0004"), "a2az")),
//                2, List.of(new Alpha(new BigDecimal("0.0003"), "a3az"))
//        );

        Queue<State> stateQueue = new LinkedList<>();
        Graph graph = new Graph(adjMatrix);
        stateQueue.add(new State(
                graph,
                Elements.createEmpty(elements)
        ));
        Set<Elements> visited = new HashSet<>();

        MutableGraph g = mutGraph(LAB_1)
                .setDirected(true)
                .graphAttrs()
                .add(Rank.sep(2D));

        Map<String, Node> nodeById = new HashMap<>();

        // пошук в ширину
        while (!stateQueue.isEmpty()) {
            var state = stateQueue.poll();
            if (visited.contains(state.elements())) {
                continue;
            }
            visited.add(state.elements());
            var node = createIfNeeded(nodeById, state);
            if (state.isValid()) {
                node.getMutableNode().add(Color.GREEN);
                for (int i = 0; i < elements; i++) {
                    if (state.elements().isDisabled(i)) {
                        continue;
                    }
                    int finalI = i;
                    failAlphasByElement.get(i).forEach(alpha -> {
                        var newElements = state.elements().breakAt(finalI, alpha.value());
                        var newState = new State(graph, newElements);
                        var connectedNode = createIfNeeded(nodeById, newState);

                        node.addOutgoing(new Edge(connectedNode, alpha));
                        connectedNode.addIncoming(new Edge(node, alpha));

                        Link edge = node.getMutableNode().linkTo(connectedNode.getMutableNode())
                                .with(Color.BLACK)
                                .with(Label.html("&#945; = " + alpha.value()));
                        node.getMutableNode().addLink(edge);
                        stateQueue.add(newState);
                    });
                }
            }
            else {
                node.getMutableNode().add(Color.RED);
                System.out.println("State " + state + " invalid!");
            }
            g.add(node.getMutableNode());
        }

        System.out.println("Генерую граф...");
        File output = new File("example/" + LAB_1 + "_Graph.png");
        Graphviz.fromGraph(g)
                .render(Format.PNG)
                .toFile(output);
        System.out.println("Граф збережено в ... " + output);

        System.out.println("Відображаю систему диф рівнянь...");

        nodeById.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(a -> a.getValue().getNodeId()))
                .forEach(e -> {
                    System.out.println(e.getValue().getAsDiffEquation());
                });
        System.out.println("Система диф рівнянь відображена");

        int x0 = 0;
        int y0 = 1;
        double t = 33;
        double h = 0.0001;

        // 33 = 0.9989805075250527
        // 66 = 0.9960438183818878

        System.out.println("Рахую ймовірності");

        int nodes = nodeById.size();
        Map<Integer, Node> map = nodeById.entrySet().stream().collect(Collectors.toMap(e -> e.getValue().getNodeId() - 1, Map.Entry::getValue));

        double[] constantArray = createConstantArray(nodes, 0);
        constantArray[0] = y0;
        double[] res = RungeKutta.solve(constantArray, x0, t, h, (x, t1) -> {
            double[] res1 = new double[nodes];
            for (int i = 0; i < nodes; i++) {
                res1[i] = map.get(i).calculateDerivative(x, t1);
            }
            return res1;
        });
        double sum = Arrays.stream(res).sum();
        System.out.println("Початкова сума = " + sum);
        double sumValid = 0;
        for (int i = 0; i < res.length; i++) {
            boolean validState = map.get(i).isValidState();
            System.out.println("Ймовірність стану " + (i + 1) + " валідний = " + validState + " = " + res[i]);
            if (validState) {
                sumValid += res[i];
            }
        }
        System.out.println("Ймовірність того що система буде в валідному стані = " + sumValid);
    }

    private static double[] createConstantArray(int n, double c) {
        double[] arr = new double[n];
        Arrays.fill(arr, c);
        return arr;
    }

    private static Node createIfNeeded(Map<String, Node> map, State state) {
        return map.computeIfAbsent(state.elements().calcId(),
                s -> {
                    var stateDescription = state.elements().serialize();
                    var stateId = nodeId.getAndIncrement();
                    var nodeName = Label.html("<b>" + stateId + "</b> <i>" + stateDescription + "</i>");
                    return new Node(stateId, mutNode(nodeName), state);
                });
    }

}
