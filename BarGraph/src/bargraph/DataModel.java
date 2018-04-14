package bargraph;


import java.util.ArrayList;

/**
 * Data model underlying bar graph display. Objects represent indexed
 * lists of numbers.
 *
 * @author E. Stark
 * @version 20180407
 *
 */
public class DataModel {
    
    private final ArrayList<Double> data;
    private ArrayList<DataListener> observers;

    /**
     * Initialize a data model with a specified sequence of values.
     * 
     * @param dataValues the sequence of values.
     */
    public DataModel(double[] dataValues) {
        data = new ArrayList<>();
        observers = new ArrayList<>();
        for (int i = 0; i < dataValues.length; i++) {
            data.add(dataValues[i]);
        }
    }

    /**
     * Get the number of values in the data model.
     * 
     * @return the number of values in the data model.
     */
    public int size() {
        return data.size();
    }
    
    /**
     * Get the value at a specified index in the data model.
     * 
     * @param i  The index of the value to be retrieved.
     * @return  The value at the specified index.
     */
    public double getValue(int i) {
        return data.get(i);
    }
    
    /**
     * Set the value at a specified index in the data model.
     * 
     * @param i  The index whose value is to be set.
     * @param v  The value to set.
     */
    public void setValue(int i, double v) {
        data.set(i, v);
        publish(i, v);
        
    }
    
    public void subscribe(DataListener dl) {
        observers.add(dl);
        System.out.println(dl);
    }
    
    public void unsubscribe(DataListener dl) {
        observers.remove(dl);
    }
    
    public void publish(int i, double v) {
        for (DataListener dl : observers) {
            dl.dataChanged(i, v);
        }
    }
}
