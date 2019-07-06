package com.example.derongliu.mediacodec.encode;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.derongliu.opengltest.R;

import java.io.IOException;

public class MediaEncodeActivity extends Activity implements SurfaceHolder.Callback,Camera.PreviewCallback {

    Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_media_encode);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SurfaceView surfaceView =findViewById(R.id.sv_encode);
        surfaceView.getHolder().addCallback(this);
        Button button = findViewById(R.id.btn_record);
        button.setText("开始录像");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setPreviewSize(2160,1080);
        //parameters.setPreviewFormat(ImageFormat.YUV_420_888);
        camera.setParameters(parameters);
        camera.setPreviewCallbackWithBuffer(this);
        camera.addCallbackBuffer(new byte[((2160 * 1080) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
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
    public void onPreviewFrame(byte[] data, Camera camera) {

        camera.addCallbackBuffer(data);
    }
}
