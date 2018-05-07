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
     * Test of getDialog method, of class ClusteringConfig.
     */
    @Test
    public void testGetDialog() {
        System.out.println("getDialog");
        ClusteringConfig expResult = null;
        ClusteringConfig result = ClusteringConfig.getDialog();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of init method, of class ClusteringConfig.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        Stage owner = null;
        ClusteringConfig instance = null;
        instance.init(owner);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of showConfig method, of class ClusteringConfig.
     */
    @Test
    public void testShowConfig_0args() {
        System.out.println("showConfig");
        ClusteringConfig instance = null;
        instance.showConfig();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of showConfig method, of class ClusteringConfig.
     */
    @Test
    public void testShowConfig_4args() {
        System.out.println("showConfig");
        int defaultMaxIter = 0;
        int defaultUpdateInter = 0;
        int defaultNumCluster = 0;
        boolean defaultContRun = false;
        ClusteringConfig instance = null;
        instance.showConfig(defaultMaxIter, defaultUpdateInter, defaultNumCluster, defaultContRun);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxIterations method, of class ClusteringConfig.
     */
    @Test
    public void testGetMaxIterations() {
        System.out.println("getMaxIterations");
        ClusteringConfig instance = null;
        Integer expResult = null;
        Integer result = instance.getMaxIterations();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUpdateInterval method, of class ClusteringConfig.
     */
    @Test
    public void testGetUpdateInterval() {
        System.out.println("getUpdateInterval");
        ClusteringConfig instance = null;
        Integer expResult = null;
        Integer result = instance.getUpdateInterval();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNumClusters method, of class ClusteringConfig.
     */
    @Test
    public void testGetNumClusters() {
        System.out.println("getNumClusters");
        ClusteringConfig instance = null;
        Integer expResult = null;
        Integer result = instance.getNumClusters();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getContinuousRun method, of class ClusteringConfig.
     */
    @Test
    public void testGetContinuousRun() {
        System.out.println("getContinuousRun");
        ClusteringConfig instance = null;
        Boolean expResult = null;
        Boolean result = instance.getContinuousRun();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
