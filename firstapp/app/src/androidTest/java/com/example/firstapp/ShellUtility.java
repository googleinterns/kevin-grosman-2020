package com.example.firstapp;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;


public class ShellUtility {
    public UiDevice device;
    private int timeoutMs = 6000;


    public ShellUtility() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    public static class invalidInputException extends Exception {
        public invalidInputException(String message) {
            super(message);
        }
    }

    public abstract class Action {
        abstract UiObject executeUncachedAction() throws IOException, InterruptedException, UiObjectNotFoundException, invalidInputException;
    }
    public class StartAction extends Action {
        String pkg;
        public StartAction(String p) {
            pkg = p;
        }
        UiObject executeUncachedAction() throws IOException, InterruptedException {
            forceQuitApp(pkg);
            launchApp(pkg);
            return null;
        }
    }
    public class ClickAction extends Action {
        String text;
        Boolean strict;
        public ClickAction(String t, Boolean s) {
            text = t;
            strict = s;
        }
        UiObject executeUncachedAction() throws IOException, InterruptedException, UiObjectNotFoundException, invalidInputException {
            UiObject2 object2;
            if (strict) {
                object2 = device.wait(Until.findObject(By.text(text)), timeoutMs);
            } else {
                object2 = device.wait(Until.findObject(By.textContains(text)), timeoutMs);
            }
            UiObject object = castToObject(object2);
            object.click();
            return object;
        }
    }
    public class ClickImageAction extends Action {
        String description;
        Boolean strict;
        public ClickImageAction(String d, Boolean s) {
            description = d;
            strict = s;
        }
        UiObject executeUncachedAction() throws IOException, InterruptedException, UiObjectNotFoundException, invalidInputException {
            UiObject2 object2;
            if (strict) {
                object2 = device.wait(Until.findObject(By.desc(description)), timeoutMs);
            } else {
                object2 = device.wait(Until.findObject(By.descContains(description)), timeoutMs);
            }
            UiObject object = castToObject(object2);
            object.click();
            return object;
        }
    }
    public class EditAction extends Action {
        String text;
        String entered;
        Boolean strict;
        public EditAction(String t, String e, Boolean s) {
            text = t;
            entered = e;
            strict = s;
        }
        UiObject executeUncachedAction() throws IOException, InterruptedException, UiObjectNotFoundException, invalidInputException {
            UiObject2 object2;
            if (strict) {
                object2 = device.wait(Until.findObject(By.text(text)), timeoutMs);
            } else {
                object2 = device.wait(Until.findObject(By.textContains(text)), timeoutMs);
            }
            UiObject object = getEditableObject(object2);
            object.waitForExists(timeoutMs);
            object.legacySetText(entered);
            return object;
        }
    }



    /**
     * Launches the specified App on the specified device and returns the time just before the app was
     * launched--to be used later for timing startup.
     */
    public long launchApp(String pkg) throws InterruptedException, IOException {


    /* Might want to reinclude this later:
    //Start from the home screen
    device.pressHome();
    final String launcherPackage = device.getLauncherPackageName();
    Assert.assertThat(launcherPackage, CoreMatchers.notNullValue());
    device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), timeoutMs);
    */

        // Launch the app
        //Process process = new ProcessBuilder("am", "start", pkg).start();

        Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkg);

        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //startActivity imposes a 5 second cool-down after the home button is pressed, so we wait
        //out that cool-down before grabbing the time and launching
        //sleep(5000);
        long start = SystemClock.elapsedRealtime();
        context.startActivity(intent);

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(pkg).depth(0)), timeoutMs);

        return start;

    }


    public void forceQuitApp(String pkg) throws IOException, InterruptedException {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Process process = new ProcessBuilder("am", "force-stop", pkg).start();

    }

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

    /** INSTRUCTIONS FOR ACTION FORMATTING:
     * The first action must be a string containing the package to be opened (e.g. “start;com.google.android.apps.maps").
     * Each subsequent action must be to click on a particular View or to enter text in a particular textbox. Subsequent
     * actions can take on one of the following forms:
     *
     * 1. “edit;text_displayed;text_entered” to enter text_entered into a textbox, where text_displayed is the text currently visible in the textbox.
     * 2. “click;text_displayed” to click a non-editable view (normally a text-view or button) on the screen which has text containing the string text_displayed displayed on it
     * 3. “clickImage;text_description” to click a view without text (normally an image view) where the description of the view contains the string description_text
     *
     * Note that all text fields are case sensitive. Additionally, if any of the above strings are followed by “;strict”
     * the search for a corresponding view will enforce that an exact match is found for text_displayed (in cases 1 or 2)
     * or text_description (in case 3).
     */
    public Action parseStringAction(String str, int i) throws invalidInputException {
        String[] tokens = str.split(";");
        if (!(1 <= tokens.length && tokens.length <= 4)) {
            throw new ShellUtility.invalidInputException("input at index " + i + " is of an invalid length");
        }
        Action action;
        boolean strict = tokens[tokens.length - 1].equals("strict");
        switch (tokens[0]) {
            case "start":
                action = new StartAction(tokens[1]);
                break;

            case "click":
                action = new ClickAction(tokens[1], strict);
                break;

            case "clickImage":
                action = new ClickImageAction(tokens[1], strict);
                break;

            case "edit":
                action = new EditAction(tokens[1], tokens[2], strict);
                break;

            default:
                throw new invalidInputException("input at index " + i + " has an invalid first token");
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
    public UiObject[] cacheCUJ(Action[] cuj) throws InterruptedException, invalidInputException, UiObjectNotFoundException, IOException {
        UiObject[] cachedObjects = new UiObject[cuj.length];
        for (int i = 0; i < cuj.length; i++) {
            cachedObjects[i] = cuj[i].executeUncachedAction();
        }
        return cachedObjects;
    }

    /**
     * Executes a CUJ with preparatory actions pre and measured actions post. Caches information for later use.
     */
    public void executeCUJ(String[] preCUJ, String[] postCUJ) throws Exception {
        String[] cujStrings = new String[preCUJ.length + postCUJ.length];
        System.arraycopy(preCUJ, 0, cujStrings, 0, preCUJ.length);
        System.arraycopy(postCUJ, 0, cujStrings, preCUJ.length, postCUJ.length);
        Action[] cuj = parseStringCUJ(cujStrings);
        UiObject[] cachedObjects = cacheCUJ(cuj);
    }



}
