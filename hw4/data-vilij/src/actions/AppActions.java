package actions;

import dataprocessors.AppData;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import ui.AppUI;

import static java.io.File.separator;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import settings.AppPropertyTypes;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;

/**
 * This is the concrete implementation of the action handlers required by the
 * application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /**
     * The application to which this class of actions belongs.
     */
    private ApplicationTemplate applicationTemplate;

    /**
     * Path to the data file currently active.
     */
    Path dataFilePath;

    /**
     * The boolean property marking whether or not there are any unsaved
     * changes.
     */
    SimpleBooleanProperty isUnsaved;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public void setIsUnsavedProperty(boolean property) {
        isUnsaved.set(property);
    }

    @Override
    public void handleNewRequest() {
        try {
            if (!isUnsaved.get() || promptToSave()) {
                AppUI ui = (AppUI)applicationTemplate.getUIComponent();
                applicationTemplate.getDataComponent().clear();
                ui.clear();
                isUnsaved.set(false);
                dataFilePath = null;
                ui.showLeftPanel(true);
                ui.getTextArea().setDisable(false);
            }
        } catch (IOException e) {
            errorHandlingHelper();
        }
    }

    @Override
    public void handleSaveRequest() {
        try {
            ((AppData) applicationTemplate.getDataComponent()).processTextArea();
        } catch (Exception e) {
            ((AppData) applicationTemplate.getDataComponent()).showSaveErrorDialog(e.getMessage());
            return;
        }
        PropertyManager manager = applicationTemplate.manager;
        if (dataFilePath == null) { //file has not been saved yet
            String dataPath = String.join(separator,
                    manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PREFIX.name()),
                    manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name()));

            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(dataPath));
            fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

            String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
            String extension = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
            ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*%s)", description, extension),
                    String.format("*%s", extension));
            fileChooser.getExtensionFilters().add(extFilter);
            File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if (selected == null) {
                return;
            }
            dataFilePath = selected.toPath();
        }
        try {
            save();
        } catch (IOException e) {
            errorHandlingHelper();
        }
    }

    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
        PropertyManager manager = applicationTemplate.manager;
        try {
            if (isUnsaved.get()) {
                if (!promptToSave()) {
                    return;
                }
            }
        } catch (IOException e) {
            errorHandlingHelper();
            return;
        }

        String dataPath = String.join(separator, //always defaults to '/data' for now
                manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PREFIX.name()),
                manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name()));

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(dataPath));
        fileChooser.setTitle(manager.getPropertyValue(AppPropertyTypes.LOAD_WORK_TITLE.name()));
        String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
        String extension = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*%s)", description, extension),
                String.format("*%s", extension));
        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

        if (selected != null) {
            dataFilePath = selected.toPath();
            AppUI ui = (AppUI) applicationTemplate.getUIComponent();
            AppData data = (AppData) applicationTemplate.getDataComponent();
            data.loadData(dataFilePath);
            ui.disableSaveButton();
            data.updateMetadata(dataFilePath);
            ui.setMetadataText(data.getMetadata());
            isUnsaved.set(false);
        } else {
            //return false; // if user presses escape after initially selecting 'yes'
        }
    }

    @Override
    public void handleExitRequest() {
        try {
            if (!isUnsaved.get() || promptToSave()) {
                System.exit(0);
            }
        } catch (IOException e) {
            errorHandlingHelper();
        }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
        PropertyManager manager = applicationTemplate.manager;

        String dataPath = String.join(separator, //always defaults to '/data' for now
                manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PREFIX.name()),
                manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name()));
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(dataPath));
        fileChooser.setTitle(manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()));
        String description = manager.getPropertyValue(AppPropertyTypes.IMG_FILE_EXT_DESC.name());
        String extension = manager.getPropertyValue(AppPropertyTypes.IMG_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*%s)", description, extension),
                String.format("*%s", extension));
        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

        if (selected != null) {
            WritableImage img = ((AppUI) applicationTemplate.getUIComponent())
                    .getChart().snapshot(new SnapshotParameters(), null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", selected);
            } catch (IOException e) {
                ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                String errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
                String errMsg = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
                String errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
                dialog.show(errTitle, errMsg + errInput);
                throw e;
            }
        }
    }

    /**
     * This helper method verifies that the user really wants to save their
     * unsaved work, which they might not want to do. The user will be presented
     * with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and
     * continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the
     * action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to
     * continue with the action, but also does not want to save the work at this
     * point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and
     * <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        PropertyManager manager = applicationTemplate.manager;
        ConfirmationDialog dialog = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if (dialog.getSelectedOption() == null) {
            return false; // if user closes dialog using the window's close button
        }
        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            handleSaveRequest();
            try {
                save();
            } catch (Exception e) {
                return false;
            }
        }
        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    private void save() throws IOException {
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);
        ((AppUI) applicationTemplate.getUIComponent()).disableSaveButton();
    }

    private void errorHandlingHelper() {
        ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager = applicationTemplate.manager;
        String errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String errMsg = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }
}
