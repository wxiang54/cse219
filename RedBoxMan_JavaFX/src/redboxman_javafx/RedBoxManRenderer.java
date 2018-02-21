package redboxman_javafx;

import java.io.InputStream;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 *
 * @author McKillaGorilla
 */
public class RedBoxManRenderer extends Application {
    Canvas canvas;
    GraphicsContext gc;
    ArrayList<Point2D> imagesRedBoxManLocations;
    ArrayList<Point2D> shapesRedBoxManLocations;
    Image redBoxManImage;
    
    @Override
    public void start(Stage primaryStage) {
	// INIT THE DATA MANAGERS
	imagesRedBoxManLocations = new ArrayList<>();
	shapesRedBoxManLocations = new ArrayList<>();
	
	// LOAD THE RED BOX MAN IMAGE
        InputStream str = getClass().getResourceAsStream("/RedBoxMan.png");
	redBoxManImage = new Image(str);
	
	// MAKE THE CANVAS
	canvas = new Canvas();
	canvas.setStyle("-fx-background-color: cyan");
	gc = canvas.getGraphicsContext2D();

	// PUT THE CANVAS IN A CONTAINER
	Group root = new Group();
	root.getChildren().add(canvas);
	
	canvas.setOnMouseClicked(e->{
	    if (e.isShiftDown()) {
		shapesRedBoxManLocations.add(new Point2D(e.getX(), e.getY()));
		render();
	    }
	    else if (e.isControlDown()) {
		imagesRedBoxManLocations.add(new Point2D(e.getX(), e.getY()));
		render();
	    }
	    else {
		clear();
	    }
	});
	
	// PUT THE CONTAINER IN A SCENE
	Scene scene = new Scene(root, 800, 600);
	canvas.setWidth(scene.getWidth());
	canvas.setHeight(scene.getHeight());

	// AND START UP THE WINDOW
	primaryStage.setTitle("Red Box Man Renderer");
	primaryStage.setScene(scene);
	primaryStage.show();
    }
    
    public void clearCanvas() {
	gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    public void clear() {
	shapesRedBoxManLocations.clear();
	imagesRedBoxManLocations.clear();
	render();
    }
    
    public void render() {
	clearCanvas();
	for (int i = 0; i < shapesRedBoxManLocations.size(); i++) {
	    renderShapeRedBoxMan(shapesRedBoxManLocations.get(i));
	}
	for (int j = 0; j < imagesRedBoxManLocations.size(); j++) {
	    renderImageRedBoxMan(imagesRedBoxManLocations.get(j));
	}
    }
    
    public void renderShapeRedBoxMan(Point2D location) {
	String headColor =      "#DD0000";
	String outlineColor =   "#000000";
        String eyeColor =       "#FFFF00";
        String pupilColor =     "#000000";
        String mouthColor =     "#000000";
        String bodyColor =      "#000000";
	int headW = 115;
	int headH = 88;
        int eyeW = 35;
        int eyeH = 25;
        int[] eye1offset = new int[]{10, 10};
        int[] eye2offset = new int[]{70, 10};
        int pupilW = 10;
        int pupilH = 10;
        int[] pupiloffset = new int[]{10, 10};
        int mouthW = 100;
        int mouthH = 10;
        int[] mouthoffset = new int[]{30, 50};
        int chestW = 70;
        int chestH = 40;
        int[] chestoffset = new int[]{30, 85};
        int bodyW = 60;
        int bodyH = 20;
        int[] bodyoffset = new int[]{35, 125};
        int footW = 10;
        int footH = 20;
        int[] foot1offset = new int[]{30, 145};
        int[] foot2offset = new int[]{90, 145};
    
	// DRAW HIS RED HEAD
        gc.setFill(Paint.valueOf(headColor));
	gc.fillRect(location.getX(), location.getY(), headW, headH);
        gc.beginPath();
	gc.setStroke(Paint.valueOf(outlineColor));
	gc.setLineWidth(1);
	gc.rect(location.getX(), location.getY(), headW, headH);
	gc.stroke();
	
	// AND THEN DRAW THE REST OF HIM
        //eye 1
        gc.setFill(Paint.valueOf(eyeColor));
        gc.fillRect(location.getX()+eye1offset[0], location.getY()+eye1offset[1], eyeW, eyeH);
        gc.beginPath();
	gc.setStroke(Paint.valueOf(outlineColor));
	gc.setLineWidth(1);
	gc.rect(location.getX()+eye1offset[0], location.getY()+eye1offset[1], eyeW, eyeH);
	gc.stroke();
        //eye2
        gc.setFill(Paint.valueOf(eyeColor));
        gc.fillRect(location.getX()+eye2offset[0], location.getY()+eye2offset[1], eyeW, eyeH);
        gc.beginPath();
	gc.setStroke(Paint.valueOf(outlineColor));
	gc.setLineWidth(1);
	gc.rect(location.getX()+eye2offset[0], location.getY()+eye2offset[1], eyeW, eyeH);
	gc.stroke();
        //pupil1
        gc.setFill(Paint.valueOf(pupilColor));
        gc.fillRect(location.getX()+eye1offset[0]+pupiloffset[0], 
                location.getY()+eye1offset[1]+pupiloffset[1], pupilW, pupilH);
        gc.beginPath();
        //pupil2
        gc.setFill(Paint.valueOf(pupilColor));
        gc.fillRect(location.getX()+eye2offset[0]+pupiloffset[0], 
                location.getY()+eye2offset[1]+pupiloffset[1], pupilW, pupilH);
        gc.beginPath();
        //mouth
        gc.setFill(Paint.valueOf(mouthColor));
        gc.fillRect(location.getX()+mouthoffset[0], location.getY()+mouthoffset[1], mouthW, mouthH);
        gc.beginPath();
        //chest
        gc.setFill(Paint.valueOf(bodyColor));
        gc.fillRect(location.getX()+chestoffset[0], location.getY()+chestoffset[1], chestW, chestH);
        gc.beginPath();
        //body
        gc.setFill(Paint.valueOf(bodyColor));
        gc.fillRect(location.getX()+bodyoffset[0], location.getY()+bodyoffset[1], bodyW, bodyH);
        gc.beginPath();
        //foot1
        gc.setFill(Paint.valueOf(bodyColor));
        gc.fillRect(location.getX()+foot1offset[0], location.getY()+foot1offset[1], footW, footH);
        gc.beginPath();
        //foot2
        gc.setFill(Paint.valueOf(bodyColor));
        gc.fillRect(location.getX()+foot2offset[0], location.getY()+foot2offset[1], footW, footH);
        gc.beginPath();
    }
    
    public void renderImageRedBoxMan(Point2D location) {
	gc.drawImage(redBoxManImage, location.getX(), location.getY());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
	launch(args);
    }
    
}
