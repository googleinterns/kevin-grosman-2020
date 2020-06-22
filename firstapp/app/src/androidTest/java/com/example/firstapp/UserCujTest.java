package com.example.firstapp;

import org.junit.Test;

public class UserCujTest {
    /*************************************************
     USER INPUT:
     PACKAGE - package to be used
     pre_CUJ - preparatory actions (unrecorded)
     post_CUJ - recorded actions
     RECORDING_BUF:  how long to sleep in between prep and measured actions (can be 0 if no intent to record)
     *************************************************/
    private static String[] preCUJ = {"start;com.google.android.apps.maps"};
    private static String[] postCUJ = {"click;Takeout", "click;Open now"};
    private static int NUM_ITERATIONS = 1;
    private static long RECORDING_BUF = 1000;
    private static long TIMEOUT = 5000;

    @Test
    public void runCuj() throws Exception {
        ShellUtility shellUtility = new ShellUtility(TIMEOUT);
        shellUtility.executeCUJ(preCUJ, postCUJ, NUM_ITERATIONS, RECORDING_BUF);
    }
}
