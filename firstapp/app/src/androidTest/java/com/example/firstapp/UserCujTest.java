package com.example.firstapp;

import android.os.Bundle;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class UserCujTest {

    private static long TIMEOUTMS = 5000;

    @Test
    public void runCuj() throws Exception {
        Bundle extras = InstrumentationRegistry.getArguments();
        String preCUJ = extras.getString("pre");
        String postCUJ = extras.getString("post");
        int iterations = Integer.parseInt(extras.getString("iters"));
        boolean recordIntent = "r".equals(extras.getString("rec"));

        ShellUtility shellUtility = new ShellUtility(TIMEOUTMS);
        shellUtility.executeCUJ(preCUJ, postCUJ, iterations, recordIntent);
    }
}
