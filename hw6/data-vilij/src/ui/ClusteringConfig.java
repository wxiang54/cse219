package ui;

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
public class ClusteringConfig extends Stage implements Config {

    private static ClusteringConfig dialog;
    private Integer maxIterations, updateInterval, numClusters;
    private Boolean continuousRun;
    private TextField tf_iterations, tf_interval, tf_clusters;
    private CheckBox cb_cont_run;

    private ClusteringConfig() {
        /* empty constructor */ }

    public static ClusteringConfig getDialog() {
        if (dialog == null) {
            dialog = new ClusteringConfig();
        }
        return dialog;
    }

    private void deleteConfigHistory() {
        maxIterations = null;
        updateInterval = null;
        numClusters = null;
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
        Label label_clusters = new Label(manager.getPropertyValue(AppPropertyTypes.CONFIG_DIALOG_CLUSTERS.name()));
        Label label_cont_run = new Label(manager.getPropertyValue(AppPropertyTypes.CONFIG_DIALOG_CONT_RUN.name()));
        tf_iterations = new TextField();
        tf_interval = new TextField();
        tf_clusters = new TextField();

        tf_iterations.setPromptText("Default: 1");
        tf_interval.setPromptText("Default: 1");
        tf_clusters.setPromptText("Default: 1");
        cb_cont_run = new CheckBox();

        gridpane.add(label_iterations, 0, 0); //column, row
        gridpane.add(label_interval, 0, 1);
        gridpane.add(label_clusters, 0, 2);
        gridpane.add(label_cont_run, 0, 3);
        gridpane.add(tf_iterations, 1, 0); //column, row
        gridpane.add(tf_interval, 1, 1);
        gridpane.add(tf_clusters, 1, 2);
        gridpane.add(cb_cont_run, 1, 3);

        Button confirm = new Button("Confirm");
        confirm.setOnAction(e -> {
            confirmButtonActions();
        });

        VBox messagePane = new VBox(configTitle, gridpane, confirm);
        messagePane.setAlignment(Pos.CENTER);
        messagePane.setPadding(new Insets(10, 20, 20, 20));
        messagePane.setSpacing(10);

        this.setScene(new Scene(messagePane));
    }

    public static Object[] configTest(String iterations, String interval, String clusters, boolean contrun) {
        Object[] ret = new Object[4];
        try {
            ret[0] = Integer.parseInt(iterations);
            if ((int)ret[0] < 1) {
                ret[0] = 1;
            }
        } catch (NumberFormatException nfe) {
            ret[0] = 1;
        }
        try {
            ret[1] = Integer.parseInt(interval);
            if ((int)ret[1] < 1) {
                ret[1] = 1;
            }
        } catch (NumberFormatException fe) {
            ret[1] = 1;
        }
        try {
            ret[2] = Integer.parseInt(clusters);
            if ((int)ret[2] < 1) {
                ret[2] = 1;
            }
        } catch (NumberFormatException nfe) {
            ret[2] = 1;
        }
        ret[3] = contrun;
        return ret;
    }
    
    public void confirmButtonActions() {
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
        try {
            this.numClusters = Integer.parseInt(tf_clusters.getText());
            if (this.numClusters < 1) {
                this.numClusters = 1;
            }
        } catch (NumberFormatException nfe) {
            this.numClusters = 1;
        }
        this.continuousRun = cb_cont_run.isSelected();
        this.hide();
    }

    @Override
    public void showConfig() {
        deleteConfigHistory();
        tf_iterations.setText("");
        tf_interval.setText("");
        cb_cont_run.setSelected(false);
        showAndWait();
    }

    public void showConfig(int defaultMaxIter, int defaultUpdateInter,
            int defaultNumCluster, boolean defaultContRun) {
        deleteConfigHistory();
        tf_iterations.setText(String.valueOf(defaultMaxIter));
        tf_interval.setText(String.valueOf(defaultUpdateInter));
        tf_clusters.setText(String.valueOf(defaultNumCluster));
        cb_cont_run.setSelected(defaultContRun);
        showAndWait();
    }

    public Integer getMaxIterations() {
        return maxIterations;
    }

    public Integer getUpdateInterval() {
        return updateInterval;
    }

    public Integer getNumClusters() {
        return numClusters;
    }

    public Boolean getContinuousRun() {
        return continuousRun;
    }
}
