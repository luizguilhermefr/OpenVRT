package unioeste.br.openvrt;

import android.annotation.SuppressLint;
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
import unioeste.br.openvrt.connection.message.Message;
import unioeste.br.openvrt.connection.message.SetRateMessage;
import unioeste.br.openvrt.connection.message.dictionary.MessageResponse;
import unioeste.br.openvrt.exception.InvalidOpenVRTGeoJsonException;
import unioeste.br.openvrt.file.PrescriptionMapReader;
import unioeste.br.openvrt.file.ProtocolDictionary;
import unioeste.br.openvrt.map.FeatureStyler;
import unioeste.br.openvrt.map.LayerValidator;

import java.math.BigDecimal;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final long MIN_TIME = 500;

    private static final float MIN_DISTANCE = 1;

    private static final float ACCURACY_OK = 10;

    private static final float ACCURACY_WARN = 20;

    private GoogleMap googleMap;

    private GeoJsonLayer mapLayer;

    private String mapLocation;

    private LocationManager locationManager;

    private FeatureStyler featureStyler;

    private TextView rateIndicator;

    private TextView accuracyIndicator;

    private ConnectedThread connectedThread;

    private FloatingActionButton fab;

    private Snackbar snackbar;

    private boolean applying = false;

    private void parseMapToJson(String mapStr) {
        try {
            JSONObject geoJson = new JSONObject(mapStr);
            addMapLayerToMap(geoJson);
        } catch (JSONException | InvalidOpenVRTGeoJsonException e) {
            onFileParseError(e);
        }
    }

    private void openFile() {
        PrescriptionMapReader mapReader = new PrescriptionMapReader(mapLocation);
        mapReader.setOnFileReadListener(this::parseMapToJson);
        mapReader.setOnIOExceptionListener(this::onFileParseError);
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

    private int getAccuracyColor(float accuracy) {
        if (accuracy < ACCURACY_OK) {
            return ContextCompat.getColor(getApplicationContext(), R.color.colorOk);
        } else if (accuracy < ACCURACY_WARN) {
            return ContextCompat.getColor(getApplicationContext(), R.color.colorWarn);
        } else {
            return ContextCompat.getColor(getApplicationContext(), R.color.colorDanger);
        }
    }

    private void onRateChanged(float nextRate) {
        String rateString = getString(R.string.current_rate, nextRate);
        runOnUiThread(() -> rateIndicator.setText(rateString));
        if (applying) {
            sendRateMessage(nextRate);
        }
    }

    private void sendRateMessage(float nextRate) {
        BigDecimal rateDecimal = new BigDecimal(nextRate);
        SetRateMessage nextRateMessage = SetRateMessage.newInstance(nextRate);
        nextRateMessage.setResponseListener(response -> {
            if (!response.equals(MessageResponse.ACK_POSITIVE)) {
                onCannotSendMessage(nextRateMessage);
            }
        });
        connectedThread.send(nextRateMessage);
    }

    private void onAccuracyChanged(float nextAccuracy) {
        String accuracyString = getString(R.string.current_precision, nextAccuracy);
        int accuracyColor = getAccuracyColor(nextAccuracy);
        runOnUiThread(() -> {
            accuracyIndicator.setTextColor(accuracyColor);
            accuracyIndicator.setText(accuracyString);
            if (nextAccuracy > ACCURACY_WARN) {
                onBadAccuracy();
            }
        });
    }

    private void onApplying() {
        runOnUiThread(() -> {
            fab.setOnClickListener(v -> setNotApplying());
            int color = ContextCompat.getColor(getApplicationContext(), R.color.colorStop);
            fab.setBackgroundTintList(ColorStateList.valueOf(color));
            fab.setImageResource(R.drawable.close_circle);
            applying = true;
        });
    }

    private void setNotApplying() {
        runOnUiThread(() -> {
            fab.setOnClickListener(v -> onApplying());
            int color = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
            fab.setBackgroundTintList(ColorStateList.valueOf(color));
            fab.setImageResource(R.drawable.send);
            applying = false;
        });
        sendRateMessage((float) 0);
    }

    private void createFloatingActionButton() {
        runOnUiThread(() -> {
            fab = findViewById(R.id.start_fab);
            fab.setOnClickListener(v -> onApplying());
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
            snackbar.setAction(getString(R.string.ok), v -> snackbar.dismiss());
            snackbar.show();
        });
    }

    private void onLostLocationProvider() {
        runOnUiThread(() -> {
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            snackbar.setText(getString(R.string.location_lost));
            snackbar.setAction(getString(R.string.ok), v -> snackbar.dismiss());
            snackbar.show();
        });
    }

    private void onLocationProviderFailure() {
        runOnUiThread(() -> {
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            snackbar.setText(getString(R.string.location_unavailable));
            snackbar.setAction(getString(R.string.ok), v -> snackbar.dismiss());
            snackbar.show();
        });
    }

    private void onBadAccuracy() {
        runOnUiThread(() -> {
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            snackbar.setText(getString(R.string.bad_accuracy));
            snackbar.setAction(getString(R.string.ok), v -> snackbar.dismiss());
            snackbar.show();
        });
    }

    private void onCannotSendMessage(Message message) {
        runOnUiThread(() -> {
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            snackbar.setText(getString(R.string.communication_error));
            snackbar.setAction(getString(R.string.retry), v -> connectedThread.send(message));
            snackbar.show();
        });
    }

    private void onFileParseError(Exception e) {
        e.printStackTrace();
        runOnUiThread(() -> {
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            snackbar.setText(getString(R.string.io_error));
            snackbar.setAction(getString(R.string.ok), v -> snackbar.dismiss());
            snackbar.show();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectedThread = ConnectedThread.getInstance();
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
