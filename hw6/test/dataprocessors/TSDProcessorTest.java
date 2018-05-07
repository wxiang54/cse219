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
     * @throws java.lang.Exception
     */
    @Test
    public void testProcessString() throws Exception {
        System.out.println("processString");
        String tsdString = "@Instance" + "\t" + "label" + "\t" + "3.5,1.4";
        TSDProcessor instance = new TSDProcessor();
        instance.clear();
        instance.processString(tsdString);
        assertEquals(instance.getNumInstances(), 1);
        assertTrue(instance.getLabelNames().contains("label"));
    }
    
    /**
     * Test of processString method, of class TSDProcessor.
     * This specific method tests edge case where instance name is invalid
     * @throws java.lang.Exception
     */
    @Test(expected = Exception.class)
    public void testInvalidName() throws Exception {
        System.out.println("processString - invalid name");
        String tsdString = "Instancc" + "\t" + "label" + "\t" + "3.5,1.4";
        TSDProcessor instance = new TSDProcessor();
        instance.clear();
        instance.processString(tsdString);
        //assertEquals(instance.getNumInstances(), 1);
    }
    
    /**
     * Test of processString method, of class TSDProcessor.
     * This specific method tests edge case where label name is empty str (not null)
     * (expected to function normally)
     * @throws java.lang.Exception
     */
    @Test()
    public void testEmptyLabel() throws Exception {
        System.out.println("processString - empty label");
        String tsdString = "@Instance" + "\t" + "" + "\t" + "3.5,1.4";
        TSDProcessor instance = new TSDProcessor();
        instance.clear();
        instance.processString(tsdString);
        assertEquals(instance.getNumInstances(), 1);
        assertTrue(instance.getLabelNames().contains(""));
        
    }
    
    /**
     * Test of processString method, of class TSDProcessor.
     * This specific method tests edge case where label name is NULL
     * (expected to not see 'null' in labels)
     * @throws java.lang.Exception
     */
    @Test()
    public void testNullLabel() throws Exception {
        System.out.println("processString - null label");
        String tsdString = "@Instance" + "\t" + "null" + "\t" + "3.5,1.4";
        TSDProcessor instance = new TSDProcessor();
        instance.clear();
        instance.processString(tsdString);
        assertEquals(instance.getNumInstances(), 1);
        assertFalse(instance.getLabelNames().contains("null"));
    }
    
    /**
     * Test of processString method, of class TSDProcessor.
     * This specific method tests edge case where supplied location is invalid
     * @throws java.lang.Exception
     */
    @Test(expected = Exception.class)
    public void testInvalidLocation() throws Exception {
        System.out.println("processString - invalid location");
        String tsdString = "@Instance" + "\t" + "label" + "\t" + "ayy,bee";
        TSDProcessor instance = new TSDProcessor();
        instance.clear();
        instance.processString(tsdString);
    }
    
    /**
     * Test of processString method, of class TSDProcessor.
     * This specific method tests edge case where supplied location is int (not float)
     * (expected to function normally)
     * @throws java.lang.Exception
     */
    @Test()
    public void testIntLocation() throws Exception {
        System.out.println("processString - int location");
        String tsdString = "@Instance" + "\t" + "label" + "\t" + "3,5";
        TSDProcessor instance = new TSDProcessor();
        instance.clear();
        instance.processString(tsdString);
        assertEquals(instance.getNumInstances(), 1);
        assertTrue(instance.getLabelNames().contains("label"));
    }
}
