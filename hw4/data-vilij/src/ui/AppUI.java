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
import javafx.scene.control.Accordion;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
    public static final boolean LEFTPANE_VISIBLE = false; //testing purposes

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton;              // toolbar button to take a screenshot of the data
    private LineChart<Number, Number> chart;    // the chart where data will be displayed
    private TextArea textArea;                  // text area for new data input
    private boolean hasNewText;                 // whether text area has new data since last display
    private String appCSSPath;                  // path to data-vilij css file
    private String[] remainingData;             // when > 10 lines, rest of data should be stored here
    private int remainingDataInd;               // keeps track of where you are in remainingData
    private boolean leftPanelShown;             // self-evident
    private VBox leftPanel;                     // to add TextBox after New/Load button pressed
    //private HBox processButtonsBox;           // to add after New/Load button pressed
    private Button runButton;                   // workspace button to run algorithm
    private Button toggleDoneEditing;           // toggle for textarea after clicking New
    private Text metadataText;                  // algo metadata area
    private VBox algochooser;                   // set of controls/elements related to choosing algo

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
        remainingData = null;
        remainingDataInd = 0;
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
            dataComponent.showFileTooLongDialog(allData.length);
        } else {
            remainingData = null;
            textArea.textProperty().setValue(data);
        }
        showLeftPanel_load();
    }

    public void disableSaveButton() {
        saveButton.setDisable(true);
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
    }

    public void showLeftPanel_new() {
        PropertyManager manager = applicationTemplate.manager;
        leftPanel.setVisible(true);
        textArea.setDisable(false);
        toggleDoneEditing.setVisible(true);
        algochooser.setVisible(false);
        metadataText.setText("");
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

        leftPanel = new VBox(14);
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
        runButton = new Button("Test");

        String iconsPath = "/" + String.join("/",
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String configIconPath = String.join(separator, iconsPath,
                manager.getPropertyValue(AppPropertyTypes.CONFIG_ICON.name()));

        algochooser = new VBox(10);
        VBox.setMargin(algochooser, new Insets(10, 0, 0, 0));

        Text algotypeText = new Text(manager.getPropertyValue(AppPropertyTypes.ALGO_TYPE_TITLE.name()));
        String atfontname = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double atfontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        algotypeText.setFont(Font.font(atfontname, atfontsize));
        //algotypeText.setUnderline(true);

        Accordion chooseAlgoType = new Accordion();

        //classification algos
        String[] classification_algos = {"Algorithm A", "Algorithm B", "Algorithm C"};
        GridPane gridpane_classification = new GridPane();
        gridpane_classification.getColumnConstraints().add(new ColumnConstraints(150));
        ToggleGroup toggle_classification = new ToggleGroup();
        for (int i = 0; i < classification_algos.length; i++) {
            RadioButton b = new RadioButton(classification_algos[i]);
            b.setToggleGroup(toggle_classification);
            gridpane_classification.add(b, 0, i); // column, row
            if (i == 0) {
                b.setSelected(true);
            }
            Button settings = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(configIconPath))));
            gridpane_classification.add(settings, 1, i); // column, row
        }

        //clustering algos
        String[] clustering_algos = {"Algorithm D", "Algorithm E", "Algorithm F"};
        GridPane gridpane_clustering = new GridPane();
        gridpane_clustering.getColumnConstraints().add(new ColumnConstraints(150));
        ToggleGroup toggle_clustering = new ToggleGroup();
        for (int i = 0; i < clustering_algos.length; i++) {
            RadioButton b = new RadioButton(clustering_algos[i]);
            b.setToggleGroup(toggle_clustering);
            gridpane_clustering.add(b, 0, i); // column, row
            if (i == 0) {
                b.setSelected(true);
            }
            Button settings = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(configIconPath))));
            gridpane_clustering.add(settings, 1, i); // column, row
        }

        TitledPane classification = new TitledPane("Classification", gridpane_classification);
        TitledPane clustering = new TitledPane("Clustering", gridpane_clustering);
        chooseAlgoType.getPanes().addAll(classification, clustering);

        algochooser.getChildren().addAll(algotypeText, chooseAlgoType, runButton);

        leftPanel.getChildren().addAll(leftPanelTitle, textArea, toggleDoneEditing,
                metadataText, algochooser);

        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);

        leftPanel.setVisible(LEFTPANE_VISIBLE);
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setToggleButtonActions();
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

    /*
    private void setCheckBoxActions() {
        toggleReadOnly.selectedProperty().addListener((observable, oldValue, newValue) -> {
            textArea.setDisable(newValue);
        });
    }
     */
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
                    System.out.println(e);
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
                //scrnshotButton.setDisable(false);
                //}
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
