package com.example.derongliu.opengltest.ffmpeg;

import android.view.Surface;

public class FFmpegUtils {

    static {
        System.loadLibrary("ndklib");
    }

    public static native int playVideo(String url);

    public static native void setSurface(Surface surface);
}
