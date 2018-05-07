/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

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
public class ClusteringConfigTest {
    
    public ClusteringConfigTest() {
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
     * Test of configTest method, of class ClusteringConfig.
     */
    @Test
    public void testConfigTest() {
        System.out.println("configTest");
        Object[] ret = ClusteringConfig.configTest("1", "1", "2", false);
        assertEquals(ret[0], 1);
        assertEquals(ret[1], 1);
        assertEquals(ret[2], 2);
        assertEquals(ret[3], false);
    }
    
    /**
     * Test of configTest method, of class ClusteringConfig.
     * Tests boundary case where max iter is negative
     */
    @Test
    public void testConfigTestNegativeIter() {
        System.out.println("configTest");
        Object[] ret = ClusteringConfig.configTest("-1", "1", "2", false);
        assertEquals(ret[0], 1);
        assertEquals(ret[1], 1);
        assertEquals(ret[2], 2);
        assertEquals(ret[3], false);
    }
    
    /**
     * Test of configTest method, of class ClusteringConfig.
     * Tests boundary case where interval is negative
     */
    @Test
    public void testConfigTestNegativeInter() {
        System.out.println("configTest");
        Object[] ret = ClusteringConfig.configTest("1", "-11", "2", true);
        assertEquals(ret[0], 1);
        assertEquals(ret[1], 1);
        assertEquals(ret[2], 2);
        assertEquals(ret[3], true);
    }
    
    /**
     * Test of configTest method, of class ClusteringConfig.
     * Tests boundary case where interval > max iter
     * (should still work as normal)
     */
    @Test
    public void testConfigTestTopKekerino() {
        System.out.println("configTest");
        Object[] ret = ClusteringConfig.configTest("1", "2", "2", false);
        assertEquals(ret[0], 1);
        assertEquals(ret[1], 2);
        assertEquals(ret[2], 2);
        assertEquals(ret[3], false);
    }
    
    /**
     * Test of configTest method, of class ClusteringConfig.
     * Tests boundary case where clusters is < 2
     * (should still work normal because it gets fixed in algo)
     */
    @Test
    public void testConfigTestUnderflowClusters() {
        System.out.println("configTest");
        Object[] ret = ClusteringConfig.configTest("1", "2", "1", false);
        assertEquals(ret[0], 1);
        assertEquals(ret[1], 2);
        assertEquals(ret[2], 1);
        assertEquals(ret[3], false);
    }
    
    /**
     * Test of configTest method, of class ClusteringConfig.
     * Tests boundary case where clusters is > 4
     * (should still work normal because it gets fixed in algo)
     */
    @Test
    public void testConfigTestOverflowClusters() {
        System.out.println("configTest");
        Object[] ret = ClusteringConfig.configTest("1", "2", "5", false);
        assertEquals(ret[0], 1);
        assertEquals(ret[1], 2);
        assertEquals(ret[2], 5);
        assertEquals(ret[3], false);
    }
}
