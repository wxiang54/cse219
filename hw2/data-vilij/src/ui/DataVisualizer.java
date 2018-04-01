package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.stage.Stage;
import vilij.settings.InitializationParams;
import vilij.templates.ApplicationTemplate;


/**
 * The main class from which the application is run. The various components used here must be concrete implementations
 * of types defined in {@link vilij.components}.
 *
 * @author Ritwik Banerjee
 */
public final class DataVisualizer extends ApplicationTemplate {

    @Override
    public void start(Stage primaryStage) {
        dialogsAudit(primaryStage);
        if (propertyAudit())
            userInterfaceAudit(primaryStage);
    }

    @Override
    protected boolean propertyAudit() {
        boolean failed = manager == null || !(loadProperties(InitializationParams.PROPERTIES_XML) 
                && loadProperties(InitializationParams.WORKSPACE_PROPERTIES_XML));
        if (failed)
            errorDialog.show(InitializationParams.LOAD_ERROR_TITLE.getParameterName(), 
                    InitializationParams.PROPERTIES_LOAD_ERROR_MESSAGE.getParameterName());
        return !failed;
    }

    @Override
    protected void userInterfaceAudit(Stage primaryStage) {
        setUIComponent(new AppUI(primaryStage, this));
        setActionComponent(new AppActions(this));
        setDataComponent(new AppData(this));

        uiComponent.initialize();
    }
    
    public static void main(String[] args) {
        launch();
    }

}
