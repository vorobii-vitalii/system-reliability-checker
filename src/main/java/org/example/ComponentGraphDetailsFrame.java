package org.example;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class ComponentGraphDetailsFrame extends JPanel {
    public static final double STEP = 0.001;

    public static final Color[] COLORS = {Color.RED, Color.GREEN, Color.DARKGOLDENROD, Color.GRAY, Color.BLUE};
    public static final java.awt.Font LABEL_FONT = new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 15);
    private final AtomicInteger nodeId = new AtomicInteger(1);
    private final int numComponents;
    private JScrollPane resultsPane;

    public ComponentGraphDetailsFrame(
            ComponentDetails[] componentDetails,
            Map<Integer, List<Integer>> adjMatrix
    ) {
        numComponents = componentDetails.length;
        var graph = new Graph(adjMatrix);
        var g = mutGraph("Lab 1")
                .setDirected(true)
                .graphAttrs()
                .add(Rank.sep(2D));
        Map<String, Node> nodeById = new HashMap<>();

        // пошук в ширину
        bfs(componentDetails, nodeById, graph, g);
        this.add(new JLabel("Граф"), BorderLayout.CENTER);
        JLabel image = new JLabel(new ImageIcon(Graphviz.fromGraph(g)
                .width(1280)
                .height(720)
                .render(Format.PNG)
                .toImage()));
        this.add(image, BorderLayout.CENTER);
        renderDifferentialEquations(nodeById);

        var integrateHighModel = new SpinnerNumberModel(0, 0, 1000, 0.5);
        var integrationStepModel = new SpinnerNumberModel(0, 0, 1, STEP);

        var integrateHighSpinner = new JSpinner(integrateHighModel);
        var integrationStepSpinner = new JSpinner(integrationStepModel);

        increaseSize(integrateHighSpinner);
        increaseSize(integrationStepSpinner);

        add(renderNamedSpinner("Вища межа", integrateHighSpinner));
        add(renderNamedSpinner("Крок інтеграції", integrationStepSpinner));

        var computeButton = new JButton("Обрахувати");
        var nodes = nodeById.size();
        var map = nodeById.entrySet().stream().collect(Collectors.toMap(e -> e.getValue().getNodeId() - 1, Map.Entry::getValue));

        var probabilityWorking = new JLabel("");
        var probabilityNotWorking = new JLabel("");
        var averageTime = new JLabel("");

        add(probabilityWorking);
        add(probabilityNotWorking);
        add(averageTime);

        computeButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (resultsPane != null) {
                    remove(resultsPane);
                }
                double[] constantArray = new double[nodes];
                constantArray[0] = 1;
                double integrateHigh = integrateHighModel.getNumber().doubleValue();
                double integrateStep = integrationStepModel.getNumber().doubleValue();
                double[][] iterationsResults =
                        RungeKutta.solve(constantArray, 0, integrateHigh, integrateStep, (x, t1) -> {
                            double[] res1 = new double[nodes];
                            for (int i = 0; i < nodes; i++) {
                                res1[i] = map.get(i).calculateDerivative(x);
                            }
                            return res1;
                        });
                var tableData = calculateTableData(iterationsResults);
                var headers = createHeaders(iterationsResults);
                JTable table = new JTable(tableData, headers);
                JScrollPane scrollPane = new JScrollPane(table);
                resultsPane = scrollPane;
                table.setFillsViewportHeight(true);
                add(scrollPane);
                double sumValid = 0;
                double[] lastResult = iterationsResults[iterationsResults.length - 2];
                for (var i = 0; i < lastResult.length; i++) {
                    if (map.get(i).isValidState()) {
                        sumValid += lastResult[i];
                    }
                }
                probabilityWorking.setText("Ймовірність безвідомної роботи = " + sumValid);
                probabilityNotWorking.setText("Ймовірність вімови = " + (1 - sumValid));

                double averageT = 0D;
                for (int i = 0; i < iterationsResults.length - 2; i++) {
                    double sum = 0D;
                    for (int j = 0; j < iterationsResults[i].length; j++) {
                        if (map.get(j).isValidState()) {
                            sum += lastResult[j];
                        }
                    }
                    averageT += sum * integrateStep;
                }
                averageTime.setText("Середнє значення тривалості роботи до відмови = " + averageT);
                repaint();
                revalidate();
            }

            private Object[][] calculateTableData(double[][] iterationsResults) {
                int rows = iterationsResults.length - 1;
                var columns = iterationsResults[0].length + 2;
                var matrix = new Object[rows][columns];
                for (var i = 0; i < rows; i++) {
                    var iteration = iterationsResults[i];
                    matrix[i][0] = "Ітерація " + i;
                    for (int j = 0; j < iteration.length; j++) {
                        matrix[i][j + 1] = String.valueOf(iteration[j]);
                    }
                    matrix[i][columns - 1] = String.valueOf(Arrays.stream(iteration).sum());
                }
                return matrix;
            }


            private String[] createHeaders(double[][] iterationsResults) {
                var columns = iterationsResults[0].length + 2;
                var headerRow = new String[columns];
                headerRow[0] = "Номер ітерації";
                for (int i = 0; i < iterationsResults[0].length; i++) {
                    headerRow[i + 1] = "P(" + (i + 1) + ")";
                }
                headerRow[headerRow.length - 1] = "Cума P(i), 1 <= i <= m";
                return headerRow;
            }

        });
        add(computeButton);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    private JPanel renderNamedSpinner(String spinnerLabel, JSpinner spinner) {
        var panel = new JPanel();
        var comp = new JLabel(spinnerLabel, SwingConstants.CENTER);
        comp.setFont(LABEL_FONT);
        panel.add(comp);
        panel.add(spinner);
        panel.setLayout(new GridLayout(1, 2));
        return panel;
    }

    private void increaseSize(JSpinner spinner) {
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.000"));
    }

    private void bfs(
            ComponentDetails[] componentDetails,
            Map<String, Node> nodeById,
            Graph graph,
            MutableGraph g
    ) {
        var stateQueue = new LinkedList<State>();
        stateQueue.add(new State(graph, Elements.createEmpty(numComponents)));
        var visited = new HashSet<Elements>();
        var m = new AtomicInteger();
        while (!stateQueue.isEmpty()) {
            var state = stateQueue.poll();
            if (visited.contains(state.elements())) {
                continue;
            }
            visited.add(state.elements());
            var node = createIfNeeded(nodeById, state);
            if (state.isValid()) {
                node.getMutableNode().add(Color.GREEN).add(Font.size(25));
                for (int i = 0; i < numComponents; i++) {
                    if (state.elements().isDisabled(i)) {
                        continue;
                    }
                    int componentId = i;
                    componentDetails[i].alphas().forEach(alpha -> {
                        var newElements = state.elements().breakAt(componentId, alpha.value());
                        var newState = new State(graph, newElements);
                        var connectedNode = createIfNeeded(nodeById, newState);

                        node.addOutgoing(new Edge(connectedNode, alpha));
                        connectedNode.addIncoming(new Edge(node, alpha));

                        Color color = COLORS[(m.getAndIncrement()) % COLORS.length];
                        Link edge = node.getMutableNode().linkTo(connectedNode.getMutableNode())
                                .with(color)
                                .with(color.labelFont())
                                .with(Style.BOLD)
                                .with(Font.size(18))
                                .with(Label.html("&#945; = " + alpha.value().stripTrailingZeros()));
                        node.getMutableNode().addLink(edge);
                        stateQueue.add(newState);
                    });
                }
            } else {
                node.getMutableNode().add(Color.RED).add(Font.size(25));
            }
            g.add(node.getMutableNode());
        }
    }

    private void renderDifferentialEquations(Map<String, Node> nodeById) {
        var differentialEquations = new JPanel();
        differentialEquations.add(new JLabel("Система диф рівнянь"));
        nodeById.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(a -> a.getValue().getNodeId()))
                .forEach(e -> differentialEquations.add(new JLabel(e.getValue().getAsDiffEquation())));
        differentialEquations.setLayout(new GridLayout(nodeById.size() + 1, 1));
        this.add(differentialEquations);
    }

    private Node createIfNeeded(Map<String, Node> map, State state) {
        return map.computeIfAbsent(state.elements().calcId(),
                s -> {
                    var stateDescription = state.elements().serialize();
                    var stateId = nodeId.getAndIncrement();
                    var nodeName = Label.html("<b>" + stateId + "</b> <i>" + stateDescription + "</i>");
                    return new Node(stateId, mutNode(nodeName), state);
                });
    }

}
