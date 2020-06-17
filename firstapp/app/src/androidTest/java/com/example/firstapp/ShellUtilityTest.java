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
    private ShellUtility shellUtility;


    @Before
    public void openingBasicApp() throws IOException, InterruptedException {
        shellUtility = new ShellUtility();
        shellUtility.forceQuitApp(BASIC_SAMPLE_PACKAGE);
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
    }


    @Test
    public void launching() {
        Boolean appeared = shellUtility.device.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), TIMEOUT);
        assertEquals(true, appeared);
    }



    @Test
    public void quitingApp() throws IOException, InterruptedException {
        UiObject2 randomButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        randomButton.click();

        //quit and make sure we are back on the first fragment, not on the second fragment where we left off
        shellUtility.forceQuitApp(BASIC_SAMPLE_PACKAGE);
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        randomButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        assertNotEquals(null, randomButton);
    }
    @Test
    public void gettingClickableAncestor() throws ShellUtility.invalidInputException, UiObjectNotFoundException {
        //grab a view that is not clickable (but which has a an ancestor which is)
        UiObject2 alsoRandomButton = shellUtility.device.wait(Until.findObject(By.textContains("ALSO RANDOM")),TIMEOUT);
        assertEquals(false, alsoRandomButton.isClickable());

        //click on the ancestor
        UiObject2 clickable_button = shellUtility.getLowestClickableAncestor(alsoRandomButton);
        assertEquals(true, clickable_button.isClickable());
        clickable_button.click();

        //check whether the desired action took place (previous button should be visible)
        UiObject2 previousButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);
        assertNotEquals(null, previousButton);
    }

    @Test
    public void casting() throws UiObjectNotFoundException, ShellUtility.invalidInputException {
        UiObject2 randomButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        UiObject randomButtonCasted = shellUtility.castToObject(randomButton);
        randomButtonCasted.waitForExists(TIMEOUT);
        randomButtonCasted.click();

        //ensure that button click worked properly (previous button should be visible)
        UiObject2 previousButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);
        assertNotEquals(null, previousButton);
    }


    @Test
    public void gettingEditableAncestor() throws ShellUtility.invalidInputException, UiObjectNotFoundException {

        UiObject2 search_text_view = shellUtility.device.wait(Until.findObject(By.textContains("How does this app")),TIMEOUT);
        UiObject clickable = shellUtility.getEditableObject(search_text_view);
        clickable.waitForExists(TIMEOUT);
        clickable.legacySetText(entered_text);

        //ensure that the textbox now displays the text
        UiObject final_search_box = shellUtility.device.findObject(new UiSelector().text(entered_text));
        final_search_box.waitForExists(TIMEOUT);
        assertNotEquals(null, entered_text);
    }

    @Test
    public void executingActionClickStrict() throws ShellUtility.invalidInputException, UiObjectNotFoundException, IOException, InterruptedException {
        String str = "click;COUNT;strict";
        shellUtility.executeUncachedAction(shellUtility.parseStringAction(str, 0), 0);
        UiObject2 one = shellUtility.device.wait(Until.findObject(By.textContains("1")),TIMEOUT);
        assertNotEquals(null, one);
    }

    @Test
    public void executingActionClickSubstring() throws ShellUtility.invalidInputException, UiObjectNotFoundException, IOException, InterruptedException {
        String str = "click;OUN";
        shellUtility.executeUncachedAction(shellUtility.parseStringAction(str, 0), 0);
        UiObject2 one = shellUtility.device.wait(Until.findObject(By.textContains("1")),TIMEOUT);
        assertNotEquals(null, one);
    }

    @Test
    public void executingActionClickNonClickable() throws ShellUtility.invalidInputException, UiObjectNotFoundException, IOException, InterruptedException {
        String str = "click;ALSO RAND";
        shellUtility.executeUncachedAction(shellUtility.parseStringAction(str, 0), 0);
        UiObject2 previousButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);
        assertNotEquals(null, previousButton);
    }

    @Test
    public void executingActionEdit() throws ShellUtility.invalidInputException, UiObjectNotFoundException, IOException, InterruptedException {
        String str = "edit;you feel;" + entered_text;
        shellUtility.executeUncachedAction(shellUtility.parseStringAction(str, 0), 0);
        UiObject final_search_box = shellUtility.device.findObject(new UiSelector().text(entered_text));
        final_search_box.waitForExists(TIMEOUT);
        assertNotEquals(null, entered_text);
    }


    //Basic clicking test
    @Test
    public void clickTest() throws Exception {
        String[] preCUJ = {};
        String[] postCUJ = {"start;" + BASIC_SAMPLE_PACKAGE, "click;COUNT", "click;COUNT", "click;COUNT", "click;COUNT", "click;COUNT"};
        shellUtility.executeCUJ(preCUJ, postCUJ);
        UiObject2 five = shellUtility.device.wait(Until.findObject(By.textContains("5")), TIMEOUT);
        assertNotEquals(null, five);
    }

    //test Non-empty preCUJ (and shortest acceptable postCUJ), strict matching
    @Test
    public void nonEmptyPreCUJStrictMatching() throws Exception {
        String[] preCUJ = {"start;" + BASIC_SAMPLE_PACKAGE, "click;COUNT;strict", "click;COUNT", "click;COUNT"};
        String[] postCUJ = {"click;COUNT", "click;COUNT;strict"};
        shellUtility.executeCUJ(preCUJ, postCUJ);
        UiObject2 five = shellUtility.device.wait(Until.findObject(By.textContains("5")), TIMEOUT);
        assertNotEquals(null, five);
    }

    //test clicking on non-clickables, editing text, using proper substring for matching
    @Test
    public void properSubstringAndClickingOnNonClickables() throws Exception {
        String[] preCUJ = {"start;" + BASIC_SAMPLE_PACKAGE};
        String[] postCUJ = {"click;ALSO RAND", "click;PREVIOUS", "edit;How does;Good!"};
        shellUtility.executeCUJ(preCUJ, postCUJ);
    }


}