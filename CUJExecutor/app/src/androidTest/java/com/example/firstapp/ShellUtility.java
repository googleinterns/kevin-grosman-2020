package com.example.firstapp;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Tracer;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;
import org.hamcrest.CoreMatchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;


public class ShellUtility {
    public UiDevice device;
    private long timeoutMs;
    private String curPackage;

    public ShellUtility(long timeoutMillis) {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        timeoutMs = timeoutMillis;
    }

    public static class invalidInputException extends Exception {
        public invalidInputException(String message) {
            super(message);
        }
    }

            /******************************
                                 *       ACTION CLASS
                                 ******************************/
    public abstract class Action {
        private UiObject cachedObject;
        long actionTimeout;

        void setCachedObject(UiObject object) {
            cachedObject = object;
        }
        UiObject getCachedObject() {
            return cachedObject;
        }

        /**
         * execute the action and cache the result in cachedObject
         **/
        abstract void executeUncachedAction() throws IOException, InterruptedException, UiObjectNotFoundException, invalidInputException;

        /**
         * execute the action on the object specified by cachedObject
         * return the time (since epoch) just before the action is performed
         **/
        abstract long executeCachedAction() throws UiObjectNotFoundException, IOException, InterruptedException;
    }

    public class StartAction extends Action {
        String pkg;

        public StartAction(String p, long timeoutMillis) {
            pkg = p;
            actionTimeout = timeoutMillis;
        }

        @Override
        void executeUncachedAction() throws IOException, InterruptedException {
            forceQuitApp(pkg);
            launchApp(pkg);
            setCachedObject(null);
        }

        @Override
        long executeCachedAction() throws IOException, InterruptedException {
            forceQuitApp(pkg);
            return launchApp(pkg);
        }
    }

    public class ClickAction extends Action {
        String text;
        Boolean strict;
        public ClickAction(String t, Boolean s, long timeoutMillis) {
            text = t;
            strict = s;
            actionTimeout = timeoutMillis;
        }

        @Override
        void executeUncachedAction() throws UiObjectNotFoundException, invalidInputException {
            UiObject2 object2;
            if (strict) {
                object2 = device.wait(Until.findObject(By.text(text)), actionTimeout);
            } else {
                object2 = device.wait(Until.findObject(By.textContains(text)), actionTimeout);
            }
            UiObject object = castToObject(object2);
            object.click();
            setCachedObject(object);
        }

        @Override
        long executeCachedAction() throws UiObjectNotFoundException {
            UiObject object = getCachedObject();
            object.waitForExists(actionTimeout);
            long t = getTime();
            object.click();
            return t;
        }
    }

    public class ClickImageAction extends Action {
        String description;
        Boolean strict;
        public ClickImageAction(String d, Boolean s, long timeoutMillis) {
            description = d;
            strict = s;
            actionTimeout = timeoutMillis;
        }

        @Override
        void executeUncachedAction() throws UiObjectNotFoundException, invalidInputException {
            UiObject2 object2;
            if (strict) {
                object2 = device.wait(Until.findObject(By.desc(description)), actionTimeout);
            } else {
                object2 = device.wait(Until.findObject(By.descContains(description)), actionTimeout);
            }
            if (object2 == null) {
                List<UiObject2> views = device.wait(Until.findObjects(By.pkg(curPackage)), actionTimeout);
                if (views == null) {
                    throw new invalidInputException("no image matching description \"" + description + "\' found," +
                            " and no Images were were found either... Try something else.");
                }
                List<String> possibleDescriptions = new ArrayList<>();
                for (int i = 0; i < views.size(); i++) {
                    UiObject2 object = views.get(i);
                    if (object.getText() == null && object.getContentDescription() != null) {
                        possibleDescriptions.add(object.getContentDescription());
                    }
                }
                throw new invalidInputException("no image matching description \"" + description + "\' found," +
                        " but images with the following descriptions were found:\n" + possibleDescriptions + "\nTry again with one of these.");
            }
            UiObject object = castToObject(object2);
            object.click();
            setCachedObject(object);
        }

        @Override
        long executeCachedAction() throws UiObjectNotFoundException {
            UiObject object = getCachedObject();
            object.waitForExists(actionTimeout);
            long t = getTime();
            object.click();
            return t;
        }
    }

    public class EditAction extends Action {
        String text;
        String entered;
        Boolean strict;
        public EditAction(String t, String e, Boolean s, long timeoutMillis) {
            text = t;
            entered = e;
            strict = s;
            actionTimeout = timeoutMillis;
        }

        @Override
        void executeUncachedAction() throws UiObjectNotFoundException, invalidInputException {
            UiObject2 object2;
            if (strict) {
                object2 = device.wait(Until.findObject(By.text(text)), actionTimeout);
            } else {
                object2 = device.wait(Until.findObject(By.textContains(text)), actionTimeout);
            }
            UiObject object = getEditableObject(object2);
            object.waitForExists(actionTimeout);
            object.legacySetText(entered);
            setCachedObject(object);
        }

        @Override
        long executeCachedAction() throws UiObjectNotFoundException {
            UiObject object = getCachedObject();
            object.waitForExists(actionTimeout);
            long t = getTime();
            object.legacySetText(entered);
            return t;
        }
    }
                                /******************************
                                 *    APP LIFECYCLE HELPERS
                                 ******************************/
    /**
     * Launches the specified App on the specified device and returns the time just before the app was
     * launched--to be used later for timing startup.
     */
    public long launchApp(String pkg) throws InterruptedException, IOException {

        curPackage = pkg;
        //Start from the home screen
        device.pressHome();
        final String launcherPackage = device.getLauncherPackageName();
        Assert.assertThat(launcherPackage, CoreMatchers.notNullValue());
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), timeoutMs);


        // Launch the app
        //Process process = new ProcessBuilder("am", "start", pkg).start();

        Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkg);

        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //startActivity imposes a 5 second cool-down after the home button is pressed, so we wait
        //out that cool-down before grabbing the time and launching
        sleep(6000);
        long start = getTime();
        context.startActivity(intent);

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(pkg).depth(0)), timeoutMs);

        return start;

    }

    public void forceQuitApp(String pkg) throws IOException, InterruptedException {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Process process = new ProcessBuilder("am", "force-stop", pkg).start();

    }


                                /******************************
                                 *    OBJECT FINDING HELPERS
                                 ******************************/
    /**
     * Walks up view tree until a clickable ancestor is found. Google apps often have clickable
     * views that have a textbox as a descendant and thus the view containing the text cannot be
     * clicked
     */
    public UiObject2 getLowestClickableAncestor(UiObject2 object2) throws invalidInputException {
        if (object2 == null) {
            throw new invalidInputException("passed object is null. Consider increasing TIMEOUT parameter.");
        }
        while (!object2.isClickable()) {
            object2 = object2.getParent();
        }
        if (object2 == null) {
            throw new invalidInputException("No clickable ancestors. Try more specific search terms.");
        }
        return object2;
    }

    /**
     * Creates a UiObject using the identifying information from a UIObject. Necessary for caching,
     * since UiObject2s store references to particular views and are thus worthless once that view
     * has been destroyed.
     */
    public UiObject castToObject(UiObject2 object2) throws invalidInputException {

        //Grab identifying information
        String resourceName = object2.getResourceName();
        String className = object2.getClassName();
        String text = object2.getText();
        String contentDescription = object2.getContentDescription();

        //For some reason, UiSelectors don't work properly unless fields are set during
        // instantiation, so we consider each case separately
        UiSelector selector = null;
        if (resourceName != null && text != null) {
            selector = new UiSelector().resourceId(resourceName).text(text).className(className);
        } else if (resourceName != null && contentDescription != null) {
            selector = new UiSelector().resourceId(resourceName).description(contentDescription).className(className);
        } else if (contentDescription != null && text != null) {
            selector = new UiSelector().description(contentDescription).text(text).className(className);
        } else if (resourceName != null) {
            selector = new UiSelector().resourceId(resourceName).className(className);
        } else if (text != null) {
            selector = new UiSelector().text(text).className(className);
        } else if (contentDescription != null) {
            selector = new UiSelector().description(contentDescription).className(className);
        }

        UiObject casted = device.findObject(selector);
        if (casted == null) {
            throw new invalidInputException("Casting failed. Try different actions.");
        }
        return casted;
    }

    /**
     * Retrieves an editable UiObject from the UiObject2 corresponding to the view containing the
     * hint text in a textbox.
     */
    public UiObject getEditableObject(UiObject2 object2) throws invalidInputException {
        return castToObject(getLowestClickableAncestor(object2));
    }


                                    /******************************
                                     *   PARSING+CACHING HELPERS
                                     ******************************/


    String[] parseToArray(String s) throws JSONException {
        if (s == null || s.length() == 0) return new String[0];
        JSONArray ja = new JSONArray(s);
        String[] res = new String[ja.length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = ja.getString(i);
        }
        return res;
    }



    /** INSTRUCTIONS FOR ACTION FORMATTING:
     * Each action must be to launch an app, click on a particular View or to enter text in a particular textbox.
     * Actions are represented in one of the following forms:
     *
     * 1. “start;package_name" to launch the specified package.
     * 2. “edit;text_displayed;text_entered” to enter text_entered into a textbox, where text_displayed is the text currently visible in the textbox.
     * 3. “click;text_displayed” to click a non-editable view (normally a text-view or button) on the screen which has text containing the string text_displayed displayed on it
     * 4. “clickImage;text_description” to click a view without text (normally an image view) where the description of the view contains the string description_text
     *
     * Note that all text fields are case sensitive. Additionally, if an action of form 2, 3, or 4 is followed by “;strict”
     * the search for a corresponding view will enforce that an exact match is found for text_displayed (in cases 2 or 3)
     * or text_description (in case 4).
     */
    public Action parseStringAction(String str, int idx) throws invalidInputException {
        String[] tokens = str.split(";");
        Action action;
        boolean strict = tokens[tokens.length - 1].equals("strict");
        switch (tokens[0]) {
            case "start":
                if (!(tokens.length == 2)) {
                    throw new ShellUtility.invalidInputException("input at index " + idx + " is of an invalid length");
                }
                action = new StartAction(tokens[1], timeoutMs);
                break;

            case "click":
                if (!(2 <= tokens.length && tokens.length <= 3)) {
                    throw new ShellUtility.invalidInputException("input at index " + idx + " is of an invalid length");
                }
                action = new ClickAction(tokens[1], strict, timeoutMs);
                break;

            case "clickImage":
                if (!(2 <= tokens.length && tokens.length <= 3)) {
                    throw new ShellUtility.invalidInputException("input at index " + idx + " is of an invalid length");
                }
                action = new ClickImageAction(tokens[1], strict, timeoutMs);
                break;

            case "edit":
                if (!(3 <= tokens.length && tokens.length <= 4)) {
                    throw new ShellUtility.invalidInputException("input at index " + idx + " is of an invalid length");
                }
                action = new EditAction(tokens[1], tokens[2], strict, timeoutMs);
                break;

            default:
                throw new invalidInputException("input at index " + idx + " has an invalid first token:" + tokens[0]);
        }
        return action;
    }

    /**
     * A CUJ is passed in as two arrays of sequential actions, the first being preparatory (not measured)
     * and the second being measured and starting from where the first left off. e.g:
     *
     * ‘{"com.google.android.apps.maps", "click;Takeout}' '{"click;Billy Barooz", "clickImage;Search;strict", "edit;Search here;McDonalds", "click;West Kirby", "click;DIRECTIONS"}’
     *
     */
    public Action[] parseStringCUJ(String[] cuj) throws invalidInputException {
        Action[] res = new Action[cuj.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = parseStringAction(cuj[i], i);
        }
        return res;
    }

    /**
     * Executes CUJ and returns cached objects
     */
    public void  cacheCUJ(Action[] cuj) throws InterruptedException, invalidInputException, UiObjectNotFoundException, IOException {
        for (int i = 0; i < cuj.length; i++) {
            try {
                cuj[i].executeUncachedAction();
            } catch (NullPointerException e) {
                throw new invalidInputException("CUJ argument at index " + i + " was not found. Make sure your action is currently executable on your device.");
            }
        }
    }


                                /****************************
                                 * DATA MANIPULATION HELPERS
                                 ****************************/
    public long getTime() {
        return System.currentTimeMillis();
    }

    public long sumArr(long[] arr) {
        long total = 0;
        for (long t : arr) total += t;
        return total;
    }

    /**
     * differences between successive elements
     */
    public long[] differences(long[] arr) {
        if (arr.length == 0) return arr;
        long[] res = new long[arr.length - 1];
        for (int i = 0; i < res.length; i++) {
            res[i] = arr[i + 1] - arr[i];
        }
        return res;
    }
    /**
     * last (n - 1) values scaled, where first value is set to 0
     * e.g : {5, 23, 45} -> {23 - 5, 45 - 5} = {18, 40}
     */
    public long[] relativeValues(long[] arr) {
        if (arr.length == 0) return null;
        long[] res = new long[arr.length - 1];
        for (int i = 0; i < res.length; i++) {
            res[i] = arr[i + 1] - arr[0];
        }
        return res;
    }

    public long[] averageColumns(long[][] arr) {
        if (arr == null || arr.length == 0) return null;
        long[] averages = new long[arr[0].length];
        for (int j = 0; j < arr[0].length; j++) {
            long sum = 0;
            for (int i = 0; i < arr.length; i++) {
                sum += arr[i][j];
            }
            averages[j] = sum / arr.length;
        }
        return averages;
    }

    public String zeroPad(String s, int finalLength) {
        StringBuilder sBuilder = new StringBuilder(s);
        while (sBuilder.length() < finalLength) {
            sBuilder.insert(0, "0");
        }
        return sBuilder.toString();
    }

    public void logData(long[][] allActionStamps) {
        int iterations = allActionStamps.length;

        //Log action durations
        long[][] allActionDurations = new long[iterations][];
        for (int iter = 0; iter < iterations; iter++) {
            long[] actionDurations = differences(allActionStamps[iter]);
            allActionDurations[iter] = actionDurations;
            Log.i("iterations-actions", "ITERATION " + (iter + 1) + ": " + Arrays.toString(actionDurations) + ", TOTAL: " + sumArr(actionDurations));
        }

        //Log average action durations
        long[] averageActionDurations = averageColumns(allActionDurations);
        Log.i("averages-actions", "AVERAGE:     " + Arrays.toString(averageActionDurations) + ", TOTAL: " + sumArr(averageActionDurations));


        //Log time stamps relative to moment first measured action became available and store iteration durations
        long[][] allRelativeStamps = new long[iterations][];
        long[][] iterDurations = new long[iterations][2]; //to be used later for finding median
        for (int iter = 0; iter < iterations; iter++) {
            long[] actionStamps = allActionStamps[iter];
            long[] relativeStamps = relativeValues(actionStamps);

            allRelativeStamps[iter] = relativeStamps;
            iterDurations[iter][0] = iter;
            iterDurations[iter][1] = relativeStamps[relativeStamps.length - 1];
            Log.i("iterations-stamps", "ITERATION " + (iter + 1) + ": " + Arrays.toString(relativeStamps));
        }

        //log average relative stamps
        long[] averageRelativeStamps = averageColumns(allRelativeStamps);
        Log.i("averages-stamps", "AVERAGE:     " + Arrays.toString(averageRelativeStamps));

        //log median iteration
        Arrays.sort(iterDurations, (a,b) -> Long.compare(a[1], b[1]));
        int median_idx = (int) iterDurations[iterDurations.length / 2][0];
        long med_start = allActionStamps[median_idx][0];
        long med_end = allActionStamps[median_idx][allActionStamps[0].length - 1];
        Log.i("median", "MEDIAN RUN: " + (median_idx + 1));


        //log median clip data
        Log.i("clip_start", "" + med_start);
        Log.i("clip_end", "" + med_end);
    }

    /******************************************************************************
     * Executes a CUJ, caches data and then runs through it again iterations times
     * measuring and logging data on the measured actions. If there is an intent to record,
     * also leaves a window of space before and after the measured actions for recording boundaries
     *
     * @param preStr Preparatory actions array represented as a string
     *               (i.e. "['action_1', 'action_2', ... ])
     * @param preStr Measured actions array represented as a string
     *               (i.e. "['action_1', 'action_2', ... ])
     *
     * @param iterations The number of times to run through and measure the CUJ
     * @param recordIntent Whether the user intends to record the test
     ******************************************************************************/
    public void executeCUJ(String preStr, String postStr, int iterations, boolean recordIntent) throws Exception {
        String[] preCUJ = parseToArray(preStr);
        String[] postCUJ = parseToArray(postStr);
        if (postCUJ.length < 2) throw new invalidInputException("Measured CUJ must have length >= 2. Make sure you have a final 'termination' action");
        String[] cujStrings = new String[preCUJ.length + postCUJ.length];
        long recordingBufMs = recordIntent ? 1000 : 0;
        System.arraycopy(preCUJ, 0, cujStrings, 0, preCUJ.length);
        System.arraycopy(postCUJ, 0, cujStrings, preCUJ.length, postCUJ.length);
        Action[] cuj = parseStringCUJ(cujStrings);

        //caching run
        cacheCUJ(cuj);

        //cached runs:
        int iter = -1;
        long[][] allActionStamps = new long[iterations][postCUJ.length];
        while (++iter < iterations) {
            //Execute preparatory actions:
            for (int i = 0; i < preCUJ.length; i++) {
                cuj[i].executeCachedAction();
            }

            //execute recorded actions:
            //start_recording()
            sleep(recordingBufMs);
            for (int i = preCUJ.length; i < cuj.length; i++) {
                 allActionStamps[iter][i - preCUJ.length] = cuj[i].executeCachedAction();
             }
            sleep(recordingBufMs);
            //stop_recording();
        }
        if (iterations > 0) {
            logData(allActionStamps);
        }
    }
}
