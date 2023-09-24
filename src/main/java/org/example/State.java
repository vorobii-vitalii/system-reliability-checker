package org.example;

import java.util.ArrayList;
import java.util.List;

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
