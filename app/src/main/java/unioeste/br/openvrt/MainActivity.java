package unioeste.br.openvrt;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

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

    private AlertDialog createSpinnerDialog() {
        AlertDialog.Builder spinnerDialogBuilder = new AlertDialog.Builder(this);
        spinnerDialogBuilder.setTitle(R.string.scanning_maps);
        spinnerDialogBuilder.setView(R.layout.progress_dialog);
        spinnerDialogBuilder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            //
        });
        return spinnerDialogBuilder.create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> createSpinnerDialog().show());
    }
}
