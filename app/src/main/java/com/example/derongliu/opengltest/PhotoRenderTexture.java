package com.example.derongliu.opengltest;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PhotoRenderTexture implements Renderer {
    private Context context;
    private FloatBuffer vertexData;
    private static final int BYTE_PER_FLOAT = 4;

    public PhotoRenderTexture(Context context) {
        this.context = context;
        float[] tableVertices = {
                0.0f, 0.0f, 0.0f, 1.5f, 1f, 1f, 1f,
                -0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f,
                0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f,
                0.5f, 0.8f, 0f, 2f, 0.7f, 0.7f, 0.7f,
                -0.5f, 0.8f, 0f, 2f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f,

                -0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f,
                0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f,

                0f, -0.25f, 0f, 1.25f, 0f, 0f, 1f,
                0f, 0.25f, 0f, 1.75f, 1f, 0f, 1f
        };
        vertexData = ByteBuffer
                .allocateDirect(tableVertices.length * BYTE_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(tableVertices);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
