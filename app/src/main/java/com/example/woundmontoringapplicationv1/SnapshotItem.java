package com.example.woundmontoringapplicationv1;

import com.google.gson.annotations.SerializedName;

/**
 * Class that defines the object to be displayed in the HomeFragment's recycler view
 * that shows the user the details of their most recent analyses
 */
public class SnapshotItem implements Comparable<SnapshotItem>{

    @SerializedName("QRInfo")
    private String QRInfo;

    @SerializedName("Timestamp")
    private String timestamp;

    @SerializedName("DeltaEC1")
    private String deltaEC1;

    @SerializedName("DeltaEC2")
    private String deltaEC2;

    @SerializedName("DeltaEC3")
    private String deltaEC3;

    @SerializedName("DeltaEC4")
    private String deltaEC4;

    @SerializedName("Warning")
    private String warning;

    /**
     *
     * @param QRInfo
     * @param timestamp
     */
    public SnapshotItem(String QRInfo, String timestamp, String deltaEC1, String deltaEC2,
                            String deltaEC3, String deltaEC4, String warning){
        this.QRInfo = QRInfo;
        this.timestamp = timestamp;
        this.deltaEC1 = deltaEC1;
        this.deltaEC2 = deltaEC2;
        this.deltaEC3 = deltaEC3;
        this.deltaEC4 = deltaEC4;
        this.warning = warning;
    }

    public String getQRInfo(){
        return QRInfo;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public String getDeltaEC1() { return deltaEC1; }

    public String getDeltaEC2() { return deltaEC2; }

    public String getDeltaEC3() { return deltaEC3; }

    public String getDeltaEC4() { return deltaEC4; }

    public String getWarning() { return warning; }

    /**
     * Override the comparator method to order the arraylist by timestamp
     * @param snapshotItem
     * @return
     */
    @Override
    public int compareTo(SnapshotItem snapshotItem) {
        String ourTime = this.timestamp.toUpperCase();
        String compareTime = snapshotItem.getTimestamp().toUpperCase();

        return compareTime.compareTo(ourTime);    }
}
