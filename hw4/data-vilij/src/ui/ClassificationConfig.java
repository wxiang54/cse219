package ui;

import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;

/**
 * @author will
 */
public class ClassificationConfig extends Stage implements Config {

    private static ClassificationConfig dialog;
    private Integer maxIterations, updateInterval;
    private Boolean continuousRun;
    private TextField tf_iterations, tf_interval;
    private CheckBox cb_cont_run;


    private ClassificationConfig() {
        /* empty constructor */ }

    public static ClassificationConfig getDialog() {
        if (dialog == null) {
            dialog = new ClassificationConfig();
        }
        return dialog;
    }

    private void deleteConfigHistory() {
        maxIterations = null;
        updateInterval = null;
        continuousRun = null;
    }

    @Override
    public void init(Stage owner) {
        PropertyManager manager = PropertyManager.getManager();

        Label configTitle = new Label(manager.getPropertyValue(AppPropertyTypes.CONFIG_DIALOG_TITLE.name()));
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);

        GridPane gridpane = new GridPane();
        gridpane.setVgap(10);
        Label label_iterations = new Label(manager.getPropertyValue(AppPropertyTypes.CONFIG_DIALOG_ITERATIONS.name()));
        Label label_interval = new Label(manager.getPropertyValue(AppPropertyTypes.CONFIG_DIALOG_INTERVAL.name()));
        Label label_cont_run = new Label(manager.getPropertyValue(AppPropertyTypes.CONFIG_DIALOG_CONT_RUN.name()));
        tf_iterations = new TextField();
        tf_interval = new TextField();
        
        tf_iterations.setPromptText("Default: 1");
        tf_interval.setPromptText("Default: 1");
        cb_cont_run = new CheckBox();
        
        gridpane.add(label_iterations, 0, 0); //column, row
        gridpane.add(label_interval, 0, 1);
        gridpane.add(label_cont_run, 0, 2);
        gridpane.add(tf_iterations, 1, 0); //column, row
        gridpane.add(tf_interval, 1, 1);
        gridpane.add(cb_cont_run, 1, 2);

        Button confirm = new Button("Confirm");
        confirm.setOnAction(e -> {
            try {
                this.maxIterations = Integer.parseInt(tf_iterations.getText());
                if (this.maxIterations < 1) {
                    this.maxIterations = 1;
                }
            } catch (NumberFormatException nfe) {
                this.maxIterations = 1;
            }
            try {
                this.updateInterval = Integer.parseInt(tf_interval.getText());
                if (this.updateInterval < 1) {
                    this.updateInterval = 1;
                }
            } catch (NumberFormatException nfe) {
                this.updateInterval = 1;
            }
            this.continuousRun = cb_cont_run.isSelected();
            this.hide();
        });

        VBox messagePane = new VBox(configTitle, gridpane, confirm);
        messagePane.setAlignment(Pos.CENTER);
        messagePane.setPadding(new Insets(10, 20, 20, 20));
        messagePane.setSpacing(10);

        this.setScene(new Scene(messagePane));
    }

    @Override
    public void showConfig() {
        deleteConfigHistory();
        tf_iterations.setText("");
        tf_interval.setText("");
        cb_cont_run.setSelected(false);
        showAndWait();
    }
    
    public void showConfig(int defaultMaxIter, int defaultUpdateInter, boolean defaultContRun) {
        deleteConfigHistory();
        tf_iterations.setText(String.valueOf(defaultMaxIter));
        tf_interval.setText(String.valueOf(defaultUpdateInter));
        cb_cont_run.setSelected(defaultContRun);
        showAndWait();
    }

    public Integer getMaxIterations() {
        return maxIterations;
    }

    public Integer getUpdateInterval() {
        return updateInterval;
    }

    public Boolean getContinuousRun() {
        return continuousRun;
    }
}
