package com.example.derongliu.opengltest.camera2;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.derongliu.opengltest.utils.OpenGLHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glViewport;

public class Camera2Render implements GLSurfaceView.Renderer {

    Context context;
    GLSurfaceView glSurfaceView;
    CameraDevice cameraDevice;

    private int mOESTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private float[] transformMatrix = new float[16];

    private int[] mFBOIds = new int[1];


    private final String vertexShaderCode =
            "attribute vec4 aPosition;\n" +
                    "uniform mat4 uTextureMatrix;\n" +
                    "attribute vec4 aTextureCoordinate;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main()\n" +
                    "{\n" +
                    "  vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;\n" +
                    "  gl_Position = aPosition;\n" +
                    "}";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES uTextureSampler;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main()\n" +
                    "{\n" +
                   /* "  vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);\n" +
                    "  float fGrayColor = (0.3*vCameraColor.r + 0.59*vCameraColor.g + 0.11*vCameraColor.b);\n" +
                    "  gl_FragColor = vec4(fGrayColor, fGrayColor, fGrayColor, 1.0);\n" +*/
                    "  gl_FragColor = texture2D(uTextureSampler, vTextureCoord);\n" +
                    "}\n";
    private FloatBuffer vertexBuffer, Texturebuffer;


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

    private int mProgram;
    private int a_position;
    private int a_textCoordinate;
    private int u_Text;

    private int u_matrix;

    private boolean bIsPreviewStarted;

    Camera2Render(Context context, GLSurfaceView glSurfaceView, CameraDevice cameraDevice) {
        this.context = context;
        this.glSurfaceView = glSurfaceView;
        this.cameraDevice = cameraDevice;

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
        mOESTextureId = createOESTexture();
        int vertexShader = OpenGLHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = OpenGLHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);

        a_position = GLES20.glGetAttribLocation(mProgram, "aPosition");
        a_textCoordinate = GLES20.glGetAttribLocation(mProgram, "aTextureCoordinate");
        u_matrix = glGetUniformLocation(mProgram, "uTextureMatrix");
        u_Text = GLES20.glGetUniformLocation(mProgram, "uTextureSampler");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);

    }


    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }

        if (!bIsPreviewStarted) {
            bIsPreviewStarted = initSurfaceTexture();
            return;
        }

        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        glUniform1i(u_Text, 0);

        GLES20.glUniformMatrix4fv(u_matrix, 1, false, transformMatrix, 0);

        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        Texturebuffer.clear();
        Texturebuffer.put(textureCoord).position(0);
        glEnableVertexAttribArray(a_textCoordinate);
        GLES20.glVertexAttribPointer(a_textCoordinate, 2, GLES20.GL_FLOAT, false, 0, Texturebuffer);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }


    private int createOESTexture() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }

    public boolean initSurfaceTexture() {
        if (glSurfaceView == null) {
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                glSurfaceView.requestRender();
            }
        });
        ((Camera2Activity) context).setPreviewTexture(mSurfaceTexture);
        ((Camera2Activity) context).startPreview();
        return true;
    }
}
