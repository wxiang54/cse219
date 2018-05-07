/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author will
 */
public class ClassificationConfigTest {
    
    public ClassificationConfigTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of showConfig method, of class ClassificationConfig.
     */
    @Test
    public void testShowConfig() {
        System.out.println("showConfig");
        TextField tf_iterations = new TextField("test1");
        TextField tf_interval = new TextField("test2");
        CheckBox cb_cont_run = new CheckBox();
        //ClassificationConfig instance = ClassificationConfig.getDialog();
        tf_iterations.setText("");
        tf_interval.setText("");
        cb_cont_run.setSelected(false);
        
        //test if correct
        assertEquals(tf_iterations, "test1");
        assertEquals(tf_interval, "test2");
        assertEquals(cb_cont_run.isSelected(), false);
        //instance.init(null);
        //instance.showConfig();
    }

    /**
     * Test of showConfig method, of class ClassificationConfig.
     */
    @Test
    public void testShowConfig_3args() {
        System.out.println("showConfig");
        int defaultMaxIter = 0;
        int defaultUpdateInter = 0;
        boolean defaultContRun = false;
        ClassificationConfig instance = null;
        instance.showConfig(defaultMaxIter, defaultUpdateInter, defaultContRun);
    }
}
