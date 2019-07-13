package com.example.derongliu.mediacodec.encoder;

import android.media.MediaCodec;
import android.media.MediaMuxer;
import android.util.Log;

import java.nio.ByteBuffer;

public abstract class Encoder implements Runnable {

    protected static final String TAG = "HWRecorder";

    private static final long DEFAULT_TIMEOUT = 10 * 1000;


    private static int formatChangeCount = 0;

    protected MediaCodec mediaCodec;

    private MediaMuxer mediaMuxer;

    private MediaCodec.BufferInfo bufferInfo;

    private int trackIndex;

    protected volatile boolean isRecord;

    private volatile boolean started;

    private long startTime = -1;
    private long lastPts = -1;

    private final Object locker = new Object();

    public Encoder(MediaMuxer mediaMuxer) {
        this.mediaMuxer = mediaMuxer;
        bufferInfo = new MediaCodec.BufferInfo();
        started = true;
        new Thread(this, getClass().getSimpleName()).start();
    }


    public abstract void prepare();


    protected abstract byte[] getFrame();


    private void encoder(long pts) {

        byte[] frame = getFrame();

        if (frame == null) {
            return;
        }

        int inputIndex = -1;
        try {
            inputIndex = mediaCodec.dequeueInputBuffer(DEFAULT_TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (inputIndex >= 0) {

            if (frame.length > 0) {

                ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                ByteBuffer buffer = inputBuffers[inputIndex];
                buffer.clear();
                buffer.put(frame);
                mediaCodec.queueInputBuffer(inputIndex, 0, frame.length, pts, 0);
            } else {
                mediaCodec.queueInputBuffer(inputIndex, 0, 0, pts, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                //mediaCodec.signalEndOfInputStream();
            }
        }
    }

    private long getPts() {
        long pts;
        if (startTime == -1) {
            startTime = System.nanoTime();
            pts = 0;
        } else {
            pts = (System.nanoTime() - startTime) / 1000;
        }
        if (pts <= lastPts) {
            pts += (lastPts - pts) + 1000;
        }
        return pts;
    }

    public void startRecord() {
        prepare();
        isRecord = true;
        synchronized (locker) {
            locker.notifyAll();
        }
    }

    public void stopRecord() {

        started = false;

        isRecord = false;

        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;

        formatChangeCount = 0;
    }


    @Override
    final public void run() {


        while (started) {

            if (!isRecord) {
                synchronized (locker) {
                    try {
                        locker.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            long pts = getPts();

            lastPts = pts;

            encoder(pts);

            drain();
        }
    }

    private void drain() {

        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();

        while (true) {
            if (mediaCodec == null) {
                return;
            }

            int outputIndex;
            try {
                outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_TIMEOUT);
            } catch (IllegalStateException e) {
                outputIndex = MediaCodec.INFO_TRY_AGAIN_LATER;
            }

            if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mediaCodec.getOutputBuffers();
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                trackIndex = mediaMuxer.addTrack(mediaCodec.getOutputFormat());
                formatChangeCount++;

                if (formatChangeCount == 2) {
                    mediaMuxer.start();
                    synchronized (this) {
                        notifyAll();
                    }
                } else if (formatChangeCount > 2) {
                    throw new RuntimeException("format change more than 2 times");
                }

            } else if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (outputIndex < 0) {
                Log.w(TAG, "drainEncoder unexpected result: " + outputIndex);
            } else {
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    continue;
                }

                if (bufferInfo.size != 0) {
                    ByteBuffer outputBuffer = outputBuffers[outputIndex];

                    if (outputBuffer == null) {
                        throw new RuntimeException("drainEncoder get outputBuffer " + outputIndex + " was null");
                    }

                    synchronized (this) {
                        if (formatChangeCount < 2) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    bufferInfo.presentationTimeUs = getPts();

                    mediaMuxer.writeSampleData(trackIndex, outputBuffer, bufferInfo);

                    //lastPts = bufferInfo.presentationTimeUs;
                }

                mediaCodec.releaseOutputBuffer(outputIndex, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    // when EOS come.
                    break;      // out of while
                }
            }
        }
    }
}
