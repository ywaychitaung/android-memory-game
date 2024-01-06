package com.example.memorygame;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;


public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private Thread bgmSound;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null) {
                return;
            }
            if(action.equalsIgnoreCase("Stop BGM")){
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.appbgm);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(100, 100);
        // Register broadcast Receiver
        register();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        bgmSound = new Thread(new Runnable() {
            @Override
            public void run() {
                if(Thread.interrupted()) {
                    return;
                }
                mediaPlayer.start();
            }
        });
        bgmSound.start();
        return startId;
    }

    @Override
    public void onDestroy() {
        bgmSound.interrupt();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
    @Override
    public void onLowMemory() {
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Stop BGM");
        registerReceiver(myReceiver,intentFilter);
    }

}
