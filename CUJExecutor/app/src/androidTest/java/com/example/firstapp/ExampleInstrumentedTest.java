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
import android.content.Context;
import android.os.RemoteException;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.Until;

import java.io.IOException;

import static java.lang.Thread.sleep;



import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String BASIC_SAMPLE_PACKAGE = "com.example.firstapp";
    private static final int TIMEOUT = 5000;


    //target value for searchForTargetNumber()
    private static Integer TARGET = 2;
    //Celebration message to be displayed when target is found
    private static String CELEBRATION = "YAYYYY! " + TARGET + "!!";

    private ShellUtility shellUtility;

    @Before
    public void getDevice() {
        shellUtility = new ShellUtility(TIMEOUT);
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.firstapp", appContext.getPackageName());
    }



    @Test
    public void searchForTargetNumber() throws InterruptedException, IOException, RemoteException, UiObjectNotFoundException {

        shellUtility.forceQuitApp(BASIC_SAMPLE_PACKAGE);
        shellUtility.launchApp(BASIC_SAMPLE_PACKAGE);

        Integer val = -1;
        //Keep getting random values in the range [0, TARGET] until TARGET comes up
        while (val != TARGET) {
            //wait for count and random buttons to appear on screen
            UiObject2 countButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "count_button")),TIMEOUT);
            UiObject2 randomButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE,"random_button")), TIMEOUT);


            int count = TARGET;
            while (count-- != 0) countButton.click();
            //get random number in range [0, counter] (counter is now TARGET)
            randomButton.click();
            UiObject2 randomText = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "textview_random")), TIMEOUT );
            UiObject2 previousButton = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "button_second")),TIMEOUT);


            //ensure that random produced a value in the appropriate range
            val = Integer.parseInt((randomText.getText()));
            assert(0 <= val && val <= TARGET);

            //go back to first fragment
            previousButton.click();
        }
        //display CELEBRATION message and make sure it appears on the screen
        sleep(1000);
        UiObject2 sentimentText = shellUtility.device.wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "plaintext_sentiment")),TIMEOUT);
        sentimentText.setText(CELEBRATION);
        assertEquals(CELEBRATION, sentimentText.getText());
        sleep(1000);


        //Can we use old UIObjects after leaving a fragment and returning?


    }

}
