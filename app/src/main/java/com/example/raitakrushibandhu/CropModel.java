package com.example.raitakrushibandhu;

public class CropModel {
    private String cropName;
    private int imageResId;

    public CropModel(String cropName, int imageResId) {
        this.cropName = cropName;
        this.imageResId = imageResId;
    }

    public String getCropName() {
        return cropName;
    }

    public int getImageResId() {
        return imageResId;
    }
}
