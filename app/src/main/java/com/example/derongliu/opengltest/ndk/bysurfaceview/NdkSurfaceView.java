package com.example.derongliu.opengltest.ndk.bysurfaceview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class NdkSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public NdkSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public NdkSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        onStart();
        onResume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setSurface(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        onPause();
        onStop();
        setSurface(null);
    }


    public native void onStart();


    public native void onResume();


    public native void onPause();


    public native void onStop();

    public native void setSurface(Surface surface);

    static {
        System.loadLibrary("gl_helper");
    }
}
