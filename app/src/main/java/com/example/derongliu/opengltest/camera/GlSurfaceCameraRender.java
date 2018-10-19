package com.example.derongliu.opengltest.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.derongliu.opengltest.R;
import com.example.derongliu.opengltest.utils.Gl2Utils;
import com.example.derongliu.opengltest.utils.OpenGLHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.Matrix.multiplyMM;

public class GlSurfaceCameraRender implements GLSurfaceView.Renderer {

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 u_Matrix;" +
                    "attribute vec2 atextureCoordinate;" +
                    "varying vec2 aCoordinate;" +

                    "void main() {" +
                    "  gl_Position = u_Matrix * vPosition;" +
                    "  aCoordinate = atextureCoordinate;" +
                    "}";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES uTexture;" +

                    "varying vec2 aCoordinate;" +

                    "void main() {" +
                    "vec4 nColor = texture2D(uTexture,aCoordinate);" +

                    "gl_FragColor=nColor;" +

                    "}";
    private Activity activity;
    private FloatBuffer vertexBuffer, Texturebuffer;
    private CameraGLSurfaceView glSurfaceView;


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


    float[] color = {
            1.0f, 1.0f, 1.0f, 1.0f
    };

    private int mProgram;
    private int a_position;
    private int a_textCoordinate;
    private int u_Text;


    private int textureId;
    private int u_matrix;


    private float[] matrix;

    private Camera camera;

    private int cameraId;
    private SurfaceTexture surfaceTexture;


    public GlSurfaceCameraRender(Activity activity, CameraGLSurfaceView glSurfaceView) {
        this.activity = activity;
        this.glSurfaceView = glSurfaceView;
        vertexBuffer = ByteBuffer.allocateDirect(cube.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(cube);
        vertexBuffer.position(0);

        Texturebuffer = ByteBuffer.allocateDirect(textureCoord.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        Texturebuffer.put(textureCoord);
        Texturebuffer.position(0);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1f, 1f, 1f, 1f);

        int vertexShader = OpenGLHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = OpenGLHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);

        a_position = GLES20.glGetAttribLocation(mProgram, "vPosition");
        a_textCoordinate = GLES20.glGetAttribLocation(mProgram, "atextureCoordinate");
        u_matrix = glGetUniformLocation(mProgram, "u_Matrix");
        u_Text = GLES20.glGetUniformLocation(mProgram, "uTexture");

        openCamera();
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                glSurfaceView.requestRender();
            }
        });

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float[] matrix = new float[16];
        Camera.Size size = camera.getParameters().getPreviewSize();

        Gl2Utils.getShowMatrix(matrix, size.height, size.width, width, height);
        if (cameraId == 1) {

            Gl2Utils.rotate(matrix, 90);
        } else {
            Gl2Utils.flip(matrix, true, false);
            Gl2Utils.rotate(matrix, 270);
        }
        setMatrix(matrix);

    }

    @Override
    public void onDrawFrame(GL10 gl) {


        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        GLES20.glUniformMatrix4fv(u_matrix, 1, false, matrix, 0);


        glUniform1i(u_Text, 0);
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        Texturebuffer.clear();
        Texturebuffer.put(textureCoord).position(0);
        glEnableVertexAttribArray(a_textCoordinate);
        GLES20.glVertexAttribPointer(a_textCoordinate, 2, GLES20.GL_FLOAT, false, 0, Texturebuffer);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        surfaceTexture.updateTexImage();


    }

    public enum Filter {

        NONE(0, new float[]{0.0f, 0.0f, 0.0f}),
        GRAY(1, new float[]{0.299f, 0.587f, 0.114f}),
        COOL(2, new float[]{0.0f, 0.0f, 0.1f}),
        WARM(2, new float[]{0.1f, 0.1f, 0.0f}),
        BLUR(3, new float[]{0.006f, 0.004f, 0.002f}),
        MAGN(4, new float[]{0.0f, 0.0f, 0.4f});


        private int vChangeType;
        private float[] data;

        Filter(int vChangeType, float[] data) {
            this.vChangeType = vChangeType;
            this.data = data;
        }

        public int getType() {
            return vChangeType;
        }

        public float[] data() {
            return data;
        }

    }


    private Filter filter = Filter.NONE;

    private boolean isHalf = false;


    public void sethsHalf(boolean isHalf) {
        this.isHalf = isHalf;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }


    private void openCamera() {
        try {
            camera = Camera.open(cameraId);
            textureId = createCameraTexture();
            surfaceTexture = new SurfaceTexture(textureId);

            Camera.Parameters parameters = camera.getParameters();
            Rect focusRect = new Rect(500,500,700,700);
            if (parameters.getMaxNumFocusAreas() > 0) {
                final List<Camera.Area> focusAreas = new ArrayList<>(1);
                focusAreas.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(focusAreas);
            }

            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(parameters);

            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();

        } catch (RuntimeException | Error e) {

        } catch (IOException e) {
            e.printStackTrace();
        }



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

    private void closeCamera() {
        camera.stopPreview();
        camera.release();
    }


    private void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

    public void switchCamera() {
        cameraId = (cameraId + 1) % 2;
        closeCamera();
        glSurfaceView.onPause();
        glSurfaceView.onResume();
    }


}
