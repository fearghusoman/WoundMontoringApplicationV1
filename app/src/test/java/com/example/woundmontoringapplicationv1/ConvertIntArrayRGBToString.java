package com.example.woundmontoringapplicationv1;

import android.graphics.Color;

import com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities.ProcessImageActivity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
/**
 * this Test class will test the convertIntArrayTGBToString method from the ProcessImageActivity activity
 */
public class ConvertIntArrayRGBToString{

    //declare and initialise the int arrays to test
    int[] rgbArrayRed = {Color.red(Color.RED), Color.green(Color.RED), Color.blue(Color.RED)};
    int[] rgbArrayBlue = {Color.red(Color.BLUE), Color.green(Color.BLUE), Color.blue(Color.BLUE)};
    int[] rgbArrayGreen = {Color.red(Color.GREEN), Color.green(Color.GREEN), Color.blue(Color.GREEN)};
    int[] rgbArrayWhite = {Color.red(Color.WHITE), Color.green(Color.WHITE), Color.blue(Color.WHITE)};
    int[] rgbArrayBlack = {Color.red(Color.BLACK), Color.green(Color.BLACK), Color.blue(Color.BLACK)};

    String redString, blueString, greenString, whiteString, blackString;

    @Before
    public void setUp() throws Exception {
        redString = "{255, 0, 0}";
        blueString = "{0, 255, 0}";
        greenString = "{0, 0, 255}";
        whiteString = "{255, 255, 255}";
        blackString = "{0, 0, 0}";
    }

    @Test
    public void testConversionMethodForRed(){
        ProcessImageActivity processImageActivity = new ProcessImageActivity();

        String redStringResult = processImageActivity.convertIntArrayRGBToString(rgbArrayRed);

        assertEquals(redStringResult, redString);

    }

}
