package com.example.firstapp;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.io.IOException;


@RunWith(AndroidJUnit4.class)
public class MapsInstrumentedTest {

    private ShellUtility shellUtility;
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final int TIMEOUT = 5000;
    private static final String SEARCH = "Googleplex";

    @Before
    public void getDevice() {
        shellUtility = new ShellUtility();
    }

    @Test
    public void getDirections() throws InterruptedException, UiObjectNotFoundException, IOException {
        shellUtility.forceQuitApp(MAPS_PACKAGE);
        shellUtility.launchApp(MAPS_PACKAGE);


        UiObject search_edit_text = shellUtility.device.findObject(new UiSelector().className("android.widget.EditText").resourceId("com.google.android.apps.maps:id/search_omnibox_text_box"));
        search_edit_text.waitForExists(TIMEOUT);
        search_edit_text.legacySetText(SEARCH);

        //grab the RecyclerView with the possible locations and select the first one
        UiObject first_desitnation = shellUtility.device.findObject(new UiSelector().className("android.support.v7.widget.RecyclerView").resourceId("com.google.android.apps.maps:id/recycler_view").childSelector(new UiSelector().index(0)));
        first_desitnation.waitForExists(TIMEOUT);
        first_desitnation.click();

        UiObject directions_button = shellUtility.device.findObject(new UiSelector().className("android.widget.Button").text("Directions"));
        directions_button.waitForExists(TIMEOUT);
        directions_button.click();

        UiObject choose_starting_point_box = shellUtility.device.findObject(new UiSelector().className("android.widget.EditText").resourceId("com.google.android.apps.maps:id/directions_startpoint_textbox"));
        choose_starting_point_box.waitForExists(TIMEOUT);
        choose_starting_point_box.click();

        UiObject your_location_button = shellUtility.device.findObject(new UiSelector().className("android.widget.TextView").text("Your location"));
        your_location_button.waitForExists(TIMEOUT);
        your_location_button.click();
    }


}

