package algorithms;
import dataprocessors.DataSet;
import java.util.List;

/**
 * An abstract class for clustering algorithms. The output for these
 * algorithms is a straight line, as described in Appendix C of the software
 * requirements specification (SRS). The {@link #output} is defined with
 * extensibility in mind.
 *
 * @author Ritwik Banerjee
 */
public abstract class Clusterer implements Algorithm {

    /**
     * See Appendix C of the SRS. Defining the output as a list instead of a
     * triple allows for future extension into polynomial curves instead of just
     * straight lines. See 3.4.4 of the SRS.
     */
    protected List<Integer> output;

    public void getOutput() {
        //randomly assign one of the labels {1, 2, ..., n} to each instance
    }

}
