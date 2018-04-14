package bargraph;


import java.util.ArrayList;
import javafx.scene.layout.VBox;

/**
 * View that displays data as a list of text fields.
 *
 * @author E. Stark
 * @version 20180407
 *
 */
public class TextView extends VBox implements DataListener {

    private final int COLS = 10;
    private final DataModel data;

    /**
     * Initialize a TextView for a specified data model.
     * 
     * @param data  the data model to be displayed in the view.
     */
    public TextView(DataModel data) {
        this.data = data;
        data.subscribe(this);
        initialize();
    }

    private void initialize() {
        for (int i = 0; i < data.size(); i++) {
            double value = data.getValue(i);
            NumericField text = new NumericField(value, COLS);
            getChildren().add(text);
            final int ii = i;
            text.setOnAction(e -> {
                try {
                    data.setValue(ii, Double.parseDouble(text.getText()));
                } catch (NumberFormatException x) {
                }
            });
        }
    }

    @Override
    public void dataChanged(int index, double value) {
        // TODO Auto-generated method stub
        getChildren().clear();
        for (int i = 0; i < data.size(); i++) {
            double v = data.getValue(i);
            NumericField text = new NumericField(v, COLS);
            getChildren().add(text);
            final int ii = i;
            text.setOnAction(e -> {
                try {
                    data.setValue(ii, Double.parseDouble(text.getText()));
                } catch (NumberFormatException x) {
                }
            });
        }
    }

}
