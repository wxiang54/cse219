package ui;

import actions.AppActions;
import algorithms.Algorithm;
import algorithms.Classifier;
import algorithms.Clusterer;
import dataprocessors.AppData;
import dataprocessors.DataSet;
import java.io.File;
import java.lang.reflect.Constructor;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import static settings.AppPropertyTypes.APP_CSS_RESOURCE_FILENAME;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
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
    public static final boolean LEFTPANE_VISIBLE = false; //testing purposes

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton;              // toolbar button to take a screenshot of the data
    private LineChart<Number, Number> chart;    // the chart where data will be displayed
    private TextArea textArea;                  // text area for new data input
    private boolean hasNewText;                 // whether text area has new data since last display
    private String appCSSPath;                  // path to data-vilij css file
    private String[] remainingData;             // when > 10 lines, rest of data should be stored here
    private int remainingDataInd;               // keeps track of where you are in remainingData
    //private boolean leftPanelShown;             // self-evident
    private VBox leftPanel;                     // to add TextBox after New/Load button pressed
    //private HBox processButtonsBox;           // to add after New/Load button pressed
    private Button runButton;                   // workspace button to run algorithm
    private Button nextButton;                  // signals to continue to next interval during non-cont. algo
    private Button toggleDoneEditing;           // toggle for textarea after clicking New
    private Text metadataText;                  // algo metadata area
    private Text chartMsg;                      // chart notification area
    private VBox algochooser;                   // set of controls/elements related to choosing algo
    private Accordion chooseAlgoType;           // to add the event listener
    private TitledPane classification, clustering; //to disable classification in the future
    private ToggleGroup toggle_classification, toggle_clustering;
    private ArrayList<Class<Classifier>> algos_classification;  // all class. algos
    private ArrayList<Class<Clusterer>> algos_clustering;      // all clust. algos

    private boolean classification_disabled;    // short-term storage of classification titled pane disable
    private Task algoTask;                      // main task for running algorithm
    private DataSet dataset;                    // dataset to be passed to algos

    //DIALOGS
    protected final ClassificationConfig config_classification = ClassificationConfig.getDialog();
    protected final ClusteringConfig config_clustering = ClusteringConfig.getDialog();

    private final HashMap<String, Algorithm> algorithms; //maps algo name to actual running Algorithm
    private final HashMap<String, Class<?>> algo_classes;  //maps algo name to class obj (for instantiation)

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        primaryScene.getStylesheets().add(appCSSPath);
        algorithms = new HashMap<>();
        algo_classes = new HashMap<>();
    }

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public void setDataSet(DataSet ds) {
        dataset = ds;
    }

    protected void configsAudit(Stage primaryStage) {
        config_classification.init(primaryStage);
        config_clustering.init(primaryStage);
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
        configsAudit(primaryStage);
        registerAlgos();
        layout();
        setWorkspaceActions();
    }

    public void registerAlgos() {
        PropertyManager manager = applicationTemplate.manager;
        algos_classification = new ArrayList<>();
        algos_clustering = new ArrayList<>();
        String dataPath_classification = String.join(separator,
                manager.getPropertyValue(AppPropertyTypes.DATA_SRC_PREFIX.name()),
                manager.getPropertyValue(AppPropertyTypes.ALGO_PREFIX.name()),
                manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_ALGO_DIR.name()));
        String dataPath_clustering = String.join(separator,
                manager.getPropertyValue(AppPropertyTypes.DATA_SRC_PREFIX.name()),
                manager.getPropertyValue(AppPropertyTypes.ALGO_PREFIX.name()),
                manager.getPropertyValue(AppPropertyTypes.CLUSTERING_ALGO_DIR.name()));
        File dir_classification = new File(dataPath_classification);
        File dir_clustering = new File(dataPath_clustering);
        File[] files_classification = dir_classification.listFiles();
        File[] files_clustering = dir_clustering.listFiles();

        System.out.println("registering classification algos");
        if (files_classification.length > 0 && !files_classification[0].equals("")) {
            for (File f : files_classification) {
                System.out.println("\t* " + f.getName().split("\\.")[0]);
                String algoPath = String.join(".",
                        manager.getPropertyValue(AppPropertyTypes.ALGO_PREFIX.name()),
                        manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_ALGO_DIR.name()),
                        f.getName().split("\\.")[0]);
                try {
                    algos_classification.add((Class<Classifier>) Class.forName(algoPath));
                    System.out.println("\t\t* Added " + algoPath);
                } catch (ClassNotFoundException ex) {
                    System.out.println("\t\t* Class not found: " + algoPath);
                }
            }
        }
        System.out.println("registering clustering algos");
        if (files_clustering.length > 0) {
            System.out.println("files clustering length sike: " + files_clustering[0]);
            for (File f : files_clustering) {
                String name = f.getName();
                if (!name.substring(name.lastIndexOf('.'), name.length()).equals(".java")) { //not java file
                    continue;
                }
                String algoPath = String.join(".",
                        manager.getPropertyValue(AppPropertyTypes.ALGO_PREFIX.name()),
                        manager.getPropertyValue(AppPropertyTypes.CLUSTERING_ALGO_DIR.name()),
                        name.split("\\.")[0]);
                try {
                    algos_clustering.add((Class<Clusterer>) Class.forName(algoPath));
                    System.out.println("\t\t* Added " + algoPath);
                } catch (ClassNotFoundException ex) {
                    System.out.println("\t\t* Class not found: " + algoPath);
                }
            }
        }
    }

    @Override
    public void clear() {
        textArea.clear();
        chartMsg.setText("");
        chart.getData().clear();
        scrnshotButton.setDisable(true);
        remainingData = null;
        remainingDataInd = 0;
    }

    public void setClassificationDisable(boolean b) {
        classification.setDisable(b);
    }

    public TextArea getTextArea() {
        return textArea;
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
            showLeftPanel_load();
            dataComponent.showFileTooLongDialog(allData.length);
        } else {
            remainingData = null;
            textArea.textProperty().setValue(data);
            showLeftPanel_load();
        }
    }

    public void disableSaveButton() {
        saveButton.setDisable(true);
    }

    public void disableRunButton() {
        //disable when run button was valid for class. but no longer valid
        PropertyManager manager = applicationTemplate.manager;
        TitledPane expanded = chooseAlgoType.getExpandedPane();
        if (expanded != null && expanded.getText().equals(
                manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_TITLE.name()))) {
            System.out.println("YSERT");
            chooseAlgoType.setExpandedPane(clustering);
            runButton.setDisable(true);
        }
    }

    public void setMetadataText(String text) {
        metadataText.setText(text);
    }

    public void showLeftPanel_load() {
        PropertyManager manager = applicationTemplate.manager;
        leftPanel.setVisible(true);
        textArea.setDisable(true);
        toggleDoneEditing.setVisible(false);
        algochooser.setVisible(true);
        runButton.setVisible(chooseAlgoType.getExpandedPane() != null);
    }

    public void showLeftPanel_new() {
        PropertyManager manager = applicationTemplate.manager;
        leftPanel.setVisible(true);
        textArea.setDisable(false);
        toggleDoneEditing.setVisible(true);
        toggleDoneEditing.setText(manager.getPropertyValue(AppPropertyTypes.TOGGLE_DONE_TEXT.name()));
        algochooser.setVisible(false);
        runButton.setVisible(chooseAlgoType.getExpandedPane() != null);
        metadataText.setText("");
    }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(false);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
        //remove grid lines
        chart.setHorizontalGridLinesVisible(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setVerticalZeroLineVisible(false);
        chart.setAnimated(false);

        chartMsg = new Text();
        String cmfontname = manager.getPropertyValue(AppPropertyTypes.METADATA_FONT.name());
        Double cmfontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.METADATA_FONTSIZE.name()));
        chartMsg.setFont(Font.font(cmfontname, cmfontsize));
        //metadataText.setWrappingWidth(Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.METADATA_WRAPWIDTH.name())));
        //chartMsg.setVisible(false);

        leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));
        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        //leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.5);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.5);

        Text leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();
        textArea.setPrefRowCount(10);
        toggleDoneEditing = new Button(manager.getPropertyValue(AppPropertyTypes.TOGGLE_DONE_TEXT.name()));

        metadataText = new Text();
        String mfontname = manager.getPropertyValue(AppPropertyTypes.METADATA_FONT.name());
        Double mfontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.METADATA_FONTSIZE.name()));
        metadataText.setFont(Font.font(mfontname, mfontsize));
        metadataText.setWrappingWidth(Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.METADATA_WRAPWIDTH.name())));

        //processButtonsBox = new HBox();
        //displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));
        //HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        //processButtonsBox.getChildren().addAll(displayButton);
        String iconsPath = "/" + String.join("/",
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String configIconPath = String.join(separator, iconsPath,
                manager.getPropertyValue(AppPropertyTypes.CONFIG_ICON.name()));
        String runIconPath = String.join(separator, iconsPath,
                manager.getPropertyValue(AppPropertyTypes.RUN_ICON.name()));
        String nextIconPath = String.join(separator, iconsPath,
                manager.getPropertyValue(AppPropertyTypes.NEXT_ICON.name()));

        runButton = new Button("Run", new ImageView(new Image(getClass().getResourceAsStream(runIconPath))));
        runButton.setDisable(true);

        nextButton = new Button("Next", new ImageView(new Image(getClass().getResourceAsStream(nextIconPath))));
        nextButton.setVisible(false);

        algochooser = new VBox(10);
        VBox.setMargin(algochooser, new Insets(10, 0, 0, 0));

        Text algotypeText = new Text(manager.getPropertyValue(AppPropertyTypes.ALGO_TYPE_TITLE.name()));
        String atfontname = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double atfontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        algotypeText.setFont(Font.font(atfontname, atfontsize));

        chooseAlgoType = new Accordion();

        //classification algos
        String[] names_classification = new String[algos_classification.size()];
        System.out.println("parsing classification names");
        for (int i = 0; i < names_classification.length; i++) {
            try {
                String name;
                Method m = algos_classification.get(i).getMethod("getName");
                name = (String) (m.invoke(null));
                names_classification[i] = name;
                algo_classes.put(name, algos_classification.get(i));
                System.out.println("\t* " + name);
            } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                System.out.println("\t* " + ex.getClass().getSimpleName()
                        + ": " + algos_classification.get(i).getSimpleName());
            }
        }
        GridPane gridpane_classification = new GridPane();

        gridpane_classification.getColumnConstraints()
                .add(new ColumnConstraints(150));
        toggle_classification = new ToggleGroup();
        for (int i = 0; i < names_classification.length; i++) {
            String algoName = names_classification[i];
            RadioButton b = new RadioButton(algoName);
            b.setOnAction(e -> {
                runButton.setDisable(!algorithms.containsKey(algoName));
            });
            b.setToggleGroup(toggle_classification);
            gridpane_classification.add(b, 0, i); // column, row
            if (i == 0) {
                b.setSelected(true);
            }
            Button settings = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(configIconPath))));
            settings.setOnAction(e -> {
                if (algorithms.containsKey(algoName)) {
                    Classifier algo = (Classifier) algorithms.get(algoName);
                    config_classification.showConfig(algo.getMaxIterations(), algo.getUpdateInterval(), algo.tocontinue());
                } else {
                    config_classification.showConfig();
                }
                if (config_classification.getMaxIterations() != null
                        && config_classification.getUpdateInterval() != null
                        && config_classification.getContinuousRun() != null) {
                    try {
                        Constructor ctor = algo_classes.get(algoName)
                                .getConstructor(DataSet.class, int.class, int.class, boolean.class);
                        algorithms.put(algoName, (Classifier) ctor.newInstance(dataset,
                                config_classification.getMaxIterations(),
                                config_classification.getUpdateInterval(),
                                config_classification.getContinuousRun()));
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | NoSuchMethodException | SecurityException ex) {
                        System.out.println("\t* " + ex.getClass().getSimpleName() + ": " + algoName);
                    }
                    runButton.setDisable(!algorithms.containsKey(
                            ((RadioButton) toggle_classification.getSelectedToggle()).getText()));
                }
            });
            gridpane_classification.add(settings, 1, i); // column, row
        }

        //clustering algos
        String[] names_clustering = new String[algos_clustering.size()];

        System.out.println("parsing clustering names");
        for (int i = 0; i < names_clustering.length; i++) {
            try {
                String name;
                Method m = algos_clustering.get(i).getMethod("getName");
                name = (String) (m.invoke(null));
                names_clustering[i] = name;
                algo_classes.put(name, algos_clustering.get(i));
                System.out.println("\t* " + name);
            } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                System.out.println("\t* " + ex.getClass().getSimpleName()
                        + ": " + algos_clustering.get(i).getSimpleName());
            }
        }
        GridPane gridpane_clustering = new GridPane();

        gridpane_clustering.getColumnConstraints()
                .add(new ColumnConstraints(150));
        toggle_clustering = new ToggleGroup();
        for (int i = 0; i < names_clustering.length; i++) {
            String algoName = names_clustering[i];
            RadioButton b = new RadioButton(names_clustering[i]);
            b.setToggleGroup(toggle_clustering);
            b.setOnAction(e -> {
                runButton.setDisable(!algorithms.containsKey(algoName));
            });
            gridpane_clustering.add(b, 0, i); // column, row
            if (i == 0) {
                b.setSelected(true);
            }
            Button settings = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(configIconPath))));
            settings.setOnAction(e -> {
                if (algorithms.containsKey(algoName)) {
                    Clusterer algo = (Clusterer) algorithms.get(algoName);
                    config_clustering.showConfig(algo.getMaxIterations(), algo.getUpdateInterval(),
                            algo.getNumClusters(), algo.tocontinue());
                } else {
                    config_clustering.showConfig();
                }
                if (config_clustering.getMaxIterations() != null
                        && config_clustering.getUpdateInterval() != null
                        && config_clustering.getNumClusters() != null
                        && config_clustering.getContinuousRun() != null) {
                    try {
                        Constructor ctor = algo_classes.get(algoName)
                                .getConstructor(DataSet.class, int.class, int.class, int.class, boolean.class);
                        //System.out.println("dataset: " + Arrays.asList(dataset.getLabels()));
                        algorithms.put(algoName, (Clusterer) ctor.newInstance(dataset,
                                config_clustering.getMaxIterations(),
                                config_clustering.getUpdateInterval(),
                                config_clustering.getNumClusters(),
                                config_clustering.getContinuousRun()));
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | NoSuchMethodException | SecurityException ex) {
                        System.out.println("\t* " + ex.getClass().getSimpleName() + ": " + algoName);
                    }
                    runButton.setDisable(!algorithms.containsKey(
                            ((RadioButton) toggle_clustering.getSelectedToggle()).getText()));
                }
            });
            gridpane_clustering.add(settings, 1, i); // column, row
        }

        classification = new TitledPane(
                manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_TITLE.name()),
                gridpane_classification);
        clustering = new TitledPane(
                manager.getPropertyValue(AppPropertyTypes.CLUSTERING_TITLE.name()),
                gridpane_clustering);

        classification.setOnMouseClicked(e -> {
            if (!algos_classification.isEmpty()) {
                runButton.setDisable(!algorithms.containsKey(
                        ((RadioButton) toggle_classification.getSelectedToggle()).getText()));
            }
        }
        );
        clustering.setOnMouseClicked(e -> {
            if (!algos_clustering.isEmpty()) {
                runButton.setDisable(!algorithms.containsKey(
                        ((RadioButton) toggle_clustering.getSelectedToggle()).getText()));
            }
        }
        );
        chooseAlgoType.getPanes()
                .addAll(classification, clustering);

        HBox leftButtons = new HBox(15);

        leftButtons.getChildren()
                .addAll(runButton, nextButton);
        algochooser.getChildren()
                .addAll(algotypeText, chooseAlgoType, leftButtons);

        leftPanel.getChildren()
                .addAll(leftPanelTitle, textArea, toggleDoneEditing,
                        metadataText, algochooser);

        VBox rightPanel = new VBox(10);
        StackPane chartPane = new StackPane(chart);

        chartPane.setMaxSize(windowWidth
                * 0.69, windowHeight * 0.69);
        chartPane.setMinSize(windowWidth
                * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(chartPane, Pos.CENTER);

        rightPanel.getChildren()
                .addAll(chartPane, chartMsg);

        workspace = new HBox(leftPanel, rightPanel);

        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren()
                .add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);

        leftPanel.setVisible(LEFTPANE_VISIBLE);
    }

    public void drawLine(double x0, double y0, double x1, double y1, int A, int B, int C) {
        PropertyManager manager = applicationTemplate.manager;
        XYChart.Series<Number, Number> class_line = new XYChart.Series<>();
        class_line.setName(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_LINE_NAME.name()));
        ArrayList<Point2D> points = new ArrayList<>();
        boolean printPoints = false;
        assert !(A == 0 && B == 0); //can't be both 0
        if (A == 0) {
            class_line.getData().add(new XYChart.Data<>(x0, -C / B));
            class_line.getData().add(new XYChart.Data<>(x1, -C / B));
            if (printPoints) {
                System.out.println("Point added: (" + x0 + ", " + -C / B + ")");
                System.out.println("Point added: (" + x1 + ", " + -C / B + ")");
            }
        } else if (B == 0) {
            class_line.getData().add(new XYChart.Data<>(-C / A, y0));
            class_line.getData().add(new XYChart.Data<>(-C / A, y1));
            if (printPoints) {
                System.out.println("Point added: (" + -C / A + ", " + y0 + ")");
                System.out.println("Point added: (" + -C / A + ", " + y1 + ")");
            }
        } else {
            if (pointInRect(x0, y0, x1, y1,
                    -(B * y0 + C) / A, y0)) {
                class_line.getData().add(new XYChart.Data<>(-(B * y0 + C) / A, y0));
                if (printPoints) {
                    System.out.println("Point added: (" + -(B * y0 + C) / A + ", " + y0 + ")");
                }
            } else if (pointInRect(x0, y0, x1, y1,
                    x0, -(A * x0 + C) / B)) {
                class_line.getData().add(new XYChart.Data<>(x0, -(A * x0 + C) / B));
                if (printPoints) {
                    System.out.println("Point added: (" + x0 + ", " + -(A * x0 + C) / B + ")");
                }
            }

            if (pointInRect(x0, y0, x1, y1,
                    -(B * y1 + C) / A, y1)) {
                class_line.getData().add(new XYChart.Data<>(-(B * y1 + C) / A, y1));
                if (printPoints) {
                    System.out.println("Point added: (" + -(B * y1 + C) / A + ", " + y1 + ")");
                }
            } else if (pointInRect(x0, y0, x1, y1,
                    x1, -(A * x1 + C) / B)) {
                class_line.getData().add(new XYChart.Data<>(x1, -(A * x1 + C) / B));
                if (printPoints) {
                    System.out.println("Point added: (" + x1 + ", " + -(A * x1 + C) / B + ")");
                }
            }
        }

        chart.getData().add(class_line);
        try {
            class_line.getNode().setId(manager.getPropertyValue(AppPropertyTypes.AVG_LINE_ID.name()));
            class_line.getData().get(0).getNode().setStyle("-fx-background-radius: 0.0px; -fx-padding: 0.0px;");
            class_line.getData().get(1).getNode().setStyle("-fx-background-radius: 0.0px; -fx-padding: 0.0px;");
            chartMsg.setText("");
        } catch (IndexOutOfBoundsException e) {
            chartMsg.setText(manager.getPropertyValue(AppPropertyTypes.OUTOFBOUNDS_MSG.name()));
        }
    }

    private boolean pointInRect(double x0, double y0, double x1, double y1, double pt_x, double pt_y) {
        return (pt_x >= x0 && pt_x <= x1) && (pt_y >= y0 && pt_y <= y1);
    }

    //public void update
    private void setWorkspaceActions() {
        setTextAreaActions();
        setToggleButtonActions();
        setTitlePaneActions();
        setRunButtonActions();
        //drawLine(0, 0, 110, 110, 20, -64, -300);
    }

    public synchronized void dataChanged_classification(List<Integer> data) {
        //runButton.setDisable(true);
        //scrnshotButton.setDisable(tocontinue);
        textToData();
        NumberAxis x_axis = (NumberAxis) chart.getXAxis();
        NumberAxis y_axis = (NumberAxis) chart.getYAxis();
        x_axis.setForceZeroInRange(false);
        y_axis.setForceZeroInRange(false);
        x_axis.setAutoRanging(false);
        y_axis.setAutoRanging(false);
        drawLine(x_axis.getLowerBound(), y_axis.getLowerBound(),
                x_axis.getUpperBound(), y_axis.getUpperBound(),
                data.get(0), data.get(1), data.get(2));
        //System.out.println("published");
    }

    public void showErrorDialog_classification() { //probably when both A and B = 0
        PropertyManager manager = applicationTemplate.manager;
        ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        String errTitle = manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_ERROR_TITLE.name());
        String errMsg = manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_ERROR_MSG.name());
        dialog.show(errTitle, errMsg);
    }

    public void done_classification() {
        runButton.setDisable(false);
        scrnshotButton.setDisable(false);
        nextButton.setVisible(false);
        toggleDoneEditing.setDisable(false);
        classification.setDisable(classification_disabled);
        clustering.setDisable(false);
        ((AppActions) applicationTemplate.getActionComponent()).setIsAlgoRunningProperty(false);
        System.out.println("done!");
    }

    public synchronized void datachanged_clustering(DataSet ds) {
        //update labels
        AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
        dataComponent.clear();
        dataComponent.loadData(ds);
        chart.getData().clear();
        dataComponent.displayData();
        setDataPointListeners();

    }

    //IF THIS WORKS OUT COMBINE DONE_CLASSIFICATION AND DONE_CLUSTERING CUZ ITS REDUNDANT
    public void done_clustering() {
        runButton.setDisable(false);
        scrnshotButton.setDisable(false);
        nextButton.setVisible(false);
        toggleDoneEditing.setDisable(false);
        classification.setDisable(classification_disabled);
        clustering.setDisable(false);
        ((AppActions) applicationTemplate.getActionComponent()).setIsAlgoRunningProperty(false);
        System.out.println("done!");
    }

    private void textToData() {
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
    }

    private void setRunButtonActions() {
        PropertyManager manager = applicationTemplate.manager;
        runButton.setOnAction(event -> {
            //System.out.println("dataSet: " + Arrays.asList(dataset.getLabels()));
            ((AppActions) applicationTemplate.getActionComponent()).setIsAlgoRunningProperty(true);
            classification_disabled = classification.isDisable();
            classification.setDisable(true);
            clustering.setDisable(true);
            toggleDoneEditing.setDisable(true);
            textToData();

            String algoName;
            Algorithm algo;
            if (chooseAlgoType.getExpandedPane().getText().equals(
                    manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_TITLE.name()))) {
                algoName = ((RadioButton) toggle_classification.getSelectedToggle()).getText();
                algo = algorithms.get(algoName);
            } else if (chooseAlgoType.getExpandedPane().getText().equals(
                    manager.getPropertyValue(AppPropertyTypes.CLUSTERING_TITLE.name()))) {
                algoName = ((RadioButton) toggle_clustering.getSelectedToggle()).getText();
                algo = algorithms.get(algoName);
            } else {
                throw new IllegalStateException("wat did u pick?????");
            }
            algo.setDataSet(dataset);
            nextButton.setVisible(!algo.tocontinue()); //visible when non-cont
            algo.subscribe(this);
            nextButton.setOnAction(e -> {
                //scrnshotButton.setDisable(true);
                algo.wake();
            });

            runButton.setDisable(true);
            scrnshotButton.setDisable(algo.tocontinue());
            System.out.println(algoName);

            if (algoTask != null) {
                algoTask.cancel();
            }
            algoTask = new Task() {
                @Override
                protected Object call() throws Exception {
                    if (!isCancelled()) {
                        algo.run();
                    }
                    return null;
                }
            };
            new Thread(algoTask).start();
        });
    }

    public void cancelAlgoTask() {
        algoTask.cancel();
        done_classification(); //CHECK IF CLUSTERING HERE
        //or done_clustering();
    }

    private void setTitlePaneActions() {
        chooseAlgoType.expandedPaneProperty().addListener((observable, oldValue, newValue) -> {
            runButton.setVisible(newValue != null);
            runButton.setDisable((newValue == classification && algos_classification.isEmpty())
                    || (newValue == clustering && algos_clustering.isEmpty())); //selected titlepane is empty
        });
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

    private void setToggleButtonActions() {
        PropertyManager manager = applicationTemplate.manager;
        toggleDoneEditing.setOnAction(event -> {
            if (toggleDoneEditing.getText().equals(manager.getPropertyValue(AppPropertyTypes.TOGGLE_DONE_TEXT.name()))) {
                //if (hasNewText) {
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
                    //System.out.println(e);
                    return;
                }
                chart.getData().clear();
                //dataComponent.displayData();
                //setDataPointListeners();
                dataComponent.updateMetadata(manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name()));
                setMetadataText(dataComponent.getMetadata());
                textArea.setDisable(true);
                toggleDoneEditing.setText(manager.getPropertyValue(AppPropertyTypes.TOGGLE_EDIT_TEXT.name()));
                algochooser.setVisible(true);
                /*
                if (chooseAlgoType.getExpandedPane() != null) {
                    runButton.setDisable(false);
                }*/
                //scrnshotButton.setDisable(false);
            } else { //done --> edit
                textArea.setDisable(false);
                algochooser.setVisible(false);
                metadataText.setText("");
                toggleDoneEditing.setText(manager.getPropertyValue(AppPropertyTypes.TOGGLE_DONE_TEXT.name()));
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
