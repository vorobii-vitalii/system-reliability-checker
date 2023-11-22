package org.reliability.task2;

import org.reliability.dto.Alpha;
import org.reliability.graph.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GraphNode {
    private final int nodeId;
    private final List<GraphEdge> incomingEdges = new ArrayList<>();
    private final List<GraphEdge> outgoingEdges = new ArrayList<>();
    private final RecoverableElements recoverableElements;

    public GraphNode(int nodeId, RecoverableElements recoverableElements) {
        this.nodeId = nodeId;
        this.recoverableElements = recoverableElements;
    }

    public String getDifferentialEquation() {
        String currentNode = getP(nodeId);
        StringBuilder builder = new StringBuilder("d" + currentNode + " / dt = ");

        builder.append(incomingEdges.stream()
                .map(edge -> edge.alpha().name() + edge.graphNode().getNodeId() + " * " + getP(edge.graphNode().nodeId))
                .collect(Collectors.joining(" + ")));
        if (!outgoingEdges.isEmpty()) {
            builder.append(outgoingEdges.stream()
                    .map(edge -> edge.alpha().name() + edge.graphNode().getNodeId() + " * " + currentNode)
                    .collect(Collectors.joining(" - ", " - ", "")));
        }
        return builder.toString();
    }

    public double calculateDerivative(double[] arr) {
        double res = 0D;
        for (var edge : outgoingEdges) {
            res += edge.alpha().value().negate().doubleValue() * arr[nodeId - 1];
        }
        for (var edge : incomingEdges) {
            res += arr[edge.graphNode().nodeId - 1] * edge.alpha().value().doubleValue();
        }
        return res;
    }

    private String getP(int nodeId) {
        return "P" + nodeId + "(t)";
    }

    public RecoverableElements getRecoverableElements() {
        return recoverableElements;
    }

    public int getNodeId() {
        return nodeId;
    }

    public List<GraphEdge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public void addIncomingEdge(GraphEdge edge) {
        incomingEdges.add(edge);
    }

    public void addOutgoingEdge(GraphEdge edge) {
        outgoingEdges.add(edge);
    }

    @Override
    public String toString() {
        return "GraphNode{" +
                "nodeId=" + nodeId +
                ", incomingEdges=" + incomingEdges.stream().map(e -> "{" + e.graphNode.getNodeId() + " " + e.alpha + "}").collect(Collectors.joining(", ", "[", "]")) +
                ", outgoingEdges=" + outgoingEdges.stream().map(e -> "{" + e.graphNode.getNodeId() + " " + e.alpha + "}").collect(Collectors.joining(", ", "[", "]")) +
                '}';
    }

    public record GraphEdge(Alpha alpha, GraphNode graphNode) {
    }

}
