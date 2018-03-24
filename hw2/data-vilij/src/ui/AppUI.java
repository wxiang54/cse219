package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import static java.io.File.separator;
import java.io.IOException;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import static settings.AppPropertyTypes.APP_CSS_RESOURCE_FILENAME;
import static vilij.settings.PropertyTypes.CSS_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton;              // toolbar button to take a screenshot of the data
    private LineChart<Number, Number> chart; // the chart where data will be displayed
    private Button displayButton;               // workspace button to display data on the chart
    private TextArea textArea;                  // text area for new data input
    private CheckBox toggleReadOnly;            // read-only checkbox
    private boolean hasNewText;                 // whether text area has new data since last display
    private String appCSSPath;                  // path to data-vilij css file
    private String[] remainingData;             // when > 10 lines, rest of data should be stored here
    private int remainingDataInd;               // keeps track of where you are in remainingData

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        primaryScene.getStylesheets().add(appCSSPath);
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        appCSSPath = "/" + String.join("/",
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(CSS_RESOURCE_PATH.name()),
                manager.getPropertyValue(APP_CSS_RESOURCE_FILENAME.name()));
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join("/",
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(separator,
                iconsPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                true);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException ex) {
                //do nothing
            }
        });
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
    }

    public String getCurrentText() {
        return textArea.getText();
    }

    public String[] getRemainingData() {
        return remainingData;
    }

    public int getRemainingDataInd() {
        return remainingDataInd;
    }

    //upon loading a file
    public void updateTextArea(String data) {
        AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
        dataComponent.clear();
        try {
            dataComponent.loadData(data);
        } catch (Exception e) {
            return;
        }
        String[] allData = data.split("\n");
        remainingDataInd = 0;
        if (allData.length > 10) {
            String lastLine = allData[10];
            remainingData = new String[allData.length - 10];
            for (int i = 10; i < allData.length; i++) {
                remainingData[i - 10] = allData[i];
            }
            textArea.textProperty().setValue(data.substring(0, data.indexOf(lastLine)));
            dataComponent.showFileTooLongDialog(allData.length);
        } else {
            remainingData = null;
            textArea.textProperty().setValue(data);
        }
    }

    public void disableSaveButton() {
        saveButton.setDisable(true);
    }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
        //remove grid lines
        chart.setHorizontalGridLinesVisible(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setVerticalZeroLineVisible(false);

        VBox leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.5);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.5);

        Text leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();
        toggleReadOnly = new CheckBox(manager.getPropertyValue(AppPropertyTypes.TOGGLE_READONLY_TEXT.name()));

        HBox processButtonsBox = new HBox();
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.getChildren().addAll(displayButton);

        leftPanel.getChildren().addAll(leftPanelTitle, textArea, toggleReadOnly, processButtonsBox);

        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setCheckBoxActions();
        setDisplayButtonActions();
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {

                    //check if lines need to be added from remainingData
                    String[] splitted = newValue.split("\n");
                    if ((splitted.length < 10 || (splitted.length == 10 && splitted[9].equals("")))
                            && remainingData != null) {
                        int numLinesToAdd = 10 - newValue.split("\n").length;
                        String toAdd = newValue.charAt(newValue.length() - 1) == '\n' ? "" : "\n";
                        for (int i = 0; i < numLinesToAdd; i++) {
                            if (remainingDataInd >= remainingData.length) {
                                break;
                            }
                            toAdd += remainingData[remainingDataInd] + "\n";
                            remainingDataInd++;
                        }
                        textArea.textProperty().setValue(textArea.getText() + toAdd);
                        newValue = textArea.getText() + toAdd;
                    }
                    if (!newValue.isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        if (newValue.charAt(newValue.length() - 1) == '\n') {
                            hasNewText = true;
                        }
                        newButton.setDisable(false);
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }

                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
    }

    private void setCheckBoxActions() {
        toggleReadOnly.selectedProperty().addListener((observable, oldValue, newValue) -> {
            textArea.setDisable(newValue);
        });
    }

    private void setDisplayButtonActions() {
        displayButton.setOnAction(event -> {
            if (hasNewText) {
                AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                dataComponent.clear();

                try {
                    if (remainingData != null && remainingData.length > 0 && remainingDataInd < remainingData.length) {
                        String toAdd = textArea.getText().charAt(textArea.getText().length() - 1) == '\n' ? "" : "\n";
                        for (int i = remainingDataInd; i < remainingData.length; i++) {
                            toAdd += remainingData[i] + "\n";
                        }
                        dataComponent.loadData(textArea.getText() + toAdd);
                    } else {
                        dataComponent.loadData(textArea.getText());
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    return;
                }
                chart.getData().clear();
                dataComponent.displayData();
                setDataPointListeners();
                scrnshotButton.setDisable(false);
            }
        });
    }

    private void setDataPointListeners() {
        for (int i = 0; i < 7; i++) {
            if (chart.lookup(".default-color" + i + ".chart-line-symbol") != null) {
                for (Node point : chart.lookupAll(".default-color" + i + ".chart-line-symbol")) {
                    point.setOnMouseEntered(event -> {
                        primaryScene.setCursor(Cursor.CROSSHAIR);
                        if (((StackPane) point).getChildren().size() > 0) {
                            Node label = ((StackPane) point).getChildren().get(0);
                            label.setVisible(true);
                            label.setManaged(true);
                            label.toFront();
                        }
                    });
                    point.setOnMouseExited(event -> {
                        primaryScene.setCursor(Cursor.DEFAULT);
                        if (((StackPane) point).getChildren().size() > 0) {
                            Node label = ((StackPane) point).getChildren().get(0);
                            label.setVisible(false);
                            label.setManaged(false);
                        }
                    });
                }
            }
        }
    }
}
