package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,
    APP_CSS_RESOURCE_FILENAME,

    /* user interface icon file names */
    SCREENSHOT_ICON,
    RUN_ICON,
    CONFIG_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    FORMATTING_ERROR_MSG,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    LOAD_WORK_TITLE,
    FILE_TOO_LONG_TITLE,
    FILE_TOO_LONG_MSG,
    CONFIG_DIALOG_TITLE,
    CONFIG_DIALOG_ITERATIONS,
    CONFIG_DIALOG_INTERVAL,
    CONFIG_DIALOG_CONT_RUN,
    CONFIG_DIALOG_CLUSTERS,
    
    /* application-specific messages */
    SAVE_UNSAVED_WORK,
    
    /* application-specific parameters */
    DATA_RESOURCE_PREFIX,
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    IMG_FILE_EXT,
    IMG_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    LEFT_PANE_TITLE,
    LEFT_PANE_TITLEFONT,
    LEFT_PANE_TITLESIZE,
    ALGO_TYPE_TITLE,
    CHART_TITLE,
    RUN_BUTTON_TEXT,
    TOGGLE_DONE_TEXT,
    TOGGLE_EDIT_TEXT,
    AVG_LINE_NAME,
    AVG_LINE_ID,
    METADATA_FORMAT,
    METADATA_FONT,
    METADATA_FONTSIZE,
    METADATA_WRAPWIDTH,
    
    CLASSIFICATION_ALGOS,
    CLUSTERING_ALGOS
}
