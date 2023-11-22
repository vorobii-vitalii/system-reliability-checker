package org.reliability.task2;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableNode;
import org.reliability.dto.Alpha;
import org.reliability.graph.Graph;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class GraphGenerateTest {

    public static final int INITIAL_NODE_ID = 1;

    public static void main(String[] args) throws IOException {
        var visitedStates = new HashSet<RecoverableElements>();
        var queue = new LinkedList<RecoverableElements>();
        // Data
        queue.add(new RecoverableElements(
                List.of(
                    new RecoverableElement(Map.of(
                            ComponentKind.HARDWARE, new ComponentState(
                                    new Alpha(BigDecimal.valueOf(0.001), "hardware_recovery"),
                                    new Alpha(BigDecimal.valueOf(0.001), "hardware_error"),
                                    new InfiniteComponentFailState(),
                                    false,
                                    false
                            ),
                            ComponentKind.SOFTWARE, new ComponentState(
                                    new Alpha(BigDecimal.valueOf(0.002), "software_recovery"),
                                    new Alpha(BigDecimal.valueOf(0.003), "software_error"),
                                    new LimitedComponentFailState(1),
                                    false,
                                    false
                            )
                    ))
                )));
        // Graph
        Map<Integer, List<Integer>> graph = Map.of(
                Graph.START, List.of(0),
                0, List.of(Graph.END)
        );
        var nodeIdProvider = new AtomicInteger(INITIAL_NODE_ID);
        var nodeByElements = new HashMap<RecoverableElements, GraphNode>();
        var systemStateChecker = new SystemStateChecker(graph);
        while (!queue.isEmpty()) {
            var elements = queue.pollLast();

            // Make sure same node not processed twice
            if (visitedStates.contains(elements)) {
                continue;
            }
            visitedStates.add(elements);

            // Update maps
            var currentGraphNode = nodeByElements.computeIfAbsent(elements, getGraphNodeCreator(nodeIdProvider));

            var elementsTransitions = elements.calculateTransitions(systemStateChecker);
            for (var elementsTransition : elementsTransitions) {
                var alpha = elementsTransition.alpha();
                var targetElements = elementsTransition.targetState();
                var targetNode = nodeByElements.computeIfAbsent(targetElements, getGraphNodeCreator(nodeIdProvider));
                currentGraphNode.addOutgoingEdge(new GraphNode.GraphEdge(alpha, targetNode));
                targetNode.addIncomingEdge(new GraphNode.GraphEdge(alpha, currentGraphNode));
                queue.add(targetElements);
            }
        }
        nodeByElements.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(a -> a.getValue().getNodeId()))
                .map(e -> e.getValue().getDifferentialEquation())
                .forEach(System.out::println);
        generateGraphImage(nodeByElements, systemStateChecker);
    }

    private static void generateGraphImage(
            HashMap<RecoverableElements, GraphNode> nodeByElements,
            SystemStateChecker systemStateChecker
    ) throws IOException {
        var mutableGraph = mutGraph("Lab 2")
                .setDirected(true)
                .graphAttrs()
                .add(Rank.sep(2D));
        var mutableNodeById = new HashMap<Integer, MutableNode>();
        for (var entry : nodeByElements.entrySet()) {
            var recoverableElements = entry.getKey();
            var graphNode = entry.getValue();
            var nodeId = graphNode.getNodeId();
            var mutableNode = mutableNodeById.computeIfAbsent(nodeId, id -> {
                var stateCheckResult = systemStateChecker.checkSystemState(recoverableElements);
                return mutNode(Label.html("<b>" + id + "</b> <i>" + stateCheckResult + "</i>"))
                        .add(calculateColorByStateCheckResult(stateCheckResult).font())
                        .add(Color.BLACK.background());
            });
            mutableGraph.add(mutableNode);
            for (var outgoingEdge : graphNode.getOutgoingEdges()) {
                var node = outgoingEdge.graphNode();
                var connectedMutableNode = mutableNodeById.computeIfAbsent(node.getNodeId(), id -> {
                    var stateCheckResult = systemStateChecker.checkSystemState(node.getRecoverableElements());
                    return mutNode(Label.html(
                            "<b>" + id + "</b> " +
                                    "<i>" + stateCheckResult + "</i> "
                    ))
                            .add(calculateColorByStateCheckResult(stateCheckResult).font())
                            .add(Color.BLACK.background());
                });
                var edge = mutableNode.linkTo(connectedMutableNode)
                        .with(Color.GREEN)
                        .with(Color.YELLOW.labelFont())
                        .with(Style.BOLD)
                        .with(Font.size(18))
                        .with(Label.html(outgoingEdge.alpha().name()));
                mutableNode.addLink(edge);
            }
        }
        Graphviz.fromGraph(mutableGraph)
                .render(Format.PNG)
                .toFile(new File("output_graph.png"));
    }

    private static Color calculateColorByStateCheckResult(SystemStateCheckResult systemStateCheckResult) {
        return switch (systemStateCheckResult) {
            case TERMINATED -> Color.RED;
            case WORKING -> Color.GREEN;
            case RECOVERABLE -> Color.GOLD;
        };
    }

    private static Function<RecoverableElements, GraphNode> getGraphNodeCreator(
            AtomicInteger nodeIdProvider
    ) {
        return recoverableElements -> new GraphNode(nodeIdProvider.getAndIncrement(), recoverableElements);
    }

}
