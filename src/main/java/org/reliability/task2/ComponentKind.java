package org.reliability.task2;

public enum ComponentKind {
    SOFTWARE("sw"),
    HARDWARE("hw");

    private final String code;

    ComponentKind(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
