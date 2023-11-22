package org.reliability.dto;

import org.reliability.graph.Graph;

public record State(Graph graph, Elements elements) {
    public String serialize() {
        return elements.serialize();
    }

    @Override
    public String toString() {
        return "State{" + "mask=" + serialize() + '}';
    }
    public boolean isValid() {
        return graph.connected(elements);
    }
}
