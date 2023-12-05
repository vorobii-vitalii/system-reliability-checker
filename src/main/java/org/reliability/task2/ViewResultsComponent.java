package org.reliability.task2;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.model.MutableNode;
import org.reliability.algo.RungeKutta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static org.reliability.task1.ComponentGraphDetailsFrame.LABEL_FONT;

public class ViewResultsComponent extends JPanel {
    public static final double STEP = 0.001;
    public static final int INITIAL_NODE_ID = 1;

    private JScrollPane resultsPane;

    public ViewResultsComponent(RecoverableElements recoverableElements, SystemStateChecker systemStateChecker) {
        Graphviz.useEngine(new GraphvizCmdLineEngine().timeout(100, TimeUnit.HOURS));

        var visitedStates = new HashSet<RecoverableElements>();
        var queue = new LinkedList<RecoverableElements>();
        // Data
        queue.add(recoverableElements);
        var nodeIdProvider = new AtomicInteger(INITIAL_NODE_ID);
        var nodeByElements = new HashMap<RecoverableElements, GraphNode>();
        while (!queue.isEmpty()) {
            var elements = queue.pollLast();

            // Make sure same node not processed twice
            if (visitedStates.contains(elements)) {
                continue;
            }
            visitedStates.add(elements);

            // Update maps
            var currentGraphNode = nodeByElements.computeIfAbsent(elements, getGraphNodeCreator(nodeIdProvider));

            var elementsTransitions = elements.calculateTransitions(systemStateChecker);

            for (var elementsTransition : elementsTransitions) {
                var alpha = elementsTransition.alpha();
                var targetElements = elementsTransition.targetState();
                var targetNode = nodeByElements.computeIfAbsent(targetElements, getGraphNodeCreator(nodeIdProvider));
                currentGraphNode.addOutgoingEdge(new GraphNode.GraphEdge(alpha, targetNode));
                targetNode.addIncomingEdge(new GraphNode.GraphEdge(alpha, currentGraphNode));
                queue.add(targetElements);
            }
        }
        var differentialEquations = nodeByElements.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(a -> a.getValue().getNodeId()))
                .map(e -> e.getValue().getDifferentialEquation())
                .collect(Collectors.joining("\n"));
        System.out.println("DIFFERENTIAL EQUATIONS");
        System.out.println(differentialEquations);

        nodeByElements.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(a -> a.getValue().getNodeId()))
                .map(e -> e.getValue().getDifferentialEquation())
                .forEach(e -> this.add(new JLabel(e)));

        generateGraphImage(nodeByElements, systemStateChecker);


        var integrateHighModel = new SpinnerNumberModel(0, 0, 1000, 0.5);
        var integrationStepModel = new SpinnerNumberModel(0, 0, 1, STEP);

        var integrateHighSpinner = new JSpinner(integrateHighModel);
        var integrationStepSpinner = new JSpinner(integrationStepModel);

        increaseSize(integrateHighSpinner);
        increaseSize(integrationStepSpinner);

        add(renderNamedSpinner("Вища межа", integrateHighSpinner));
        add(renderNamedSpinner("Крок інтеграції", integrationStepSpinner));

        var computeButton = new JButton("Обрахувати");
        var nodes = nodeByElements.size();
        var map = nodeByElements.entrySet().stream().collect(Collectors.toMap(e -> e.getValue().getNodeId() - 1, Map.Entry::getValue));

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
                var headers = createHeaders(iterationsResults);
                var tableData = calculateTableData(iterationsResults);

                File csvOutputFile = new File("result.csv");
                try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                    pw.println(String.join(";", headers));
                    for (Object[] row : tableData) {
                        pw.println(Stream.of(row).map(Object::toString).collect(Collectors.joining(";")));
                    }

                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }

                var table = new JTable(tableData, headers);
                var scrollPane = new JScrollPane(table);
                resultsPane = scrollPane;
                table.setFillsViewportHeight(true);

                add(scrollPane);

                double sumValid = 0;
                double[] lastResult = iterationsResults[iterationsResults.length - 2];
                for (var i = 0; i < lastResult.length; i++) {
                    if (systemStateChecker.checkSystemState(map.get(i).getRecoverableElements()) == SystemStateCheckResult.WORKING) {
                        sumValid += lastResult[i];
                    }
                }
                probabilityWorking.setText("Ймовірність безвідомної роботи = " + sumValid);
                probabilityNotWorking.setText("Ймовірність вімови = " + (1 - sumValid));

                double averageT = 0D;
                for (int i = 0; i < iterationsResults.length - 2; i++) {
                    double sum = 0D;
                    for (int j = 0; j < iterationsResults[i].length; j++) {
                        if (systemStateChecker.checkSystemState(map.get(j).getRecoverableElements()) == SystemStateCheckResult.WORKING) {
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

    private void generateGraphImage(
            HashMap<RecoverableElements, GraphNode> nodeByElements,
            SystemStateChecker systemStateChecker
    ) {
        var mutableGraph = mutGraph("Lab 2")
                .setDirected(true)
                .graphAttrs()
                .add(Rank.sep(2D));
        var mutableNodeById = new HashMap<Integer, MutableNode>();
        for (var entry : nodeByElements.entrySet()) {
            var recoverableElements = entry.getKey();
            var graphNode = entry.getValue();
            var nodeId = graphNode.getNodeId();
            var mutableNode = mutableNodeById.computeIfAbsent(nodeId, id -> {
                var stateCheckResult = systemStateChecker.checkSystemState(recoverableElements);
                return mutNode(Label.html("<b>" + id + "</b> <i>" + stateCheckResult + "</i>"))
                        .add(calculateColorByStateCheckResult(stateCheckResult).font())
                        .add(Color.BLACK.background());
            });
            mutableGraph.add(mutableNode);
            for (var outgoingEdge : graphNode.getOutgoingEdges()) {
                var node = outgoingEdge.graphNode();
                var connectedMutableNode = mutableNodeById.computeIfAbsent(node.getNodeId(), id -> {
                    var stateCheckResult = systemStateChecker.checkSystemState(node.getRecoverableElements());
                    String value = "<b>" + id + "</b> " +
                            "<i>" + stateCheckResult + "</i> "
                            + "<i>"
                            + node.getRecoverableElements().serializeToHTML()
                            + "</i>";
                    System.out.println(value);
                    return mutNode(Label.html(
                            value
                    ))
                            .add(calculateColorByStateCheckResult(stateCheckResult).font())
                            .add(Color.BLACK.background());
                });
                var edge = mutableNode.linkTo(connectedMutableNode)
                        .with(Color.GREEN)
                        .with(Color.YELLOW.labelFont())
                        .with(Style.BOLD)
                        .with(Font.size(18))
                        .with(Label.html(outgoingEdge.alpha().name()));
                mutableNode.addLink(edge);
            }
        }
        new Thread(() -> {
            long start = System.currentTimeMillis();
            System.out.println("Creating PNG file!");
            try {
                Graphviz.fromGraph(mutableGraph)
                        .render(Format.PNG)
                        .toFile(new File("output_graph.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Created PNG file in " + (System.currentTimeMillis() - start) + " ms");
        }).start();
        long startBeforeJLabel = System.currentTimeMillis();

        JLabel image = new JLabel(new ImageIcon(Graphviz.fromGraph(mutableGraph)
                .width(1920)
                .height(1080)
                .render(Format.PNG)
                .toImage()));
        this.add(image, BorderLayout.CENTER);
        System.out.println("Created Swing image in " + (System.currentTimeMillis() - startBeforeJLabel) + " ms");
    }

    private static Color calculateColorByStateCheckResult(SystemStateCheckResult systemStateCheckResult) {
        return switch (systemStateCheckResult) {
            case TERMINATED -> Color.RED;
            case WORKING -> Color.GREEN;
            case RECOVERABLE -> Color.GOLD;
        };
    }

    private static Function<RecoverableElements, GraphNode> getGraphNodeCreator(
            AtomicInteger nodeIdProvider
    ) {
        return recoverableElements -> new GraphNode(nodeIdProvider.getAndIncrement(), recoverableElements);
    }

}
