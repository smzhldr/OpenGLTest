package com.example.derongliu.opengltest.ndk.byglsurfaceview;

public class NdkGlHelper {

    static {
        System.loadLibrary("ndklib");
    }

    public static native void onSurfaceCreated();

    public static native void onSurfaceChanged(int width,int height);

    public static native void onDrawFrame();
}
