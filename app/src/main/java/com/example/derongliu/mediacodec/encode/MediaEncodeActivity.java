package com.example.derongliu.mediacodec.encode;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaSync;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.daasuu.mp4compose.composer.Mp4Composer;
import com.example.derongliu.opengltest.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.concurrent.ArrayBlockingQueue;

public class MediaEncodeActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    public final static String CAMERA_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/";

    Camera camera;
    boolean isRecording;
    int width = 1280;
    int height = 720;

    Encoder encoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_media_encode);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SurfaceView surfaceView = findViewById(R.id.sv_encode);
        surfaceView.getHolder().addCallback(this);
        final Button button = findViewById(R.id.btn_record);
        button.setText("开始录像");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = !isRecording;
                if (encoder == null) {
                    encoder = new Encoder();
                }
                if (isRecording) {
                    button.setText("正在录像");
                    encoder.startRecord();
                } else {
                    button.setText("开始录像");
                    encoder.stopRecording();
                    encoder = null;
                }
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setPreviewSize(width, height);
        parameters.setPreviewFormat(ImageFormat.NV21);

        camera.setParameters(parameters);

        camera.setPreviewCallbackWithBuffer(this);
        camera.addCallbackBuffer(new byte[((width * height) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (encoder != null) {
            if (encoder.getDegree() == 0 || encoder.getDegree() == 180) {
                encoder.putData(YuvLibs.rotateYUV420Degree270(data, width, height));
            } else {
                encoder.putData(data);
            }
        }
        camera.addCallbackBuffer(data);
    }

    private class Encoder {

        MediaCodec mediaCodec;
        MediaMuxer mediaMuxer;

        static final int TIMEOUT_USEC = 12000;

        long pts = 0;
        long generateIndex = 0;

        volatile boolean isRecording;

        int videoTrackIndex;

        ArrayBlockingQueue<byte[]> blockingQueue = new ArrayBlockingQueue<>(10);

        Encoder() {

            int degree = getDegree();

            MediaFormat format;
            if (degree == 0 || degree == 180) {
                format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, height, width);
            } else {
                format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            }
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 5 * width * height);
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            try {
                mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
                mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

                File file = new File(CAMERA_FOLDER + "smzh.mp4");
                if (file.exists()) {
                    file.delete();
                }
                mediaMuxer = new MediaMuxer(file.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        void startRecord() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    prepare();

                    isRecording = true;
                    byte[] input;
                    byte[] yuv420sp = new byte[width * height * 3 / 2];
                    byte[] tmp = new byte[yuv420sp.length];

                    ByteBuffer[] outBuffers = mediaCodec.getOutputBuffers();

                    while (isRecording) {
                        input = blockingQueue.poll();

                        if (input != null) {

                            YuvLibs.NV21ToNV12(input, yuv420sp, width, height);

                            int inIndex = mediaCodec.dequeueInputBuffer(-1);

                            try {
                                if (inIndex >= 0) {
                                    pts = computePresentationTime(generateIndex);

                                    ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                                    ByteBuffer inputBuffer = inputBuffers[inIndex];
                                    inputBuffer.clear();
                                    inputBuffer.put(yuv420sp);
                                    mediaCodec.queueInputBuffer(inIndex, 0, yuv420sp.length, pts, 0);

                                    generateIndex += 1;
                                }

                                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                                int outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);


                                if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                    videoTrackIndex = mediaMuxer.addTrack(mediaCodec.getOutputFormat());
                                    mediaMuxer.start();
                                } else if (outIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                                    outBuffers = mediaCodec.getOutputBuffers();
                                } else if (outIndex >= 0) {

                                    ByteBuffer outBuffer = outBuffers[outIndex];
                                    mediaMuxer.writeSampleData(videoTrackIndex, outBuffer, bufferInfo);
                                }

                                mediaCodec.releaseOutputBuffer(outIndex, false);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }

        void putData(byte[] data) {
            if (blockingQueue.size() >= 10) {
                blockingQueue.poll();
            }
            blockingQueue.add(data);
        }

        void stopRecording() {
            isRecording = false;

            mediaMuxer.stop();
            mediaCodec.stop();
        }


        private void prepare() {
            mediaCodec.start();
        }


        private long computePresentationTime(long frameIndex) {
            return 132 + frameIndex * 1000000 / 30;
        }


        private int getDegree() {
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break; // Natural orientation
                case Surface.ROTATION_90:
                    degrees = 90;
                    break; // Landscape left
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;// Upside down
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;// Landscape right
            }
            return degrees;
        }

    }
}
