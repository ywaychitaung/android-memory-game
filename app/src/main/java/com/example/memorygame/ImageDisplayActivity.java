package com.example.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ImageDisplayActivity extends AppCompatActivity {

    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        gridView = findViewById(R.id.gridView);

        // Get URL from Intent
        String url = getIntent().getStringExtra("URL");
        if (url != null && !url.isEmpty()) {
            fetchImages(url);
        } else {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchImages(String url) {
        // TODO: Implement image fetching logic, update progress dialog, and display images in gridView
    }
}