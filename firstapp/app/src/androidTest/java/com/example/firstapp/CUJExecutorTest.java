package com.example.firstapp;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;

public class CUJExecutorTest {
    private static final String BASIC_SAMPLE_PACKAGE = "com.example.firstapp";
    private static final int TIMEOUT = 6000;



    //Basic clicking test
    @Test
    public void countToFive() throws Exception {
        String[] preCUJ = {};
        String[] postCUJ = {BASIC_SAMPLE_PACKAGE, "click;COUNT", "click;COUNT", "click;COUNT", "click;COUNT", "click;COUNT"};
        CUJExecutor.executeCUJ(preCUJ, postCUJ);
        UiObject2 five = ShellUtility.device.wait(Until.findObject(By.textContains("5")),TIMEOUT);
        assertNotEquals(null, five);
    }
    //test Non-empty preCUJ (and shortest acceptable postCUJ), strict matching
    @Test
    public void countToFiveMeasureLast() throws Exception {
        String[] preCUJ = {BASIC_SAMPLE_PACKAGE, "click;COUNT;strict", "click;COUNT", "click;COUNT"};
        String[] postCUJ = {"click;COUNT", "click;COUNT;strict"};
        CUJExecutor.executeCUJ(preCUJ, postCUJ);
        UiObject2 five = ShellUtility.device.wait(Until.findObject(By.textContains("5")),TIMEOUT);
        assertNotEquals(null, five);
    }
    //test clicking on non-clickables, editing text, using proper substring for matching
    @Test
    public void funGame() throws Exception {
        String[] preCUJ = {BASIC_SAMPLE_PACKAGE};
        String[] postCUJ = {"click;ALSO RAND", "click;PREVIOUS", "edit;How does;Good!"};
        CUJExecutor.executeCUJ(preCUJ, postCUJ);
    }




}
