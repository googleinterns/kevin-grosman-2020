package com.example.firstapp;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.uiautomator.UiDevice;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import java.util.Timer;

import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;



import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String BASIC_SAMPLE_PACKAGE = "com.example.firstapp";
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final int TIMEOUT = 5000;
    private static final String SEARCH = "Googleplex";
    private UiDevice device;
    //target value for searchForTargetNumber()
    private static Integer TARGET = 2;
    //Celebration message to be displayed when target is found
    private static String CELEBRATION = "YAYYYY! " + TARGET + "!!";


    private void launchApp(String Package) {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        device.pressHome();

        // Wait for launcher
        final String launcherPackage = device.getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), TIMEOUT);

        // Launch the app
        Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(Package);
        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(Package).depth(0)), TIMEOUT);

    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.firstapp", appContext.getPackageName());
    }


    @Test
    public void getDirections() throws InterruptedException, UiObjectNotFoundException {
        launchApp(MAPS_PACKAGE);


        UiObject search_edit_text = device.findObject(new UiSelector().className("android.widget.EditText").resourceId("com.google.android.apps.maps:id/search_omnibox_text_box"));
        search_edit_text.waitForExists(TIMEOUT);
        search_edit_text.legacySetText(SEARCH);

        //grab the RecyclerView with the possible locations and select the first one
        UiObject first_desitnation = device.findObject(new UiSelector().className("android.support.v7.widget.RecyclerView").resourceId("com.google.android.apps.maps:id/recycler_view").childSelector(new UiSelector().index(0)));
        first_desitnation.waitForExists(TIMEOUT);
        first_desitnation.click();

        UiObject directions_button = device.findObject(new UiSelector().className("android.widget.Button").text("Directions"));
        directions_button.waitForExists(TIMEOUT);
        directions_button.click();

        UiObject choose_starting_point_box = device.findObject(new UiSelector().className("android.widget.EditText").resourceId("com.google.android.apps.maps:id/directions_startpoint_textbox"));
        choose_starting_point_box.waitForExists(TIMEOUT);
        choose_starting_point_box.click();

        UiObject your_location_button = device.findObject(new UiSelector().className("android.widget.TextView").text("Your location"));
        your_location_button.waitForExists(TIMEOUT);
        your_location_button.click();

        UiObject start_button = device.findObject(new UiSelector().className("android.widget.Button").resourceId("com.google.android.apps.maps:id/start_button"));
        start_button.waitForExists(TIMEOUT);
        start_button.click();

    }



    @Test
    public void searchForTargetNumber() throws InterruptedException {
        launchApp(BASIC_SAMPLE_PACKAGE);

        //pressing count button:
        //UiObject countButton = device.findObject(new UiSelector().className("android.widget.Button").instance(1));


        Integer val = -1;
        //Keep getting random values in the range [0, TARGET] until TARGET comes up
        while (val != TARGET) {
            //wait for count and random buttons to appear on screen
            UiObject2 countButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "count_button")),TIMEOUT);
            UiObject2 randomButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);


            int count = TARGET;
            while (count-- != 0) countButton.click();
            //get random number in range [0, counter] (counter is now TARGET)
            randomButton.click();
            UiObject2 randomText = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "textview_random")), TIMEOUT );
            UiObject2 previousButton = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);


            //ensure that random produced a value in the appropriate range
            val = Integer.parseInt((randomText.getText()));
            assert(0 <= val && val <= TARGET);

            //go back to first fragment
            previousButton.click();
        }
        //display CELEBRATION message and make sure it appears on the screen
        sleep(1000);
        UiObject2 sentimentText = device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "plaintext_sentiment")),TIMEOUT);
        sentimentText.setText(CELEBRATION);
        assertEquals(CELEBRATION, sentimentText.getText());
        sleep(1000);


        //Can we use old UIObjects after leaving a fragment and returning?


    }

}