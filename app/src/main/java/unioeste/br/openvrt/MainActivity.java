package unioeste.br.openvrt;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SelectShapeFragment.ShapeListFragmentInteractionListener {

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
        fab.setOnClickListener((view) -> {
            toFilesListFragment();
        });

        return fab;
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
        System.out.println(item);
    }
}
