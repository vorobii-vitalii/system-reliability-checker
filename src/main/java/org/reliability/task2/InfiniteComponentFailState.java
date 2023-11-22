package org.reliability.task2;

public record InfiniteComponentFailState() implements ComponentFailState {
    @Override
    public boolean canRecover() {
        return true;
    }

    @Override
    public boolean isInfiniteRecoveryAllowed() {
        return true;
    }

    @Override
    public int getNumRecoveryLeft() {
        return Integer.MAX_VALUE;
    }
}
