package com.example.derongliu.mediacodec;

import android.content.Context;
import android.opengl.GLES20;

import com.example.derongliu.opengltest.utils.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glGetUniformLocation;

public class RecordFilter {

    private Context context;

    public RecordFilter(Context context) {
        this.context = context;
    }

    static final String NO_FILTER_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";

    static final String NO_FILTER_FRAGMENT_SHADER = "" +
            "precision mediump float;" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = vec4(texture2D(inputImageTexture, textureCoordinate).rgb, 1.0);\n" +
            "}";

    private final float cube[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };
    private float[] textureCords = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    private int programId;
    private FloatBuffer cubeBuffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private FloatBuffer textureBuffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private int glAttrPosition;
    private int glAttrTextureCoordinate;
    private int glUniformTexture;


    public void init() {
        programId = OpenGLUtils.createGlProgram(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
        glAttrPosition = GLES20.glGetAttribLocation(programId, "position");
        glAttrTextureCoordinate = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");
        glUniformTexture = glGetUniformLocation(programId, "inputImageTexture");
        //textureId = OpenGLHelper.loadTexture(context, R.drawable.beauty3);
    }

    public void draw(int textureId) {

        GLES20.glUseProgram(programId);

        GLES20.glBindTexture(GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(glUniformTexture, 0);


        cubeBuffer.clear();
        cubeBuffer.put(cube).position(0);
        GLES20.glEnableVertexAttribArray(glAttrPosition);
        GLES20.glVertexAttribPointer(glAttrPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);

        textureBuffer.clear();
        textureBuffer.put(textureCords).position(0);
        GLES20.glEnableVertexAttribArray(glAttrTextureCoordinate);
        GLES20.glVertexAttribPointer(glAttrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

        GLES20.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

}
