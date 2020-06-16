package com.example.firstapp;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import org.junit.Test;
import androidx.test.uiautomator.Until;
import java.io.IOException;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ShellUtilityTest {
    private static final String BASIC_SAMPLE_PACKAGE = "com.example.firstapp";
    private UiDevice device;
    private static final int TIMEOUT = 6000;


    @Test
    public void launching() throws InterruptedException, IOException {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        ShellUtility.launchApp(device, BASIC_SAMPLE_PACKAGE, TIMEOUT);
        Boolean appeared = device.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), TIMEOUT);
        assertEquals(true, appeared);
    }



    @Test
    public void quitApp() throws IOException, InterruptedException {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        ShellUtility.forceQuitApp(BASIC_SAMPLE_PACKAGE);
        ShellUtility.launchApp(device, BASIC_SAMPLE_PACKAGE, TIMEOUT);
        UiObject2 randomButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        randomButton.click();

        //quit and make sure we are back on the first fragment, not on the second fragment where we left off
        ShellUtility.forceQuitApp(BASIC_SAMPLE_PACKAGE);
        ShellUtility.launchApp(device, BASIC_SAMPLE_PACKAGE, TIMEOUT);
        randomButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        assertNotEquals(null, randomButton);
    }


}