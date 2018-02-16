package actions;

import java.io.BufferedWriter;
import java.io.File;
import static java.io.File.separator;
import java.io.FileWriter;
import vilij.components.ActionComponent;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import static settings.AppPropertyTypes.*;
import ui.AppUI;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;

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

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void handleNewRequest() {
        // TODO for homework 1
        try {
            if (promptToSave()) {
                applicationTemplate.getUIComponent().clear();
            }
        } catch (IOException ex) {
            //do nothing for now
        }
    }

    @Override
    public void handleSaveRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleExitRequest() {
        // TODO for homework 1
        Platform.exit();
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
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
        // TODO for homework 1
        PropertyManager manager = applicationTemplate.manager;
        Dialog confirmation = ConfirmationDialog.getDialog();
        confirmation.show(
                manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()), 
                manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
        if (((ConfirmationDialog) confirmation).getSelectedOption() == ConfirmationDialog.Option.CANCEL) {
            return false;
        } else {
            if (((ConfirmationDialog) confirmation).getSelectedOption() == ConfirmationDialog.Option.YES) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName(manager.getPropertyValue(DEFAULT_FILE_NAME.name()));
                String dataPath = "/" + String.join(separator,
                                             manager.getPropertyValue(DATA_RESOURCE_PATH.name()));
                fileChooser.setInitialDirectory(new File(dataPath));
                fileChooser.getExtensionFilters().add(
                        new ExtensionFilter(
                                manager.getPropertyValue(DATA_FILE_EXT_DESC.name()),
                                manager.getPropertyValue(DATA_FILE_EXT.name())));
                
                File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                
                if (file != null) { //user made a selection
                    dataFilePath = file.toPath();
                    String data = ((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText();
                    BufferedWriter out = new BufferedWriter(new FileWriter(file));
                    out.write(data);
                    out.close();
                }
                else {
                    //'cancel' clicked or ESC pressed
                    return false;
                }
            } else { //assume NO was clicked
                //do nothing, move on
            }
            return true;
        }
    }
}
