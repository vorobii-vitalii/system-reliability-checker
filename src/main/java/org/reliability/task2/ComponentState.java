package org.reliability.task2;

import org.reliability.dto.Alpha;

import java.util.List;

public record ComponentState(
        Alpha recoveryCoefficient,
        Alpha failCoefficient,
        ComponentFailState failState,
        boolean isRecovering,
        boolean isTerminated
) {

    public List<ComponentStateTransition> calculateOtherStates() {
        if (isTerminated) {
            return List.of();
        }
        if (isRecovering) {
            return List.of(
                    new ComponentStateTransition(
                            new ComponentState(recoveryCoefficient, failCoefficient, failState, false, false),
                            recoveryCoefficient
                    )
            );
        }
        if (failState.isInfiniteRecoveryAllowed()) {
            return List.of(new ComponentStateTransition(
                    new ComponentState(recoveryCoefficient, failCoefficient, failState, true, false),
                    failCoefficient
            ));
        }
        if (failState.canRecover()) {
            return List.of(new ComponentStateTransition(
                    new ComponentState(
                            recoveryCoefficient,
                            failCoefficient,
                            new LimitedComponentFailState(failState.getNumRecoveryLeft() - 1),
                            true,
                            false
                    ),
                    failCoefficient
            ));
        }
        return List.of(new ComponentStateTransition(
                new ComponentState(recoveryCoefficient, failCoefficient, failState, false, true),
                failCoefficient
        ));
    }

    public record ComponentStateTransition(ComponentState targetState, Alpha alpha) {
    }


}
