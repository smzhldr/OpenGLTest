package com.example.derongliu.opengltest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.example.derongliu.opengltest.utils.OpenGLHelper;
import com.example.derongliu.opengltest.utils.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;

public class TestActivity extends Activity {

    GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(glSurfaceView);

    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        static final String NO_FILTER_VERTEX_SHADER = "" +
                "attribute vec4 position;\n" +
                "attribute vec4 inputTextureCoordinate;\n" +
                "uniform mat4 u_Matrix;" +
                " \n" +
                "varying vec2 textureCoordinate;\n" +
                " \n" +
                "void main()\n" +
                "{\n" +
                "    gl_Position = u_Matrix*vec4(position.x,position.y,position.z,1.0f);\n" +
                "    textureCoordinate = inputTextureCoordinate.xy;\n" +
                "}";

        static final String NO_FILTER_FRAGMENT_SHADER = "" +
                "precision mediump float;"+
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
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
        };

        private int programId;
        private FloatBuffer cubeBuffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        private FloatBuffer textureBuffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        private int glAttrPosition;
        private int glAttrTextureCoordinate;
        private int glUniformTexture;
        private int textureId;
        private int glUniformMatrix;

        private float[] projectionMatrix = new float[16];

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            programId = OpenGLUtils.createGlProgram(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
            glAttrPosition = GLES20.glGetAttribLocation(programId, "position");
            glAttrTextureCoordinate = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");
            glUniformTexture = glGetUniformLocation(programId, "inputImageTexture");
            glUniformMatrix = glGetUniformLocation(programId, "u_Matrix");

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.beauty, options);
            textureId = OpenGLHelper.loadTexture(TestActivity.this, R.drawable.beauty3);
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
            GLES20.glUseProgram(programId);
            GLES20.glBindTexture(GL_TEXTURE_2D, textureId);

            GLES20.glUniform1i(glUniformTexture, 0);

            float[] tempMatrix = new float[16];
            multiplyMM(tempMatrix,0,projectionMatrix,0, projectionMatrix,0);
            GLES20.glUniformMatrix4fv(glUniformMatrix, 1, false, tempMatrix, 0);

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
}