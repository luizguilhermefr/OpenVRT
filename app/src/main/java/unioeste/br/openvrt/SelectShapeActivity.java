package unioeste.br.openvrt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class SelectShapeActivity extends AppCompatActivity implements SelectShapeFragment.ShapeListFragmentInteractionListener {

    private static final int PERMISSION_BLUETOOTH = 3;

    private String selectedShape;

    private void toDevicesActivity() {
        Intent intent = new Intent(this, SelectDeviceActivity.class);
        startActivity(intent);
    }

    @NonNull
    private Boolean hasPermissionToBluetooth() {
        return ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
    }

    private void askPermissionToBluetooth() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_ADMIN
        }, PERMISSION_BLUETOOTH);
    }

    private void askPermissionsToBluetoothOrGoToSelectDeviceActivity() {
        if (hasPermissionToBluetooth()) {
            toDevicesActivity();
        } else {
            askPermissionToBluetooth();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_shape);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.shape_fragment_container, SelectShapeFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_BLUETOOTH:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toDevicesActivity();
                }
                break;
        }
    }

    @Override
    public void onShapeListFragmentInteraction(String item) {
        selectedShape = item;
        askPermissionsToBluetoothOrGoToSelectDeviceActivity();
    }
}
