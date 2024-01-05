package com.example.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUrl;
    private Button fetchButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUrl = findViewById(R.id.editTextUrl);
        fetchButton = findViewById(R.id.buttonFetch);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editTextUrl.getText().toString();
                startImageFetch(url);
            }
        });
    }

    private void startImageFetch(String url) {
        // Initialize and configure the ProgressDialog
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Downloading images...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(20); // Assuming you're always fetching 20 images
        progressDialog.show();

        // Intent to start ImageDisplayActivity
        Intent intent = new Intent(MainActivity.this, ImageDisplayActivity.class);
        intent.putExtra("URL", url);
        startActivity(intent);
    }
}
