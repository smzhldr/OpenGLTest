package com.example.derongliu.opengltest.drsurfaceview;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.derongliu.opengltest.utils.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CustomGlSurfaceActivity extends Activity {
    private GLRenderer glRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DRSurfaceView drSurfaceView = new DRSurfaceView(this);
        setContentView(drSurfaceView);

        drSurfaceView.setRenderer(new DRSurfaceView.Renderer() {

            // 顶点着色器的脚本
            private static final String verticesShader
                    = "attribute vec2 vPosition;            \n" // 顶点位置属性vPosition
                    + "void main(){                         \n"
                    + "   gl_Position = vec4(vPosition,0,1);\n" // 确定顶点位置
                    + "}";

            // 片元着色器的脚本
            private static final String fragmentShader
                    = "precision mediump float;         \n" // 声明float类型的精度为中等(精度越高越耗资源)
                    + "uniform vec4 uColor;             \n" // uniform的属性uColor
                    + "void main(){                     \n"
                    + "   gl_FragColor = uColor;        \n" // 给此片元的填充色
                    + "}";

            float[] cube = {
                    0.0f, 0.5f,
                    -0.5f, -0.5f,
                    0.5f, -0.5f
            };

            int programId;
            int vPosition;
            int uColor;
            FloatBuffer buffer = ByteBuffer.allocateDirect(cube.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();

            @Override
            public void onSurfaceCreated() {
                programId = OpenGLUtils.createGlProgram(verticesShader, fragmentShader);
                vPosition = GLES20.glGetAttribLocation(programId, "vPosition");
                uColor = GLES20.glGetUniformLocation(programId, "uColor");
            }

            @Override
            public void onSurfaceChanged(int width, int height) {
                GLES20.glViewport(0, 0, width, height);
            }

            @Override
            public void onDraw() {
                GLES20.glClearColor(0f, 0f, 0f, 1f);
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

                GLES20.glUseProgram(programId);

                buffer.clear();
                buffer.put(cube);
                buffer.position(0);

                GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, buffer);
                // 允许顶点位置数据数组
                GLES20.glEnableVertexAttribArray(vPosition);
                // 设置属性uColor(颜色 索引,R,G,B,A)
                GLES20.glUniform4f(uColor, 0.0f, 1.0f, 0.0f, 1.0f);
                // 绘制
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

                GLES20.glDisableVertexAttribArray(vPosition);
            }
        });


   /* @Override
    protected void onDestroy() {
        glRenderer.release();
        glRenderer = null;
        super.onDestroy();
    }*/

    }
}
