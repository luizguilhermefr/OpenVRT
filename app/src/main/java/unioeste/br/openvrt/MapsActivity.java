package unioeste.br.openvrt;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import org.json.JSONException;
import org.json.JSONObject;
import unioeste.br.openvrt.file.PrescriptionMapReader;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private GeoJsonLayer mapLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String mapLocation = intent.getStringExtra("map");
        openFile(mapLocation);
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

    private void openFile(String fileLocation) {
        PrescriptionMapReader mapReader = new PrescriptionMapReader(fileLocation);
        mapReader.setOnFileReadListener(this::parseMapToJson);
        mapReader.setOnIOExceptionListener(Throwable::printStackTrace);
        Thread mapReaderThread = new Thread(mapReader);
        mapReaderThread.start();
    }

    private void addMapLayerToMap(JSONObject geoJson) {
        GeoJsonLayer layer = new GeoJsonLayer(this.googleMap, geoJson);
        layer.addLayerToMap();
        LatLngBounds layerBounds = layer.getBoundingBox();
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(layerBounds.getCenter()));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
}
