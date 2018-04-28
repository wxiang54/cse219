package algorithms.classification;

import algorithms.Classifier;
import dataprocessors.DataSet;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;
    private final boolean continuousRun;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    @Override
    public synchronized void wake() {
        tocontinue.set(true);
        notifyAll();
    }

    public RandomClassifier(DataSet dataset,
            int maxIterations,
            int updateInterval,
            boolean tocontinue) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue); //becomes lock
        this.continuousRun = tocontinue; //permanent version
    }

    @Override
    public synchronized void run() {
        for (int i = 1; i <= maxIterations; i++) {
            /*
            int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int constant = new Double(RAND.nextDouble() * 100).intValue();
             */
            int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant = RAND.nextInt(11);

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                publish();
                break; //HANDLE THIS
            }

            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i); //
                flush();
                publish();
                if (continuousRun) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    tocontinue.set(false);
                    if (i + 1 >= maxIterations) {
                        break;
                    }
                    //stall until tocontinue() changed back to true
                    while (!tocontinue()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            System.out.println("???????//");
                            //do nothing
                        }
                    }
                }
            }
        }
        done();
    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    /**
     * A placeholder main method to just make sure this code runs smoothly
     */
    public static void main(String... args) throws IOException {
        DataSet dataset = DataSet.fromTSDFile(Paths.get("/path/to/some-data.tsd"));
        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
        classifier.run(); // no multithreading yet
    }
}
