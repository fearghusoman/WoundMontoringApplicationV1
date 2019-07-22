package com.example.woundmontoringapplicationv1;

import com.google.gson.annotations.SerializedName;

/**
 * The DressingItem class defines the item to be used in the Registered Dressing Activites
 * recycler view.
 */
public class DressingItem {

    @SerializedName("QRID")
    private String QRID;

    @SerializedName("QRInfo")
    private String QRInfo;

    @SerializedName("Location")
    private String location;

    /**
     *
     * @param QRID
     */
    public DressingItem(String QRID){
        this.QRID = QRID;
    }

    /**
     *
     * @param QRID
     * @param QRInfo
     * @param location
     */
    public DressingItem(String QRID, String QRInfo, String location){
        this.QRID = QRID;
        this.QRInfo = QRInfo;
        this.location = location;
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
}
