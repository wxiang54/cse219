/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataprocessors;

import java.util.Set;
import javafx.scene.chart.XYChart;
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
public class TSDProcessorTest {
    
    public TSDProcessorTest() {
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
     * Test of processString method, of class TSDProcessor.
     */
    @Test
    public void testProcessString() throws Exception {
        System.out.println("processString");
        String tsdString = "@Instance" + "\t" + "label" + "\t" + "3.5,1.4";
        TSDProcessor instance = new TSDProcessor();
        instance.clear();
        instance.processString(tsdString);
        assertEquals(instance.getNumInstances(), 1);
        assert
    }

    
}
