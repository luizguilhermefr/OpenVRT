package unioeste.br.openvrt;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import unioeste.br.openvrt.file.ShapeFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private Integer selectedMeasurement = 0;

    private FloatingActionButton floatingActionButton = null;

    private AlertDialog fileSeekSpinnerDialog = null;

    private ArrayList<File> availableFiles = null;

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
            //
        });
        return spinnerDialogBuilder.create();
    }

    private void seekForFiles() {
        File startingPoint = Environment.getExternalStorageDirectory();
        Future<ArrayList<File>> futureAvailableFiles = new ShapeFinder(startingPoint).find();
        Thread waiter = new Thread(() -> {
            try {
                while (!futureAvailableFiles.isDone() && !futureAvailableFiles.isCancelled()) {
                    Thread.sleep(300);
                }
                availableFiles = futureAvailableFiles.get();
            } catch (InterruptedException | ExecutionException e) {
                fileSeekSpinnerDialog.cancel();
            }
        });
        waiter.start();
    }

    private FloatingActionButton createFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((view) -> {
            AlertDialog dialog = createSpinnerDialog();
            fileSeekSpinnerDialog.show();
            seekForFiles();
        });
        return fab;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        floatingActionButton = createFloatingActionButton();
        fileSeekSpinnerDialog = createSpinnerDialog();
    }
}
