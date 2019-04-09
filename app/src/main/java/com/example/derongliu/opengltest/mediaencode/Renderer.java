package com.example.derongliu.opengltest.mediaencode;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.derongliu.opengltest.utils.OpenGLUtils;

import java.io.IOException;
import java.util.LinkedList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class Renderer implements GLSurfaceView.Renderer {

    private Context context;

    private int cameraId = 1;
    private SurfaceTexture surfaceTexture;
    private Camera camera;
    private GLSurfaceView glSurfaceView;
    private PreviewFilter previewFilter;
    private RecordFilter recordFilter;


    private WindowSurface windowSurface;
    private EglCore eglCore;
    private MediaVideoEncoder mVideoEncoder;
    private MediaMuxerWrapper mMuxer;
    private MediaAudioEncoder mAudioEncoder;
    private int outputWidth;
    private int outputHeight;

    private volatile boolean isRecording;

    private LinkedList<Runnable> runOnDraw;


    public Renderer(Context context, GLSurfaceView glSurfaceView) {
        this.context = context;
        this.glSurfaceView = glSurfaceView;
        previewFilter = new PreviewFilter();
        recordFilter = new RecordFilter(context);

        runOnDraw = new LinkedList<>();

        camera = Camera.open(cameraId);
        int textureId = createCameraTexture();
        surfaceTexture = new SurfaceTexture(textureId);

        previewFilter.setTextureId(textureId);

        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                Renderer.this.glSurfaceView.requestRender();
            }
        });
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        previewFilter.init();
        recordFilter.init();
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float[] matrix = new float[16];
        Camera.Size size = camera.getParameters().getPreviewSize();

        OpenGLUtils.getShowMatrix(matrix, size.height, size.width, width, height);
        if (cameraId == 1) {
            OpenGLUtils.rotate(matrix, 90);
        } else {
            OpenGLUtils.flip(matrix, true, false);
            OpenGLUtils.rotate(matrix, 270);
        }
        previewFilter.setMatrix(matrix);
        previewFilter.setSize(width, height);
        outputWidth = size.height;
        outputHeight = size.width;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        surfaceTexture.updateTexImage();
        int textureId = previewFilter.draw();
        recordFilter.draw(textureId);
        runOnDrawFrame();
        if (isRecording) {

            EGL10 mEGL = (EGL10) EGLContext.getEGL();
            EGLDisplay mEGLDisplay = mEGL.eglGetCurrentDisplay();
            EGLContext mEGLContext = mEGL.eglGetCurrentContext();
            EGLSurface mEGLScreenSurface = mEGL.eglGetCurrentSurface(EGL10.EGL_DRAW);
            // create encoder surface
            if (windowSurface == null) {
                eglCore = new EglCore(EGL14.eglGetCurrentContext(), EglCore.FLAG_RECORDABLE);
                windowSurface = new WindowSurface(eglCore, mVideoEncoder.getSurface(), false);
            }

            // Draw on encoder surface
            windowSurface.makeCurrent();
            recordFilter.draw(textureId);

            if (isRecording && windowSurface != null) {
                windowSurface.swapBuffers();
                mVideoEncoder.frameAvailableSoon();
            }

            // Make screen surface be current surface
            mEGL.eglMakeCurrent(mEGLDisplay, mEGLScreenSurface, mEGLScreenSurface, mEGLContext);
        }
    }


    public void onResume() {
        try {
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onPause() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
    }


    public void startRecording(final String outputPath, final MediaEncoder.MediaEncoderListener encoderListener) {
        if (isRecording) {
            return;
        }
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                try {
                    mMuxer = new MediaMuxerWrapper(outputPath);
                    // for video capturing
                    mVideoEncoder = new MediaVideoEncoder(mMuxer, encoderListener, outputWidth, outputHeight);
                    // for audio capturing
                    if (hasAudioPermission()) {
                        mAudioEncoder = new MediaAudioEncoder(mMuxer, encoderListener);
                    }

                    mMuxer.prepare();
                    mMuxer.startRecording();

                    isRecording = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stopRecording() {
        if (!isRecording) {
            return;
        }
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                mMuxer.stopRecording();
                if (windowSurface != null) {
                    windowSurface.release();
                    windowSurface = null;
                }
                isRecording = false;

                if (eglCore != null) {
                    EGL10 mEGL = (EGL10) EGLContext.getEGL();
                    EGLDisplay mEGLDisplay = mEGL.eglGetCurrentDisplay();
                    EGLContext mEGLContext = mEGL.eglGetCurrentContext();
                    EGLSurface mEGLScreenSurface = mEGL.eglGetCurrentSurface(EGL10.EGL_DRAW);
                    eglCore.makeNothingCurrent();
                    eglCore.release();
                    eglCore = null;
                    // Make screen surface be current surface
                    mEGL.eglMakeCurrent(mEGLDisplay, mEGLScreenSurface, mEGLScreenSurface, mEGLContext);
                }
            }
        });
    }


    private int createCameraTexture() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    private boolean hasAudioPermission() {
        try {
            int res = context.checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO);
            return res == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void runOnDraw(final Runnable runnable) {
        synchronized (runOnDraw) {
            runOnDraw.addLast(runnable);
        }
    }

    private void runOnDrawFrame() {
        synchronized (runOnDraw) {
            while (!runOnDraw.isEmpty()) {
                runOnDraw.removeFirst().run();
            }
        }
    }
}
