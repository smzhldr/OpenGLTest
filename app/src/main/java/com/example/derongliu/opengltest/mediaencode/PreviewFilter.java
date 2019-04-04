package com.example.derongliu.opengltest.mediaencode;

import android.graphics.SurfaceTexture;

import java.nio.FloatBuffer;

public class PreviewFilter {

    private static final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 u_Matrix;" +
                    "attribute vec2 textureCoordinate;" +
                    "varying vec2 aCoordinate;" +

                    "void main() {" +
                    "  gl_Position = u_Matrix * vPosition;" +
                    "  aCoordinate = textureCoordinate;" +
                    "}";

    private static final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES uTexture;" +

                    "varying vec2 aCoordinate;" +

                    "void main() {" +
                    "vec4 nColor = texture2D(uTexture,aCoordinate);" +
                    "gl_FragColor=nColor;" +

                    "}";

    private FloatBuffer vertexBuffer, TextureBuffer;
    private SurfaceTexture surfaceTexture;


    float[] cube = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,

    };
    float[] textureCoord = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,

    };


    public void init(){

    }
}
