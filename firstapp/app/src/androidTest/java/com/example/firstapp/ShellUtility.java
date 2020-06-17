package com.example.firstapp;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
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

    public static class invalidInputException extends Exception {
        public invalidInputException(String message) {
            super(message);
        }
    }

    /**
     * Launches the specified App on the specified device and returns the time just before the app was
     * launched--to be used later for timing startup.
     */
    public static long launchApp(UiDevice device, String pkg, int timeoutMs) throws InterruptedException, IOException {
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
        sleep(5000);
        long start = SystemClock.elapsedRealtime();
        context.startActivity(intent);

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(pkg).depth(0)), timeoutMs);

        return start;

    }


    public static void forceQuitApp(String pkg) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("am", "force-stop", pkg).start();

    }

    /**
     * Walks up view tree until a clickable ancestor is found. Google apps often have clickable
     * views that have a textbox as a descendant and thus the view containing the text cannot be
     * clicked
     */
    public static UiObject2 getLowestClickableAncestor(UiObject2 object2) throws invalidInputException {
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
    public static UiObject castToObject(UiDevice device, UiObject2 object2) throws invalidInputException {

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
    public static UiObject getEditableObject(UiDevice device, UiObject2 object2) throws invalidInputException {
        return castToObject(device, getLowestClickableAncestor(object2));
    }


}
