package org.reliability.parameters_enter;

import org.reliability.generic.Observer;
import org.reliability.dto.Alpha;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class OnlyHardwareAlphaEnterComponent extends AlphaEnterComponent {
    public static final double STEP = 0.0001;
    private static final int PRECISION = 4;

    private final AtomicReference<BigDecimal> alphaValue = new AtomicReference<>();
    private final Set<Observer<List<Alpha>>> observers = new HashSet<>();

    public OnlyHardwareAlphaEnterComponent() {
        var model = new SpinnerNumberModel(0, 0, 1, STEP);
        var spinner = new JSpinner(model);
        spinner.addChangeListener(e -> {
            alphaValue.set(new BigDecimal(model.getNumber().toString()).round(new MathContext(PRECISION)));
            updateListeners();
        });
        increaseSize(spinner);
        this.add(new JLabel("аз"));
        this.add(spinner);
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
        for (var observer : observers) {
            observer.onChange(List.of(new Alpha(alphaValue.get(), "аз")));
        }
    }

}
