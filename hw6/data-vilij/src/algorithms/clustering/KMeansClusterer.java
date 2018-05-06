package algorithms.clustering;

import algorithms.Clusterer;
import algorithms.classification.RandomClassifier;
import dataprocessors.DataSet;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private List<Point2D> centroids;

    private final int maxIterations;
    private final int updateInterval;
    private final boolean continuousRun;
    private final AtomicBoolean tocontinue;

    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, int numClusters, boolean tocontinue) {
        super(numClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.continuousRun = tocontinue; //permanent version
    }

    public static String getName() {
        return "K-Means Clusterer";
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
        initializeCentroids();
        int iteration = 0;
        while (iteration++ < maxIterations) {
            assignLabels();
            recomputeCentroids();
            
            if (iteration % updateInterval == 0) {
                System.out.printf("Iteration number %d\n", iteration);
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

    private void initializeCentroids() {
        Set<String> chosen = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random r = new Random();
        while (chosen.size() < numClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i))) {
                ++i;
            }
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance = Double.MAX_VALUE;
            int minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }
}
