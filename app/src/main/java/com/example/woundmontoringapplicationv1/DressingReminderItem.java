package com.example.woundmontoringapplicationv1;

import com.google.gson.annotations.SerializedName;

/**
 * The class that defines the object to be adapted to the RemindersRecyclerAdapter
 * and displayed in the Reminders recycler view.
 */
public class DressingReminderItem {

    @SerializedName("QRID")
    private String QRID;

    @SerializedName("QRInfo")
    private String QRInfo;

    @SerializedName("Location")
    private String location;

    @SerializedName("Timestamp")
    private String timestamp;

    @SerializedName("CurrentWarningLevel")
    private String currentWarningLevel;

    @SerializedName("AlarmNeedsUpdating")
    private int alarmNeedsUpdating;

    /**
     *
     * @param QRID
     */
    public DressingReminderItem(String QRID){
        this.QRID = QRID;
    }

    /**
     *
     * @param QRID
     * @param QRInfo
     * @param location
     */
    public DressingReminderItem(String QRID, String QRInfo, String location, String timestamp, String currentWarningLevel, int alarmNeedsUpdating){
        this.QRID = QRID;
        this.QRInfo = QRInfo;
        this.location = location;
        this.timestamp = timestamp;
        this.currentWarningLevel = currentWarningLevel;
        this.alarmNeedsUpdating = alarmNeedsUpdating;
    }

    public String getQRID(){
        return QRID;
    }

    public String getQRInfo(){
        return QRInfo;
    }

    public String getLocation(){
        return location;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public String getCurrentWarningLevel() {
        return currentWarningLevel;
    }

    public int getAlarmNeedsUpdating() {
        return alarmNeedsUpdating;
    }

}
