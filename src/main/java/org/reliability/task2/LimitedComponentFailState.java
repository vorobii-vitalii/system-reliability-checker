package org.reliability.task2;

public record LimitedComponentFailState(int numLeft) implements ComponentFailState {

    @Override
    public boolean canRecover() {
        return numLeft > 0;
    }

    @Override
    public boolean isInfiniteRecoveryAllowed() {
        return false;
    }

    @Override
    public int getNumRecoveryLeft() {
        return numLeft;
    }

}
