/**
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.firstapp;
import android.os.RemoteException;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;

import org.json.JSONException;
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
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final int TIMEOUT = 6000;
    private static final String entered_text = "Not bad";
    private ShellUtility shellUtility;


    @Before
    public void getShellUtility() throws IOException, InterruptedException, RemoteException, UiObjectNotFoundException {
        shellUtility = new ShellUtility(TIMEOUT);
    }

    /**
     * LAUNCHING AND QUITING
     */
    @Test
    public void quitingApp() throws IOException, InterruptedException, RemoteException, UiObjectNotFoundException {
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        UiObject2 randomButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        randomButton.click();

        //quit and make sure we are back on the first fragment, not on the second fragment where we left off
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        randomButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        assertNotEquals(null, randomButton);
    }

    /**
     * CASTING AND GETTING ANCESTORS
     */
    @Test
    public void gettingClickableAncestor() throws ShellUtility.InvalidInputException, UiObjectNotFoundException, InterruptedException, RemoteException, IOException {
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        //grab a view that is not clickable (but which has a an ancestor which is)
        UiObject2 alsoRandomButton = shellUtility.device.wait(Until.findObject(By.textContains("ALSO RANDOM")), TIMEOUT);
        assertEquals(false, alsoRandomButton.isClickable());

        //click on the ancestor
        UiObject2 clickable_button = shellUtility.getLowestClickableAncestor(alsoRandomButton);
        assertEquals(true, clickable_button.isClickable());
        clickable_button.click();

        //check whether the desired action took place (previous button should be visible)
        UiObject2 previousButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")), TIMEOUT);
        assertNotEquals(null, previousButton);
    }

    @Test
    public void casting() throws UiObjectNotFoundException, ShellUtility.InvalidInputException, InterruptedException, RemoteException, IOException {
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        UiObject2 randomButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);
        UiObject randomButtonCasted = shellUtility.castToObject(randomButton);
        randomButtonCasted.waitForExists(TIMEOUT);
        randomButtonCasted.click();

        //ensure that button click worked properly (previous button should be visible)
        UiObject2 previousButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")), TIMEOUT);
        assertNotEquals(null, previousButton);
    }


    @Test
    public void gettingEditableAncestor() throws ShellUtility.InvalidInputException, UiObjectNotFoundException, InterruptedException, RemoteException, IOException {
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        UiObject2 search_text_view = shellUtility.device.wait(Until.findObject(By.textContains("How does this app")), TIMEOUT);
        UiObject clickable = shellUtility.getEditableObject(search_text_view);
        clickable.waitForExists(TIMEOUT);
        clickable.legacySetText(entered_text);

        //ensure that the textbox now displays the text
        UiObject final_search_box = shellUtility.device.findObject(new UiSelector().text(entered_text));
        final_search_box.waitForExists(TIMEOUT);
        assertNotEquals(null, entered_text);
    }

    /**
     * PARSING TESTS
     */

    @Test
    public void parsingToArray() throws JSONException {
        String actionList = "['a;b', 'c;d;e']";
        String[] arr = shellUtility.parseToArray(actionList);
        assertEquals(2, arr.length);
        assertEquals("a;b", arr[0]);
        assertEquals("c;d;e", arr[1]);
    }

    @Test
    public void parsingToArrayExtraSpacing() throws JSONException {
        String actionList = "['a  b',     ' c;d; e ']";
        String[] arr = shellUtility.parseToArray(actionList);
        assertEquals(2, arr.length);
        assertEquals("a  b", arr[0]);
        assertEquals(" c;d; e ", arr[1]);
    }

    @Test
    public void parsingToArrayEmpty() throws JSONException {
        String actionList = "[]";
        String[] arr = shellUtility.parseToArray(actionList);
        assertEquals(0, arr.length);
    }



    /**
     * ACTION EXECUTION TESTS (Uncached -> Cached)
     */

    @Test
    public void executingStartAction() throws Exception {
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        String str = "start;" + BASIC_SAMPLE_PACKAGE;
        ShellUtility.Action action = shellUtility.parseStringAction(str, 0);

        //Uncached:
        action.executeUncachedAction();
        UiObject2 zero = shellUtility.device.wait(Until.findObject(By.text("0")), TIMEOUT);
        assertNotEquals(null, zero);

        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);

        //Cached:
        action.executeCachedAction();
        UiObject2 zeroCached = shellUtility.device.wait(Until.findObject(By.text("0")), TIMEOUT);
        assertNotEquals(null, zeroCached);
    }

    @Test
    public void executingClickActionStrict() throws Exception {
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        String str = "click;COUNT;+strict";
        ShellUtility.Action action = shellUtility.parseStringAction(str, 0);

        //Uncached:
        action.executeUncachedAction();
        UiObject2 one = shellUtility.device.wait(Until.findObject(By.text("1")), TIMEOUT);
        assertNotEquals(null, one);

        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);

        //Cached:
        action.executeCachedAction();
        UiObject2 oneCached = shellUtility.device.wait(Until.findObject(By.text("1")), TIMEOUT);
        assertNotEquals(null, oneCached);
    }

    @Test
    public void executingClickActionNoop() throws Exception {
        String str = "click;RANDOM;+noop";
        ShellUtility.Action action = shellUtility.parseStringAction(str, 0);

        //Uncached:
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        action.executeUncachedAction();
        UiObject2 count = shellUtility.device.wait(Until.findObject(By.text("COUNT")), TIMEOUT);
        assertNotEquals(null, count);

        //Cached:
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        action.executeCachedAction();
        UiObject2 countCached = shellUtility.device.wait(Until.findObject(By.text("COUNT")), TIMEOUT);
        assertNotEquals(null, countCached);
    }

    @Test
    public void executingClickActionSubstring() throws Exception {
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        String str = "click;OUN";
        ShellUtility.Action action = shellUtility.parseStringAction(str, 0);

        //Uncached:
        action.executeUncachedAction();
        UiObject2 one = shellUtility.device.wait(Until.findObject(By.text("1")), TIMEOUT);
        assertNotEquals(null, one);

        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);

        //Cached:
        action.executeCachedAction();
        UiObject2 oneCached = shellUtility.device.wait(Until.findObject(By.text("1")), TIMEOUT);
        assertNotEquals(null, oneCached);
    }

    @Test
    public void executingClickActionNonClickable() throws Exception {
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        String str = "click;ALSO RAND";
        ShellUtility.Action action = shellUtility.parseStringAction(str, 0);

        //Uncached:
        action.executeUncachedAction();
        UiObject2 previousButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);
        assertNotEquals(null, previousButton);

        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);

        //Cached:
        action.executeCachedAction();
        UiObject2 previousButtonCached = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);
        assertNotEquals(null, previousButtonCached);
    }

    @Test
    public void executingEditAction() throws Exception {
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        String str = "edit;you feel;" + entered_text;
        ShellUtility.Action action = shellUtility.parseStringAction(str, 0);

        //Uncached:
        action.executeUncachedAction();
        UiObject finaSearchBox = shellUtility.device.findObject(new UiSelector().text(entered_text));
        finaSearchBox.waitForExists(TIMEOUT);
        assertNotEquals(null, entered_text);

        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);

        //Cached:
        action.executeCachedAction();
        UiObject finaSearchBoxCached = shellUtility.device.findObject(new UiSelector().text(entered_text));
        finaSearchBoxCached.waitForExists(TIMEOUT);
        assertNotEquals(null, entered_text);
    }

    @Test
    public void executingClickImageAction() throws Exception {
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);
        String str = "clickImage;increment";
        ShellUtility.Action action = shellUtility.parseStringAction(str, 0);

        //Uncached:
        action.executeUncachedAction();
        UiObject2 one = shellUtility.device.wait(Until.findObject(By.text("1")),TIMEOUT);
        assertNotEquals(null, one);

        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);

        //Cached:
        action.executeCachedAction();
        UiObject2 oneCached = shellUtility.device.wait(Until.findObject(By.text("1")),TIMEOUT);
        assertNotEquals(null, oneCached);
    }

    /**
     * DATA AGGREGATION TESTS
     */

    @Test
    public void gettingDifferences() {
        long[] arr = {3,5,8,16};
        long[] expected = {2,3,8};
        assertArrayEquals(expected, shellUtility.differences(arr));
    }

    @Test
    public void padding() {
        String num = "31";
        assertEquals("0031", shellUtility.zeroPad(num, 4));
    }

    @Test
    public void convertingMilisecondsToTime() {
        long time = 12345678;
        assertEquals("03:25:45.678", shellUtility.milisecondsToTime(time));
    }

    @Test
    public void concatting() {
        String[] a = new String[] {"one", "two"};
        String[] b = new String[0];
        String[] c = new String[] {"three"};

        assertArrayEquals(new String[]{"one", "two", "three"}, shellUtility.concat(a, b, c));

    }

    /**
     * Caching CUJ tests
     */

    //Basic clicking test
    @Test
    public void clickTest() throws Exception {
        String preCUJ = "[]";
        String measuredCUJ = "[]";
        String postCUJ = "['start;" + BASIC_SAMPLE_PACKAGE + "', 'click;COUNT', 'click;COUNT', 'click;COUNT', 'click;COUNT', 'click;COUNT']";

        shellUtility.iterateAndMeasureCuj(preCUJ, measuredCUJ, postCUJ, 1, false, 0);
        UiObject2 five = shellUtility.device.wait(Until.findObject(By.text("5")), TIMEOUT);
        assertNotEquals(null, five);
    }

    //test Non-empty preCUJ (and shortest acceptable postCUJ), strict matching

    @Test
    public void allCujsNonEmptyStrictMatching() throws Exception {
        String preCUJ = "['start;" + BASIC_SAMPLE_PACKAGE + "']";
        String measuredCUJ = "['click;COUNT;+strict', 'click;COUNT', 'click;COUNT']";
        String postCUJ = "['click;COUNT',  'click;COUNT;+strict']";

        shellUtility.iterateAndMeasureCuj(preCUJ, postCUJ, measuredCUJ, 2, false, 0);
        UiObject2 five = shellUtility.device.wait(Until.findObject(By.text("5")), TIMEOUT);
        assertNotEquals(null, five);
    }

    //test clicking on non-clickables, editing text, using proper substring for matching
    @Test
    public void properSubstringAndClickingOnNonClickables() throws Exception {
        String preCUJ = "";
        String measuredCUJ =  "['start;" + BASIC_SAMPLE_PACKAGE + "']";
        String postCUJ =  "['click;ALSO RAND', 'click;PREVIOUS', 'edit;How does;Good!']";

        shellUtility.iterateAndMeasureCuj(preCUJ, measuredCUJ, postCUJ, 1, false,  0);
    }

    /**
     * Cached CUJ Tests
     */
    //Basic clicking test

    @Test
    public void clickTestCached() throws Exception {
        String preCUJ = "";
        String measuredCUJ = "['start;" + BASIC_SAMPLE_PACKAGE + "', 'click;COUNT', 'click;COUNT', 'click;COUNT', 'click;COUNT']";
        String postCUJ = "['click;COUNT']";

        shellUtility.iterateAndMeasureCuj(preCUJ, measuredCUJ, postCUJ, 1, false, 0);
        UiObject2 five = shellUtility.device.wait(Until.findObject(By.text("5")), TIMEOUT);
        assertNotEquals(null, five);
    }

    /**
     * Walk CUJ Tests
     */
    @Test
    public void walkCujW() throws Exception {
        String preCUJ = "['start;" + BASIC_SAMPLE_PACKAGE + "']";
        String measuredCUJ = "['click;RANDOM']";
        String postCUJ = "['click;PREVIOUS', 'click;COUNT']";
        int n = 2;

        String sectionCUJ = "w";
        shellUtility.parseAndWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionCUJ, n);
        UiObject2 one = shellUtility.device.wait(Until.findObject(By.text("1")), TIMEOUT);
        assertNotEquals(null, one);
    }


    @Test
    public void walkCuj_C_CR() throws Exception {
        String preCUJ = "['start;" + BASIC_SAMPLE_PACKAGE + "']";
        String measuredCUJ = "['click;COUNT']";
        String postCUJ = "['click;RANDOM', 'click;PREVIOUS']";
        int n = 1;

        String sectionCUJ = "c";
        shellUtility.parseAndWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionCUJ, n);
        UiObject2 one = shellUtility.device.wait(Until.findObject(By.text("1")), TIMEOUT);
        assertNotEquals(null, one);

        sectionCUJ = "cr";
        shellUtility.parseAndWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionCUJ, n);
        UiObject2 zero = shellUtility.device.wait(Until.findObject(By.text("0")), TIMEOUT);
        assertNotEquals(null, zero);
    }

    @Test
    public void walkCuj_P_PR() throws Exception {
        String preCUJ = "['start;" + BASIC_SAMPLE_PACKAGE + "', 'click;COUNT']";
        String measuredCUJ = "['click;COUNT']";
        String postCUJ = "['click;PREVIOUS']";
        int n = 1;

        String sectionCUJ = "p";
        shellUtility.parseAndWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionCUJ, n);
        UiObject2 one = shellUtility.device.wait(Until.findObject(By.text("1")), TIMEOUT);
        assertNotEquals(null, one);

        sectionCUJ = "pr";
        shellUtility.parseAndWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionCUJ, n);
        UiObject2 zero = shellUtility.device.wait(Until.findObject(By.text("0")), TIMEOUT);
        assertNotEquals(null, zero);
    }

    @Test
    public void walkCuj_F_FR() throws Exception {
        String preCUJ = "['start;" + BASIC_SAMPLE_PACKAGE + "', 'click;COUNT']";
        String measuredCUJ = "['click;COUNT']";
        String postCUJ = "['click;COUNT']";
        int n = 1;

        String sectionCUJ = "f";
        shellUtility.parseAndWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionCUJ, n);
        UiObject2 zero = shellUtility.device.wait(Until.findObject(By.text("0")), TIMEOUT);
        assertNotEquals(null, zero);

        sectionCUJ = "fr";
        shellUtility.parseAndWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionCUJ, n);
        UiObject2 three = shellUtility.device.wait(Until.findObject(By.text("3")), TIMEOUT);
        assertNotEquals(null, three);
    }

    @Test
    public void walkCuj_P_M_MR() throws Exception {
        String preCUJ = "['start;" + BASIC_SAMPLE_PACKAGE + "', 'click;COUNT']";
        String measuredCUJ = "['click;COUNT']";
        String postCUJ = "['click;COUNT']";
        int n = 1;

        String sectionCUJ = "p";
        shellUtility.parseAndWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionCUJ, n);
        UiObject2 one = shellUtility.device.wait(Until.findObject(By.text("1")), TIMEOUT);
        assertNotEquals(null, one);

        sectionCUJ = "m";
        shellUtility.parseAndWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionCUJ, n);
        UiObject2 two = shellUtility.device.wait(Until.findObject(By.text("2")), TIMEOUT);
        assertNotEquals(null, two);


        sectionCUJ = "mr";
        shellUtility.parseAndWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionCUJ, n);
        UiObject2 three = shellUtility.device.wait(Until.findObject(By.text("3")), TIMEOUT);
        assertNotEquals(null, three);
    }
}   