package com.example.derongliu.opengltest.mediaencode;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.derongliu.opengltest.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaActivity extends Activity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public final static String CAMERA_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/";
    private Button recordButton;
    private Renderer renderer;
    private boolean isRecording;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_media);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        GLSurfaceView glSurfaceView = findViewById(R.id.record_sv);
        glSurfaceView.setEGLContextClientVersion(2);
        renderer = new Renderer(this, glSurfaceView);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        recordButton = findViewById(R.id.bt_record);

        locationManager = new LocationManager(this, null);
        locationManager.recordLocation(true);

        recordButton.setOnClickListener(this);
        renderer.start();
    }


    @Override
    public void onClick(View v) {
        if (!isRecording) {
            recordButton.setText("录制中...");
            isRecording = true;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){ //表示未授权时
                //进行授权
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},0);
            } else {
                startRecord();
            }
        } else {
            recordButton.setText("开始录制");
            isRecording = false;
            renderer.stopRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: {
                // 如果授权被取消，结果数组是空的，注意这里是empty ，而不是null
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 已经授权，在这里做你想要做的事
                    startRecord();
                }
            }
        }
    }

    private void startRecord() {
        File mediaStorageDir;
        mediaStorageDir = new File(CAMERA_FOLDER);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return;
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
        String timeStamp = dateFormat.format(new Date());
        String prefix = "VID_";
        String extension = ".mp4";

        String title = prefix + timeStamp;
        final String finalTitle = title;
        File mediaFile = new File(mediaStorageDir, title + extension);
        final String displayName = mediaFile.getName();
        final String outputPath = mediaFile.getPath();

        int index = 1;
        while (mediaFile.exists()) {
            title = prefix + timeStamp + "-" + index;
            mediaFile = new File(mediaStorageDir, title + extension);
            index++;
        }
        final Location loc = locationManager.getCurrentLocation();
        final long recordingStartTime = SystemClock.uptimeMillis();

        MediaEncoder.MediaEncoderListener encoderListener = new MediaEncoder.MediaEncoderListener() {
            @Override
            public void onPrepared(MediaEncoder encoder) {

            }

            @Override
            public void onStopped(MediaEncoder encoder) {

            }

            @Override
            public void onMuxerStopped() {
                ContentValues currentVideoValues = new ContentValues();
                currentVideoValues.put(MediaStore.Video.Media.TITLE, finalTitle);
                currentVideoValues.put(MediaStore.Video.Media.DISPLAY_NAME, displayName);
                currentVideoValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
                currentVideoValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                currentVideoValues.put(MediaStore.Video.Media.DATA, outputPath);
                currentVideoValues.put(MediaStore.Video.Media.DESCRIPTION, "com.example.derongliu.opengltest");
                if (loc != null) {
                    currentVideoValues.put(MediaStore.Video.Media.LATITUDE, loc.getLatitude());
                    currentVideoValues.put(MediaStore.Video.Media.LONGITUDE, loc.getLongitude());
                }
                currentVideoValues.put(MediaStore.Video.Media.SIZE, new File(outputPath).length());
                long duration = SystemClock.uptimeMillis() - recordingStartTime;
                if (duration > 0) {
                    currentVideoValues.put(MediaStore.Video.Media.DURATION, duration);
                } else {
                    Log.d("Record", "Video duration <= 0 : " + duration);
                }
            }

            @Override
            public void onMuxerStopFailed() {
                new File(outputPath).delete();
            }
        };
        renderer.startRecording(outputPath, encoderListener);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        renderer.stop();
    }
}
