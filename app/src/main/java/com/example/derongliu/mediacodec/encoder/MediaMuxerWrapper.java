package com.example.derongliu.mediacodec.encoder;

import android.media.MediaMuxer;

import java.io.File;
import java.io.IOException;

public class MediaMuxerWrapper {

    private VideoEncoder videoEncoder;
    private AudioEncoder audioEncoder;
    private MediaMuxer mediaMuxer;


    public MediaMuxerWrapper(String filePath, int width, int height) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean success = file.delete();
        }
        try {
            mediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        videoEncoder = new VideoEncoder(mediaMuxer, width, height);
        audioEncoder = new AudioEncoder(mediaMuxer);

    }

    public void start() {
        if (videoEncoder != null) {
            videoEncoder.startRecord();
        }

        if (audioEncoder != null) {
            audioEncoder.startRecord();
        }
    }


    public void putVideodate(byte[] data) {
        videoEncoder.putData(data);
    }


    public void stop() {

        try {

            if (videoEncoder != null) {
                videoEncoder.stopRecord();
            }

            if (audioEncoder != null) {
                audioEncoder.stopRecord();
            }

            if (mediaMuxer != null) {
                mediaMuxer.stop();
                mediaMuxer.release();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
