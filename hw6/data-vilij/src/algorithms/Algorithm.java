package algorithms;

import dataprocessors.DataSet;
import ui.AppUI;

/**
 * This interface provides a way to run an algorithm
 * on a thread as a {@link java.lang.Runnable} object.
 *
 * @author Ritwik Banerjee
 */
public interface Algorithm extends Runnable {

    int getMaxIterations();

    int getUpdateInterval();

    boolean tocontinue();
    
    void wake();
    void subscribe(AppUI ui);
    void unsubscribe(AppUI ui);
    void publish();
    void done();
    void setDataSet(DataSet ds);
}
