package com.example.myapplication.activities.chat;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.squareup.picasso.Picasso;
public class ImageViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        ImageView imageView = findViewById(R.id.imageView);
        ImageButton buttonDownload = findViewById(R.id.buttonDownload);
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        String imageUrl = getIntent().getStringExtra("imageUrl");
        // Sử dụng Picasso để tải ảnh
        Picasso.get().load(imageUrl).into(imageView);
        buttonBack.setOnClickListener(v -> finish());
        buttonDownload.setOnClickListener(v -> downloadFile(imageUrl, "image.jpg"));
    }
    private void downloadFile(String url, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Đang tải ảnh...");
        request.setTitle(fileName);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Đang tải ảnh...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Tải không thành công", Toast.LENGTH_SHORT).show();
        }
    }
}
