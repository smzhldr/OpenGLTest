package com.example.derongliu.opengltest.triangle;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.derongliu.opengltest.utils.OpenGLHelper;
import com.example.derongliu.opengltest.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.Matrix.orthoM;

public class TriangleRender implements GLSurfaceView.Renderer {

    private Context context;
    private static final int POSITION_COMPONENT_COUNT = 4;
    private static final int BYTE_PER_FLOAT = 4;
    private static final int COLOR_COMPNENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPNENT_COUNT) * BYTE_PER_FLOAT;
    private FloatBuffer vertexData;
    private static final String A_COLOR = "a_Color";
    private int aColorLocation;
    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;
    private static final String U_MATRIX = "u_Matrix";
    private final float[] projectionMatrix = new float[16];
    private int uMatrixPosition;

    public TriangleRender(Context context) {
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
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        String vertexStr = OpenGLHelper.readGlslFile(context, R.raw.vertex_shader);
        String fragmentStr = OpenGLHelper.readGlslFile(context, R.raw.fragment_shader);
        final int vertexId = OpenGLHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexStr);
        final int fragmentId = OpenGLHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentStr);
        final int programId = OpenGLHelper.createProgram(vertexId, fragmentId);
        OpenGLHelper.linkProgram(programId);
        GLES20.glUseProgram(programId);
        aColorLocation = GLES20.glGetAttribLocation(programId, A_COLOR);
        aPositionLocation = GLES20.glGetAttribLocation(programId, A_POSITION);

        vertexData.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT, false, STRIDE, vertexData);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        vertexData.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aColorLocation, COLOR_COMPNENT_COUNT, GLES20.GL_FLOAT, false, STRIDE, vertexData);
        GLES20.glEnableVertexAttribArray(aColorLocation);

        uMatrixPosition = GLES20.glGetUniformLocation(programId, U_MATRIX);


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        final float aspectRatio = width > height ? (float) width / (float) height : (float) height / (float) width;
        if (width > height) {
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);

        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);

        GLES20.glUniformMatrix4fv(uMatrixPosition, 1, false, projectionMatrix, 0);
    }
}
