package com.example.bluetoothmessanger;

import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private EditText messageInput;
    private Button sendMessageButton;
    private File messageFile;
    private BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        messageInput = findViewById(R.id.message_input);
        sendMessageButton = findViewById(R.id.send_message_button);

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Android 12 and above
            requestBluetoothPermission();
        }

        createInternalStorageStructure();
        createConversationsFile();

        displayMessages();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = messageInput.getText().toString().trim();
                if (!messageContent.isEmpty()) {
                    if (bluetoothManager != null && bluetoothManager.getAdapter() != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Android 12 and above
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                requestBluetoothPermission();
                                return;
                            }
                        }
                        String deviceName = bluetoothManager.getAdapter().getName();
                        storeMessage(messageContent, deviceName);
                        messageInput.setText("");
                    } else {
                        updateStatus("Bluetooth not available or permission denied.");
                    }
                } else {
                    updateStatus("Please enter a message!");
                }
            }
        });
    }

    private void requestBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
    }

    private void createInternalStorageStructure() {
        File internalDir = new File(getFilesDir(), "conversations_storage");
        if (!internalDir.exists()) {
            internalDir.mkdirs();
            updateStatus("Created conversations_storage directory");
        } else {
            updateStatus("conversations_storage directory already exists");
        }

        // Example for conversation_id_1
        File conversationDir = new File(internalDir, "conversation_id_1");
        if (!conversationDir.exists()) {
            conversationDir.mkdirs();
            updateStatus("Created conversation_id_1 directory");
        } else {
            updateStatus("conversation_id_1 directory already exists");
        }

        File messagesFile = new File(conversationDir, "messages.txt");
        if (!messagesFile.exists()) {
            try {
                messagesFile.createNewFile();
                updateStatus("Created messages.txt file");
            } catch (IOException e) {
                e.printStackTrace();
                updateStatus("Failed to create messages.txt file");
            }
        } else {
            updateStatus("messages.txt file already exists");
        }

        File assetsDir = new File(conversationDir, "assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdirs();
            updateStatus("Created assets directory");
        } else {
            updateStatus("assets directory already exists");
        }

        messageFile = new File(internalDir, "temporary_message.txt");
        if (!messageFile.exists()) {
            try {
                messageFile.createNewFile();
                updateStatus("Created temporary_message.txt file");
            } catch (IOException e) {
                e.printStackTrace();
                updateStatus("Failed to create temporary_message.txt file");
            }
        } else {
            updateStatus("temporary_message.txt file already exists");
        }
    }

    private void createConversationsFile() {
        File conversationsFile = new File(getFilesDir(), "conversations.json");
        if (!conversationsFile.exists()) {
            try {
                conversationsFile.createNewFile();
                FileWriter writer = new FileWriter(conversationsFile);
                JSONObject initialData = new JSONObject();
                writer.write(initialData.toString());
                writer.flush();
                writer.close();
                updateStatus("Created conversations.json file");
            } catch (IOException e) {
                e.printStackTrace();
                updateStatus("Failed to create conversations.json file");
            }
        } else {
            updateStatus("conversations.json file already exists");
        }
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> statusText.append(message + "\n"));
    }

    private void storeMessage(String messageContent, String deviceName) {
        try (FileWriter writer = new FileWriter(messageFile, false)) {

            JSONArray participantsArray = new JSONArray();
            participantsArray.put(deviceName);
            participantsArray.put("user2");

            ArrayList<String> participantsList = new ArrayList<>();
            for (int i = 0; i < participantsArray.length(); i++) {
                participantsList.add(participantsArray.getString(i));
            }
            Collections.sort(participantsList);

            String conversationId = String.join("_", participantsList);

            JSONObject messageData = new JSONObject();
            messageData.put("conversation_id", conversationId);
            messageData.put("participants", participantsArray);
            messageData.put("sender", deviceName);

            String formattedTimestamp = getFormattedTimestamp();
            messageData.put("timestamp", formattedTimestamp);

            messageData.put("message", messageContent);

            writer.write(messageData.toString() + "\n");
            writer.flush();

            updateStatus("Message stored in temporary_message.txt:\n" + messageData.toString(4));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            updateStatus("Failed to store message in temporary_message.txt");
        }
    }

    private String getFormattedTimestamp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  // Android 8.0 and above
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        }
    }

    private void displayMessages() {
        // Display messages from temporary_message.txt (already existing functionality)
        if (messageFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(messageFile))) {
                String line;
                updateStatus("Displaying messages from temporary_message.txt:");
                while ((line = reader.readLine()) != null) {
                    JSONObject messageData = new JSONObject(line);
                    updateStatus(messageData.toString(4));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                updateStatus("Failed to read temporary_message.txt");
            }
        } else {
            updateStatus("temporary_message.txt file does not exist");
        }

        // Display messages from messages.txt in the conversation directory
        File conversationDir = new File(getFilesDir(), "conversations_storage/conversation_id_1");
        File messagesFile = new File(conversationDir, "messages.txt");

        if (messagesFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(messagesFile))) {
                String line;
                updateStatus("Displaying messages from messages.txt:");
                while ((line = reader.readLine()) != null) {
                    JSONObject messageData = new JSONObject(line);
                    updateStatus(messageData.toString(4));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                updateStatus("Failed to read messages.txt");
            }
        } else {
            updateStatus("messages.txt file does not exist in the conversation directory");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateStatus("Bluetooth permission granted.");
        } else {
            updateStatus("Bluetooth permission denied.");
        }
    }
}