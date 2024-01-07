package com.example.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private List<Integer> matchedImageIndices;

    private List<Integer> openImageIndices;

    private int score;
    private int secondsElapsed;
    private TextView scoreDisplay;

    private List<Bitmap> gameImagePairs;

    private List<ImageView> currentOpenImages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        openImageIndices = new ArrayList<>();
        matchedImageIndices = new ArrayList<>();
        currentOpenImages = new ArrayList<>();

        score = 0;
        secondsElapsed = 0;

        scoreDisplay = findViewById(R.id.score_text_view);
        updateScoreText();

        App app = (App) getApplicationContext();
        List<Bitmap> selectedImages = app.getSelectedImages();

        gameImagePairs = new ArrayList<>();
        for (Bitmap bitmap : selectedImages) {
            gameImagePairs.add(bitmap);
            gameImagePairs.add(bitmap);
        }

        Collections.shuffle(gameImagePairs);

        ListView listView = findViewById(R.id.listView);
        ImageAdapter adapter = new ImageAdapter(this, gameImagePairs, openImageIndices, matchedImageIndices, currentOpenImages,score);

        listView.setAdapter(adapter);
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
                throw new IllegalStateException("Cannot create a folder: "+ parent);
            }
            FileOutputStream fos = new FileOutputStream(targetFile,true);
            fos.write(content.getBytes());
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void updateScoreText() {
        scoreDisplay.setText(score + " of 6 matches");
    }

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
                int minutes = secondsElapsed / 60;
                int seconds = secondsElapsed % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);
                timer.setText("Time elapsed: " + timeString);

                secondsElapsed++;

                if (score >= 6) {
                    handler.removeCallbacksAndMessages(null);
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("seconds", secondsElapsed);
        outState.putInt("score", score);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        secondsElapsed = savedInstanceState.getInt("seconds");
        score = savedInstanceState.getInt("score");
        updateScoreText();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}