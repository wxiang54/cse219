package algorithms.clustering;

import algorithms.Clusterer;
import algorithms.classification.RandomClassifier;
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
public class RandomClusterer extends Clusterer {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")

    private final int maxIterations;
    private final int updateInterval;
    private final boolean continuousRun;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

    public RandomClusterer(DataSet dataset,
            int maxIterations,
            int updateInterval,
            int numClusters,
            boolean tocontinue) {
        super(numClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue); //becomes lock
        this.continuousRun = tocontinue; //permanent version
    }
    
    public static String getName() {
        return "Random Clusterer";
    }

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
    
    @Override
    public synchronized void run() {
        for (int i = 1; i <= maxIterations; i++) {
            dataset.getLocations().forEach((instanceName, location) -> {
                dataset.getLabels().put(instanceName, Integer.toString(RAND.nextInt(numClusters)));
            });
            
            if (i >= maxIterations || (i > maxIterations * .6 && RAND.nextDouble() < 0.05)) {
                System.out.printf("Iteration number %d\n", i);
                publish();
                break; //HANDLE THIS
            }
            
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d\n", i);
                //System.out.println("labels: " + Arrays.asList(dataset.getLabels()));
                //System.out.println("points: " + Arrays.asList(dataset.getLocations()));
                publish();
                if (continuousRun) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    tocontinue.set(false);
                    //stall until tocontinue() changed back to true
                    while (!tocontinue()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            System.out.println("task interrupted...");
                            //do nothing
                        }
                    }
                }
            }
        }
        tocontinue.set(continuousRun);
        done();
    }

    /**
     * A placeholder main method to just make sure this code runs smoothly
     */
    public static void main(String... args) throws IOException {
        DataSet dataset = DataSet.fromTSDFile(Paths.get("/path/to/some-data.tsd"));
        RandomClusterer clusterer = new RandomClusterer(dataset, 100, 5, 2, true);
        clusterer.run(); // no multithreading yet
    }
}
