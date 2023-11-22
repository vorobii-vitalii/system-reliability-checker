package org.reliability.parameters_enter;

import org.reliability.dto.ComponentDetails;
import org.reliability.generic.Observable;
import org.reliability.generic.Observer;

import java.util.HashSet;
import java.util.Set;

public class AlphasModel implements Observable<ComponentDetails[]> {
    private final ComponentDetails[] componentDetailsArray;
    private final Set<Observer<ComponentDetails[]>> listeners = new HashSet<>();

    public AlphasModel(int numComponents) {
        this.componentDetailsArray = new ComponentDetails[numComponents];
    }

    public ComponentDetails[] getComponentDetailsArray() {
        return componentDetailsArray;
    }

    public void updateComponentDetails(int componentIndex, ComponentDetails componentDetails) {
        componentDetailsArray[componentIndex] = componentDetails;
        update();
    }

    @Override
    public void subscribe(Observer<ComponentDetails[]> observer) {
        listeners.add(observer);
    }

    @Override
    public void unsubscribe(Observer<ComponentDetails[]> observer) {
        listeners.remove(observer);
    }

    private void update() {
        for (var listener : listeners) {
            listener.onChange(componentDetailsArray);
        }
    }

}
