package com.example.firstapp;
import android.os.SystemClock;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import org.junit.Test;

public class CUJExecutor {

  

    /**
     * Executes a CUJ with preparatory actions pre and measured actions post. Caches information for later use.
     */

    public static void executeCUJ(String[] preCUJ, String[] postCUJ) throws Exception {
        String[] CUJ = new String[preCUJ.length + postCUJ.length];
        if (preCUJ != null) {
            System.arraycopy(preCUJ, 0, CUJ, 0, preCUJ.length);
        }
        System.arraycopy(postCUJ, 0, CUJ, preCUJ.length, postCUJ.length);
        String pkg = CUJ[0];
        ShellUtility.forceQuitApp(pkg);

        String[][] cached_tokens = new String[CUJ.length][];
        UiObject[] cached_objects = new UiObject[CUJ.length];
        for (int i = 0; i < CUJ.length; i++) {
            if (i == 0) {
                ShellUtility.launchApp(pkg);
            } else {
                String action = CUJ[i];
                String[] tokens = action.split(";");
                cached_tokens[i] = tokens;
                if (!(2 <= tokens.length && tokens.length <= 4)) {
                    throw new ShellUtility.invalidInputException("input at index " + i + " is of an invalid length");
                }
                cached_objects[i] = ShellUtility.executeUncachedAction(tokens);
            }
        }
    }
}
