package com.example.derongliu.opengltest.mediaencode;

import android.Manifest;
import android.app.Activity;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.derongliu.opengltest.R;
import com.example.derongliu.opengltest.utils.OpenGLUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pub.devrel.easypermissions.EasyPermissions;

import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

public class MediaActivity extends Activity implements View.OnClickListener, GLSurfaceView.Renderer {

    GLSurfaceView glSurfaceView;
    Button recordButton;
    Camera camera;
    MediaCodec mediaCodec;
    MediaFormat mediaFormat;
    boolean isRecording;

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 u_Matrix;" +
                    "attribute vec2 textureCoordinate;" +
                    "varying vec2 aCoordinate;" +

                    "void main() {" +
                    "  gl_Position = u_Matrix * vPosition;" +
                    "  aCoordinate = textureCoordinate;" +
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


    float[] color = {
            1.0f, 1.0f, 1.0f, 1.0f
    };

    private int program;
    private int a_position;
    private int a_textCoordinate;
    private int u_Text;


    private int textureId;
    private int u_matrix;

    private int cameraId = 1;
    private float[] matrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        glSurfaceView = findViewById(R.id.record_sv);
        recordButton = findViewById(R.id.bt_record);
        recordButton.setOnClickListener(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        vertexBuffer = ByteBuffer.allocateDirect(cube.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(cube);
        vertexBuffer.position(0);

        TextureBuffer = ByteBuffer.allocateDirect(textureCoord.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        TextureBuffer.put(textureCoord);
        TextureBuffer.position(0);
    }

    @Override
    public void onClick(View v) {
        if (isRecording) {

        } else {

        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1f, 1f, 1f, 1f);
        program = OpenGLUtils.createGlProgram(vertexShaderCode, fragmentShaderCode);

        a_position = GLES20.glGetAttribLocation(program, "vPosition");
        a_textCoordinate = GLES20.glGetAttribLocation(program, "textureCoordinate");
        u_matrix = glGetUniformLocation(program, "u_Matrix");
        u_Text = GLES20.glGetUniformLocation(program, "uTexture");

        try {
            camera = Camera.open(cameraId);
            textureId = createCameraTexture();
            surfaceTexture = new SurfaceTexture(textureId);
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        OpenGLUtils.getShowMatrix(matrix, size.height, size.width, width, height);
        if (cameraId == 1) {

            OpenGLUtils.rotate(matrix, 90);
        } else {
            OpenGLUtils.flip(matrix, true, false);
            OpenGLUtils.rotate(matrix, 270);
        }
        this.matrix = matrix;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glUseProgram(program);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        GLES20.glUniformMatrix4fv(u_matrix, 1, false, matrix, 0);

        glUniform1i(u_Text, 0);
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        TextureBuffer.clear();
        TextureBuffer.put(textureCoord).position(0);
        glEnableVertexAttribArray(a_textCoordinate);
        GLES20.glVertexAttribPointer(a_textCoordinate, 2, GLES20.GL_FLOAT, false, 0, TextureBuffer);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        surfaceTexture.updateTexImage();
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
}
