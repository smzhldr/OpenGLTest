package com.example.derongliu.opengltest;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.TextureView;

import java.io.IOException;

public class MyTextureView extends TextureView {
    public Camera camera;

    public MyTextureView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        init();
    }

    private void init(){
        camera= Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        Camera.Parameters parameters=camera.getParameters();
        camera.setDisplayOrientation(90);
        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                try {
                    camera.setPreviewTexture(surfaceTexture);
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                camera.stopPreview();
                camera.release();
                camera=null;
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });

    }

}
