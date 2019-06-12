package com.example.derongliu.opengltest.ffmpeg;

public class FFmpegUtils {

    static {
        System.loadLibrary("ndklib");
    }

    public static native String getFFmpegInfo();
}
