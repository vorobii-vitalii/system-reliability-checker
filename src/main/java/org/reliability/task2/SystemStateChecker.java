package org.reliability.task2;

import java.util.List;
import java.util.Map;

public class SystemStateChecker {
    public static int START = -2;
    public static int END = -1;

    private final Map<Integer, List<Integer>> adjMatrix;

    public SystemStateChecker(Map<Integer, List<Integer>> adjMatrix) {
        this.adjMatrix = adjMatrix;
    }

    public SystemStateCheckResult checkSystemState(RecoverableElements recoverableElements) {
        return checkState(START, recoverableElements);
    }

    private SystemStateCheckResult checkState(int nodeIndex, RecoverableElements elements) {
        if (nodeIndex == END) {
            return SystemStateCheckResult.WORKING;
        }
        if (nodeIndex != START && elements.get(nodeIndex).isTerminated()) {
            return SystemStateCheckResult.TERMINATED;
        }
        var list = adjMatrix.get(nodeIndex);
        if (list == null) {
            return SystemStateCheckResult.TERMINATED;
        }
        var stateToReturnIfPathPossible = nodeIndex != START && elements.get(nodeIndex).isRecovering()
                ? SystemStateCheckResult.RECOVERABLE
                : SystemStateCheckResult.WORKING;
        for (Integer adjNode : list) {
            SystemStateCheckResult state = checkState(adjNode, elements);
            if (state == SystemStateCheckResult.RECOVERABLE) {
                return SystemStateCheckResult.RECOVERABLE;
            } else if (state == SystemStateCheckResult.WORKING) {
                return stateToReturnIfPathPossible;
            }
        }
        return SystemStateCheckResult.TERMINATED;
    }

}
