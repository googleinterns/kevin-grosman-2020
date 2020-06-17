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
    private static final int TIMEOUT = 6000;
    private static final String entered_text = "Not bad";


    @Before
    public void openingBasicApp() throws IOException, InterruptedException {
        ShellUtility.forceQuitApp(BASIC_SAMPLE_PACKAGE);
        ShellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
    }


    @Test
    public void launching() {
        Boolean appeared = ShellUtility.device.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), TIMEOUT);
        assertEquals(true, appeared);
    }



    @Test
    public void quitingApp() throws IOException, InterruptedException {
        UiObject2 randomButton = ShellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        randomButton.click();

        //quit and make sure we are back on the first fragment, not on the second fragment where we left off
        ShellUtility.forceQuitApp(BASIC_SAMPLE_PACKAGE);
        ShellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        randomButton = ShellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        assertNotEquals(null, randomButton);
    }
    @Test
    public void gettingClickableAncestor() throws ShellUtility.invalidInputException, UiObjectNotFoundException {
        //grab a view that is not clickable (but which has a an ancestor which is)
        UiObject2 alsoRandomButton = ShellUtility.device.wait(Until.findObject(By.textContains("ALSO RANDOM")),TIMEOUT);
        assertEquals(false, alsoRandomButton.isClickable());

        //click on the ancestor
        UiObject2 clickable_button = ShellUtility.getLowestClickableAncestor(alsoRandomButton);
        assertEquals(true, clickable_button.isClickable());
        clickable_button.click();

        //check whether the desired action took place (previous button should be visible)
        UiObject2 previousButton = ShellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);
        assertNotEquals(null, previousButton);
    }

    @Test
    public void casting() throws UiObjectNotFoundException, ShellUtility.invalidInputException {
        UiObject2 randomButton = ShellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        UiObject randomButtonCasted = ShellUtility.castToObject(randomButton);
        randomButtonCasted.waitForExists(TIMEOUT);
        randomButtonCasted.click();

        //ensure that button click worked properly (previous button should be visible)
        UiObject2 previousButton = ShellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);
        assertNotEquals(null, previousButton);
    }


    @Test
    public void gettingEditableAncestor() throws ShellUtility.invalidInputException, UiObjectNotFoundException {

        UiObject2 search_text_view= ShellUtility.device.wait(Until.findObject(By.textContains("How does this app")),TIMEOUT);
        UiObject clickable = ShellUtility.getEditableObject(search_text_view);
        clickable.waitForExists(TIMEOUT);
        clickable.legacySetText(entered_text);

        //ensure that the textbox now displays the text
        UiObject final_search_box = ShellUtility.device.findObject(new UiSelector().text(entered_text));
        final_search_box.waitForExists(TIMEOUT);
        assertNotEquals(null, entered_text);
    }


    @Test
    public void clickStrict() throws ShellUtility.invalidInputException, UiObjectNotFoundException {
        String[] tokens = {"click", "COUNT","strict"};
        ShellUtility.executeUncachedAction(tokens);
        UiObject2 one = ShellUtility.device.wait(Until.findObject(By.textContains("1")),TIMEOUT);
        assertNotEquals(null, one);
    }

    @Test
    public void clickSubstring() throws ShellUtility.invalidInputException, UiObjectNotFoundException {
        String[] tokens = {"click", "OUN"};
        ShellUtility.executeUncachedAction(tokens);
        UiObject2 one = ShellUtility.device.wait(Until.findObject(By.textContains("1")),TIMEOUT);
        assertNotEquals(null, one);
    }

    @Test
    public void clickNonClickable() throws ShellUtility.invalidInputException, UiObjectNotFoundException {
        String[] tokens = {"click", "ALSO RAND"};
        ShellUtility.executeUncachedAction(tokens);
        UiObject2 previousButton = ShellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);
        assertNotEquals(null, previousButton);
    }

    @Test
    public void edit() throws ShellUtility.invalidInputException, UiObjectNotFoundException {
        String[] tokens = {"edit", "you feel", entered_text};
        ShellUtility.executeUncachedAction(tokens);

        UiObject final_search_box = ShellUtility.device.findObject(new UiSelector().text(entered_text));
        final_search_box.waitForExists(TIMEOUT);
        assertNotEquals(null, entered_text);
    }


}