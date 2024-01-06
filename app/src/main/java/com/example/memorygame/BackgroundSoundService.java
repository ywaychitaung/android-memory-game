package com.example.memorygame;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;


public class BackgroundSoundService extends Service {
    private MediaPlayer mediaPlayer;
    private Thread background_music;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.bg_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(100, 100);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        background_music = new Thread(new Runnable() {
            @Override
            public void run() {
                if(Thread.interrupted()){
                    return;
                }
                mediaPlayer.start();
            }
        });

        background_music.start();

        return startId;
    }
    public void onStart(Intent intent, int startId) {
    }
    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        background_music.interrupt();
    }
    @Override
    public void onLowMemory() {
    }
}
