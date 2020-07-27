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

import android.os.Bundle;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.logging.LogManager;

public class UserCujTest {

    private static long TIMEOUTMS = 30000;

    @Test
    public void userIterateAndMeasureCuj() throws Exception {
        Bundle extras = InstrumentationRegistry.getArguments();
        String preCUJ = extras.getString("pre");
        String measuredCUJ = extras.getString("measured");
        String postCUJ = extras.getString("post");
        int iterations = Integer.parseInt(extras.getString("iters"));
        String recStr = extras.getString("rec");
        boolean recordIntent = ("r".equals(recStr) || "f".equals(recStr)); //r -> we want median clip, f -> we want full recording only
        long recstart =  Long.parseLong(extras.getString("recstart"));

        ShellUtility shellUtility = new ShellUtility(TIMEOUTMS);
        shellUtility.iterateAndMeasureCuj(preCUJ, measuredCUJ, postCUJ, iterations, recordIntent, recstart);
    }

    @Test
    public void userWalkCujNTimes() throws Exception {
        ShellUtility shellUtility = new ShellUtility(TIMEOUTMS);

        Bundle extras = InstrumentationRegistry.getArguments();
        String preCUJ = extras.getString("pre");
        String measuredCUJ = extras.getString("measured");
        String postCUJ = extras.getString("post");
        String sectionFlag = extras.getString("include"); //Tells us which section of the CUJ to walk through
        int n = Integer.parseInt(extras.getString("n")); // number of iterations

       shellUtility.parseUserWalkCujNTimes(preCUJ, measuredCUJ, postCUJ, sectionFlag, n);
    }
}
