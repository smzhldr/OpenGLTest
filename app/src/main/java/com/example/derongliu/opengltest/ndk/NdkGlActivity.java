package com.example.derongliu.opengltest.ndk;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class NdkGlActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new NdkGlRenderer());
        setContentView(glSurfaceView);
    }
}
