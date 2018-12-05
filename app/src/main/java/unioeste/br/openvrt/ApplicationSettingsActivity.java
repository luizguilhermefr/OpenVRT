package unioeste.br.openvrt;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import unioeste.br.openvrt.connection.ConnectedThread;
import unioeste.br.openvrt.connection.message.Message;
import unioeste.br.openvrt.connection.message.SetMeasurementMessage;
import unioeste.br.openvrt.connection.message.SetWorkWidthMessage;
import unioeste.br.openvrt.connection.message.dictionary.Measurement;
import unioeste.br.openvrt.connection.message.dictionary.MessageResponse;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ApplicationSettingsActivity extends AppCompatActivity {

    private String mapLocation;

    private Measurement selectedMeasurement;

    private Float workWidth;

    private Snackbar snackbar;

    private Button measurementButton;

    private TextView validationText;

    private EditText workWidthEditText;

    private ConnectedThread connectedThread;

    private void toMapsActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("map", mapLocation);
        startActivity(intent);
    }

    public void askMeasurement(View view) {
        selectedMeasurement = Measurement.fromIndex(0, getApplicationContext());
        HashMap<Measurement, String> measurementsMap = Measurement.translationMap(getApplicationContext());
        ArrayList<String> measurementsList = new ArrayList<>(measurementsMap.values());
        AlertDialog.Builder measurementDialogBuilder = new AlertDialog.Builder(this);
        measurementDialogBuilder.setTitle(R.string.rate_measurement);
        measurementDialogBuilder.setSingleChoiceItems(measurementsList.toArray(new String[0]), 0, (dialogInterface, i) -> {
            selectedMeasurement = Measurement.fromIndex(i, getApplicationContext());
            measurementButton.setText(MessageFormat.format("{0} ({1})", getString(R.string.rate_measurement), measurementsList.get(i)));
        });
        measurementDialogBuilder.setPositiveButton(R.string.ok, (dialogInterface, which) -> {
            //
        });
        measurementDialogBuilder.create().show();
    }

    public void onPressNext(View view) {
        String workWidthStr = workWidthEditText.getText().toString();
        if (selectedMeasurement != null && workWidthStr.length() > 0) {
            try {
                workWidth = Float.parseFloat(workWidthStr);
                if (workWidth > 0) {
                    dispatchMessages();
                } else {
                    onValidationError();
                }
            } catch (NumberFormatException e) {
                onValidationError();
            }
        } else {
            onValidationError();
        }
    }

    private void onValidationError() {
        validationText.setVisibility(View.VISIBLE);
    }

    private void dispatchMessages() {
        SetMeasurementMessage nextMeasurementMessage = SetMeasurementMessage.newInstance(selectedMeasurement);
        nextMeasurementMessage.setResponseListener(measurementResponse -> {
            if (!measurementResponse.equals(MessageResponse.ACK_POSITIVE)) {
                onCannotSendMessage(nextMeasurementMessage);
            } else {
                sendWorkWidthMessage();
            }
        });
        connectedThread.send(nextMeasurementMessage);
    }

    private void sendWorkWidthMessage() {
        onSendingMessages();
        SetWorkWidthMessage nextWorkWidthMessage = SetWorkWidthMessage.newInstance(workWidth);
        nextWorkWidthMessage.setResponseListener(workWidthResponse -> {
            if (!workWidthResponse.equals(MessageResponse.ACK_POSITIVE)) {
                onCannotSendMessage(nextWorkWidthMessage);
            } else {
                toMapsActivity();
            }
        });
        connectedThread.send(nextWorkWidthMessage);
    }

    private void onCannotSendMessage(Message message) {
        runOnUiThread(() -> {
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            snackbar.setText(getString(R.string.communication_error));
            snackbar.setAction(getString(R.string.retry), v -> connectedThread.send(message));
            snackbar.show();
        });
    }

    private void onSendingMessages() {
        runOnUiThread(() -> {
            snackbar.setText(R.string.preparing);
            snackbar.setAction("", v -> {
                // No action
            });
            snackbar.show();
        });
    }

    private void makeSnackbar() {
        snackbar = Snackbar.make(findViewById(R.id.settings), "", Snackbar.LENGTH_LONG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectedThread = ConnectedThread.getInstance();
        Intent intent = getIntent();
        mapLocation = intent.getStringExtra("map");
        setContentView(R.layout.activity_application_settings);
        measurementButton = findViewById(R.id.measurement_button);
        validationText = findViewById(R.id.validation_text);
        workWidthEditText = findViewById(R.id.work_width_edit_text);
        makeSnackbar();
    }
}
