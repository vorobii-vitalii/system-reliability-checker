package org.example;

import java.util.List;
import java.util.Map;

public class Graph {
    public static int START = -2;
    public static int END = -1;

    private final Map<Integer, List<Integer>> adjMatrix;

    public Graph(Map<Integer, List<Integer>> adjMatrix) {
        this.adjMatrix = adjMatrix;
    }

    public boolean connected(Elements elements) {
        return connected(START, elements);
    }

    private boolean connected(int node, Elements elements) {
        if (node == END) {
            return true;
        }
        if (node != START && elements.isDisabled(node)) {
            return false;
        }
        var list = adjMatrix.get(node);
        if (list == null) {
            return false;
        }
        for (Integer adjNode : list) {
            if (connected(adjNode, elements)) {
                return true;
            }
        }
        return false;
    }

}
