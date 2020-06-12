package com.example.firstapp;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.content.res.TypedArrayUtils;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import junit.runner.Version;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
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
    private static final String YT_PACKAGE = "com.google.android.youtube";

    private static final int TIMEOUT = 6000;
    private static final String SEARCH = "Googleplex";
    private static final int NUM_ITERATIONS = 3;
    private UiDevice device;
    class invalidInputException extends Exception {
        public invalidInputException(String message) {
            super(message);
        }
    }

    /*************************************************
                     TEST CUJS for GM
     *************************************************/
    private static final String[] directions = {"start", "edit;Search here;Googleplex", "click;Amphitheatre", "click;Directions", "click;Choose starting", "click;Your location;strict"};
    private static final String[] nav = {"start", "edit;Search here;Googleplex", "click;Amphitheatre", "click;Directions", "click;Choose starting", "click;Your location;strict", "click;Start"};
    private static final String[] drinks = {"start", "click;Cheap drinks", "click;Top rated", "click;Open now", "click;View map", "clickImage;your location"};
    private static final String[] change_of_heart = {"start", "click;Takeout", "click;Billy Barooz", "clickImage;Search;strict", "edit;Search here;McDonalds", "click;West Kirby", "click;DIRECTIONS"};
    private static final String[][] all_tests = {directions, drinks, change_of_heart};



    /*************************************************
                       USER INPUT:
     PACKAGE - package to be used
     pre_CUJ - preparatory actions (unrecorded)
     post_CUJ - recorded actions
     *************************************************/
    private static final String PACKAGE = MAPS_PACKAGE;
    private static String[] pre_CUJ = {};
    private static String[] post_CUJ = {"start", "edit;Search here;Cold Bay"};





    private static String[] CUJ = null;
    private static String[][] cached_tokens = null;
    private static UiObject[] cached_objects = null;





    /*************************************************
                     Helper Functions:
     *************************************************/


    private UiObject2 lowestClickableAncestor(UiObject2 object) throws invalidInputException {
        if (object == null) {
            throw new invalidInputException("passed object is null. Consider increasing TIMEOUT parameter");
        }
        while (!object.isClickable()) {
            object = object.getParent();
        }
        return object;
    }
    private UiObject castToObject(UiObject2 object) {
        String resourceName = object.getResourceName();
        String className = object.getClassName();
        String text = object.getText();
        String contentDescription = object.getContentDescription();

        //String contentDescription = object.getContentDescription();

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


        return device.findObject(selector);
    }

    private UiObject getEditableObject(UiObject2 object) throws invalidInputException {
        return castToObject(lowestClickableAncestor(object));
    }

    private void clearRecentApps() throws RemoteException, UiObjectNotFoundException, InterruptedException {
        //Clear recent Apps
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressRecentApps();


        /* CLEAR LAST APP ON PIXEL:*/
        UiObject maps_button = device.findObject(new UiSelector().className("android.view.View").resourceId("com.google.android.apps.nexuslauncher:id/snapshot").instance(0));
        if (maps_button.waitForExists(TIMEOUT)) {
            maps_button.swipeUp(400);
        } else {
            device.pressHome();
        }


        /*CLEAR LAST APP ON S10
        UiObject maps_button = device.findObject(new UiSelector().text("Close all"));
        if (maps_button.waitForExists(TIMEOUT)) {
            maps_button.click();
        } else {
            device.pressHome();
        }*/

    }
    

    private long launchApp(String Package) throws RemoteException, UiObjectNotFoundException, InterruptedException {


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

        //sleep(1000);
        long start = SystemClock.elapsedRealtime();

        context.startActivity(intent);

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(Package).depth(0)), TIMEOUT);

        return start;
    }
    public void start_recording () throws Exception {
        /*Using 3rd party software:
        launchApp("us.rec.screen");
        device.findObject(new UiSelector().className("android.widget.ImageButton").resourceId("us.rec.screen:id/fab_record")).click();
        device.findObject(new UiSelector().className("android.widget.Button").resourceId("us.rec.screen:id/simple_dialog_button_ok")).click();
        device.findObject(new UiSelector().className("android.widget.Button").resourceId("android:id/button1")).click();
        */
        try{

            Process su = Runtime.getRuntime().exec("su");

            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("screenrecord --time-limit 10 /sdcard/MyVideo.mp4\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
        }catch(IOException e){
            throw new Exception(e);
        }catch(InterruptedException e){
            throw new Exception(e);
        }

    }
    public void stop_recording () throws Exception {
        /*Using 3rd party software:
        launchApp("us.rec.screen");
        device.findObject(new UiSelector().className("android.widget.ImageButton").resourceId("us.rec.screen:id/fab_record")).click();*/
    }

    public long execute_cached_action(UiObject object, String[] tokens) throws UiObjectNotFoundException {
        object.waitForExists(TIMEOUT);
        long start = SystemClock.elapsedRealtime();
        if (tokens[0].equals("edit")) {
            object.legacySetText(tokens[2]);
        } else {
            object.click();
        }
        return start;
    }

    public long sumArr(long[] arr) {
        long total = 0;
        for (long t : arr) total += t;
        return total;
    }
    public void execute_uncached_action(int i, String[] tokens) throws invalidInputException, UiObjectNotFoundException {
        boolean strict = (tokens[tokens.length - 1].equals("strict"));
        switch (tokens[0]) {
            case "click":
                String a_displayed = tokens[1];
                UiObject2 a_object2 = null;
                if (strict) {
                    a_object2 = device.wait(Until.findObject(By.text(a_displayed)), TIMEOUT);
                } else {
                    a_object2 = device.wait(Until.findObject(By.textContains(a_displayed)), TIMEOUT);
                }
                ///lowestClickableAncestor(a_object2).click();
                UiObject a_object = castToObject(a_object2);
                cached_objects[i] = a_object;
                a_object.click();
                break;

            case "clickImage":
                String b_description = tokens[1];
                UiObject2 b_object2 = null;
                if (strict) {
                    b_object2 = device.wait(Until.findObject(By.desc(b_description)), TIMEOUT);
                } else {
                    b_object2 = device.wait(Until.findObject(By.descContains(b_description)), TIMEOUT);
                }
                UiObject b_object = castToObject(b_object2);
                cached_objects[i] = b_object;
                b_object.click();
                break;


            case "edit":
                String c_displayed = tokens[1];
                String c_entered = tokens[2];
                UiObject2 c_object2 = null;
                if (strict) {
                    c_object2 = device.wait(Until.findObject(By.text(c_displayed)), TIMEOUT);
                } else {
                    c_object2 = device.wait(Until.findObject(By.textContains(c_displayed)), TIMEOUT);
                }
                UiObject c_object = getEditableObject(c_object2);
                cached_objects[i] = c_object;
                c_object.waitForExists(TIMEOUT);
                c_object.legacySetText(c_entered);
                break;


            default:
                throw new invalidInputException("input at index " + i + " has an invalid first token");
        }
    }

    public long round_time_up(long t) {
        return ((1000 + t) / 1000) * 1000;
    }

    /*************************************************
                     MASTER TEST
     *************************************************/

    /*Caching should store:
        whether a clickable is a textview or button?
        reference to a UIObject?
     */
    //@Test
    public void validate_change() throws Exception {
        for (String[] test : all_tests) {
            pre_CUJ = null;
            post_CUJ = test;
            //CUJ_test();
        }
    }

    @Test
    public void cache_CUJ_test() throws Exception {

        /******************************
         * PERFORM CACHING RUN :
         ******************************/
        long start = SystemClock.elapsedRealtime();
        long pre_offset = 0;
        CUJ = new String[pre_CUJ.length + post_CUJ.length];
        if (pre_CUJ != null) {
            System.arraycopy(pre_CUJ, 0, CUJ, 0, pre_CUJ.length);
        }
        System.arraycopy(post_CUJ, 0, CUJ, pre_CUJ.length, post_CUJ.length);
        //PACKAGE = CUJ[0];
        clearRecentApps();


        cached_tokens = new String[CUJ.length][];
        cached_objects = new UiObject[CUJ.length];
        for (int i = 0; i < CUJ.length; i++) {
            if(i == pre_CUJ.length) {
                pre_offset = round_time_up(SystemClock.elapsedRealtime() - start);
            }
            if (i == 0) {
                launchApp(PACKAGE);
            } else {
                String action = CUJ[i];
                String[] tokens = action.split(";");
                cached_tokens[i] = tokens;
                if (!(2 <= tokens.length && tokens.length <= 4)) {
                    throw new invalidInputException("input at index " + i + " is of an invalid length");
                }
                execute_uncached_action(i, tokens);
            }
        }
        sleep(5000); //leeway for loading

        long checkpoint = SystemClock.elapsedRealtime();
        long interval = round_time_up(10000 + checkpoint - start); //allot each subsequent run time_to_complete_caching_run + 10 seconds

        /*******************************
         * PERFORM RUN USING CACHED DATA :
         *******************************/
        //recorded runs:
        int iter = -1;
        long[][] all_stamps = new long[NUM_ITERATIONS][];
        while (++iter < NUM_ITERATIONS) {

            clearRecentApps();

            /******************************
             * EXECUTE PREPARATORY ACTIONS :
             ******************************/

            for (int i = 0; i < pre_CUJ.length; i++) {
                if (i == 0) {
                    launchApp(PACKAGE);
                } else {
                    String[] tokens = cached_tokens[i];
                    UiObject object = cached_objects[i];
                    execute_cached_action(object, tokens);
                }
            }

            /******************************
             * EXECUTE RECORDED ACTIONS :
             ******************************/

            long[] stamps = new long[post_CUJ.length - 1];
            long stamp_start_time = SystemClock.elapsedRealtime();



            //start_recording();
            long waitFor = (iter + 1) * interval + pre_offset + start; //when this run should begin

            while (SystemClock.elapsedRealtime() < waitFor) {}
            for (int i = pre_CUJ.length; i < CUJ.length; i++) {
                if (i == 0) {
                    stamp_start_time = launchApp(PACKAGE);
                    stamps[i - pre_CUJ.length] = SystemClock.elapsedRealtime() - stamp_start_time;
                } else {
                    String[] tokens = cached_tokens[i];
                    UiObject object = cached_objects[i];
                    long available_time = execute_cached_action(object, tokens);
                    stamps[i - 1 - pre_CUJ.length] = available_time - stamp_start_time;
                    stamp_start_time = available_time;
                }
            }
            sleep(5000); //leeway for loading
            //stop_recording();




            Log.i("iterations", "ITERATION " + iter + ": " + Arrays.toString(stamps) + ", TOTAL: " + sumArr(stamps));
            all_stamps[iter] = stamps;
        }
        /******************************
         * COMPUTE AND REPORT METRICS:
         ******************************/
        long[] averages = new long[post_CUJ.length - 1];
        long[][] copy = new long[all_stamps.length][all_stamps[0].length];
         for (int i = 0; i < averages.length; i++) {
             long sum = 0;
             for (iter = 0; iter < NUM_ITERATIONS; iter++) {
                 sum += all_stamps[iter][i];
                 copy[iter][i] = all_stamps[iter][i];
             }
             averages[i] = sum / NUM_ITERATIONS;
         }
        long total = 0;
        for (long t : averages) total += t;



        Arrays.sort(copy, (a,b) -> Long.compare(sumArr(a), sumArr(b)));
        long[] median = copy[copy.length / 2];
        int median_idx = 0;
        for (int i = 0; i < all_stamps.length; i++) {
            long[] run = all_stamps[i];
            if (Arrays.equals(median, run)) median_idx = i;
        }

        long clip_start = (median_idx + 1) * interval + pre_offset;
        long clip_end =  (median_idx + 2) * interval + pre_offset;;

        Log.i("averages", "AVERAGES: " + Arrays.toString(averages) + ", TOTAL: " + total);

        Log.i("median", "MEDIAN RUN: " + median_idx);
        Log.i("median", "MEDIAN RUN STARTS @: " + clip_start);
        Log.i("median", "MEDIAN RUN ENDS @: " + clip_end);



    }



    /*************************************************
                           TESTS
     *************************************************/


    //@Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.firstapp", appContext.getPackageName());
    }


    //getClickableObject Unit Test -- Going up one Level

    //@Test
    public void UNIT_TEST_1_getClickableObject() throws RemoteException, UiObjectNotFoundException, invalidInputException, InterruptedException {
        launchApp(MAPS_PACKAGE);


        UiObject2 search_text_view= device.wait(Until.findObject(By.clazz("android.widget.TextView").textContains("Try gas")),TIMEOUT);
        UiObject clickable = getEditableObject(search_text_view);

        clickable.waitForExists(TIMEOUT);
        clickable.legacySetText(SEARCH);

        UiObject final_search_box = device.findObject(new UiSelector().className("android.widget.TextView").text(SEARCH));
        final_search_box.waitForExists(TIMEOUT);
        assertNotEquals(null, final_search_box);


    }

    //getClickableObject Unit Test -- Going up several levels, but works when not going up levels????????

    //@Test
    public void UNIT_TEST_2_getClickableObject() throws RemoteException, UiObjectNotFoundException, invalidInputException, InterruptedException {
        launchApp(MAPS_PACKAGE);

        sleep(2000);
        UiObject2 cheap_drinks = device.wait(Until.findObject(By.clazz("android.widget.TextView").textContains("Cheap drinks")),TIMEOUT);

        //assertEquals(cheap_drinks.getParent().getParent(), lowestClickableAncestor(cheap_drinks));
        /*UiObject clickable = getClickableObject(search_text_view);

        System.out.println();

        clickable.waitForExists(TIMEOUT);
        clickable.click();*/
        cheap_drinks.click();
/*
        UiObject final_search_box = device.findObject(new UiSelector().className("android.widget.TextView").text(SEARCH));
        final_search_box.waitForExists(TIMEOUT);
        assertNotEquals(null, final_search_box);
*/

    }



    //Testing
    //@Test
    public void getDirectionsObject2() throws InterruptedException, UiObjectNotFoundException, RemoteException, IOException, invalidInputException {


        launchApp(MAPS_PACKAGE);

        //UiObject2 search_edit_text = device.wait(Until.findObjects(By.clazz("android.widget.EditText")),TIMEOUT).get(0);
        //System.out.println(search_edit_text.size());
        //sleep(1000);
        UiObject2 search_edit_text = device.wait(Until.findObject(By.clazz("android.widget.TextView").textContains("Search here")),TIMEOUT);
        UiObject editable_seach_edit_text = getEditableObject(search_edit_text);
        editable_seach_edit_text.waitForExists(TIMEOUT);
        editable_seach_edit_text.legacySetText(SEARCH);

        //grab the RecyclerView with the possible locations and select the first one
        UiObject2 first_desitnation = device.wait(Until.findObject(By.clazz("android.widget.TextView").textContains("Amphitheatre")), TIMEOUT);
        lowestClickableAncestor(first_desitnation).click();


        UiObject2 directions_button = device.wait(Until.findObject(By.clazz("android.widget.Button").textContains("Directions")), TIMEOUT);
        lowestClickableAncestor(directions_button).click();


        UiObject2 choose_starting_point_box = device.wait(Until.findObject(By.clazz("android.widget.TextView").textContains("Choose starting")),TIMEOUT);
        UiObject editable_start_box = getEditableObject(choose_starting_point_box);
        editable_start_box.waitForExists(TIMEOUT);
        editable_start_box.click();

        UiObject2 your_location_button = device.wait(Until.findObject(By.clazz("android.widget.TextView").textContains("Your location")), TIMEOUT);
        lowestClickableAncestor(your_location_button).click();

        UiObject2 start_button = device.wait(Until.findObject(By.clazz("android.widget.Button").textContains("Start")), TIMEOUT*10);
        //System.out.print("AAAAAAAAAAAAAAAAA" + start_button.isClickable());

        lowestClickableAncestor(start_button).click();






    }

    /*
    //Version with UiObjects, uses format of CUJ input.
    @Test
    public void getDirectionsObject() throws InterruptedException, UiObjectNotFoundException, RemoteException, IOException {


        launchApp(MAPS_PACKAGE);


        UiObject search_edit_text = device.findObject(new UiSelector().className("android.widget.EditText").index(0));
        search_edit_text.waitForExists(TIMEOUT);
        search_edit_text.legacySetText(SEARCH);

        //grab the RecyclerView with the possible locations and select the first one
        UiObject first_desitnation = device.findObject(new UiSelector().className("android.widget.TextView").textContains("Amphitheatre"));
        first_desitnation.waitForExists(TIMEOUT);
        first_desitnation.click();


        UiObject directions_button = device.findObject(new UiSelector().className("android.widget.Button").textContains("Directions"));
        directions_button.waitForExists(TIMEOUT);
        directions_button.click();

        UiObject choose_starting_point_box = device.findObject(new UiSelector().className("android.widget.EditText").index(0));
        choose_starting_point_box.waitForExists(TIMEOUT);
        choose_starting_point_box.click();

        UiObject your_location_button = device.findObject(new UiSelector().className("android.widget.TextView").textContains("Your location"));
        your_location_button.waitForExists(TIMEOUT);
        your_location_button.click();

        UiObject start_button = device.findObject(new UiSelector().className("android.widget.Button").textContains("Start"));
        start_button.waitForExists(TIMEOUT);
        start_button.click();


        launchApp(MAPS_PACKAGE);


        //UiObject search_edit_text = device.findObject(new UiSelector().className("android.widget.EditText").index(0));
        search_edit_text.waitForExists(TIMEOUT);
        search_edit_text.legacySetText(SEARCH);

        //grab the RecyclerView with the possible locations and select the first one
        //UiObject first_desitnation = device.findObject(new UiSelector().className("android.widget.TextView").textContains("Amphitheatre"));
        first_desitnation.waitForExists(TIMEOUT);
        first_desitnation.click();

        //UiObject directions_button = device.findObject(new UiSelector().className("android.widget.Button").textContains("Directions"));
        directions_button.waitForExists(TIMEOUT);
        directions_button.click();

        //choose_starting_point_box = device.findObject(new UiSelector().className("android.widget.EditText").index(0));
        choose_starting_point_box.waitForExists(TIMEOUT);
        choose_starting_point_box.click();

        //UiObject your_location_button = device.findObject(new UiSelector().className("android.widget.TextView").textContains("Your location"));
        your_location_button.waitForExists(TIMEOUT);
        your_location_button.click();

        //UiObject start_button = device.findObject(new UiSelector().className("android.widget.Button").textContains("Start"));
        start_button.waitForExists(TIMEOUT);
        start_button.click();

}


     */



    //ORIGINAL VERSION:
    //@Test
    public void getDirections() throws InterruptedException, UiObjectNotFoundException, RemoteException {
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

/*

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


    }*/
}

