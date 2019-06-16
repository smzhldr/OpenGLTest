package com.example.derongliu.opengltest.ffmpeg;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.derongliu.opengltest.R;
import com.example.derongliu.opengltest.mediaencode.Renderer;
import com.example.derongliu.opengltest.utils.OpenGLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FFmpegActivity extends Activity {

    FFmpegUtils ffmpeg;
    private AudioTrack mAudioTrack;
    private int audioBufSize;
    private boolean isDestoryed;

    private int sampleRate;
    private int bitRate;
    private int channelCount;
    private int audioFormat;
    private int frameSize;

    private byte[] tempData;
    private float[] tempFloatData;

    private FFmpegFilter filter;
    private boolean isCodecStarted = false;
    private float[] matrix = new float[16];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS}, 0);
        }

        FFmpegUtils.init();
        ffmpeg = new FFmpegUtils();

        final Button accPlay = findViewById(R.id.id_ffmpeg_aac);
        accPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioBufSize = AudioTrack.getMinBufferSize(41000,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT);
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 41000,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT,
                        audioBufSize,
                        AudioTrack.MODE_STREAM);
                mAudioTrack.play();
                playFloatPCM();
            }
        });

        Button accChooser = findViewById(R.id.id_ffmpeg_acc_chooser);
        accChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decodeAAC();

            }
        });

        filter = new FFmpegFilter();

        final GLSurfaceView surfaceView = findViewById(R.id.id_ffmpeg_sv);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                filter.init();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES20.glViewport(0, 0, width, height);
                filter.onSizeChange(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {

                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

                if (isCodecStarted) {
                    if (tempData == null || tempData.length != ffmpeg.get(FFmpegUtils.KEY_WIDTH) * ffmpeg.get(FFmpegUtils.KEY_HEIGHT)) {
                        tempData = new byte[ffmpeg.get(FFmpegUtils.KEY_WIDTH) * ffmpeg.get(FFmpegUtils.KEY_HEIGHT) * 3 / 2];
                    }
                    if (ffmpeg.output(tempData) == 0) {
                        filter.updateFrame(ffmpeg.get(FFmpegUtils.KEY_WIDTH), ffmpeg.get(FFmpegUtils.KEY_HEIGHT), tempData);
                        filter.draw();
                    }
                }
            }
        });


        Button videoPlay = findViewById(R.id.id_ffmpeg_mp4);
        videoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.requestRender();
            }
        });
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        Button videoChooser = findViewById(R.id.id_ffmpeg_mp4_chooser);
        videoChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCodecStarted = true;
                ffmpeg.start(1);
            }
        });
    }

    private void decodeAAC() {
        ffmpeg.start(0);
        sampleRate = ffmpeg.get(FFmpegUtils.KEY_SAMPLE_RATE);
        channelCount = ffmpeg.get(FFmpegUtils.KEY_CHANNEL_COUNT);
        audioFormat = ffmpeg.get(FFmpegUtils.KEY_AUDIO_FORMAT);
        frameSize = ffmpeg.get(FFmpegUtils.KEY_FRAME_SIZE);

        audioBufSize = AudioTrack.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT);
        tempData = new byte[frameSize * channelCount * 4];
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                audioBufSize,
                AudioTrack.MODE_STREAM);
        mAudioTrack.play();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isDestoryed) {
                    if (ffmpeg.output(tempData) != FFmpegUtils.EOF) {
                        mAudioTrack.write(byte2float(tempData), 0, tempData.length / 4, AudioTrack.WRITE_BLOCKING);

                    } else {
                        break;
                    }

                }
                ffmpeg.stop();
            }
        }).start();
    }

    private void playFloatPCM() {
        new Thread(new Runnable() {
            byte[] data1 = new byte[audioBufSize * 2];
            File file = new File("/mnt/sdcard/save.pcm");
            int off1 = 0;
            FileInputStream fileInputStream;

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    fileInputStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                while (!isDestoryed) {
                    try {
                        int len = fileInputStream.read(data1, 0, audioBufSize * 2);
                        if (len > 0) {
                            off1 += len;
                            mAudioTrack.write(byte2float(data1), 0, len / 4, AudioTrack.WRITE_BLOCKING);
                            Log.e("FFMPEG_LOG_", "ll->" + len);
                            if (file.length() <= off1) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }

                mAudioTrack.stop();
            }
        }).start();

    }

    public float[] byte2float(byte[] b) {
        if (tempFloatData == null || tempFloatData.length != b.length / 4) {
            tempFloatData = new float[b.length / 4];
        }
        for (int i = 0; i < b.length / 4; i++) {
            int l;
            l = b[i * 4];
            l &= 0xff;
            l |= ((long) b[i * 4 + 1] << 8);
            l &= 0xffff;
            l |= ((long) b[i * 4 + 2] << 16);
            l &= 0xffffff;
            l |= ((long) b[i * 4 + 3] << 24);
            tempFloatData[i] = Float.intBitsToFloat(l);
        }
        return tempFloatData;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestoryed = true;
    }
}
