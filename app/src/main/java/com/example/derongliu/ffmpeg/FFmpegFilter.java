package com.example.derongliu.ffmpeg;

import android.opengl.GLES20;

import com.example.derongliu.opengltest.utils.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class FFmpegFilter {

    private static final String vertex = "attribute vec4 vPosition;\n" +
            "attribute vec2 vCoord;\n" +
            "uniform mat4 vMatrix;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main(){\n" +
            "    gl_Position = vMatrix*vPosition;\n" +
            "    textureCoordinate = vCoord;\n" +
            "}";


    private static final String fragment = "precision mediump float;\n" +
            "uniform sampler2D texY,texU,texV;\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main(){                           \n" +
            "  vec4 color = vec4((texture2D(texY, textureCoordinate).r - 16./255.) * 1.164);\n" +
            "  vec4 U = vec4(texture2D(texU, textureCoordinate).r - 128./255.);\n" +
            "  vec4 V = vec4(texture2D(texV, textureCoordinate).r - 128./255.);\n" +
            "  color += U * vec4(0, -0.392, 2.017, 0);\n" +
            "  color += V * vec4(1.596, -0.813, 0, 0);\n" +
            "  color.a = 1.0;\n" +
            "  gl_FragColor = color;\n" +
            "}";

    private int aPosition;
    private int aCoordnation;
    private int uMatrix;
    private int uTextures[] = new int[3];
    private int programId;

    private ByteBuffer y, u, v;
    private int[] textures = new int[3];


    //顶点坐标
    private float pos[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,
    };


    //纹理坐标
    private float[] coord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    public static final float[] OM = OpenGLUtils.getOriginalMatrix();
    private float[] matrix;

    private int width;
    private int height;

    private FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private FloatBuffer coordBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();

    public void init() {
        programId = OpenGLUtils.createGlProgram(vertex, fragment);
        aPosition = GLES20.glGetAttribLocation(programId, "vPosition");
        aCoordnation = GLES20.glGetAttribLocation(programId, "vCoord");
        uMatrix = GLES20.glGetUniformLocation(programId, "vMatrix");
        uTextures[0] = GLES20.glGetUniformLocation(programId, "texY");
        uTextures[1] = GLES20.glGetUniformLocation(programId, "texU");
        uTextures[2] = GLES20.glGetUniformLocation(programId, "texV");

        createTexture();

        vertexBuffer.put(pos).position(0);
        coordBuffer.put(coord).position(0);
    }

    public void draw() {
        GLES20.glUseProgram(programId);

        onBindTexture();

        GLES20.glUniformMatrix4fv(uMatrix, 1, false, matrix, 0);

        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aCoordnation);
        GLES20.glVertexAttribPointer(aCoordnation, 2, GLES20.GL_FLOAT, false, 0, coordBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(aPosition);
        GLES20.glDisableVertexAttribArray(aCoordnation);
    }

    private void onBindTexture() {
        for (int i = 0; i < 3; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            GLES20.glUniform1i(uTextures[i], i);
        }
    }

    public void updateFrame(int width, int height, byte[] data) {
        if (y == null) {
            y = ByteBuffer.allocate(width * height);
            u = ByteBuffer.allocate(width * height >> 2);
            v = ByteBuffer.allocate(width * height >> 2);
        }

        if (matrix == null) {
            matrix = Arrays.copyOf(OM, 16);
            OpenGLUtils.getShowMatrix(matrix, width, height, this.width, this.height);
        }
        y.clear();
        y.put(data, 0, width * height);

        u.clear();
        u.put(data, width * height, width * height >> 2);

        v.clear();
        v.put(data, width * height + (width * height >> 2), width * height >> 2);


        y.position(0);
        u.position(0);
        v.position(0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width, height, 0, GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, y);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width >> 1, height >> 1, 0, GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, u);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width >> 1, height >> 1, 0, GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, v);
    }

    private void createTexture() {
        GLES20.glGenTextures(3, textures, 0);
        for (int i = 0; i < 3; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
    }

    public void onSizeChange(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
