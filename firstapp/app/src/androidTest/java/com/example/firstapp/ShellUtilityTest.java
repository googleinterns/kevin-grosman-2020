package com.example.firstapp;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;

import org.junit.Before;
import org.junit.Test;

import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;
import java.io.IOException;
import static org.junit.Assert.*;
import static java.lang.Thread.sleep;
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ShellUtilityTest {
    private static final String BASIC_SAMPLE_PACKAGE = "com.example.firstapp";
    private UiDevice device;
    private static final int TIMEOUT = 6000;


    @Before
    public void openingBasicApp() throws IOException, InterruptedException {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        ShellUtility.forceQuitApp(BASIC_SAMPLE_PACKAGE);
        ShellUtility.launchApp(device, BASIC_SAMPLE_PACKAGE, TIMEOUT);
    }


    @Test
    public void launching() {
        Boolean appeared = device.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), TIMEOUT);
        assertEquals(true, appeared);
    }



    @Test
    public void quitingApp() throws IOException, InterruptedException {
        UiObject2 randomButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        randomButton.click();

        //quit and make sure we are back on the first fragment, not on the second fragment where we left off
        ShellUtility.forceQuitApp(BASIC_SAMPLE_PACKAGE);
        ShellUtility.launchApp(device, BASIC_SAMPLE_PACKAGE, TIMEOUT);
        randomButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        assertNotEquals(null, randomButton);
    }
    @Test
    public void gettingClickableAncestor() throws ShellUtility.invalidInputException, UiObjectNotFoundException {
        //grab a view that is not clickable (but which has a an ancestor which is)
        UiObject2 alsoRandomButton = device.wait(Until.findObject(By.textContains("ALSO RANDOM")),TIMEOUT);
        assertEquals(false, alsoRandomButton.isClickable());

        //click on the ancestor
        UiObject2 clickable_button = ShellUtility.getLowestClickableAncestor(alsoRandomButton);
        assertEquals(true, clickable_button.isClickable());
        clickable_button.click();

        //check whether the desired action took place (previous button should be visible)
        UiObject2 previousButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);
        assertNotEquals(null, previousButton);
    }

    @Test
    public void casting() throws UiObjectNotFoundException, ShellUtility.invalidInputException {
        UiObject2 randomButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        UiObject randomButtonCasted = ShellUtility.castToObject(device, randomButton);
        randomButtonCasted.waitForExists(TIMEOUT);
        randomButtonCasted.click();

        //ensure that button click worked properly (previous button should be visible)
        UiObject2 previousButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);
        assertNotEquals(null, previousButton);
    }


    @Test
    public void gettingEditableAncestor() throws ShellUtility.invalidInputException, UiObjectNotFoundException {
        String entered_text = "Not bad";
        UiObject2 search_text_view= device.wait(Until.findObject(By.textContains("How does this app")),TIMEOUT);
        UiObject clickable = ShellUtility.getEditableObject(device, search_text_view);
        clickable.waitForExists(TIMEOUT);
        clickable.legacySetText(entered_text);

        //ensure that the textbox now displays "Not bad"
        UiObject final_search_box = device.findObject(new UiSelector().text(entered_text));
        final_search_box.waitForExists(TIMEOUT);
        assertNotEquals(null, entered_text);
    }

}