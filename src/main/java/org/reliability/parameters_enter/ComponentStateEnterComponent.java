package org.reliability.parameters_enter;

import org.reliability.dto.Alpha;
import org.reliability.generic.Observable;
import org.reliability.generic.Observer;
import org.reliability.task2.ComponentKind;
import org.reliability.task2.ComponentState;
import org.reliability.task2.InfiniteComponentFailState;
import org.reliability.task2.LimitedComponentFailState;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ComponentStateEnterComponent extends JPanel implements Observable<ComponentState> {
    private final Set<Observer<ComponentState>> observers = new HashSet<>();

    public ComponentStateEnterComponent(int componentId, boolean isAlwaysRecoverable, ComponentKind kind) {
        AtomicReference<BigDecimal> failureRate = new AtomicReference<>();
        AtomicReference<BigDecimal> recoveryRate = new AtomicReference<>();
        AtomicInteger numRetries = new AtomicInteger(1);

        this.add(new JLabel("Введіть коефіцієнт відмови для компоненту " + componentId));
        this.add(createNumberInput(num -> {
            failureRate.set(num);
            propagateChangeIfNeeded(failureRate.get(), recoveryRate.get(), numRetries.get(), isAlwaysRecoverable, componentId, kind);

        }, null));
        this.add(new JLabel("Введіть коефіцієнт відновлення для компоненту " + componentId));
        this.add(createNumberInput(num -> {
            recoveryRate.set(num);
            propagateChangeIfNeeded(failureRate.get(), recoveryRate.get(), numRetries.get(), isAlwaysRecoverable, componentId, kind);

        }, null));
        if (!isAlwaysRecoverable) {
            this.add(new JLabel("Введіть кількість відновлень " + componentId));
            this.add(
                createNumberInput(num -> {
                    numRetries.set(num.intValue());
                    propagateChangeIfNeeded(failureRate.get(), recoveryRate.get(), numRetries.get(), false, componentId, kind);
                }, BigDecimal.valueOf(numRetries.get())));
        }
        this.setLayout(new GridLayout(6, 1));
    }

    private void propagateChangeIfNeeded(
            BigDecimal failure,
            BigDecimal recovery,
            int numRecoveries,
            boolean isAlwaysRecoverable,
            int componentId,
            ComponentKind kind
    ) {
        if (failure == null || recovery == null) {
            return;
        }
        var componentState = new ComponentState(
                new Alpha(recovery, componentId + "_recovery_" + kind),
                new Alpha(failure, componentId + "_failure_" + kind),
                isAlwaysRecoverable ? new InfiniteComponentFailState() : new LimitedComponentFailState(numRecoveries),
                false,
                false
        );
        for (var observer : observers) {
            observer.onChange(componentState);
        }
    }

    @Override
    public void subscribe(Observer<ComponentState> observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(Observer<ComponentState> observer) {
        observers.remove(observer);
    }

    private JTextField createNumberInput(Consumer<BigDecimal> onNumberChange, BigDecimal defaultValue) {
        var numberInput = defaultValue != null ? new JTextField(defaultValue.toString()) : new JTextField();
        numberInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    onNumberChange.accept(new BigDecimal(numberInput.getText().trim()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    onNumberChange.accept(new BigDecimal(numberInput.getText().trim()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                try {
                    onNumberChange.accept(new BigDecimal(numberInput.getText().trim()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return numberInput;
    }

}
