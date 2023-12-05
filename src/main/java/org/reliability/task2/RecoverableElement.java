package org.reliability.task2;

import org.reliability.dto.Alpha;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record RecoverableElement(Map<ComponentKind, ComponentState> componentStateByKind) {

    public boolean isValid() {
        return !isTerminated() && !isRecovering();
    }

    public String serializeToHTML() {
        return componentStateByKind.entrySet()
                .stream()
                .map(e -> e.getKey() + " => " + e.getValue().serialize())
                .map(e -> "[" + e + "]")
                .collect(Collectors.joining(", "));
    }

    public boolean isTerminated() {
        return componentStateByKind.values().stream().anyMatch(ComponentState::isTerminated);
    }

    public boolean isRecovering() {
        return componentStateByKind.values().stream().anyMatch(ComponentState::isRecovering);
    }

    public List<ElementStateTransition> getStateTransitions() {
        var elementStateTransitions = getElementStateTransitions();
        System.out.println("Element state transitions = " + elementStateTransitions);
        return elementStateTransitions;
    }

    private List<ElementStateTransition> getElementStateTransitions() {
        if (isTerminated()) {
            return List.of();
        }
        if (isRecovering()) {
            return componentStateByKind
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().isRecovering())
                    .flatMap(this::toElementStateTransition)
                    .toList();
        }
        return componentStateByKind
                .entrySet()
                .stream()
                .flatMap(this::toElementStateTransition)
                .toList();
    }

    private Stream<ElementStateTransition> toElementStateTransition(Map.Entry<ComponentKind, ComponentState> e) {
        var componentKind = e.getKey();
        return e.getValue()
                .calculateOtherStates()
                .stream()
                .map(stateTransition -> {
                    var newStateByKind = new HashMap<>(componentStateByKind);
                    newStateByKind.put(componentKind, stateTransition.targetState());
                    return new ElementStateTransition(stateTransition.alpha(), new RecoverableElement(newStateByKind));
                });
    }

    public record ElementStateTransition(Alpha alpha, RecoverableElement recoverableElement) {
    }

}
