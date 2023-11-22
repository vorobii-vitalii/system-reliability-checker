package org.reliability.graph;

import guru.nidi.graphviz.model.MutableNode;
import org.reliability.dto.State;

import java.util.*;
import java.util.stream.Collectors;

public final class Node {
    private final int nodeId;
    private final List<Edge> incoming = new ArrayList<>();
    private final List<Edge> outgoing = new ArrayList<>();
    private final MutableNode mutableNode;
    private final State state;

    public Node(int nodeId, MutableNode mutableNode, State state) {
        this.nodeId = nodeId;
        this.mutableNode = mutableNode;
        this.state = state;
    }

    public boolean isValidState() {
        return state.isValid();
    }

    public void addIncoming(Edge edge) {
        incoming.add(edge);
    }

    public void addOutgoing(Edge edge) {
        outgoing.add(edge);
    }

    public MutableNode getMutableNode() {
        return mutableNode;
    }

    public String getAsDiffEquation() {
        String currentNode = getP(nodeId);
        StringBuilder builder = new StringBuilder("d" + currentNode + " / dt = ");

        builder.append(incoming.stream()
                .map(edge -> edge.alpha().name() + edge.node().getNodeId() + " * " + getP(edge.node().nodeId))
                .collect(Collectors.joining(" + ")));
        if (!outgoing.isEmpty()) {
            builder.append(outgoing.stream()
                    .map(edge -> edge.alpha().name() + edge.node().getNodeId() + " * " + currentNode)
                    .collect(Collectors.joining(" - ", " - ", "")));
        }
        return builder.toString();
    }

    public int getNodeId() {
        return nodeId;
    }

    private String getP(int nodeId) {
        return "P" + nodeId + "(t)";
    }

    public double calculateDerivative(double[] arr) {
        double res = 0D;
        for (Edge edge : outgoing) {
            res += edge.alpha().value().negate().doubleValue() * arr[nodeId - 1];
        }
        for (Edge edge : incoming) {
            res += arr[edge.node().nodeId - 1] * edge.alpha().value().doubleValue();
        }
        return res;
    }

}
