package com.example.derongliu.opengltest.ndk.byglsurfaceview;

public class NdkGlHelper {

    static {
        System.loadLibrary("gl_helper");
    }

    public static native void onSurfaceCreated();

    public static native void onSurfaceChanged(int width,int height);

    public static native void onDrawFrame();
}
