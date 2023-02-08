package com.demo.app.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "location")
public class LocationObject implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int LID;
    private String pickup_lat;
    private String pickup_long;
    private String pickup_address;
    private String drop_lat;
    private String drop_long;
    private String drop_address;

    public LocationObject() {
    }

    public LocationObject(int LID, String pickup_lat, String pickup_long, String pickup_address, String drop_lat, String drop_long, String drop_address) {
        this.LID = LID;
        this.pickup_lat = pickup_lat;
        this.pickup_long = pickup_long;
        this.pickup_address = pickup_address;
        this.drop_lat = drop_lat;
        this.drop_long = drop_long;
        this.drop_address = drop_address;
    }

    protected LocationObject(Parcel in) {
        LID = in.readInt();
        pickup_lat = in.readString();
        pickup_long = in.readString();
        pickup_address = in.readString();
        drop_lat = in.readString();
        drop_long = in.readString();
        drop_address = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(LID);
        dest.writeString(pickup_lat);
        dest.writeString(pickup_long);
        dest.writeString(pickup_address);
        dest.writeString(drop_lat);
        dest.writeString(drop_long);
        dest.writeString(drop_address);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocationObject> CREATOR = new Creator<LocationObject>() {
        @Override
        public LocationObject createFromParcel(Parcel in) {
            return new LocationObject(in);
        }

        @Override
        public LocationObject[] newArray(int size) {
            return new LocationObject[size];
        }
    };

    public int getLID() {
        return LID;
    }

    public void setLID(int LID) {
        this.LID = LID;
    }

    public String getPickup_lat() {
        return pickup_lat;
    }

    public void setPickup_lat(String pickup_lat) {
        this.pickup_lat = pickup_lat;
    }

    public String getPickup_long() {
        return pickup_long;
    }

    public void setPickup_long(String pickup_long) {
        this.pickup_long = pickup_long;
    }

    public String getPickup_address() {
        return pickup_address;
    }

    public void setPickup_address(String pickup_address) {
        this.pickup_address = pickup_address;
    }

    public String getDrop_lat() {
        return drop_lat;
    }

    public void setDrop_lat(String drop_lat) {
        this.drop_lat = drop_lat;
    }

    public String getDrop_long() {
        return drop_long;
    }

    public void setDrop_long(String drop_long) {
        this.drop_long = drop_long;
    }

    public String getDrop_address() {
        return drop_address;
    }

    public void setDrop_address(String drop_address) {
        this.drop_address = drop_address;
    }
}
