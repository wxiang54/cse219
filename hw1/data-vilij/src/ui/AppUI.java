package ui;

import actions.AppActions;
import dataprocessors.AppData;
import static java.io.File.separator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

// my imports
import javafx.scene.layout.HBox;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;
import static settings.AppPropertyTypes.*;
import vilij.propertymanager.PropertyManager;

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
    private Button scrnshotButton;   // toolbar button to take a screenshot of the data
    private String scrnshoticonPath; // path to the 'screenshot' icon
    private ScatterChart<Number, Number> chart;            // the chart where data will be displayed
    private Button displayButton;    // workspace button to display data on the chart
    private TextArea textArea;         // text area for new data input
    private boolean hasNewText;       // whether or not the text area has any new data since last display

    public ScatterChart<Number, Number> getChart() {
        return chart;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        PropertyManager manager = applicationTemplate.manager;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnshoticonPath = String.join(separator, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        // TODO for homework 1
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        scrnshotButton = setToolbarButton(scrnshoticonPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
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
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        // TODO for homework 1

    }

    private void layout() {
        // TODO for homework 1
        textArea = new TextArea();
        displayButton = new Button("Display");
        
        VBox left = new VBox();
        left.setPadding(new Insets(10));
        left.getChildren().addAll(new Label("Data File"), textArea, displayButton);

        VBox right = new VBox();
        right.setPadding(new Insets(10));

        NumberAxis xAxis = new NumberAxis(0, 20, 2);
        NumberAxis yAxis = new NumberAxis(0, 20, 2);
        chart = new ScatterChart<Number, Number>(xAxis, yAxis);
        chart.setTitle("Data Visualization");
        right.getChildren().add(chart);
        
        HBox content = new HBox();
        content.getChildren().addAll(left, right);
        
        appPane.getChildren().add(content);
    }

    private void setWorkspaceActions() {
        // TODO for homework 1
        textArea.textProperty().addListener(e -> {
            if (textArea.getText().equals("")) {
                newButton.setDisable(true); //disable 'new' button
                saveButton.setDisable(true); //disable 'save' button
            }
            else {
                if (newButton.isDisabled()) //assuming this is less costly operation
                    newButton.setDisable(false); //enable 'new button'
                if (saveButton.isDisabled())
                    saveButton.setDisable(false);
            }
        });
        
        displayButton.setOnAction(e -> {
            applicationTemplate.getDataComponent().clear();
            try {
                ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());
                chart.getData().clear();
                ((AppData) applicationTemplate.getDataComponent()).displayData();
            } catch (Exception ex) {
                Alert alert = new Alert(AlertType.ERROR, "Invalid data. Make sure your data is in valid TSD format.");
                alert.showAndWait();
                //Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        
    }
}
