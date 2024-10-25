// ImageViewActivity.java
package com.example.myapplication.activities.post;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityImageViewBinding;
import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity {

    private ActivityImageViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String imageUrl = getIntent().getStringExtra("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(binding.imageView);
        }

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonDownload.setOnClickListener(v -> downloadImage(imageUrl));
    }

    private void downloadImage(String imageUrl) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
        request.setDescription("Downloading image");
        request.setTitle("Image download");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "image_" + System.currentTimeMillis() + ".jpg");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Downloading image...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to download image", Toast.LENGTH_SHORT).show();
        }
    }
}
