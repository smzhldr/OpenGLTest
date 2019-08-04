package com.example.derongliu.ffmpeg.decoder;

import android.view.Surface;

public class FFmpegDecoder {

    static {
        System.loadLibrary("ndklib");
    }

    public static native void playVideo(String filePath, Surface surface);
}
