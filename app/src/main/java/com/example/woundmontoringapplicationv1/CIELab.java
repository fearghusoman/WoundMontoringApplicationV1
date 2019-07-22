package com.example.woundmontoringapplicationv1;

import androidx.core.graphics.ColorUtils;
import android.util.Log;

/**
 * Class created by user of stackoverflow - performs a number of useful CIELAB conversions
 * and delta E calculations
 * Credit to: https://stackoverflow.com/questions/49114582/rgb-to-xyz-and-thenxyz-to-lab-in-androidjava
 * answer by: Muhammad Yasir
 */
public class CIELab {

    /**
     *
     * @param R
     * @param G
     * @param B
     * @return
     */
    public double[] rgbToLab(int R, int G, int B) {

        double[] lab=new double[3];

        double r, g, b, X, Y, Z, xr, yr, zr;

        ColorUtils.RGBToLAB(R,G,B,lab);

        //Core.absdiff();

        // D65/2°
        double Xr = 95.047;
        double Yr = 100.0;
        double Zr = 108.883;


        // --------- RGB to XYZ ---------//

        r = R/255.0;
        g = G/255.0;
        b = B/255.0;

        if (r > 0.04045)
            r = Math.pow((r+0.055)/1.055,2.4);
        else
            r = r/12.92;

        if (g > 0.04045)
            g = Math.pow((g+0.055)/1.055,2.4);
        else
            g = g/12.92;

        if (b > 0.04045)
            b = Math.pow((b+0.055)/1.055,2.4);
        else
            b = b/12.92 ;

        r*=100;
        g*=100;
        b*=100;
        Log.d("FEARG","R:"+r+" G:"+g+" B:"+b);
        X =  0.4124*r + 0.3576*g + 0.1805*b;
        Y =  0.2126*r + 0.7152*g + 0.0722*b;
        Z =  0.0193*r + 0.1192*g + 0.9505*b;


        // --------- XYZ to Lab --------- //

        xr = X/Xr;
        yr = Y/Yr;
        zr = Z/Zr;

        if ( xr > 0.008856 )
            xr =  (float) Math.pow(xr, 1/3.);
        else
            xr = (float) ((7.787 * xr) + 16 / 116.0);

        if ( yr > 0.008856 )
            yr =  (float) Math.pow(yr, 1/3.);
        else
            yr = (float) ((7.787 * yr) + 16 / 116.0);

        if ( zr > 0.008856 )
            zr =  (float) Math.pow(zr, 1/3.);
        else
            zr = (float) ((7.787 * zr) + 16 / 116.0);


        //new double array to hold the converted values
        double[] labNew = new double[3];

        labNew[0] = (116 * yr) - 16;
        labNew[1] = 500 * (xr - yr);
        labNew[2] = 200 * (yr - zr);

        return lab;

    }
}
