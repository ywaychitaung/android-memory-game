package com.example.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardActivity extends AppCompatActivity {

    private List<Bitmap> duplicatedImages;
    private List<Integer> revealedImagesPositions;
    private List<Integer> matchedImagesPositions;
    private List<ImageView> revealedImage;
    private int score;
    private TextView scoreTextView;

    private int mSeconds;

    //background sound intent
    private Intent backgroundSoundIntent;

    //play matching sound
    private MediaPlayer chime_mediaPlayer;

    private MediaPlayer wrong_mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // Success matching sound Chime.mp3
        chime_mediaPlayer = MediaPlayer.create(this,R.raw.chime);
        chime_mediaPlayer.setVolume(100, 100);

        wrong_mediaPlayer = MediaPlayer.create(this,R.raw.wrong);
        wrong_mediaPlayer.setVolume(30,30);

        revealedImagesPositions = new ArrayList<>();
        matchedImagesPositions = new ArrayList<>();
        revealedImage = new ArrayList<>();

        score = 0;
        mSeconds = 0;

        // Find the score TextView (FOR WHOEVER IS DOING SCORING)
        scoreTextView = findViewById(R.id.score_text_view);
        updateScoreText();

        // Get the selected images from MainActivity
        MyApplication myApp = (MyApplication) getApplicationContext();
        List<Bitmap> selectedImages = myApp.getSelectedImages();

        // Duplicate the selected images
        duplicatedImages = new ArrayList<>();
        for (Bitmap bitmap : selectedImages) {
            duplicatedImages.add(bitmap);
            duplicatedImages.add(bitmap);
        }

        // Shuffle the duplicated images randomly
        Collections.shuffle(duplicatedImages);

        // Display the shuffled duplicated images in a ListView
        ListView listView = findViewById(R.id.listView);
        ImageAdapter adapter = new ImageAdapter(this, duplicatedImages, revealedImagesPositions, matchedImagesPositions,revealedImage,score, chime_mediaPlayer,wrong_mediaPlayer);

        listView.setAdapter(adapter);
        playBackgroundSound();
        Toast.makeText(this, "Playing Music", Toast.LENGTH_SHORT).show();
        runTimer();
    }

    public String getTime(){
        TextView timer = findViewById(R.id.timer);
        String[] time = timer.getText().toString().split(" ");
        return time[2];
    }

    public void writeFile(int score){
        String time = getTime();
        String filePath = "MemoryGame";
        String filename = "ScoreHistory.txt";
        File targetFile = new File(this.getFilesDir(),filePath+"/"+filename);

        String content = score + " \t" + time+"\n";

        try{
            File parent = targetFile.getParentFile();
            if(!parent.exists() && !parent.mkdirs()){
                throw new IllegalStateException("Couldn't create dir: "+ parent);
            }
            FileOutputStream fos = new FileOutputStream(targetFile,true);
            fos.write(content.getBytes());
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void updateScoreText() {
        scoreTextView.setText(score + " of 6 matches");
    }

    // Call this method whenever a match is found
    public void increaseScore(int increment) {
        score += increment;
        updateScoreText();
    }

    private void runTimer() {
        final TextView timer = findViewById(R.id.timer);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int minutes = mSeconds / 60;
                int seconds = mSeconds % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);
                timer.setText("Time elapsed: " + timeString);

                mSeconds++;

                if (score >= 6) {
                    handler.removeCallbacksAndMessages(null); // Stop the timer
                } else {
                    handler.postDelayed(this, 1000); // Continue the timer
                }
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("SavedInstanceState", "onSaveInstanceState is called");
        super.onSaveInstanceState(outState);

        outState.putInt("seconds", mSeconds);
        outState.putInt("score", score);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i("SavedInstanceState", "onRestoreInstanceState is called");
        super.onRestoreInstanceState(savedInstanceState);

        mSeconds = savedInstanceState.getInt("seconds");
        score = savedInstanceState.getInt("score");
        updateScoreText();

    }

    public void playBackgroundSound(){
        backgroundSoundIntent = new Intent(this, BackgroundSoundService.class);
        startService(backgroundSoundIntent);
    }
    public void stopBackgroundSound(){
        if(backgroundSoundIntent !=null){
            stopService(backgroundSoundIntent);
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
        chime_mediaPlayer.release();
        wrong_mediaPlayer.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playBackgroundSound();
    }

}