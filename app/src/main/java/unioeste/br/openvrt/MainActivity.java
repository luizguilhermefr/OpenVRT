package unioeste.br.openvrt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_READ_EXTERNAL_DIR = 1;

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 2;

    private boolean hasFilesPermission = false;

    private boolean hasLocationPermission = false;

    public void askPermissionsOrGoToSelectShapeActivity(View view) {
        if (hasAllPermissions()) {
            toSelectShapeActivity();
        } else {
            askPermissions();
        }
    }

    private boolean hasAllPermissions() {
        return hasFilesPermission && hasLocationPermission;
    }


    private void checkPermissions() {
        hasFilesPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        hasLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void askPermissions() {
        if (!hasFilesPermission) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSION_READ_EXTERNAL_DIR);
        } else if (!hasLocationPermission) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_ACCESS_FINE_LOCATION);
        }
    }

    private void toSelectShapeActivity() {
        Intent intent = new Intent(this, SelectShapeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        askPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_EXTERNAL_DIR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasFilesPermission = true;
                    askPermissions();
                }
                break;
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasLocationPermission = true;
                    askPermissions();
                }
                break;
        }
    }
}
