package com.example.messengerapp.activities.chat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.messengerapp.R;

public class VideoViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        VideoView videoView = findViewById(R.id.videoView);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ImageButton buttonDownload = findViewById(R.id.buttonDownload);
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        String videoUrl = getIntent().getStringExtra("videoUrl");
        videoView.setVideoURI(Uri.parse(videoUrl));
        videoView.setOnPreparedListener(mp -> {
            progressBar.setVisibility(View.GONE);
            videoView.start();
        });
        buttonBack.setOnClickListener(v -> finish());

        buttonDownload.setOnClickListener(v -> downloadFile(videoUrl, "video.mp4"));

        videoView.setOnCompletionListener(mp -> finish());
    }
    private void downloadFile(String url, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Đang tải video...");
        request.setTitle(fileName);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Đang tải video...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Tải video không thành công", Toast.LENGTH_SHORT).show();
        }
    }
}