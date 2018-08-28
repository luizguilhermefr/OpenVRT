package unioeste.br.openvrt;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import unioeste.br.openvrt.connection.ConnectedThread;
import unioeste.br.openvrt.exception.InvalidOpenVRTGeoJsonException;
import unioeste.br.openvrt.file.PrescriptionMapReader;
import unioeste.br.openvrt.file.ProtocolDictionary;
import unioeste.br.openvrt.map.FeatureStyler;
import unioeste.br.openvrt.map.LayerValidator;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final long MIN_TIME = 500;

    private static final float MIN_DISTANCE = 1;

    private GoogleMap googleMap;

    private GeoJsonLayer mapLayer;

    private String mapLocation;

    private LocationManager locationManager;

    private FeatureStyler featureStyler;

    private float currentRate = 0;

    private float currentAccuracy = 0;

    private TextView rateIndicator;

    private TextView accuracyIndicator;

    private ConnectedThread connectedThread;

    private FloatingActionButton fab;

    private Snackbar snackbar;

    private int selectedMeasurement;

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
            Objects.requireNonNull(locationManager).requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        });
    }

    private void addMapLayerToMap(JSONObject geoJson) throws InvalidOpenVRTGeoJsonException {
        mapLayer = new GeoJsonLayer(googleMap, geoJson);
        LayerValidator.validate(mapLayer);
        featureStyler = FeatureStyler.newInstance(mapLayer);
        applyMapsStyles();
        runOnUiThread(() -> mapLayer.addLayerToMap());
        useUserLocation();
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

    private float calculateNextApplicationRate(LatLng point) {
        for (GeoJsonFeature feature : mapLayer.getFeatures()) {
            if (feature.getGeometry() instanceof GeoJsonPolygon) {
                if (featureContainsLocation(point, feature)) {
                    return Float.valueOf(feature.getProperty(ProtocolDictionary.RATE_KEY));
                }
            }
        }

        return 0;
    }

    private void onRateChanged(Float nextRate) {
        currentRate = nextRate;
        String rateString = getString(R.string.current_rate, currentRate);
        runOnUiThread(() -> rateIndicator.setText(rateString));
    }

    private void onAccuracyChanged(Float nextAccuracy) {
        currentAccuracy = nextAccuracy;
        String accuracyString = getString(R.string.current_precision, currentAccuracy);
        runOnUiThread(() -> accuracyIndicator.setText(accuracyString));
    }

    private void setApplying() {
        runOnUiThread(() -> {
            fab.setOnClickListener(v -> setNotApplying());
            int color = ContextCompat.getColor(getApplicationContext(), R.color.colorStop);
            fab.setBackgroundTintList(ColorStateList.valueOf(color));
            fab.setImageResource(R.drawable.close_circle);
        });
    }

    private void setNotApplying() {
        runOnUiThread(() -> {
            fab.setOnClickListener(v -> askMeasurement());
            int color = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
            fab.setBackgroundTintList(ColorStateList.valueOf(color));
            fab.setImageResource(R.drawable.send);
        });
    }

    private void askMeasurement() {
        AlertDialog.Builder measurementDialogBuilder = new AlertDialog.Builder(this);
        measurementDialogBuilder.setTitle(R.string.rate_measurement);
        measurementDialogBuilder.setSingleChoiceItems(R.array.measurements, 0, (dialogInterface, i) -> selectedMeasurement = i);
        measurementDialogBuilder.setPositiveButton(R.string.ok, (dialog, id) -> setApplying());
        measurementDialogBuilder.setNegativeButton(R.string.cancel, (dialog, id
        ) -> setNotApplying());
        measurementDialogBuilder.create().show();
    }

    private void createFloatingActionButton() {
        runOnUiThread(() -> {
            fab = findViewById(R.id.start_fab);
            fab.setOnClickListener(v -> askMeasurement());
            fab.bringToFront();
            fab.setVisibility(FloatingActionButton.VISIBLE);
        });
    }

    private void makeSnackbar() {
        snackbar = Snackbar.make(findViewById(R.id.map), "", Snackbar.LENGTH_LONG);
    }

    private void onRestoreLocationProvider() {
        runOnUiThread(() -> {
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            snackbar.setText(getString(R.string.location_estabilished));
            snackbar.setAction("", v -> {
                // No action
            });
            snackbar.show();
        });
    }

    private void onLostLocationProvider() {
        runOnUiThread(() -> {
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            snackbar.setText(getString(R.string.location_lost));
            snackbar.setAction("", v -> {
                // No action
            });
            snackbar.show();
        });
    }

    private void onLocationProviderFailure() {
        runOnUiThread(() -> {
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            snackbar.setText(getString(R.string.location_unavailable));
            snackbar.setAction(getString(R.string.ok), v -> {
                snackbar.dismiss();
            });
            snackbar.show();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mapLocation = intent.getStringExtra("map");
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        rateIndicator = findViewById(R.id.rate_indicator);
        accuracyIndicator = findViewById(R.id.accuracy_indicator);
        mapFragment.getMapAsync(this);
        createFloatingActionButton();
        makeSnackbar();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        openFile();
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, 16);
        this.googleMap.animateCamera(cameraUpdate);
        Float nextRate = calculateNextApplicationRate(point);
        Float nextAccuracy = location.getAccuracy();
        onRateChanged(nextRate);
        onAccuracyChanged(nextAccuracy);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                onRestoreLocationProvider();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                onLostLocationProvider();
                break;
            case LocationProvider.OUT_OF_SERVICE:
                onLocationProviderFailure();
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        onRestoreLocationProvider();
    }

    @Override
    public void onProviderDisabled(String provider) {
        onLostLocationProvider();
    }
}
