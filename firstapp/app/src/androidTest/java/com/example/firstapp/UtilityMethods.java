package com.example.firstapp;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;

import java.io.IOException;

import static java.lang.Thread.sleep;


public class UtilityMethods {
    /*
    Launches the specified App on the specified device and returns the time just before the app was
    launched--to be used later for timing startup.
     */
    static long launchApp(UiDevice device, String pkg, int timeout) throws InterruptedException {
        // Start from the home screen
        device.pressHome();

        // Wait for launcher
        final String launcherPackage = device.getLauncherPackageName();
        Assert.assertThat(launcherPackage, CoreMatchers.notNullValue());
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), timeout);

        // Launch the app
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
        device.wait(Until.hasObject(By.pkg(pkg).depth(0)), timeout);

        return start;

    }


    static void force_quit_app(String pkg) throws IOException, InterruptedException {
        Runtime.getRuntime().exec(new String[] {"am", "force-stop", pkg});
        sleep(5000);
    }


}
