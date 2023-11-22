package org.reliability.task1;

import org.reliability.dto.ComponentType;
import org.reliability.graph.Graph;
import org.reliability.parameters_enter.AlphasModel;
import org.reliability.parameters_enter.ComponentDetailsEnterPanel;
import org.reliability.parameters_enter.HardwareAndSoftwareAlphaEnterComponent;
import org.reliability.parameters_enter.OnlyHardwareAlphaEnterComponent;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private static final int NUM_COMPONENTS = 5;
    public static final Map<Integer, List<Integer>> ADJ_MATRIX = Map.of(
            Graph.START, List.of(0, 2),
            0, List.of(1),
            1, List.of(3, 4),
            2, List.of(3, 4),
            3, List.of(Graph.END),
            4, List.of(Graph.END)
    );

    public MainFrame() {
        var alphasModel = new AlphasModel(NUM_COMPONENTS);
        var componentDetailsEnterPanel = new ComponentDetailsEnterPanel(
                NUM_COMPONENTS,
                alphasModel,
                Map.of(
                        ComponentType.ONLY_HARDWARE, OnlyHardwareAlphaEnterComponent::new,
                        ComponentType.HARDWARE_AND_SOFTWARE, HardwareAndSoftwareAlphaEnterComponent::new
                )) {
            @Override
            public void onDetailsEntered() {
                this.setVisible(false);
                var panel = new ComponentGraphDetailsFrame(alphasModel.getComponentDetailsArray(), ADJ_MATRIX);
                var scrollPane = new JScrollPane(panel);
                MainFrame.this.add(scrollPane, BorderLayout.CENTER);
            }
        };
        this.add(componentDetailsEnterPanel);
    }

    public static void main(String[] args) {
        var mainFrame = new MainFrame();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }

}
