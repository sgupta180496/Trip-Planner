package com.example.parta;

import android.os.Parcel;
import android.os.Parcelable;
import android.print.PrinterId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Trip  implements Parcelable {

    private String imageURL;
    private String tripName;
    private String latitude;
    private String longitude;
    private Map<String,String> usersMap;
    private String ownerOfTrip;

    public Trip() {
    }

    public Trip(String imageURL, String tripName, String latitude, String longitude,
                Map<String, String> usersMap, String ownerOfTrip) {
        this.imageURL = imageURL;
        this.tripName = tripName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.usersMap = usersMap;
        this.ownerOfTrip = ownerOfTrip;
    }

    protected Trip(Parcel in) {
        imageURL = in.readString();
        tripName = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        ownerOfTrip = in.readString();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imageURL);
        parcel.writeString(tripName);
        parcel.writeString(latitude);
        parcel.writeString(longitude);
        parcel.writeString(ownerOfTrip);
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Map<String, String> getUsersMap() {
        return usersMap;
    }

    public void setUsersMap(Map<String, String> usersMap) {
        this.usersMap = usersMap;
    }

    public String getOwnerOfTrip() {
        return ownerOfTrip;
    }

    public void setOwnerOfTrip(String ownerOfTrip) {
        this.ownerOfTrip = ownerOfTrip;
    }
}
