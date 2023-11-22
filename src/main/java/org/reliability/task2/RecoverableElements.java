package org.reliability.task2;

import org.reliability.dto.Alpha;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record RecoverableElements(List<RecoverableElement> recoverableElements) {

    public RecoverableElement get(int index) {
        return recoverableElements.get(index);
    }

    public List<ElementsTransition> calculateTransitions(SystemStateChecker systemStateChecker) {
        var stateCheckResult = systemStateChecker.checkSystemState(this);
        return switch (stateCheckResult) {
            case TERMINATED -> List.of();
            case RECOVERABLE -> generateAllTransitions()
                    .filter(e -> systemStateChecker.checkSystemState(e.targetState) == SystemStateCheckResult.WORKING)
                    .toList();
            case WORKING -> generateAllTransitions().toList();
        };
    }

    private Stream<ElementsTransition> generateAllTransitions() {
        return IntStream.range(0, numElements())
                .boxed()
                .flatMap(i -> {
                    var stateTransitions = recoverableElements.get(i).getStateTransitions();
                    return stateTransitions.stream()
                            .map(transition -> {
                                var elementsCopy = new ArrayList<>(recoverableElements);
                                elementsCopy.set(i, transition.recoverableElement());
                                return new ElementsTransition(
                                        new RecoverableElements(elementsCopy),
                                        transition.alpha()
                                );
                            });
                });
    }

    private int numElements() {
        return recoverableElements.size();
    }

    public record ElementsTransition(RecoverableElements targetState, Alpha alpha) {
    }

}
