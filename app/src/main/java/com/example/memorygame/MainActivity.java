package com.example.memorygame;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText urlEditText;
    private Button fetchButton;
    private List<ImageView> imageViews;
    private Button chooseButton;
    public List<Bitmap> selectedImages;

    private ProgressBar progressBar;

    // handle stopping download
    private boolean isDownloading;
    private DownloadImagesTask downloadtask;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlEditText = findViewById(R.id.urlEditText);
        fetchButton = findViewById(R.id.fetchButton);
        chooseButton = findViewById(R.id.choose);
        chooseButton.setEnabled(false);
        imageViews = new ArrayList<>();
        // for adding views
        for (int i = 1; i <= 4; i++) {
            for (int j = 1; j <= 5; j++) {
                int imageViewId = getResources().getIdentifier("imageView" + i + "_" + j, "id", getPackageName());
                ImageView imageView = findViewById(imageViewId);
                imageViews.add(imageView);
            }
        }

        // For starting the download: Checks if a url is entered
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isDownloading) {
                    String url = urlEditText.getText().toString();
                    if (!url.isEmpty()) {
                        downloadtask = new DownloadImagesTask();
                        downloadtask.execute(url);
                        isDownloading = true;
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter a URL", Toast.LENGTH_SHORT).show();
                    }
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(urlEditText.getWindowToken(), 0);
                } else {
                    downloadtask.cancel(true);
                    progressBar.setProgress(0);
                    progressBar.setVisibility(View.GONE);
                    isDownloading = false;
                }

            }
        });

        // Choosing 6 image button, that launch into CardActivity, terminate the BGM and
        // start the Card Game BGM, past scores that are saved are accessed here
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication myApp = (MyApplication) getApplicationContext();
                myApp.setSelectedImages(selectedImages);
                Intent intent = new Intent(MainActivity.this, CardActivity.class);
                intent.putExtra("fileData",readFile());
                // stop the bgm service to switch to another music
                Intent stopBGM = new Intent();
                stopBGM.setAction("Stop BGM");
                sendBroadcast(stopBGM);
                startActivity(intent);
            }
        });

    }


    private class DownloadImagesTask extends AsyncTask<String, Integer, List<Bitmap>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Bitmap> doInBackground(String... urls) {
            List<Bitmap> bitmaps = new ArrayList<>();
            String url = urls[0];
            try {
                Document document = Jsoup.connect(url).get();
                Elements imageElements = document.select("img");
                int count = 0;
                for (Element element : imageElements) {
                    if (count >= 20) {
                        break;
                    }
                    String imageUrl = element.absUrl("src");
                    if (!imageUrl.isEmpty()) {
                        Bitmap bitmap = downloadImage(imageUrl);
                        if (bitmap != null) {
                            bitmaps.add(bitmap);
                            count++;
                            publishProgress(count);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmaps;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            // 在此处更新进度条的进度
            progressBar.setProgress(progress);
        }

        @Override
        protected void onPostExecute(List<Bitmap> bitmaps) {
            chooseButton.setVisibility(View.VISIBLE);
            int imageCount = Math.min(bitmaps.size(), imageViews.size());
            selectedImages = new ArrayList<>();
            for (int i = 0; i < imageCount; i++) {
                final ImageView imageView = imageViews.get(i);
                final Bitmap bitmap = bitmaps.get(i);
                imageView.setImageBitmap(bitmap);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectedImages.contains(bitmap)) {
                            imageView.setImageBitmap(bitmap);
                            selectedImages.remove(bitmap);
                        } else {
                            if (selectedImages.size() >= 6) {
                                Toast.makeText(MainActivity.this, "You have reached the maximum selection", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                            Bitmap resizedIconBitmap = Bitmap.createScaledBitmap(iconBitmap, 100, 100, false);
                            Bitmap resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                            Canvas canvas = new Canvas(resultBitmap);
                            canvas.drawBitmap(bitmap, 0, 0, null);
                            canvas.drawBitmap(resizedIconBitmap, bitmap.getWidth() - resizedIconBitmap.getWidth(), 0, null);
                            imageView.setImageBitmap(resultBitmap);
                            selectedImages.add(bitmap);
                        }
                        if (selectedImages.size() >= 6) {
                            chooseButton.setEnabled(true);
                        } else {
                            chooseButton.setEnabled(false);
                        }
                    }

                });
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    private Bitmap downloadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                connection.disconnect();
                if (originalBitmap == null) {
                    return null;
                }
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 240, 300, false);
                originalBitmap.recycle();

                return resizedBitmap;
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    // History button to check pass scores
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.history){
            String fileData = readFile();
            Log.i("fileData",fileData);

            Log.i("menu","item history");
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Score History");
            if(fileData.equals("") || fileData == null){
                dialog.setMessage("There is no records.");
            }
            else{
                dialog.setMessage("No.   Score   Time Taken\n"+fileData);
            }

            dialog.setCancelable(true);
            dialog.show();
        }

        return true;
    }

    public String readFile(){
        String filePath = "MemoryGame";
        String filename = "ScoreHistory.txt";
        File targetFile = new File(this.getFilesDir(),filePath+"/"+filename);

        String data = "";
        try{
            FileInputStream fis = new FileInputStream(targetFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader bur = new BufferedReader(new InputStreamReader(in));
            String strLine;
            int i =1;
            while((strLine = bur.readLine()) != null){
                String[] str = strLine.split(" ");
                data = data + " "+ i + ".        " + str[0] +"            " + str[1] +"\n";
                i++;
            }
            in.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return data;
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
