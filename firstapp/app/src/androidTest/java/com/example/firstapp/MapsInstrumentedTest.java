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


@RunWith(AndroidJUnit4.class)
public class MapsInstrumentedTest {

    private UiDevice device;
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final int TIMEOUT = 5000;
    private static final String SEARCH = "Googleplex";

    @Before
    public void get_device() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void getDirections() throws InterruptedException, UiObjectNotFoundException {
        UtilityMethods.launchApp(device, MAPS_PACKAGE, TIMEOUT);


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
}

