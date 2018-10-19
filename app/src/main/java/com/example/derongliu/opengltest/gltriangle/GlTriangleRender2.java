package com.example.derongliu.opengltest.gltriangle;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.derongliu.opengltest.R;
import com.example.derongliu.opengltest.utils.OpenGLHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlTriangleRender2 implements GLSurfaceView.Renderer {
    private Context context;
    private FloatBuffer buffer;
    private FloatBuffer colorBuffer;
    private float[] viewMatrix = new float[16];
    private float[] projectMatrix = new float[16];
    private float[] mvMatrx = new float[16];
    private int programId;


    float[] triangleCoods = {
            0.5f, 0f, 0f,
            0.5f, 1f, 0.0f,
            -0.5f, 0f, 0f,
            -0.5f, -1f, 0f

    };
    float[] triangle_color = {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1f, 1f, 1f, 1f
    };


    public GlTriangleRender2(Context context) {
        this.context = context;
        buffer = ByteBuffer.allocateDirect(triangleCoods.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        buffer.put(triangleCoods);
        buffer.position(0);


        colorBuffer = ByteBuffer.allocateDirect(triangle_color.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        colorBuffer.put(triangle_color);
        colorBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        String vertex_str = OpenGLHelper.readGlslFile(context, R.raw.gltriangle_vertex2);
        String fragment_str = OpenGLHelper.readGlslFile(context, R.raw.gltriangle_fragment2);
        int vertex_id = OpenGLHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertex_str);
        int fragment_id = OpenGLHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragment_str);
        programId = OpenGLHelper.createProgram(vertex_id, fragment_id);
        OpenGLHelper.linkProgram(programId);
        GLES20.glUseProgram(programId);


        int position = GLES20.glGetAttribLocation(programId, "a_position");
        int color = GLES20.glGetAttribLocation(programId, "a_color");

        GLES20.glEnableVertexAttribArray(position);
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 0, buffer);

        GLES20.glEnableVertexAttribArray(color);
        GLES20.glVertexAttribPointer(color, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        //GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(mvMatrx, 0, projectMatrix, 0, viewMatrix, 0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        int matrix = GLES20.glGetUniformLocation(programId, "v_matrix");
        GLES20.glUniformMatrix4fv(matrix, 1, false, mvMatrx, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);



        GLES20.glUniformMatrix4fv(matrix,1,false,mvMatrx,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,4);
    }
}
