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
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;
import org.hamcrest.CoreMatchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.lang.Thread.sleep;


public class ShellUtility {
    public UiDevice device;
    private long timeoutMs;

    public ShellUtility(long timeoutMillis) {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        timeoutMs = timeoutMillis;
    }

    public static class InvalidInputException extends Exception {
        public InvalidInputException(String message) {
            super(message);
        }
    }

                                /******************************
                                 *       ACTION CLASS
                                 ******************************/
    public abstract class Action {
        private UiObject cachedObject;
        long actionTimeout;
        String actionString;

        void setCachedObject(UiObject object) {
            cachedObject = object;
        }
        UiObject getCachedObject() {
            return cachedObject;
        }

        /**
         * Spit a message to logcat indicating that the action was executed
         **/

        void logExecuted() {
            Log.i("action-completion", "Executed: " + actionString);
        }

        /**
         * execute the action and cache the result in cachedObject
         **/
        abstract void executeUncachedAction() throws Exception;

        /**
         * execute the action on the object specified by cachedObject
         * return the time (since epoch) just before the action is performed
         **/
        abstract long executeCachedAction() throws UiObjectNotFoundException, IOException, InterruptedException, RemoteException;
    }

    public class StartAction extends Action {
        String pkg;

        public StartAction(String p, long timeoutMillis, String str) {
            pkg = p;
            actionTimeout = timeoutMillis;
            actionString = str;
        }

        @Override
        void executeUncachedAction() throws IOException, InterruptedException, RemoteException, UiObjectNotFoundException {
            sleep(2000); //In case packages are changing, wait for new package to load
            launchApp(pkg);
            logExecuted();
        }

        @Override
        long executeCachedAction() throws IOException, InterruptedException, RemoteException, UiObjectNotFoundException {
            long time = launchApp(pkg);
            logExecuted();
            return time;
        }
    }

    public class ClickAction extends Action {
        String text;
        Boolean strict;
        public ClickAction(String t, Boolean s, long timeoutMillis, String str) {
            text = t;
            strict = s;
            actionTimeout = timeoutMillis;
            actionString = str;
        }

        @Override
        void executeUncachedAction() throws Exception {
            sleep(2000); //In case packages are changing, wait for new package to load
            UiObject2 object2;
            if (strict) {
                object2 = device.wait(Until.findObject(By.pkg(device.getCurrentPackageName()).text(text)), actionTimeout);
            } else {
                object2 = device.wait(Until.findObject(By.pkg(device.getCurrentPackageName()).textContains(text)), actionTimeout);
            }
            if (object2 == null) throw new Exception();
            UiObject object = castToObject(object2);
            object.click();
            setCachedObject(object);
            logExecuted();
        }

        @Override
        long executeCachedAction() throws UiObjectNotFoundException {
            UiObject object = getCachedObject();
            object.waitForExists(actionTimeout);
            object.click();
            logExecuted();
            return getTime();
        }
    }

    public class ClickImageAction extends Action {
        String description;
        Boolean strict;
        public ClickImageAction(String d, Boolean s, long timeoutMillis, String str) {
            description = d;
            strict = s;
            actionTimeout = timeoutMillis;
            actionString = str;
        }

        @Override
        void executeUncachedAction() throws UiObjectNotFoundException, InvalidInputException, InterruptedException {
            sleep(2000); //In case packages are changing, wait for new package to load
            UiObject2 object2;
            if (strict) {
                object2 = device.wait(Until.findObject(By.pkg(device.getCurrentPackageName()).desc(description)), actionTimeout);
            } else {
                object2 = device.wait(Until.findObject(By.pkg(device.getCurrentPackageName()).descContains(description)), actionTimeout);
            }
            if (object2 == null) throwImageContentDescriptions(description, actionTimeout);
            UiObject object = castToObject(object2);
            object.click();
            setCachedObject(object);
            logExecuted();
        }

        @Override
        long executeCachedAction() throws UiObjectNotFoundException {
            UiObject object = getCachedObject();
            object.waitForExists(actionTimeout);
            object.click();
            logExecuted();
            return getTime();
        }
    }

    public class EditAction extends Action {
        String text;
        String entered;
        Boolean strict;
        public EditAction(String t, String e, Boolean s, long timeoutMillis, String str) {
            text = t;
            entered = e;
            strict = s;
            actionTimeout = timeoutMillis;
            actionString = str;
        }

        @Override
        void executeUncachedAction() throws Exception {
            sleep(2000); //In case packages are changing, wait for new package to load
            UiObject2 object2;
            if (strict) {
                object2 = device.wait(Until.findObject(By.pkg(device.getCurrentPackageName()).text(text)), actionTimeout);
            } else {
                object2 = device.wait(Until.findObject(By.pkg(device.getCurrentPackageName()).textContains(text)), actionTimeout);
            }
            if (object2 == null) throw new Exception();
            UiObject object = getEditableObject(object2);
            object.waitForExists(actionTimeout);
            object.legacySetText(entered);
            setCachedObject(object);
            logExecuted();
        }

        @Override
        long executeCachedAction() throws UiObjectNotFoundException {
            UiObject object = getCachedObject();
            object.waitForExists(actionTimeout);
            object.legacySetText(entered);
            logExecuted();
            return getTime();
        }
    }
                                /******************************
                                 *    APP LIFECYCLE HELPERS
                                 ******************************/
    /**
     * Launches the specified App on the specified device and returns the time just before the app was
     * launched--to be used later for timing startup.
     */
    public long launchApp(String pkg) throws InterruptedException, IOException, RemoteException, UiObjectNotFoundException {
        forceQuitApps();
        //Start from the home screen
        final String launcherPackage = device.getLauncherPackageName();
        Assert.assertThat(launcherPackage, CoreMatchers.notNullValue());
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), timeoutMs);


        // Launch the app
        //Process process = new ProcessBuilder("am", "start", pkg).start();

        Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkg);

        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        context.startActivity(intent);

        long start = getTime();
        device.wait(Until.hasObject(By.pkg(pkg).depth(0)), timeoutMs);

        return start;

    }
    /**
     * clears all recent apps from the recent apps panel
     */
    public void forceQuitApps() throws IOException, InterruptedException, UiObjectNotFoundException, RemoteException {
        /*Context context = ApplicationProvider.getApplicationContext();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(homeIntent);*/



        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String snapshotResourceId = device.getLauncherPackageName() + ":id/snapshot";

        UiObject2 maps_button = device.wait(Until.findObject(By.clazz("android.view.View").res(snapshotResourceId)), 1000);
        if (maps_button == null) { //if recent apps page is not already opened, open it
            device.pressRecentApps();
        }

        maps_button = device.wait(Until.findObject(By.clazz("android.view.View").res(snapshotResourceId)), 1000);
        if (maps_button == null) { //if there are no recent apps, exit recent apps
            device.pressBack();
            return;
        }
        while (maps_button != null) { //clear all recent apps (after last one is cleared, we will be returned to the home screen
            maps_button.swipe(Direction.UP, 1f, 10000);
            maps_button = device.wait(Until.findObject(By.clazz("android.view.View").res(snapshotResourceId)), 1000);
        }


        /*device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Process process = new ProcessBuilder("am", "force-stop", "com.google.android.apps.maps").start();
        amKillProcess("com.google.android.apps.maps");
*/


        //ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        //sleep(10000);
        //sleep(10000);


    }



    public void amKillProcess(String process)
    {
        Context context = ApplicationProvider.getApplicationContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo>  runningProcesses = am.getRunningAppProcesses();

        for(ActivityManager.RunningAppProcessInfo runningProcess : runningProcesses)
        {
            if(runningProcess.processName.equals(process))
            {
                android.os.Process.sendSignal(runningProcess.pid, android.os.Process.SIGNAL_KILL);
            }
        }
    }


                                /******************************
                                 *    OBJECT FINDING HELPERS
                                 ******************************/
    public void throwImageContentDescriptions(String description, long actionTimeout) throws InvalidInputException {
        List<UiObject2> views = device.wait(Until.findObjects(By.pkg(device.getCurrentPackageName())), actionTimeout);
        if (views == null) {
            throw new InvalidInputException("no image matching description \"" + description + "\' found," +
                    " and no Images were were found either... Try something else.");
        }
        List<String> possibleDescriptions = new ArrayList<>();
        for (int i = 0; i < views.size(); i++) {
            UiObject2 object = views.get(i);
            if (object.getText() == null && object.getContentDescription() != null) {
                possibleDescriptions.add(object.getContentDescription());
            }
        }
        throw new InvalidInputException("no image matching description \"" + description + "\' found," +
                " but images with the following descriptions were found:\n" + possibleDescriptions + "\nTry again with one of these.");
    }
    /**
     * Walks up view tree until a clickable ancestor is found. Google apps often have clickable
     * views that have a textbox as a descendant and thus the view containing the text cannot be
     * clicked
     */
    public UiObject2 getLowestClickableAncestor(UiObject2 object2) throws InvalidInputException {
        if (object2 == null) {
            throw new InvalidInputException("passed object is null. Consider increasing TIMEOUT parameter.");
        }
        while (!object2.isClickable()) {
            object2 = object2.getParent();
        }
        if (object2 == null) {
            throw new InvalidInputException("No clickable ancestors. Try more specific search terms.");
        }
        return object2;
    }

    /**
     * Creates a UiObject using the identifying information from a UIObject. Necessary for caching,
     * since UiObject2s store references to particular views and are thus worthless once that view
     * has been destroyed.
     */
    public UiObject castToObject(UiObject2 object2) throws InvalidInputException {

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
            throw new InvalidInputException("Casting failed. Try different actions.");
        }
        return casted;
    }

    /**
     * Retrieves an editable UiObject from the UiObject2 corresponding to the view containing the
     * hint text in a textbox.
     */
    public UiObject getEditableObject(UiObject2 object2) throws InvalidInputException {
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
    public Action parseStringAction(String str, int idx) throws InvalidInputException {
        String[] tokens = str.split(";");
        Action action;
        boolean strict = tokens[tokens.length - 1].equals("strict");
        switch (tokens[0]) {
            case "start":
                if (!(tokens.length == 2)) {
                    throw new ShellUtility.InvalidInputException("input at index " + idx + " is of an invalid length");
                }
                action = new StartAction(tokens[1], timeoutMs, str);
                break;

            case "click":
                if (!(2 <= tokens.length && tokens.length <= 3)) {
                    throw new ShellUtility.InvalidInputException("input at index " + idx + " is of an invalid length");
                }
                action = new ClickAction(tokens[1], strict, timeoutMs, str);
                break;

            case "clickImage":
                if (!(2 <= tokens.length && tokens.length <= 3)) {
                    throw new ShellUtility.InvalidInputException("input at index " + idx + " is of an invalid length");
                }
                action = new ClickImageAction(tokens[1], strict, timeoutMs, str);
                break;

            case "edit":
                if (!(3 <= tokens.length && tokens.length <= 4)) {
                    throw new ShellUtility.InvalidInputException("input at index " + idx + " is of an invalid length");
                }
                action = new EditAction(tokens[1], tokens[2], strict, timeoutMs, str);
                break;

            default:
                throw new InvalidInputException("input at index " + idx + " has an invalid first token:" + tokens[0]);
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
    public Action[] parseStringCUJ(String[] cuj) throws InvalidInputException {
        Action[] res = new Action[cuj.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = parseStringAction(cuj[i], i);
        }
        return res;
    }

    /**
     * Executes CUJ and returns cached objects
     */
    public void  cacheCUJ(Action[] cuj) throws Exception {
        for (int i = 0; i < cuj.length; i++) {
            try {
                cuj[i].executeUncachedAction();
            } catch (Exception e) {
                if (!(e instanceof InvalidInputException)) {
                    throw new InvalidInputException("CUJ argument at index " + i + " was not found. Make sure your action is currently executable on your device.");
                } else {
                    throw e;
                }
            }
        }
    }

                                /****************************
                                 * DATA MANIPULATION HELPERS
                                 ****************************/
    public long getTime() {
        return System.currentTimeMillis();
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


    public String zeroPad(String s, int finalLength) {
        StringBuilder sBuilder = new StringBuilder(s);
        while (sBuilder.length() < finalLength) {
            sBuilder.insert(0, "0");
        }
        return sBuilder.toString();
    }

    public String milisecondsToTime (long t) {
        t %= 86400000;
        String hours = zeroPad((t / 3600000) + "",2);
        t %= 3600000;
        String minutes = zeroPad(t / 60000 + "", 2);
        t %= 60000;
        String seconds = zeroPad(t / 1000 + "",2);
        t %= 1000;
        String miliseconds = zeroPad(t + "", 3);
        return (hours + ":" + minutes + ":" + seconds + "." + miliseconds);
    }

    /**
     *  given a time for the start of a clip, provide a buffer at the beginning and format the resulting time
     *  in the form HH:MM:SS.sss
     */

    public String adjustAndFormatStart(long time) {
        return milisecondsToTime(time - 2000);
    }

    /**
     *  given a time for the end of a clip, provide a buffer at the end and format the resulting time
     *  in the form HH:MM:SS.sss
     *
     */
    public String adjustAndFormatEnd(long time) {
        return milisecondsToTime(time + 500);
    }


    public void logData(long[][] allActionStamps, long recstart) {
        int iterations = allActionStamps.length;

        for (int iter = 0; iter < iterations; iter++) {
            long[] actionDurations = differences(allActionStamps[iter]);
            Log.i("durations-raw", Arrays.toString(actionDurations));

            //Log times:
            long iterStart = allActionStamps[iter][0] - recstart; //offset by recording start
            long iterEnd = allActionStamps[iter][allActionStamps[0].length - 1] - recstart; //offset by recording start
            String[] recInterval = new String[] {adjustAndFormatStart(iterStart), adjustAndFormatEnd(iterEnd)};
            Log.i("durations-raw", Arrays.toString(recInterval));
        }
    }


    /******************************************************************************
     * Executes a CUJ, caches data and then runs through it again iterations times
     * measuring and logging data on the measured actions. If there is an intent to record,
     * also leaves a window of space before and after the measured actions for recording boundaries
     *
     * @param preActionsStr Preparatory actions array represented as a string
     *               (i.e. "['action_1', 'action_2', ... ])
     * @param preActionsStr Measured actions array represented as a string
     *               (i.e. "['action_1', 'action_2', ... ])
     *
     * @param iterations The number of times to run through and measure the CUJ
     * @param recordIntent Whether the user intends to record the test
     ******************************************************************************/
    public void iterateAndMeasureCuj(String preActionsStr, String cujActionsStr, int iterations, boolean recordIntent, long recstart) throws Exception {
        String[] preCUJ = parseToArray(preActionsStr);
        String[] postCUJ = parseToArray(cujActionsStr);
        if (postCUJ.length == 1) throw new InvalidInputException("Measured CUJ must have length >= 2. Make sure you have a final 'termination' action");
        String[] cujStrings = new String[preCUJ.length + postCUJ.length];
        long recordingBufMs = recordIntent ? 500 : 0;
        System.arraycopy(preCUJ, 0, cujStrings, 0, preCUJ.length);
        System.arraycopy(postCUJ, 0, cujStrings, preCUJ.length, postCUJ.length);
        Action[] cuj = parseStringCUJ(cujStrings);

        forceQuitApps(); //For CUJS that never launch an app directly, and thus never otherwise clear recent apps

        //caching run
        cacheCUJ(cuj);
        sleep(1000);

        //cached runs:
        int iter = -1;
        long[][] allActionStamps = new long[iterations][postCUJ.length];
        while (++iter < iterations) {

            forceQuitApps(); //For CUJS that never launch an app directly, and thus never otherwise clear recent apps
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
            sleep(1000);
            //stop_recording();
        }
        if (iterations > 0) {
            logData(allActionStamps, recstart);
        }
    }


    public enum cujFlag {
        ALL,         //entire CUJ
        ALLBUTLAST,  //entire CUJ, except for last action
        LAST,        //just the last action
        PRE,         //preCUJ
        POST,        //postCUJ
        FIRST,       //just the first action
        ALLBUTFIRST //entire CUJ, except for first action
    }

    /**
    *  walks through a portion of the CUJ n times as specified by the flag:
     *
     */
    public void walkCujNTimes(String preStr, String cujStr, cujFlag flag, int n) throws Exception {
        String[] preCUJ = parseToArray(preStr);
        String[] postCUJ = parseToArray(cujStr);
        String[] entireCUJ = new String[preCUJ.length + postCUJ.length];
        System.arraycopy(preCUJ, 0, entireCUJ, 0, preCUJ.length);
        System.arraycopy(postCUJ, 0, entireCUJ, preCUJ.length, postCUJ.length);


        String[] sectionCUJ = null;

        switch (flag) {
            case ALL:
                sectionCUJ = entireCUJ;
                break;
            case ALLBUTLAST:
                sectionCUJ = new String[entireCUJ.length - 1];
                System.arraycopy(entireCUJ, 0, sectionCUJ, 0, entireCUJ.length - 1);
                break;
            case LAST:
                sectionCUJ = new String[] {entireCUJ[entireCUJ.length - 1]};
                break;
            case PRE:
                sectionCUJ = preCUJ;
                break;
            case POST:
                sectionCUJ = postCUJ;
                break;
            case FIRST:
                sectionCUJ = new String[] {entireCUJ[0]};
                break;
            case ALLBUTFIRST:
                sectionCUJ = new String[entireCUJ.length - 1];
                System.arraycopy(entireCUJ, 1, sectionCUJ, 0, entireCUJ.length - 1);
                break;
        }


        Action[] cuj = parseStringCUJ(sectionCUJ);
        //run n times:
        for (int k = 0; k < n; k++) {
            //For CUJS that never launch an app directly, and thus never otherwise clear recent apps, clear them if we are executing the first action in the CUJ
            if (flag == cujFlag.ALL || flag == cujFlag.ALLBUTLAST || flag == cujFlag.PRE || flag == cujFlag.FIRST || sectionCUJ.length == entireCUJ.length) forceQuitApps();
            //Run through cuj once (cached data won't actually be used)
            cacheCUJ(cuj);
            sleep(1000);
        }

        Log.i("actions-run", Arrays.toString(sectionCUJ));

    }
}
