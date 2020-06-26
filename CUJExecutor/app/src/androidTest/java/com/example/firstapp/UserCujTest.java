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

public class UserCujTest {

    private static long TIMEOUTMS = 10000;

    @Test
    public void userTestCuj() throws Exception {
        Bundle extras = InstrumentationRegistry.getArguments();
        String preCUJ = extras.getString("pre");
        String postCUJ = extras.getString("post");
        int iterations = Integer.parseInt(extras.getString("iters"));
        boolean recordIntent = "r".equals(extras.getString("rec"));

        ShellUtility shellUtility = new ShellUtility(TIMEOUTMS);
        shellUtility.testCuj(preCUJ, postCUJ, iterations, recordIntent);
    }

    @Test
    public void userSingleCujRun() throws Exception {
        Bundle extras = InstrumentationRegistry.getArguments();
        String preCUJ = extras.getString("pre");
        String postCUJ = extras.getString("post");
        String include = extras.getString("include");
        boolean includeMeasured = "y".equals(include);


        ShellUtility shellUtility = new ShellUtility(TIMEOUTMS);
        shellUtility.singleCujRun(preCUJ, postCUJ, includeMeasured);
    }
}
