package org.example;

import guru.nidi.graphviz.model.MutableNode;

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
        // Process incoming
        String currentNode = getP(nodeId);
        StringBuilder builder = new StringBuilder("d" + currentNode + " / dt = ");

        builder.append(incoming.stream()
                .map(edge -> edge.alpha().name() + " * " + getP(edge.node().nodeId))
                .collect(Collectors.joining(" + ")));
        if (!outgoing.isEmpty()) {
            builder.append(outgoing.stream()
                    .map(edge -> edge.alpha().name() + " * " + currentNode)
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

    public double calculateDerivative(double[] arr, double time) {
        double res = 0D;
        for (Edge edge : outgoing) {
            res += edge.alpha().value().negate().doubleValue() * arr[nodeId - 1];
        }
        for (Edge edge : incoming) {
            res += arr[edge.node().nodeId - 1] * edge.alpha().value().doubleValue();
        }
        return res;
    }

    public double rungeKutta(double x0, double y0, double x, double h) {
        // Count number of iterations using step size or
        // step height h
        int n = (int)((x - x0) / h);

        double k1, k2, k3, k4;

        // Iterate for number of iterations
        double y = y0;
        for (int i = 1; i <= n; i++)
        {
            // Apply Runge Kutta Formulas to find
            // next value of y
            k1 = h * (f(x0, y, x0, y0, h));
            k2 = h * (f(x0 + 0.5 * h, y + 0.5 * k1, x0, y0, h));
            k3 = h * (f(x0 + 0.5 * h, y + 0.5 * k2, x0, y0, h));
            k4 = h * (f(x0 + h, y + k3, x0, y0, h));

            // Update next value of y
            y = y + (1.0 / 6.0) * (k1 + 2 * k2 + 2 * k3 + k4);

            // Update next value of x
            x0 = x0 + h;
        }
        return y;
    }

    private double f(double x, double y, double x0, double y0, double h) {
        double res = 0D;
        for (Edge edge : outgoing) {
            res += edge.alpha().value().negate().doubleValue() * y;
        }
        for (Edge edge : incoming) {
            res += rungeKutta(x0, y0, x, h) * edge.alpha().value().doubleValue();
        }
        return res;
    }

}
