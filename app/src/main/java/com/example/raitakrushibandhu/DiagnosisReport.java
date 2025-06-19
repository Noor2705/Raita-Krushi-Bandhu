package com.example.raitakrushibandhu;

public class DiagnosisReport {
    private String imageUrl;
    private String diseaseName;
    private String confidence;
    private String remedy;
    private long timestamp;

    public DiagnosisReport() {
        // Required empty constructor for Firestore
    }

    public DiagnosisReport(String imageUrl, String diseaseName, String confidence, String remedy, long timestamp) {
        this.imageUrl = imageUrl;
        this.diseaseName = diseaseName;
        this.confidence = confidence;
        this.remedy = remedy;
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getRemedy() {
        return remedy;
    }

    public void setRemedy(String remedy) {
        this.remedy = remedy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
