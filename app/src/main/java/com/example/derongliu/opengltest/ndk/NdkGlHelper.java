package com.example.derongliu.opengltest.ndk;

public class NdkGlHelper {

    static {
        System.loadLibrary("ndkGlHelper");
    }

    public static native void onSurfaceCreated();

    public static native void onSurfaceChanged(int width,int height);

    public static native void onDrawFrame();
}
