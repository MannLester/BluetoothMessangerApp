package com.example.menu;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.UUID;

public class Act6 extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FILE_SELECT = 3;
    private static final UUID MY_UUID = UUID.randomUUID();
    private BluetoothAdapter bluetoothAdapter;
    private Button btnSend, btnReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act6);

        btnSend = findViewById(R.id.btnSend);
        btnReceive = findViewById(R.id.btnReceive);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            finish();
        }

        // Check permissions for Bluetooth
        checkPermissions();

        btnSend.setOnClickListener(v -> {
            if (checkBluetoothEnabled()) {
                showFilePicker();
            } else {
                requestBluetoothEnable();
            }
        });

        btnReceive.setOnClickListener(v -> {
            Intent intent = new Intent(Act6.this, info_bt.class);
            startActivity(intent);
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 1);
        }
    }

    private boolean checkBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void showFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_FILE_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == REQUEST_FILE_SELECT && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            Intent intent = new Intent(this, availdev.class);
            intent.putExtra("fileUri", fileUri.toString());
            startActivity(intent);
        }
    }
}
