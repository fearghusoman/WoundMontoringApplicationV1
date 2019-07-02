package com.example.woundmontoringapplicationv1;

/**
 * Class that represents a physical dressing
 */
public class Dressing {

    private double qrSideLength;
    private double qrToEdgeOfRectangleLength;
    private double qrOrientationCornerWidth;
    private double rectangleLength;

    private double edgeOfRectToCentreC1;
    private double centreC1ToCentreC2;
    private double centreC2ToCentreC3;
    private double centreC3ToCentreC4;

    private double radiusC1;
    private double radiusC2;
    private double radiusC3;
    private double radiusC4;

    /**
     * Constructor with all qr and rectangle arguments
     * @param qrSideLength
     * @param qrToEdgeOfRectangleLength
     * @param qrOrientationCornerWidth
     * @param rectangleLength
     */
    public Dressing(double qrSideLength, double qrToEdgeOfRectangleLength, double qrOrientationCornerWidth, double rectangleLength) {
        this.qrSideLength = qrSideLength;
        this.qrToEdgeOfRectangleLength = qrToEdgeOfRectangleLength;
        this.qrOrientationCornerWidth = qrOrientationCornerWidth;
        this.rectangleLength = rectangleLength;
    }

    /**
     * Constructor with all arguments
     * @param qrSideLength
     * @param qrToEdgeOfRectangleLength
     * @param qrOrientationCornerWidth
     * @param rectangleLength
     * @param edgeOfRectToCentreC1
     * @param centreC1ToCentreC2
     * @param centreC2ToCentreC3
     * @param centreC3ToCentreC4
     * @param radiusC1
     * @param radiusC2
     * @param radiusC3
     * @param radiusC4
     */
    public Dressing(double qrSideLength, double qrToEdgeOfRectangleLength, double qrOrientationCornerWidth, double rectangleLength, double edgeOfRectToCentreC1,
                        double centreC1ToCentreC2, double centreC2ToCentreC3, double centreC3ToCentreC4, double radiusC1, double radiusC2, double radiusC3, double radiusC4) {
        this.qrSideLength = qrSideLength;
        this.qrToEdgeOfRectangleLength = qrToEdgeOfRectangleLength;
        this.qrOrientationCornerWidth = qrOrientationCornerWidth;
        this.rectangleLength = rectangleLength;
        this.edgeOfRectToCentreC1 = edgeOfRectToCentreC1;
        this.centreC1ToCentreC2 = centreC1ToCentreC2;
        this.centreC2ToCentreC3 = centreC2ToCentreC3;
        this.centreC3ToCentreC4 = centreC3ToCentreC4;
        this.radiusC1 = radiusC1;
        this.radiusC2 = radiusC2;
        this.radiusC3 = radiusC3;
        this.radiusC4 = radiusC4;
    }

    public double getEdgeOfRectToCentreC1() {
        return edgeOfRectToCentreC1;
    }

    public void setEdgeOfRectToCentreC1(double edgeOfRectToCentreC1) {
        this.edgeOfRectToCentreC1 = edgeOfRectToCentreC1;
    }

    public double getCentreC1ToCentreC2() {
        return centreC1ToCentreC2;
    }

    public void setCentreC1ToCentreC2(double centreC1ToCentreC2) {
        this.centreC1ToCentreC2 = centreC1ToCentreC2;
    }

    public double getCentreC2ToCentreC3() {
        return centreC2ToCentreC3;
    }

    public void setCentreC2ToCentreC3(double centreC2ToCentreC3) {
        this.centreC2ToCentreC3 = centreC2ToCentreC3;
    }

    public double getCentreC3ToCentreC4() {
        return centreC3ToCentreC4;
    }

    public void setCentreC3ToCentreC4(double centreC3ToCentreC4) {
        this.centreC3ToCentreC4 = centreC3ToCentreC4;
    }

    public double getRadiusC1() {
        return radiusC1;
    }

    public void setRadiusC1(double radiusC1) {
        this.radiusC1 = radiusC1;
    }

    public double getRadiusC2() {
        return radiusC2;
    }

    public void setRadiusC2(double radiusC2) {
        this.radiusC2 = radiusC2;
    }

    public double getRadiusC3() {
        return radiusC3;
    }

    public void setRadiusC3(double radiusC3) {
        this.radiusC3 = radiusC3;
    }

    public double getRadiusC4() {
        return radiusC4;
    }

    public void setRadiusC4(double radiusC4) {
        this.radiusC4 = radiusC4;
    }

    public double getQrOrientationCornerWidth() {
        return qrOrientationCornerWidth;
    }

    public double getRectangleLength() {
        return rectangleLength;
    }

    public void setQrOrientationCornerWidth(double qrOrientationCornerWidth) {
        this.qrOrientationCornerWidth = qrOrientationCornerWidth;
    }

    public void setRectangleLength(double rectangleLength) {
        this.rectangleLength = rectangleLength;
    }

    public double getQrSideLength() {
        return qrSideLength;
    }

    public double getQrToEdgeOfRectangleLength() {
        return qrToEdgeOfRectangleLength;
    }

    public void setQrSideLength(double qrSideLength) {
        this.qrSideLength = qrSideLength;
    }

    public void setQrToEdgeOfRectangleLength(double qrToEdgeOfRectangleLength) {
        this.qrToEdgeOfRectangleLength = qrToEdgeOfRectangleLength;
    }
}
