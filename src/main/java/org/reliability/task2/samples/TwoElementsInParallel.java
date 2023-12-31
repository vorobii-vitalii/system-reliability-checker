package org.reliability.task2.samples;

import org.reliability.graph.Graph;
import org.reliability.parameters_enter.RecoverableElementsEnterComponent;
import org.reliability.task2.ComponentKind;
import org.reliability.task2.RecoverableElements;
import org.reliability.task2.SystemStateChecker;
import org.reliability.task2.ViewResultsComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TwoElementsInParallel extends JFrame {
    public static final Map<Integer, List<Integer>> ADJ_MATRIX = Map.of(
            Graph.START, List.of(0, 1),
            0, List.of(Graph.END),
            1, List.of(Graph.END)
    );

    public TwoElementsInParallel() {
        var submitButton = new JButton("Далі");
        submitButton.setVisible(false);
        AtomicReference<RecoverableElements> recoverableElements = new AtomicReference<>();
        var panel = new JPanel();
        var recoverableElementsEnter = getRecoverableElementsEnterComponent(recoverableElements, submitButton);
        panel.add(recoverableElementsEnter);
        submitButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.removeAll();
                panel.add(new ViewResultsComponent(recoverableElements.get(), new SystemStateChecker(ADJ_MATRIX)), BorderLayout.CENTER);
                TwoElementsInParallel.this.revalidate();
                TwoElementsInParallel.this.repaint();
            }
        });
        panel.add(submitButton);
        this.add(panel);
        var scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private RecoverableElementsEnterComponent getRecoverableElementsEnterComponent(AtomicReference<RecoverableElements> recoverableElements, JButton submitButton) {
        var recoverableElementsEnter = new RecoverableElementsEnterComponent(List.of(
                Map.of(ComponentKind.SOFTWARE, false, ComponentKind.HARDWARE, true),
                Map.of(ComponentKind.HARDWARE, true)
        ));
        recoverableElementsEnter.subscribe(newData -> {
            System.out.println("Enough data entered!!!" + newData);
            recoverableElements.set(newData);
            submitButton.setVisible(true);
        });
        return recoverableElementsEnter;
    }


    public static void main(String[] args) {
        var mainFrame = new TwoElementsInParallel();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }

}
