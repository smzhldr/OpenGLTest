package com.example.derongliu.mediacodec.encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class VideoEncoder extends Encoder {

    private static final int DEFAULT_FRAME_RATE = 30;
    private static final int DEFAULT_IFRAME_INTERVAL = 5;
    private static final int DEFAULT_BITRATE = 10 * 1000 * 1000;


    private static final String MIME_TYPE_AVC = "video/avc";

    private ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(10);

    private byte[] output;

    private int width;
    private int height;

    public VideoEncoder(MediaMuxer mediaMuxer, int width, int height) {
        super(mediaMuxer);
        this.width = width;
        this.height = height;
        this.output = new byte[width * height * 3 / 2];
    }

    @Override
    public void prepare() {
        MediaFormat videoFormat = MediaFormat.createVideoFormat(MIME_TYPE_AVC, width, height);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_BITRATE);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_FRAME_RATE);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, DEFAULT_IFRAME_INTERVAL);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);

        try {
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE_AVC);
            mediaCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaCodec.start();
    }

    @Override
    protected byte[] getFrame() {
        byte[] input = queue.poll();
        if (input == null) {
            return null;
        } else {
            YuvLibs.NV21ToNV12(input, output, width * height);
            return output;
        }
    }

    public void putData(byte[] data) {
        if (queue.size() >= 10) {
            queue.poll();
        }
        queue.add(data);
    }
}
