package org.example;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class HardwareAndSoftwareAlphaEnterComponent extends AlphaEnterComponent {
    public static final double STEP = 0.0001;
    private static final int PRECISION = 4;
    private final Set<Observer<List<Alpha>>> observers = new HashSet<>();
    private final AtomicReference<BigDecimal> azValue = new AtomicReference<>();
    private final AtomicReference<BigDecimal> pzValue = new AtomicReference<>();

    public HardwareAndSoftwareAlphaEnterComponent() {
        var azModel = new SpinnerNumberModel(0, 0, 1, STEP);
        var pzModel = new SpinnerNumberModel(0, 0, 1, STEP);
        var azSpinner = new JSpinner(azModel);
        var pzSpinner = new JSpinner(pzModel);
        increaseSize(azSpinner);
        increaseSize(pzSpinner);
        azSpinner.addChangeListener(e -> {
            azValue.set(new BigDecimal(azModel.getNumber().toString()).round(new MathContext(PRECISION)));
            updateListeners();
        });
        pzSpinner.addChangeListener(e -> {
            pzValue.set(new BigDecimal(pzModel.getNumber().toString()).round(new MathContext(PRECISION)));
            updateListeners();
        });
        this.add(new JLabel("аз"));
        this.add(azSpinner);
        this.add(new JLabel("пз"));
        this.add(pzSpinner);
    }

    private void increaseSize(JSpinner spinner) {
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.0000"));
    }

    @Override
    public void subscribe(Observer<List<Alpha>> observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(Observer<List<Alpha>> observer) {
        observers.remove(observer);
    }

    private void updateListeners() {
        if (azValue.get() == null || pzValue.get() == null) {
            return;
        }
        for (var observer : observers) {
            observer.onChange(List.of(new Alpha(azValue.get(), "аз"), new Alpha(pzValue.get(), "пз")));
        }
    }

}
