package com.example.derongliu.mediacodec.encoder;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.derongliu.opengltest.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaEncodeActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    public final static String CAMERA_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/";

    private Camera camera;
    private boolean isRecording;
    private int width = 1280;
    private int height = 720;

    MediaMuxerWrapper muxerWrapper;

    long recordingStartTime;

    String timeStamp;
    String prefix = "VID_";
    String extension = ".mp4";

    String title;

    String displayName;
    String outputPath;
    File mediaFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_media_encode);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SurfaceView surfaceView = findViewById(R.id.sv_encode);
        surfaceView.getHolder().addCallback(this);
        final Button button = findViewById(R.id.btn_record);
        button.setText("开始录像");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = !isRecording;
                if (isRecording) {
                    button.setText("正在录像");

                    recordingStartTime = SystemClock.uptimeMillis();

                    File mediaStorageDir;
                    mediaStorageDir = new File(CAMERA_FOLDER);

                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            return;
                        }
                    }


                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
                    timeStamp = dateFormat.format(new Date());

                    title = prefix + timeStamp;

                    mediaFile = new File(mediaStorageDir, title + extension);
                    displayName = mediaFile.getName();
                    outputPath = mediaFile.getPath();

                    int index = 1;
                    while (mediaFile.exists()) {
                        title = prefix + timeStamp + "-" + index;
                        mediaFile = new File(mediaStorageDir, title + extension);
                        index++;
                    }

                    if (getDegree() == 0 || getDegree() == 180) {
                        muxerWrapper = new MediaMuxerWrapper(outputPath, height, width);
                    } else {
                        muxerWrapper = new MediaMuxerWrapper(outputPath, width, height);
                    }
                    muxerWrapper.start();


                } else {
                    button.setText("开始录像");
                    muxerWrapper.stop();
                    muxerWrapper = null;

                    ContentValues currentVideoValues = new ContentValues();
                    currentVideoValues.put(MediaStore.Video.Media.TITLE, title);
                    currentVideoValues.put(MediaStore.Video.Media.DISPLAY_NAME, displayName);
                    currentVideoValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
                    currentVideoValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                    currentVideoValues.put(MediaStore.Video.Media.DATA, outputPath);
                    currentVideoValues.put(MediaStore.Video.Media.DESCRIPTION, "com.example.derongliu.opengltest");

                    currentVideoValues.put(MediaStore.Video.Media.SIZE, new File(outputPath).length());
                    long duration = SystemClock.uptimeMillis() - recordingStartTime;
                    if (duration > 0) {
                        currentVideoValues.put(MediaStore.Video.Media.DURATION, duration);
                        getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, currentVideoValues);
                    } else {
                        Log.d("Record", "Video duration <= 0 : " + duration);
                    }
                }
            }
        });

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setPreviewSize(width, height);
        parameters.setPreviewFormat(ImageFormat.NV21);

        camera.setParameters(parameters);

        camera.setPreviewCallbackWithBuffer(this);
        camera.addCallbackBuffer(new byte[((width * height) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        if (muxerWrapper != null) {
            if (getDegree() == 0 || getDegree() == 180) {
                muxerWrapper.putVideodate(YuvLibs.rotateYUV420Degree270(data, width, height));
            } else {
                muxerWrapper.putVideodate(data);
            }
        }
        camera.addCallbackBuffer(data);
    }

    private int getDegree() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        return degrees;
    }
}
