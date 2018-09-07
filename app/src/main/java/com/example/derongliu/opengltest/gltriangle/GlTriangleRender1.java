package com.example.derongliu.opengltest.gltriangle;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.derongliu.opengltest.R;
import com.example.derongliu.opengltest.utils.OpenGLHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlTriangleRender1 implements GLSurfaceView.Renderer {

    private Context context;
    private FloatBuffer buffer;
    float[] triangleCoods={
      0.5f,0.5f,0.0f,
      -0.5f,-0.5f,0f,
      0.5f,-0.5f,0f
    };
    float[] color={
      1.0f,1.0f,1.0f,1.0f
    };

    public GlTriangleRender1(Context context){
        this.context = context;
        buffer= ByteBuffer.allocateDirect(triangleCoods.length*4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(triangleCoods);
        buffer.position(0);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
       GLES20.glClearColor(0.5f,0.5f,0.5f,1f);
       String vertexStr= OpenGLHelper.readGlslFile(context, R.raw.gltriangle1_vertex);
       String fragmentStr=OpenGLHelper.readGlslFile(context,R.raw.gltriangle_fragment);
       int vertexId=OpenGLHelper.compileShader(GLES20.GL_VERTEX_SHADER,vertexStr);
       int fragmentId=OpenGLHelper.compileShader(GLES20.GL_FRAGMENT_SHADER,fragmentStr);
       int programId=OpenGLHelper.createProgram(vertexId,fragmentId);
       OpenGLHelper.linkProgram(programId);
       GLES20.glUseProgram(programId);

       int position =GLES20.glGetAttribLocation(programId,"v_position");
       int v_color=GLES20.glGetUniformLocation(programId,"v_Color");

       GLES20.glEnableVertexAttribArray(position);
       GLES20.glVertexAttribPointer(position,3,GLES20.GL_FLOAT,false,0,buffer);


       GLES20.glUniform4fv(v_color,1,color,0);





    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,3);

    }
}
