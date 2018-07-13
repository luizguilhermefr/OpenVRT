package unioeste.br.openvrt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import unioeste.br.openvrt.file.ShapeFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_READ_EXTERNAL_DIR = 1;

    private Integer selectedMeasurement = 0;

    private FloatingActionButton floatingActionButton = null;

    private AlertDialog fileSeekSpinnerDialog = null;

    Future<ArrayList<File>> futureAvailableFiles = null;

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
        spinnerDialogBuilder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            if (futureAvailableFiles != null && !futureAvailableFiles.isDone() && !futureAvailableFiles.isCancelled()) {
                futureAvailableFiles.cancel(true);
            }
        });
        return spinnerDialogBuilder.create();
    }

    private void seekForFiles() {
        File startingPoint = Environment.getExternalStorageDirectory();
        futureAvailableFiles = new ShapeFinder(startingPoint).find();
        Thread waiter = new Thread(() -> {
            try {
                while (!futureAvailableFiles.isDone() && !futureAvailableFiles.isCancelled()) {
                    Thread.sleep(300);
                }
            } catch (InterruptedException ignored) {
                //
            }
            fileSeekSpinnerDialog.cancel();
        });
        waiter.start();
    }

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

    private FloatingActionButton createFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((view) -> {
            if (hasPermissionToReadFiles()) {
                showSpinnerAndStartScanningFiles();
            } else {
                askPermissionToReadFiles();
            }
        });
        return fab;
    }

    private void showSpinnerAndStartScanningFiles() {
        fileSeekSpinnerDialog.show();
        seekForFiles();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        floatingActionButton = createFloatingActionButton();
        fileSeekSpinnerDialog = createSpinnerDialog();
    }
}
