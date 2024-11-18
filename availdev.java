package com.example.menu;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Set;

public class availdev extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private Uri fileUri;
    private LinearLayout devicesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avail_device);

        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devicesContainer = findViewById(R.id.devicesContainer);

        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> displayPairedDevices());

        String uriString = getIntent().getStringExtra("fileUri");
        fileUri = Uri.parse(uriString);

        displayPairedDevices();
    }

    private void displayPairedDevices() {
        devicesContainer.removeAllViews();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                TextView deviceView = new TextView(this);
                deviceView.setText(device.getName());
                deviceView.setTextSize(18);
                deviceView.setPadding(10, 10, 10, 10);
                deviceView.setOnClickListener(v -> sendFileToDevice(device));
                devicesContainer.addView(deviceView);
            }
        } else {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendFileToDevice(BluetoothDevice device) {
        progressDialog = new ProgressDialog(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        progressDialog.setMessage("Sending file to " + device.getName());
        progressDialog.setCancelable(false);
        progressDialog.show();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setPackage("com.android.bluetooth");

        progressDialog.dismiss();
        startActivityForResult(Intent.createChooser(intent, "Select Bluetooth"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            progressBar.setVisibility(View.GONE);
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "File transfer complete", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "File transfer failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}