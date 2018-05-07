package algorithms;

import java.util.List;
import javafx.application.Platform;
import ui.AppUI;

/**
 * An abstract class for classification algorithms. The output for these
 * algorithms is a straight line, as described in Appendix C of the software
 * requirements specification (SRS). The {@link #output} is defined with
 * extensibility in mind.
 *
 * @author Ritwik Banerjee
 */
public abstract class Classifier implements Algorithm {

    /**
     * See Appendix C of the SRS. Defining the output as a list instead of a
     * triple allows for future extension into polynomial curves instead of just
     * straight lines. See 3.4.4 of the SRS.
     */
    protected List<Integer> output;
    protected AppUI observer;

    public List<Integer> getOutput() {
        return output;
    }

    public void subscribe(AppUI ui) {
        observer = ui;
    }

    public void unsubscribe(AppUI ui) {
        observer = null;
    }

    public void publish() {
        if (observer != null) {
            Platform.runLater(() -> {
                if (output.get(0) == 0 && output.get(1) == 0) { //invalid
                    observer.showErrorDialog_classification();
                } else {
                    observer.dataChanged_classification(output);
                }
            });

        }
    }
    
    public void done() {
        if (observer != null) {
            Platform.runLater(() -> {
                observer.done_classification();
            });
        }
    }
}
