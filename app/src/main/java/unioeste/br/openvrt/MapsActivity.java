package unioeste.br.openvrt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import org.json.JSONException;
import org.json.JSONObject;
import unioeste.br.openvrt.file.PrescriptionMapReader;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private GeoJsonLayer mapLayer;

    private String mapLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mapLocation = intent.getStringExtra("map");
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void parseMapToJson(String mapStr) {
        try {
            JSONObject mapContent = new JSONObject(mapStr);
            addMapLayerToMap(mapContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openFile() {
        PrescriptionMapReader mapReader = new PrescriptionMapReader(mapLocation);
        mapReader.setOnFileReadListener(this::parseMapToJson);
        mapReader.setOnIOExceptionListener(Throwable::printStackTrace);
        Thread mapReaderThread = new Thread(mapReader);
        mapReaderThread.start();
    }

    private void addMapLayerToMap(JSONObject geoJson) {
        mapLayer = new GeoJsonLayer(googleMap, geoJson);
        runOnUiThread(() -> {
            mapLayer.addLayerToMap();
//            LatLngBounds layerBounds = mapLayer.getBoundingBox();
//            googleMap.moveCamera(CameraUpdateFactory.newLatLng(layerBounds.getCenter()));
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        openFile();
    }
}
