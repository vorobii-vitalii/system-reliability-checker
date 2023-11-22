package org.reliability.parameters_enter;

import org.reliability.generic.Observable;
import org.reliability.generic.Observer;
import org.reliability.task2.ComponentKind;
import org.reliability.task2.ComponentState;
import org.reliability.task2.RecoverableElement;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RecoverableElementEnterComponent extends JPanel implements Observable<RecoverableElement> {
    private final Set<Observer<RecoverableElement>> observers = new HashSet<>();

    public RecoverableElementEnterComponent(int elementId, Map<ComponentKind, Boolean> isAlwaysRecoverableByKind) {
        var map = new ConcurrentHashMap<ComponentKind, ComponentState>();
        int expectedComponents = isAlwaysRecoverableByKind.size();
        for (var entry : isAlwaysRecoverableByKind.entrySet()) {
            var isAlwaysRecoverable = entry.getValue();
            var kind = entry.getKey();
            this.add(new JLabel(kind.name()));
            var component = new ComponentStateEnterComponent(elementId, isAlwaysRecoverable, kind);
            this.add(component);
            component.subscribe(newData -> {
                map.put(kind, newData);
                if (map.size() == expectedComponents) {
                    var recoverableElement = new RecoverableElement(map);
                    for (var observer : observers) {
                        observer.onChange(recoverableElement);
                    }
                }
            });
        }
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public void subscribe(Observer<RecoverableElement> observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(Observer<RecoverableElement> observer) {
        observers.remove(observer);
    }
}
