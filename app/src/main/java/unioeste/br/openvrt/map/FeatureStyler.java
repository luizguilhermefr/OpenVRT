package unioeste.br.openvrt.map;

import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;

import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;

import unioeste.br.openvrt.file.ProtocolDictionary;

public class FeatureStyler {

    private float minRate;

    private float maxRate;

    private FeatureStyler(float minRate, float maxRate) {
        this.minRate = minRate;
        this.maxRate = maxRate;
    }

    @NonNull
    public static FeatureStyler newInstance(GeoJsonLayer layer) {
        float max = Float.NEGATIVE_INFINITY;
        float min = Float.POSITIVE_INFINITY;
        for (GeoJsonFeature feature : layer.getFeatures()) {
            String rateStr = feature.getProperty(ProtocolDictionary.RATE_KEY);
            float rate = Float.valueOf(rateStr);
            if (rate > max) {
                max = rate;
            }
            if (rate < min) {
                min = rate;
            }
        }
        return new FeatureStyler(min, max);
    }

    private float rateAsPercentage(float rate) {
        return (rate - minRate) / (maxRate + minRate);
    }

    public void apply(GeoJsonFeature feature) {
        String rateStr = feature.getProperty(ProtocolDictionary.RATE_KEY);
        float rate = Float.valueOf(rateStr);
        float percentage = this.rateAsPercentage(rate);
        float hue = (1 - percentage) * 120;
        float[] hsl = new float[]{hue, 50, 50};
        int color = ColorUtils.HSLToColor(hsl);
        GeoJsonPolygonStyle style = new GeoJsonPolygonStyle();
        style.setStrokeColor(color);
        style.setFillColor(color);
        feature.setPolygonStyle(style);
    }
}
