package unioeste.br.openvrt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPolygon;
import org.json.JSONException;
import org.json.JSONObject;
import unioeste.br.openvrt.exception.InvalidOpenVRTGeoJsonException;
import unioeste.br.openvrt.file.PrescriptionMapReader;
import unioeste.br.openvrt.file.ProtocolDictionary;
import unioeste.br.openvrt.map.FeatureStyler;
import unioeste.br.openvrt.map.LayerValidator;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap googleMap;

    private GeoJsonLayer mapLayer;

    private String mapLocation;

    private LocationManager locationManager;

    private FeatureStyler featureStyler;

    private static final long MIN_TIME = 400;

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 2;

    private static final float MIN_DISTANCE = 1000;

    private float currentRate = 0;

    private TextView rateIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mapLocation = intent.getStringExtra("map");
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        rateIndicator = findViewById(R.id.rate_indicator);
        mapFragment.getMapAsync(this);
    }

    private void askPermissionsToUseGpsOrCenterMap() {
        if (hasPermissionToUseGps()) {
            useUserLocation();
        } else {
            askPermissionToUseGps();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    useUserLocation();
                }
                break;
        }
    }

    @NonNull
    private Boolean hasPermissionToUseGps() {
        return ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void askPermissionToUseGps() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        }, PERMISSION_ACCESS_FINE_LOCATION);
    }

    private void parseMapToJson(String mapStr) {
        try {
            JSONObject mapContent = new JSONObject(mapStr);
            addMapLayerToMap(mapContent);
        } catch (JSONException | InvalidOpenVRTGeoJsonException e) {
            // TODO: Show error on cannot parse as JSON
            e.printStackTrace();
        }
    }

    private void openFile() {
        PrescriptionMapReader mapReader = new PrescriptionMapReader(mapLocation);
        mapReader.setOnFileReadListener(this::parseMapToJson);
        mapReader.setOnIOExceptionListener(Throwable::printStackTrace); // TODO: Show error on cannot open file
        Thread mapReaderThread = new Thread(mapReader);
        mapReaderThread.start();
    }

    @SuppressLint("MissingPermission")
    private void useUserLocation() {
        runOnUiThread(() -> {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Objects.requireNonNull(locationManager).requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        });
    }

    private void addMapLayerToMap(JSONObject geoJson) throws InvalidOpenVRTGeoJsonException {
        mapLayer = new GeoJsonLayer(googleMap, geoJson);
        LayerValidator.validate(mapLayer);
        featureStyler = FeatureStyler.newInstance(mapLayer);
        applyMapsStyles();
        runOnUiThread(() -> mapLayer.addLayerToMap());
        askPermissionsToUseGpsOrCenterMap();
    }

    private void applyMapsStyles() {
        for (GeoJsonFeature feature : mapLayer.getFeatures()) {
            featureStyler.apply(feature);
        }
    }

    private boolean featureContainsLocation(LatLng point, @NonNull GeoJsonFeature feature) {
        if (feature.getBoundingBox() != null) {
            if (!feature.getBoundingBox().contains(point)) {
                return false;
            }
        }

        return PolyUtil.containsLocation(point, ((GeoJsonPolygon) feature.getGeometry()).getOuterBoundaryCoordinates(), false);
    }

    private void calculateNextApplicationRate(Location location) {
        for (GeoJsonFeature feature : mapLayer.getFeatures()) {
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            if (feature.getGeometry() instanceof GeoJsonPolygon) {
                if (featureContainsLocation(point, feature)) {
                    currentRate = Float.valueOf(feature.getProperty(ProtocolDictionary.RATE_KEY));
                    runOnUiThread(() -> rateIndicator.setText(String.valueOf(currentRate)));
                    break;
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        openFile();
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        this.googleMap.animateCamera(cameraUpdate);
        calculateNextApplicationRate(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //
    }

    @Override
    public void onProviderEnabled(String provider) {
        //
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO: Show message?
    }
}
