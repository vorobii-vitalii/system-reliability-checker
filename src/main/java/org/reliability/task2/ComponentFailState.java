package org.reliability.task2;

public interface ComponentFailState {
    boolean canRecover();
    boolean isInfiniteRecoveryAllowed();
    int getNumRecoveryLeft();
}
