package com.example.memorygame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends ArrayAdapter<Bitmap> {
    private List<Bitmap> images;
    private List<Integer> revealedImagesPositions;
    private List<Integer> matchedImagesPositions;
    private List<ImageView> imageViews;

    private List<ImageView> revealedImages;

    private int score;

    public ImageAdapter(Context context, List<Bitmap> images, List<Integer> revealedImagesPositions,
                        List<Integer> matchedImagesPositions, List<ImageView> revealedImages, int score) {
        super(context, 0, images);
        this.images = images;
        this.revealedImagesPositions = revealedImagesPositions;
        this.matchedImagesPositions = matchedImagesPositions;
        this.revealedImages = revealedImages;
        this.score = score;

        this.imageViews = new ArrayList<>();
        for (Bitmap image : images) {
            ImageView imageView = new ImageView(context);
            imageView.setImageBitmap(image);
            this.imageViews.add(imageView);
        }
    }

    @Override
    public int getCount() {
        return images.size() / 3; // Divide by 3 to get the number of rows
    }

    //pass imageView from clickListener
    private void handleImageClick(int rowPosition, int imagePosition, ImageView imageView) {
        int position = rowPosition * 3 + imagePosition;

        // If the image is already matched or currently revealed, do nothing
        //please disable click listener after match
        if (matchedImagesPositions.contains(position) || revealedImagesPositions.contains(position)) {
            return;
        }

        // Show the image
        //revealImage(position);
        uncoverImage(imageView, position);

        // If this is the first revealed image, just remember its position
        if (revealedImagesPositions.isEmpty()) {
            revealedImagesPositions.add(position);
            revealedImages.add(imageView);
            uncoverImage(imageView, position);
            return;
        }

        // If this is the second revealed image, check if it matches the first
        if (images.get(revealedImagesPositions.get(0)).sameAs(images.get(position))) {
            matchedImagesPositions.add(revealedImagesPositions.get(0));
            matchedImagesPositions.add(position);
            revealedImagesPositions.clear();
            int matchedImgs = matchedImagesPositions.size();
            int imgSize = images.size();

            if(matchedImgs == imgSize){
                this.score += 1;
                ((CardActivity) getContext()).increaseScore(1);

                ((CardActivity)getContext()).writeFile(score);
                String time = ((CardActivity) getContext()).getTime();
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Congratulations!");
                dialog.setMessage("Score: "+ score+" \nTime: "+time);
                dialog.setPositiveButton("Play again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Context context = dialog.getContext();
                        Intent intent = new Intent(context, CardActivity.class);
                        context.startActivity(intent);
                    }
                });
                dialog.setNegativeButton("Back to Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Context context = dialog.getContext();
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    }
                });
                dialog.show();
            }
            else {
                Toast.makeText(getContext(), "Match!", Toast.LENGTH_SHORT).show();

                // Increase the score by 1
                ((CardActivity) getContext()).increaseScore(1);
                this.score += 1;

                // disable clicking
                imageView.setOnClickListener(null);
                revealedImages.get(0).setOnClickListener(null);
                revealedImages.clear();
            }

        } else {
            // If it's not a match, hide the first revealed image and remember the new one after a delay
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    coverImage(revealedImages.get(0));
                    coverImage(imageView);
                    revealedImagesPositions.clear();
                    revealedImages.clear();
                }
            }, 1000); // Delay of 1 second (adjust as needed)


        }
    }

    @Override
    public View getView(final int position, @Nullable View convertView,
                        @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout, parent, false);
        }

        LinearLayout layout = convertView.findViewById(R.id.listView);
        ImageView imageView1 = convertView.findViewById(R.id.imageView1);
        ImageView imageView2 = convertView.findViewById(R.id.imageView2);
        ImageView imageView3 = convertView.findViewById(R.id.imageView3);

        List<Bitmap> rowImages = new ArrayList<>();

        // Calculate the start and end positions of the images for the current row
        int start = position * 3;
        int end = Math.min(start + 3, images.size());

        // Extract the images for the current row
        if (start < end) {
            rowImages = images.subList(start, end);
        }

        // Set all the initial images to the image_placeholder
        imageView1.setImageResource(R.drawable.image_placeholder);
        imageView2.setImageResource(R.drawable.image_placeholder);
        imageView3.setImageResource(R.drawable.image_placeholder);

        // Set click listeners for the ImageViews
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

        // Hide any unused ImageViews in the layout
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

    //reveal/hide image animation
    private void uncoverImage(ImageView imageView, int position){
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
                imageView.setImageBitmap(images.get(position));
                super.onAnimationEnd(animation);
                animator2.start();
            }
        });
        animator1.start();
    }

    private void coverImage(ImageView imageView){
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

}
