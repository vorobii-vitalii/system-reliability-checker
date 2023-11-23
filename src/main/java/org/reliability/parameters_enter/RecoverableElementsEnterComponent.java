package org.reliability.parameters_enter;

import org.reliability.generic.Observable;
import org.reliability.generic.Observer;
import org.reliability.task2.ComponentKind;
import org.reliability.task2.RecoverableElement;
import org.reliability.task2.RecoverableElements;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class RecoverableElementsEnterComponent extends JPanel implements Observable<RecoverableElements> {
    private final Set<Observer<RecoverableElements>> observers = new HashSet<>();

    public RecoverableElementsEnterComponent(List<Map<ComponentKind, Boolean>> componentsByElementList) {
        int n = componentsByElementList.size();
        List<RecoverableElement> elements = Collections.synchronizedList(new ArrayList<>());
        for (var i = 0; i < n; i++) {
            var map = componentsByElementList.get(i);
            var component = new RecoverableElementEnterComponent(i + 1, map);
            this.add(component);
            int elementIndex = i;
            elements.add(null);
            component.subscribe(recoverableElement -> {
                System.out.println("Date for element " + elementIndex + " entered = " + recoverableElement);
                elements.set(elementIndex, recoverableElement);
                if (elements.stream().allMatch(Objects::nonNull)) {
                    System.out.println("All entered " + elements);
                    for (var observer : observers) {
                        observer.onChange(new RecoverableElements(elements));
                    }
                }
                else {
                    System.out.println("Not entered for all " + elements);
                }
            });
        }
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public void subscribe(Observer<RecoverableElements> observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(Observer<RecoverableElements> observer) {
        observers.remove(observer);
    }
}
