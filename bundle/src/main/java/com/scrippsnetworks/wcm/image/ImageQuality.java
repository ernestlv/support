package com.scrippsnetworks.wcm.image;

public enum ImageQuality {
    web(.80);

    private double quality;

    ImageQuality(double q) {
        this.quality = q;
    }

    public double getQualityFactor() {
        return quality;
    }
}
