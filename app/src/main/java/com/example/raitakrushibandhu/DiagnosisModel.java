package com.example.raitakrushibandhu;

public class DiagnosisModel {
    private String imageUrl;
    private String diseaseName;
    private double confidence;
    private String remedy;

    public DiagnosisModel() {}

    public DiagnosisModel(String imageUrl, String diseaseName, double confidence, String remedy) {
        this.imageUrl = imageUrl;
        this.diseaseName = diseaseName;
        this.confidence = confidence;
        this.remedy = remedy;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getRemedy() {
        return remedy;
    }
}
