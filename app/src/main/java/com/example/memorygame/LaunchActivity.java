package com.example.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LaunchActivity extends AppCompatActivity {

    private Button startButton;

    private Intent BgmSoundIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        startButton = findViewById(R.id.startButton);
        // start BGMService
        playBackgroundSound();
        Toast.makeText(this,"Now playing BGM",Toast.LENGTH_SHORT).show();

        // To launch the Main Activity
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LaunchActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void playBackgroundSound(){
        BgmSoundIntent = new Intent(this, MusicService.class);
        startService(BgmSoundIntent);
    }
    public void stopBackgroundSound(){
        if(BgmSoundIntent !=null){
            stopService(BgmSoundIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBackgroundSound();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBackgroundSound();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playBackgroundSound();
    }
}