package com.example.firstapp;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.Until;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.prefs.BackingStoreException;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTests {
    private static final String BASIC_SAMPLE_PACKAGE = "com.example.firstapp";
    private UiDevice device;
    private static final int TIMEOUT = 6000;


    @Test
    public void launching () throws InterruptedException, IOException {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UtilityMethods.launchApp(device, BASIC_SAMPLE_PACKAGE, TIMEOUT);
        Boolean appeared = device.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), TIMEOUT);
        assertEquals(true, appeared);
    }



    @Test
    public void quit_app () throws IOException, InterruptedException {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UtilityMethods.force_quit_app(BASIC_SAMPLE_PACKAGE);
        UtilityMethods.launchApp(device, BASIC_SAMPLE_PACKAGE, TIMEOUT);
        UiObject2 randomButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        randomButton.click();

        //quit and make sure we are back on the first fragment, not on the second fragment where we left off
        UtilityMethods.force_quit_app(BASIC_SAMPLE_PACKAGE);
        UtilityMethods.launchApp(device, BASIC_SAMPLE_PACKAGE, TIMEOUT);
        randomButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        assertNotEquals(null, randomButton);
    }


}