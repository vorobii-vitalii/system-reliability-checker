package org.reliability.parameters_enter;

import org.reliability.dto.ComponentDetails;
import org.reliability.dto.ComponentType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.function.Supplier;

public abstract class ComponentDetailsEnterPanel extends JPanel {
    private static final ComponentType DEFAULT_TYPE = ComponentType.ONLY_HARDWARE;

    public ComponentDetailsEnterPanel(
            int numComponents,
            AlphasModel alphasModel,
            Map<ComponentType, Supplier<AlphaEnterComponent>> alphaEnterComponentCreatorMap
    ) {
        this.add(new JLabel("Введіть дані для " + numComponents + " компонентів..."));
        var componentListPanel = new JPanel();
        for (int i = 0; i < numComponents; i++) {
            int componentIndex = i;
            var componentFrame = new JPanel();
            var alphaEnterComponentWrapper = new JPanel();
            alphaEnterComponentWrapper.setLayout(new GridLayout(1, 2));
            addAlphaEnterComponent(
                    alphasModel,
                    DEFAULT_TYPE,
                    alphaEnterComponentCreatorMap,
                    alphaEnterComponentWrapper,
                    componentIndex
            );
            var comboBox = new JComboBox<>(ComponentType.values());
            var componentLabel = new JLabel("Компонент " + (i + 1));
            comboBox.addItemListener(e -> {
                ComponentType componentType = (ComponentType) e.getItem();
                alphaEnterComponentWrapper.removeAll();
                addAlphaEnterComponent(
                        alphasModel,
                        componentType,
                        alphaEnterComponentCreatorMap,
                        alphaEnterComponentWrapper,
                        componentIndex
                );
                componentFrame.repaint();
                componentFrame.revalidate();
            });
            componentFrame.add(componentLabel);
            componentFrame.add(comboBox);
            componentFrame.add(alphaEnterComponentWrapper);
            componentFrame.setVisible(true);
            componentFrame.setLayout(new GridLayout(3, 1));
            componentListPanel.add(componentFrame);
        }
        componentListPanel.setVisible(true);
        componentListPanel.setLayout(new GridLayout(numComponents, 1));
        this.add(componentListPanel);
        var button = new JButton("Обрахувати...");
        button.setSize(100, 50);
        this.add(button);
        button.setVisible(false);
        alphasModel.subscribe(newData -> {
            if (Arrays.stream(newData).allMatch(Objects::nonNull)) {
                System.out.println("Enabling button!");
                button.setVisible(true);
                repaint();
                revalidate();
            }
            else {
                System.out.println("not yet...");
            }
        });
        this.setLayout(new GridLayout(3, 1));
        button.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDetailsEntered();
            }
        });
    }

    private static void addAlphaEnterComponent(
            AlphasModel alphasModel,
            ComponentType componentType,
            Map<ComponentType, Supplier<AlphaEnterComponent>> alphaEnterComponentCreatorMap,
            JPanel alphaEnterComponentWrapper,
            int componentIndex
    ) {
        var alphaEnterComponent = alphaEnterComponentCreatorMap.get(componentType).get();
        alphaEnterComponentWrapper.add(alphaEnterComponent);
        alphaEnterComponent.subscribe(alphas ->
                alphasModel.updateComponentDetails(componentIndex, new ComponentDetails(componentType, alphas)));
    }

    public abstract void onDetailsEntered();

}
