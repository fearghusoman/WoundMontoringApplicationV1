package com.example.woundmontoringapplicationv1;

import com.google.gson.annotations.SerializedName;

/**
 *
 */
public class SnapshotItem {

    @SerializedName("QRID")
    private String QRInfo;

    @SerializedName("Timestamp")
    private String timestamp;

    /**
     *
     * @param QRInfo
     * @param timestamp
     */
    public SnapshotItem(String QRInfo, String timestamp){
        this.QRInfo = QRInfo;
        this.timestamp = timestamp;
    }

    public String getQRInfo(){
        return QRInfo;
    }

    public String getTimestamp(){
        return timestamp;
    }

}
