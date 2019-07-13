package com.example.derongliu.mediacodec.encoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Handler;

public class AudioEncoder extends Encoder {

    private static final int DEFAULT_BITRATE_AUDIO = 128 * 1000;

    private static final String MIME_TYPE_AAC = "audio/mp4a-latm";

    private static final int DEFAULT_CHANEL = 1;
    private static final int DEFAULT_SAMPLE = 48000;
    private AudioThread audioThread;

    public AudioEncoder(MediaMuxer mediaMuxer) {
        super(mediaMuxer);
    }


    @Override
    public void prepare() {
        MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE_AAC, DEFAULT_SAMPLE, DEFAULT_CHANEL);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_BITRATE_AUDIO);

        try {
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE_AAC);
            mediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
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
            return input;
        }
    }


    @Override
    public void startRecord() {
        super.startRecord();
        if (audioThread == null) {
            audioThread = new AudioThread();
            audioThread.start();
        }
    }

    private AudioRecord mAudioRecord;
    private int mBufferSize;
    private int mChannels = 1;


    private ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(10);


    private class AudioThread extends Thread {


        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            try {
                int channelConfig = mChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_OUT_STEREO;
                mBufferSize = getAudioBufferSize(channelConfig, AudioFormat.ENCODING_PCM_16BIT);
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, DEFAULT_SAMPLE, channelConfig, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);
            } catch (Exception e) {
                Log.e(TAG, "init AudioRecord exception: " + e.getLocalizedMessage());
            }

            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "cannot init AudioRecord");
            }

            if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                return;
            }

            ByteBuffer audioBuffer = ByteBuffer.allocate(mBufferSize);
            mAudioRecord.startRecording();
            Log.d(TAG, "AudioRecorder started");

            int readResult;
            try {


                while (isRecord) {
                    readResult = mAudioRecord.read(audioBuffer.array(), 0, mBufferSize);
                    if (readResult > 0) {
                        byte[] data = new byte[readResult];
                        audioBuffer.position(0);
                        audioBuffer.limit(readResult);
                        audioBuffer.get(data, 0, readResult);

                        if (queue.size() >= 10) {
                            queue.poll();
                        }
                        queue.add(data);
                    }
                }
            } finally {
                if (mAudioRecord != null) {
                    mAudioRecord.stop();
                    mAudioRecord.release();
                }
            }
        }

        // 16BIT 格式兼容性更好
        // 单声道效率更高
        private int getAudioBufferSize(int channelLayout, int pcmFormat) {
            int bufferSize = 1024;

            switch (channelLayout) {
                case AudioFormat.CHANNEL_IN_MONO:
                    bufferSize *= 1;
                    break;
                case AudioFormat.CHANNEL_IN_STEREO:
                    bufferSize *= 2;
                    break;
            }

            switch (pcmFormat) {
                case AudioFormat.ENCODING_PCM_8BIT:
                    bufferSize *= 1;
                    break;
                case AudioFormat.ENCODING_PCM_16BIT:
                    bufferSize *= 2;
                    break;
            }

            return bufferSize;
        }
    }
}
