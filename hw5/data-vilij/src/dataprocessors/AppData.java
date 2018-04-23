package dataprocessors;

import java.io.BufferedReader;
import java.io.FileReader;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This is the concrete application-specific implementation of the data
 * component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor processor;
    private ApplicationTemplate applicationTemplate;
    
    /*
    //file metadata, updated every load
    private int numInstances;
    private int numLabels;
    private Set<String> labelNames;
    private Path dataPath;

    public int getNumInstances()        {return numInstances;}
    public int getNumLabels()           {return numLabels;}
    public Set<String> getLabelNames()  {return labelNames;}
    public Path getDataPath()           {return dataPath;}
    */
    
    private String metadata;
    public String getMetadata() {return metadata;}
    
    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
        /*
        this.numInstances = this.numLabels = -1;
        this.labelNames = new HashSet<String>();
        this.dataPath = null; //??
        */
    }

    public void processTextArea() throws Exception {
        clear();
        processor.processString(
                ((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());
    }

    public void showLoadErrorDialog(String msg, String src) {
        ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager = applicationTemplate.manager;
        String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
        String errMsg = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
        dialog.show(errTitle, errMsg + src + "\n" + msg);
    }

    public void showSaveErrorDialog(String msg) {
        ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager = applicationTemplate.manager;
        String errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String errMsg = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput + "\n" + msg);
    }

    public void showFileTooLongDialog(int numLines) {
        ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager = applicationTemplate.manager;
        String errTitle = manager.getPropertyValue(AppPropertyTypes.FILE_TOO_LONG_TITLE.name());
        String errMsg = String.format(manager.getPropertyValue(AppPropertyTypes.FILE_TOO_LONG_MSG.name()), numLines);
        dialog.show(errTitle, errMsg);
    }

    public void updateMetadata(String src) {
        PropertyManager manager = applicationTemplate.manager;
        String metadataFormat = manager.getPropertyValue(AppPropertyTypes.METADATA_FORMAT.name());
        int numInstances, numLabels;
        if (processor.getNumInstances() >= 0) {
            numInstances = processor.getNumInstances();
            numLabels = processor.getLabelNames().size();
            String labels = "";
            for (String s : processor.getLabelNames()) {
                labels += "\n\t- " + s;
            }
            metadata = String.format(metadataFormat, numInstances, numLabels, src, labels);
            if (numLabels != 2) {
                ((AppUI)applicationTemplate.getUIComponent()).setClassificationDisable(true);
            } else {
                ((AppUI)applicationTemplate.getUIComponent()).setClassificationDisable(false);
            }
        } else {
            //metadata = ""; //replace??
        }
    }
    
    @Override
    public void loadData(Path dataFilePath) {
        // TODO: NOT A PART OF HW 1
        PropertyManager manager = applicationTemplate.manager;
        try {
            FileReader fr = new FileReader(dataFilePath.toFile());
            BufferedReader br = new BufferedReader(fr);
            String curLine = br.readLine();
            String dataString = "";
            while (curLine != null) {
                dataString += curLine + "\n";
                curLine = br.readLine();
            }
            clear();
            processor.processString(dataString); //stops here if invalid data
            ((AppUI) applicationTemplate.getUIComponent()).updateTextArea(dataString);
            updateMetadata(dataFilePath.toString());
            
        } catch (Exception e) {
            //System.out.println(e);
            showLoadErrorDialog(e.getMessage(),
                    manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name()));
        }
    }

    public void loadData(String dataString) throws Exception {
        PropertyManager manager = applicationTemplate.manager;
        try {
            processor.processString(dataString);
        } catch (Exception e) {
            //System.out.println(e);
            showLoadErrorDialog(e.getMessage(),
                    manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name()));
            throw e;
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        // NOTE: completing this method was not a part of HW 1. You may have implemented file saving from the
        // confirmation dialog elsewhere in a different way.
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            AppUI ui = (AppUI) applicationTemplate.getUIComponent();
            String curText = ui.getTextArea().getText();
            String[] remainingData = ui.getRemainingData();
            int remainingDataInd = ui.getRemainingDataInd();
            String toAdd = curText.charAt(curText.length() - 1) == '\n' ? "" : "\n";
            if (remainingData != null) {
                for (int i = remainingDataInd; i < remainingData.length; i++) {
                    toAdd += remainingData[i] + "\n";
                }
            }
            writer.write(curText + toAdd);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
}
