package com.example.derongliu.opengltest.camera;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import com.example.derongliu.opengltest.R;
import com.example.derongliu.opengltest.camera.GlSurfaceCameraRender.Filter;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceCameraActivity extends Activity {

    CameraGLSurfaceView glSurfaceView;
    GlSurfaceCameraRender render;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.glcamera);
        glSurfaceView = findViewById(R.id.gl_camera_surface);
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportES2 = configurationInfo.reqGlEsVersion >= 0X20000;
        if (supportES2) {
            glSurfaceView.setEGLContextClientVersion(2);
            render = new GlSurfaceCameraRender(this, glSurfaceView);
            render.setFilter(Filter.NONE);
            render.sethsHalf(false);
            glSurfaceView.setRenderer(render);
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        menu.add(Menu.NONE, 0, 0, "切换摄像头").setTitle("切换摄像头").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 0:
                render.switchCamera();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
