package unioeste.br.openvrt.map;

import android.support.annotation.NonNull;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import unioeste.br.openvrt.exception.InvalidOpenVRTGeoJsonException;
import unioeste.br.openvrt.file.ProtocolDictionary;

public class LayerValidator {

    public static void validate(@NonNull GeoJsonLayer layer) throws InvalidOpenVRTGeoJsonException {
        for (GeoJsonFeature feature : layer.getFeatures()) {
            if (!isValidFeature(feature)) {
                throw new InvalidOpenVRTGeoJsonException();
            }
        }
    }

    private static boolean isValidFeature(@NonNull GeoJsonFeature feature) {
        return feature.hasProperty(ProtocolDictionary.RATE_KEY);
    }
}
