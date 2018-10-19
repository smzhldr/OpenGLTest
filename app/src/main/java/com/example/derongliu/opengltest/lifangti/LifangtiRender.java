package com.example.derongliu.opengltest.lifangti;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.derongliu.opengltest.R;
import com.example.derongliu.opengltest.utils.OpenGLHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LifangtiRender implements GLSurfaceView.Renderer {

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;" +
                    "varying  vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {" +
                    "  gl_Position = vMatrix*vPosition;" +
                    "  vColor=aColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";


    private Context context;
    private int programId;
    private float[] cubePosition = {
            -1f, 1f, 1f,
            1f, 1f, 1f,
            1f, -1f, 1f,
            -1f, -1f, 1f,
            -1f, 1f, -1f,
            1f, 1f, -1f,
            1f, -1f, -1f,
            -1f, -1f, -1f,

    };

    private short[] index = {
            4, 5, 6, 4, 6, 7,   //far
            0, 1, 2, 0, 2, 3,   //near
            0, 3, 7, 0, 4, 7,   //left
            1, 2, 6, 1, 5, 6,   //right
            0, 1, 4, 1, 4, 5,   //top
            3, 2, 7, 2, 6, 7    //bottom


    };

    private float[] color = {
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
    };

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];


    private float[] tempMatrix = new float[16];

    private FloatBuffer vertexBuffer, colorBuffer;
    private ShortBuffer indexBuffer;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMatrixHandler;

    public LifangtiRender(Context context) {
        this.context = context;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                cubePosition.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubePosition);
        vertexBuffer.position(0);

        ByteBuffer dd = ByteBuffer.allocateDirect(
                color.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        ByteBuffer cc = ByteBuffer.allocateDirect(index.length * 2);
        cc.order(ByteOrder.nativeOrder());
        indexBuffer = cc.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);

    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);

        int vertexId = OpenGLHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentId = OpenGLHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        programId = OpenGLHelper.createProgram(vertexId, fragmentId);
        OpenGLHelper.linkProgram(programId);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //计算宽高比
        float ratio = (float) width / height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 5.0f, 5.0f, 10.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        Matrix.scaleM(mMVPMatrix,0,0.5f,0.5f,0.5f);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(programId);

        mMatrixHandler = GLES20.glGetUniformLocation(programId, "vMatrix");
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        mPositionHandle = GLES20.glGetAttribLocation(programId, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        mColorHandle = GLES20.glGetAttribLocation(programId, "aColor");
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);


        Matrix.setIdentityM(tempMatrix,0);
        Matrix.translateM(tempMatrix, 0, 3f, 3f, 0);

        float[] translateMatrix = new float[16];
        Matrix.multiplyMM(translateMatrix,0,mMVPMatrix,0,tempMatrix,0);
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, translateMatrix, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(tempMatrix,0);
        Matrix.translateM(tempMatrix, 0, -3f, -3f, 0);
        Matrix.scaleM(tempMatrix,0,0.5f,1f,1f);
        Matrix.multiplyMM(scaleMatrix,0,mMVPMatrix,0,tempMatrix,0);
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, scaleMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);


        float[] rotateMatrix = new float[16];
        Matrix.setIdentityM(tempMatrix,0);
        Matrix.translateM(tempMatrix, 0, 3f, -3f, 0);
        Matrix.scaleM(tempMatrix,0,0.5f,1f,1f);
        Matrix.rotateM(tempMatrix,0,30f,1f,0f,0f);
        Matrix.multiplyMM(rotateMatrix,0,mMVPMatrix,0,tempMatrix,0);
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, rotateMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);


        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
