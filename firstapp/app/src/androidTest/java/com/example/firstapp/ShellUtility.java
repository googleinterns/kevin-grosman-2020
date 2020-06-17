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

    public class Action {
        Boolean strict;
    }
    public class StartAction extends Action {
        String pkg;
    }
    public class ClickAction extends Action {
        String text;
    }
    public class ClickImageAction extends Action {
        String description;
    }
    public class EditAction extends Action {
        String text;
        String entered;
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


    public Action parseStringAction(String str, int i) throws invalidInputException {
        String[] tokens = str.split(";");
        if (!(1 <= tokens.length && tokens.length <= 4)) {
            throw new ShellUtility.invalidInputException("input at index " + i + " is of an invalid length");
        }
        Action action;
        switch (tokens[0]) {
            case "start":
                action = new StartAction();
                ((StartAction) action).pkg = tokens[1];
                break;

            case "click":
                action = new ClickAction();
                ((ClickAction) action).text = tokens[1];
                break;

            case "clickImage":
                action = new ClickImageAction();
                ((ClickImageAction) action).description = tokens[1];
                break;

            case "edit":
                action = new EditAction();
                ((EditAction) action).text = tokens[1];
                ((EditAction) action).entered = tokens[2];
                break;

            default:
                throw new invalidInputException("input at index " + i + " has an invalid first token");
        }
        action.strict = tokens[tokens.length - 1].equals("strict");
        return action;
    }

    public Action[] parseStringCUJ(String[] CUJ) throws invalidInputException {
        Action[] res = new Action[CUJ.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = parseStringAction(CUJ[i], i);
        }
        return res;
    }

    /**
     * Executes an action with parts speficied by tokens and returns a UiObject (for caching)
     */
    public UiObject executeUncachedAction(Action action, int i) throws invalidInputException, UiObjectNotFoundException, IOException, InterruptedException {
        boolean strict = action.strict;

        UiObject2 object2 = null;
        UiObject object = null;
        if (action instanceof StartAction) {
            String pkg = ((StartAction) action).pkg;
            forceQuitApp(pkg);
            launchApp(pkg);
        } else if (action instanceof ClickAction) {
            String text = ((ClickAction) action).text;
            if (strict) {
                object2 = device.wait(Until.findObject(By.text(text)), timeoutMs);
            } else {
                object2 = device.wait(Until.findObject(By.textContains(text)), timeoutMs);
            }
            object = castToObject(object2);
            object.click();
        } else if(action instanceof ClickImageAction) {
            String description = ((ClickImageAction) action).description;
            if (strict) {
                object2 = device.wait(Until.findObject(By.desc(description)), timeoutMs);
            } else {
                object2 = device.wait(Until.findObject(By.descContains(description)), timeoutMs);
            }
            object = castToObject(object2);
            object.click();
        } else if(action instanceof EditAction) {
            String text = ((EditAction) action).text;
            String entered = ((EditAction) action).entered;
            if (strict) {
                object2 = device.wait(Until.findObject(By.text(text)), timeoutMs);
            } else {
                object2 = device.wait(Until.findObject(By.textContains(text)), timeoutMs);
            }
            object = getEditableObject(object2);
            object.waitForExists(timeoutMs);
            object.legacySetText(entered);
        } else {
            throw new invalidInputException("an action has an invalid first token at index" + i);
        }
        return object;
    }

    /**
     * Executes CUJ and returns cached objects
     */
    public UiObject[] cacheCUJ(Action[] CUJ) throws InterruptedException, invalidInputException, UiObjectNotFoundException, IOException {
        UiObject[] cachedObjects = new UiObject[CUJ.length];
        for (int i = 0; i < CUJ.length; i++) {
            cachedObjects[i] = executeUncachedAction(CUJ[i], i);
        }
        return cachedObjects;
    }

    /**
     * Executes a CUJ with preparatory actions pre and measured actions post. Caches information for later use.
     */
    public void executeCUJ(String[] preCUJ, String[] postCUJ) throws Exception {
        String[] CUJStrings = new String[preCUJ.length + postCUJ.length];
        System.arraycopy(preCUJ, 0, CUJStrings, 0, preCUJ.length);
        System.arraycopy(postCUJ, 0, CUJStrings, preCUJ.length, postCUJ.length);
        Action[] CUJ = parseStringCUJ(CUJStrings);
        UiObject[] cachedObjects = cacheCUJ(CUJ);
    }



}
