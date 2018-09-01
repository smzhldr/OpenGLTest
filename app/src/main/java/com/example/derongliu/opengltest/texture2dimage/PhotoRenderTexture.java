package com.example.derongliu.opengltest.texture2dimage;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

import com.example.derongliu.opengltest.utils.MatrixHelper;
import com.example.derongliu.opengltest.utils.OpenGLHelper;
import com.example.derongliu.opengltest.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class PhotoRenderTexture implements Renderer {
    private Context context;
    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private int uMatrixLocation;
    private int aPositionLocation;
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";

    private int uTextureUnitLocation;
    private int aTextureCoordinatesLocation;
    protected static final String A_POSITION = "a_Position";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";



    private FloatBuffer vertexData;
    private static final int BYTE_PER_FLOAT = 4;

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTE_PER_FLOAT;
    private int textureId;


    public PhotoRenderTexture(Context context) {
        this.context = context;
        float[] tableVertices = {
                   0f,    0f, 0.5f,  0.5f,
                -0.5f, -0.8f,   0f,  0.9f,
                 0.5f, -0.8f,   1f,  0.9f,
                 0.5f,  0.8f,   1f,  0.1f,
                -0.5f,  0.8f,   0f,  0.1f,
                -0.5f, -0.8f,   0f,  0.9f
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
        String vertexStr = OpenGLHelper.readGlslFile(context, R.raw.vertex_texture_shader);
        String fragmentStr = OpenGLHelper.readGlslFile(context, R.raw.fragment_texture_shader);
        final int vertexId = OpenGLHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexStr);
        final int fragmentId = OpenGLHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentStr);
        final int programId = OpenGLHelper.createProgram(vertexId, fragmentId);
        OpenGLHelper.linkProgram(programId);
        GLES20.glUseProgram(programId);

        uMatrixLocation = glGetUniformLocation(programId, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(programId, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(programId, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(programId, A_TEXTURE_COORDINATES);

        textureId = OpenGLHelper.loadTexture(context, R.drawable.air_hockey_surface);


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0f, 0f, -2.5f);
        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);

        final float[] temp = new float[16];
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        // Set the active texture unit to texture unit 0.
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        glUniform1i(uTextureUnitLocation, 0);

        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        vertexData.position(POSITION_COMPONENT_COUNT);

        glVertexAttribPointer(aTextureCoordinatesLocation, TEXTURE_COORDINATES_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);

        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);




    }
}
