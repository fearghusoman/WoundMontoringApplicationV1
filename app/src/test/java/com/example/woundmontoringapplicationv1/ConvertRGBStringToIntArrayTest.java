package com.example.woundmontoringapplicationv1;

import com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities.AnalysisSubmissionActivity;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class ConvertRGBStringToIntArrayTest extends TestCase {

    private String rgbTestString;
    private int[] expectedRGBIntArray = new int[3];

    @Before
    public void setUp() throws Exception {
        rgbTestString = "(255, 255, 255)";

        expectedRGBIntArray[0] = 255;
        expectedRGBIntArray[1] = 255;
        expectedRGBIntArray[2] = 255;
    }

    @Test
    public void testConvertRGBToIntArray(){
        AnalysisSubmissionActivity analysisSubmissionActivity = new AnalysisSubmissionActivity();

        int[] actuaRGBIntArray = analysisSubmissionActivity.convertStringRGBtoInts(rgbTestString);

        assertEquals(expectedRGBIntArray[0], actuaRGBIntArray[0]);
        assertEquals(expectedRGBIntArray[1], actuaRGBIntArray[1]);
        assertEquals(expectedRGBIntArray[2], actuaRGBIntArray[2]);
    }

}
