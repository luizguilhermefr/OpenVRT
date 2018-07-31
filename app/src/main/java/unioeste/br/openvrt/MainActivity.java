package unioeste.br.openvrt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import unioeste.br.openvrt.file.PrescriptionMapReader;

public class MainActivity extends AppCompatActivity implements SelectShapeFragment.ShapeListFragmentInteractionListener {

    private static final int PERMISSION_READ_EXTERNAL_DIR = 1;

    private FloatingActionButton fab = null;

    private Integer selectedMeasurement = 0;

    private AlertDialog createMeasurementDialog() {
        AlertDialog.Builder measurementDialogBuilder = new AlertDialog.Builder(this);
        measurementDialogBuilder.setTitle(R.string.rate_measurement);
        measurementDialogBuilder.setSingleChoiceItems(R.array.measurements, 0, (dialogInterface, i) -> selectedMeasurement = i);
        measurementDialogBuilder.setPositiveButton(R.string.ok, (dialog, id) -> {
            //
        });
        measurementDialogBuilder.setNegativeButton(R.string.cancel, (dialog, id
        ) -> {
            //
        });
        return measurementDialogBuilder.create();
    }

    private FloatingActionButton createFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fab);
        // fab.setOnClickListener((view) -> askPermissionsToFilesOrGoToFilesFragment());
        fab.setOnClickListener((view) -> toMapsActivity());

        return fab;
    }

    private void askPermissionsToFilesOrGoToFilesFragment() {
        if (hasPermissionToReadFiles()) {
            toFilesListFragment();
        } else {
            askPermissionToReadFiles();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_EXTERNAL_DIR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toFilesListFragment();
                }
                break;
        }
    }

    @NonNull
    private Boolean hasPermissionToReadFiles() {
        return ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void askPermissionToReadFiles() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, PERMISSION_READ_EXTERNAL_DIR);
    }

    private void toEmptyStateFragment() {
        makeFloatingActionButtonVisible(true);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, EmptyStateFragment.newInstance());
        fragmentTransaction.commit();
    }

    private void toFilesListFragment() {
        makeFloatingActionButtonVisible(false);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, SelectShapeFragment.newInstance());
        fragmentTransaction.commit();
    }

    private void toMapsActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void makeFloatingActionButtonVisible(Boolean visible) {
        runOnUiThread(() -> fab.setVisibility(visible ? FloatingActionButton.VISIBLE : FloatingActionButton.INVISIBLE));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = createFloatingActionButton();
        toEmptyStateFragment();
    }

    @Override
    public void onShapeListFragmentInteraction(String item) {
        PrescriptionMapReader mapReader = new PrescriptionMapReader(item);
        mapReader.setOnFileReadListener(System.out::println);
        mapReader.setOnIOExceptionListener(Throwable::printStackTrace);
        Thread mapReaderThread = new Thread(mapReader);
        mapReaderThread.start();
    }
}
