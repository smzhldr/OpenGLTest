package com.example.derongliu.opengltest.mediaencode;

import android.graphics.SurfaceTexture;
import android.opengl.GLES10Ext;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.example.derongliu.opengltest.utils.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

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
    private int textureId;
    private float[] matrix;
    private int program;
    private int a_position;
    private int a_textCoordinate;
    private int u_Text;
    private int u_matrix;
    private FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(cube.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();


    private FloatBuffer textureBuffer = ByteBuffer.allocateDirect(textureCoord.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();


    private int[] mFrameBuffers;
    private int[] mFrameBufferTextures;
    private int mOutputWidth;
    private int mOutputHeight;

    public void init() {
        GLES20.glClearColor(1f, 1f, 1f, 1f);
        program = OpenGLUtils.createGlProgram(vertexShaderCode, fragmentShaderCode);
        a_position = GLES20.glGetAttribLocation(program, "vPosition");
        a_textCoordinate = GLES20.glGetAttribLocation(program, "textureCoordinate");
        u_matrix = glGetUniformLocation(program, "u_Matrix");
        u_Text = GLES20.glGetUniformLocation(program, "uTexture");
    }

    public int draw() {

        if (mFrameBuffers == null) {
            createFrameBuffers();
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        glUniform1i(u_Text, 0);

        GLES20.glUniformMatrix4fv(u_matrix, 1, false, matrix, 0);

        vertexBuffer.clear();
        vertexBuffer.put(cube).position(0);
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        textureBuffer.clear();
        textureBuffer.put(textureCoord).position(0);
        GLES20.glEnableVertexAttribArray(a_textCoordinate);
        GLES20.glVertexAttribPointer(a_textCoordinate, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(a_position);
        GLES20.glDisableVertexAttribArray(a_textCoordinate);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return mFrameBufferTextures[0];
       // return 0;
    }


    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

    private void createFrameBuffers() {
        if (mFrameBuffers == null && mFrameBufferTextures == null) {
            mFrameBuffers = new int[1];
            mFrameBufferTextures = new int[1];

            GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
            GLES20.glGenTextures(1, mFrameBufferTextures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mOutputWidth, mOutputHeight, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
    }

    public void setSize(int width, int height) {
        this.mOutputWidth = width;
        this.mOutputHeight = height;
    }
}
