package algorithms;

import dataprocessors.DataSet;
import java.util.List;
import javafx.application.Platform;
import ui.AppUI;

/**
 * An abstract class for clustering algorithms. The output for these algorithms
 * is a straight line, as described in Appendix C of the software requirements
 * specification (SRS). The {@link #output} is defined with extensibility in
 * mind.
 *
 * @author Ritwik Banerjee
 */
public abstract class Clusterer implements Algorithm {

    /**
     * See Appendix C of the SRS. Defining the output as a list instead of a
     * triple allows for future extension into polynomial curves instead of just
     * straight lines. See 3.4.4 of the SRS.
     */
    //protected List<Integer> output;
    protected final int numClusters;
    protected AppUI observer;
    protected DataSet dataset;

    public DataSet getDataSet() {
        return dataset;
    }
    
    public int getNumClusters() {
        return numClusters;
    }

    public Clusterer(int k) {
        if (k < 2) {
            k = 2;
        } else if (k > 4) {
            k = 4;
        }
        numClusters = k;
    }

    //randomly assign one of the labels {1, 2, ..., n} to each instance

    @Override
    public void subscribe(AppUI ui) {
        observer = ui;
    }

    @Override
    public void unsubscribe(AppUI ui) {
        observer = null;
    }

    @Override
    public void publish() {
        if (observer != null) {
            Platform.runLater(() -> {
                observer.datachanged_clustering(dataset);
            });
        }
    }

    @Override
    public void done() {
        if (observer != null) {
            Platform.runLater(() -> {
                observer.done_clustering();
            });
        }
    }
}
