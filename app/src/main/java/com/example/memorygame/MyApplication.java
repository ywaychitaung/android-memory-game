package com.example.memorygame;

import android.app.Application;
import android.graphics.Bitmap;

import java.util.List;

public class MyApplication extends Application {
    private List<Bitmap> selectedImages;

    public List<Bitmap> getSelectedImages() {
        return selectedImages;
    }

    public void setSelectedImages(List<Bitmap> selectedImages) {
        this.selectedImages = selectedImages;
    }
}

