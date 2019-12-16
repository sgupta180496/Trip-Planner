package com.example.parta;

import android.graphics.Bitmap;

public class Photo {
    Bitmap photo;
    String path;
    String downloadURL;

    public Photo(Bitmap photo, String path, String downloadURL) {
        this.photo = photo;
        this.path = path;
        this.downloadURL = downloadURL;

    }
}
