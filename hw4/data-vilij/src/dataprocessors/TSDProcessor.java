package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;

/**
 * The data files used by this data visualization applications follow a
 * tab-separated format, where each data point is named, labeled, and has a
 * specific location in the 2-dimensional X-Y plane. This class handles the
 * parsing and processing of such data. It also handles exporting the data to a
 * 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's
 * <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name, int lineNum) {
            super(String.format("Line %d: Invalid name '%s'. " + NAME_ERROR_MSG, lineNum, name));
        }
    }

    public static class InvalidFormattingException extends Exception {

        public InvalidFormattingException(int lineNum) {
            super(String.format("Line %d: Bad Formatting", lineNum));
        }
    }

    public static class DuplicateNameException extends Exception {

        private static final String DUPE_ERROR_MSG = "Duplicate names are not allowed.";

        public DuplicateNameException(String name, int lineNum) {
            super(String.format("Line %d: Duplicate name '%s'. " + DUPE_ERROR_MSG, lineNum, name));
        }
    }

    private Map<String, String> dataLabels;
    private Map<String, Point2D> dataPoints;

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the
     * <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        AtomicInteger lineCtr = new AtomicInteger(0);
        Stream.of(tsdString.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    try {
                        lineCtr.incrementAndGet();
                        String name = checkedname(list.get(0), lineCtr.get());
                        try {
                            String label = list.get(1);
                            String[] pair = list.get(2).split(",");
                            Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                            dataLabels.put(name, label);
                            dataPoints.put(name, point);
                        } catch (Exception e) {
                            throw new InvalidFormattingException(lineCtr.get());
                        }
                    } catch (Exception e) {
                        errorMessage.setLength(0);
                        errorMessage.append(e.getClass().getSimpleName()).append(" on ").append(e.getMessage());
                        hadAnError.set(true);
                        clear();
                    }
                });
        if (errorMessage.length() > 0) {
            throw new Exception(errorMessage.toString());
        }
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values()); //inst -> label
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                //instance name is a label hidden until triggered by hover in AppUI
                XYChart.Data data = new XYChart.Data<>(point.getX(), point.getY());
                Label instanceName = new Label(entry.getKey());
                instanceName.setTranslateY(-20);
                instanceName.setStyle(
                        "-fx-font-size: 10;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-color: white;"
                        + "-fx-border-width: 1px;"
                        + "-fx-border-radius: 3px;"
                        + "-fx-border-color: black;"
                        + "-fx-padding: 1px;");
                instanceName.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
                instanceName.setManaged(false);
                instanceName.setVisible(false);
                data.setNode(new StackPane(instanceName));
                series.getData().add(data);
            });
            chart.getData().add(series);
        }

        //draw the average y value line
        if (dataPoints.size() > 1) {
            double y_sum = 0.0;
            double x_min = dataPoints.values().iterator().next().getX(); //random x
            double x_max = dataPoints.values().iterator().next().getX(); //another random x
            for (Point2D point : dataPoints.values()) {
                if (point.getX() < x_min) {
                    x_min = point.getX();
                }
                if (point.getX() > x_max) {
                    x_max = point.getX();
                }
                y_sum += point.getY();
            }
            PropertyManager manager = PropertyManager.getManager();
            double y_avg = y_sum / dataPoints.size();
            XYChart.Series<Number, Number> line_avg = new XYChart.Series<>();
            line_avg.getData().addAll(new XYChart.Data<>(x_min, y_avg), new XYChart.Data<>(x_max, y_avg));
            line_avg.setName(manager.getPropertyValue(AppPropertyTypes.AVG_LINE_NAME.name()));
            chart.getData().add(line_avg);
            line_avg.getNode().setId(manager.getPropertyValue(AppPropertyTypes.AVG_LINE_ID.name()));
            line_avg.getData().get(0).getNode().setStyle("-fx-background-radius: 0.0px; -fx-padding: 0.0px;");
            line_avg.getData().get(1).getNode().setStyle("-fx-background-radius: 0.0px; -fx-padding: 0.0px;");
        }
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name, int lineNum) throws InvalidDataNameException, DuplicateNameException {
        if (!name.startsWith("@")) {
            throw new InvalidDataNameException(name, lineNum);
        }
        if (dataLabels.containsKey(name)) {
            throw new DuplicateNameException(name, lineNum);
        }
        return name;
    }
}
