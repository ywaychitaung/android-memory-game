package com.example.memorygame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends ArrayAdapter<Bitmap> {
    private List<Bitmap> gameImagePairs;
    private List<Integer> openImageIndices;
    private List<Integer> matchedImageIndices;
    private List<ImageView> imageViews;

    private List<ImageView> currentOpenImages;

    private int score;

    public ImageAdapter(Context context, List<Bitmap> images, List<Integer> revealedImagesPositions,
                        List<Integer> matchedImagesPositions, List<ImageView> revealedImages, int score) {
        super(context, 0, images);
        this.gameImagePairs = images;
        this.openImageIndices = revealedImagesPositions;
        this.matchedImageIndices = matchedImagesPositions;
        this.currentOpenImages = revealedImages;
        this.score = score;

        this.imageViews = new ArrayList<>();
        for (Bitmap image : images) {
            ImageView imageView = new ImageView(context);
            imageView.setImageBitmap(image);
            this.imageViews.add(imageView);
        }
    }

    private MediaPlayer mediaPlayer;

    private void playMatchSound() {
        // Check if mediaPlayer is already in use and release it if necessary
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.match_sound); // replace "match_sound" with the name of your audio file
        mediaPlayer.start();

        // Set an OnCompletionListener to release the MediaPlayer once the sound has finished playing
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });
    }

    @Override
    public int getCount() {
        return gameImagePairs.size() / 3;
    }

    private void handleImageClick(int rowPosition, int imagePosition, ImageView imageView) {
        int position = rowPosition * 3 + imagePosition;

        if (matchedImageIndices.contains(position) || openImageIndices.contains(position)) {
            return;
        }

        showImage(imageView, position);

        if (openImageIndices.isEmpty()) {
            openImageIndices.add(position);
            currentOpenImages.add(imageView);
            showImage(imageView, position);
            return;
        }

        if (gameImagePairs.get(openImageIndices.get(0)).sameAs(gameImagePairs.get(position))) {
            matchedImageIndices.add(openImageIndices.get(0));
            matchedImageIndices.add(position);
            openImageIndices.clear();
            int matchedImgs = matchedImageIndices.size();
            int imgSize = gameImagePairs.size();

            if(matchedImgs == imgSize){
                Toast.makeText(getContext(), "Game Finished", Toast.LENGTH_SHORT).show();

                Context context = getContext();
                Intent intent = new Intent(context, ImageDownloadActivity.class);
                context.startActivity(intent);
            }
            else {
                Toast.makeText(getContext(), "Matched!", Toast.LENGTH_SHORT).show();
                playMatchSound();
                ((GameActivity) getContext()).increaseScore(1);
                this.score += 1;

                imageView.setOnClickListener(null);
                currentOpenImages.get(0).setOnClickListener(null);
                currentOpenImages.clear();
            }

        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideImage(currentOpenImages.get(0));
                    hideImage(imageView);
                    openImageIndices.clear();
                    currentOpenImages.clear();
                }
            }, 1000);


        }
    }

    @Override
    public View getView(final int position, @Nullable View convertView,
                        @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.image_layout, parent, false);
        }

        LinearLayout layout = convertView.findViewById(R.id.listView);
        ImageView imageView1 = convertView.findViewById(R.id.imageView1);
        ImageView imageView2 = convertView.findViewById(R.id.imageView2);
        ImageView imageView3 = convertView.findViewById(R.id.imageView3);

        List<Bitmap> rowImages = new ArrayList<>();

        int start = position * 3;
        int end = Math.min(start + 3, gameImagePairs.size());

        if (start < end) {
            rowImages = gameImagePairs.subList(start, end);
        }

        imageView1.setImageResource(R.drawable.image_placeholder);
        imageView2.setImageResource(R.drawable.image_placeholder);
        imageView3.setImageResource(R.drawable.image_placeholder);

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(position, 0, imageView1);
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(position, 1, imageView2);
            }
        });

        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(position, 2, imageView3);
            }
        });

        if (rowImages.size() < 3) {
            if (imageView3.getVisibility() != View.GONE) {
                imageView3.setVisibility(View.GONE);
            }
            if (rowImages.size() < 2) {
                if (imageView2.getVisibility() != View.GONE) {
                    imageView2.setVisibility(View.GONE);
                }
            }
        }

        return convertView;
    }

    private void hideImage(ImageView imageView){
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(imageView,
                "scaleX", 1f, 0f);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(imageView,
                "scaleX", 0f,1f);
        animator1.setInterpolator(new DecelerateInterpolator());
        animator2.setInterpolator(new AccelerateInterpolator());
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.image_placeholder);
                super.onAnimationEnd(animation);
                animator2.start();
            }
        });
        animator1.start();
    }

    private void showImage(ImageView imageView, int position){
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(imageView,
                "scaleX", 1f, 0f);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(imageView,
                "scaleX", 0f,1f);
        animator1.setInterpolator(new DecelerateInterpolator());
        animator2.setInterpolator(new AccelerateInterpolator());
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(gameImagePairs.get(position));
                super.onAnimationEnd(animation);
                animator2.start();
            }
        });
        animator1.start();
    }
}
