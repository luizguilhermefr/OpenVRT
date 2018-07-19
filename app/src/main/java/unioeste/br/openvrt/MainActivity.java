package unioeste.br.openvrt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import unioeste.br.openvrt.file.PrescriptionMapFinder;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SelectShapeFragment.ShapeListFragmentInteractionListener {

    private static final int PERMISSION_READ_EXTERNAL_DIR = 1;

    private Integer selectedMeasurement = 0;

    private AlertDialog fileSeekSpinnerDialog = null;

    private Thread shapeFinderThread = null;

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

    private AlertDialog createSpinnerDialog() {
        AlertDialog.Builder spinnerDialogBuilder = new AlertDialog.Builder(this);
        spinnerDialogBuilder.setTitle(R.string.scanning_maps);
        spinnerDialogBuilder.setView(R.layout.progress_dialog);
        spinnerDialogBuilder.setNegativeButton(R.string.cancel, (dialog, id) -> stopScanningFiles());
        return spinnerDialogBuilder.create();
    }

    private void stopScanningFiles() {
        if (shapeFinderThread != null && shapeFinderThread.isAlive() && !shapeFinderThread.isInterrupted()) {
            shapeFinderThread.interrupt();
        }
    }

    private void scanForFiles() {
        File startingPoint = Environment.getExternalStorageDirectory();
        PrescriptionMapFinder shapeFinder = new PrescriptionMapFinder(startingPoint, this::onAvailableFilesLoaded);
        shapeFinderThread = new Thread(shapeFinder);
        shapeFinderThread.start();
    }

    private void onAvailableFilesLoaded(ArrayList<String> files) {
        fileSeekSpinnerDialog.cancel();
        toFilesListFragment(files);
    }

    @NonNull
    private Boolean hasPermissionToReadFiles() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void askPermissionToReadFiles() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, PERMISSION_READ_EXTERNAL_DIR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_EXTERNAL_DIR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSpinnerAndStartScanningFiles();
                }
                break;
        }
    }

    private void createFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((view) -> {
            if (hasPermissionToReadFiles()) {
                showSpinnerAndStartScanningFiles();
            } else {
                askPermissionToReadFiles();
            }
        });
    }

    private void showSpinnerAndStartScanningFiles() {
        fileSeekSpinnerDialog.show();
        scanForFiles();
    }

    private void toEmptyStateFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, EmptyStateFragment.newInstance());
        fragmentTransaction.commit();
    }

    private void toFilesListFragment(ArrayList<String> files) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, SelectShapeFragment.newInstance(files));
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toEmptyStateFragment();
        createFloatingActionButton();
        fileSeekSpinnerDialog = createSpinnerDialog();
    }

    @Override
    public void onShapeListFragmentInteraction(String item) {
        System.out.println(item);
    }
}
