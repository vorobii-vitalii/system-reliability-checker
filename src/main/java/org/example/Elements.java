package org.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record Elements(List<BigDecimal> arr) {

    public static Elements createEmpty(int n) {
        List<BigDecimal> arr = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            arr.add(null);
        }
        return new Elements(arr);
    }

    public String calcId() {
        return arr.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    public String serialize() {
        return arr.stream()
                .map(v -> String.valueOf(v != null ? '0' : '1'))
                .collect(Collectors.joining());
    }

    public boolean isDisabled(int pos) {
        return arr.get(pos) != null;
    }

    public Elements breakAt(int pos, BigDecimal alpha) {
        List<BigDecimal> newArr = new ArrayList<>(arr);
        newArr.set(pos, alpha);
        return new Elements(newArr);
    }

}
