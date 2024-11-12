package com.example.bluetoothmessanger;

import android.os.Bundle;
import android.content.Context;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);

        createInternalStorageStructure();
        createConversationsFile();
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

        // Create assets folder
        File assetsDir = new File(conversationDir, "assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdirs();
            updateStatus("Created assets directory");
        } else {
            updateStatus("assets directory already exists");
        }
    }

    private void createConversationsFile() {
        File conversationsFile = new File(getFilesDir(), "conversations.json");
        if (!conversationsFile.exists()) {
            try {
                conversationsFile.createNewFile();
                FileWriter writer = new FileWriter(conversationsFile);
                // Initialize with an empty JSON object or structure if needed
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
}
