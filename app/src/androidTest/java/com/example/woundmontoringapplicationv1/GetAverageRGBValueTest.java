package com.example.woundmontoringapplicationv1;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;

import junit.framework.TestCase;

import com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities.ProcessImageActivity;

import org.junit.Before;
import org.junit.Test;

public class GetAverageRGBValueTest extends TestCase {

    private Bitmap redBitmap;
    private int redColor, redBitmpHeight, redBitmapWidth, redBitmapCount;
    private int[] redRGBs = new int[3];

    private Point centreOfArbitraryCircle;

    @Before
    public void setUp() throws Exception{
        redColor = Color.RED;

        redBitmpHeight = 100;
        redBitmapWidth = 100;

        redBitmapCount = 10000;

        centreOfArbitraryCircle = new Point(50, 50);

        redBitmap = Bitmap.createBitmap(redBitmapWidth, redBitmpHeight, Bitmap.Config.ARGB_8888);

        for(int i = 0; i < redBitmapWidth; i++){
            for(int j = 0; j < redBitmpHeight; j++){
                redBitmap.setPixel(i, j, redColor);
            }
        }

        redRGBs[0] = Color.red(redColor);
        redRGBs[1] = Color.green(redColor);
        redRGBs[2] = Color.blue(redColor);
    }

    @Test
    public void testAverageRGBOfRedBitmapIsRed() {
        ProcessImageActivity processImageActivity = new ProcessImageActivity();

        int[] resultRGBs = processImageActivity.getAverageRGBValueFromBitmap(redBitmap, centreOfArbitraryCircle);

        assertEquals(redRGBs, resultRGBs);
    }

}
