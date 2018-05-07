/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataprocessors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ui.AppUI;

/**
 *
 * @author will
 */
public class AppDataTest {
    
    public AppDataTest() {
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
     * Test of saveData method, of class AppData.
     * @throws java.io.IOException
     */
    @Test
    public void testSaveData() throws IOException {
        System.out.println("saveData");
        String tsdString = "@test\ttest\t2,3";
        Path dataFilePath = Paths.get("test/data.tsd");
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            String curText = tsdString; //this should be from text area
            writer.write(curText);
        }
        
        //test that it works
        File file = new File(dataFilePath.toString());
        BufferedReader reader = new BufferedReader(new FileReader(file));
        assertEquals(reader.readLine(), tsdString);
    }
}
