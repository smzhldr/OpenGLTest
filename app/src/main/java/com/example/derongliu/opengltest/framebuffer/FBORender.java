package com.example.derongliu.opengltest.framebuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.derongliu.opengltest.R;
import com.example.derongliu.opengltest.utils.Gl2Utils;
import com.example.derongliu.opengltest.utils.OpenGLHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;

public class FBORender implements GLSurfaceView.Renderer {


    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 u_Matrix;" +
                    "attribute vec2 atextureCoordinate;" +
                    "varying vec2 vtextureCoordinate;" +
                    "void main() {" +
                    "  vtextureCoordinate = atextureCoordinate;" +
                    "  gl_Position = u_Matrix*vec4(vPosition.x,vPosition.y,vPosition.z,1.0f);" +

                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uTexture;" +
                    "varying vec2 vtextureCoordinate;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = (texture2D(uTexture,vtextureCoordinate)*vColor);" +
                    "}";
    private Context context;
    private FloatBuffer vertexBuffer, Texturebuffer;
    private GLSurfaceView glSurfaceView;


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


    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] ftexture = new int[1];

    private int a_position;
    private int v_color;
    private int mProgram;
    private int a_textCoordinate;
    private int u_Text;
    private int textureId;
    private int u_matrix;
    private float[] projectionMatrix = new float[16];


    public FBORender(Context context, GLSurfaceView glSurfaceView) {
        this.context = context;
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
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);

        int vertexShader = OpenGLHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = OpenGLHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);

        GLES20.glUseProgram(mProgram);
        a_position = GLES20.glGetAttribLocation(mProgram, "vPosition");
        v_color = GLES20.glGetUniformLocation(mProgram, "vColor");
        a_textCoordinate = GLES20.glGetAttribLocation(mProgram, "atextureCoordinate");
        u_Text = GLES20.glGetUniformLocation(mProgram, "uTexture");
        u_matrix = glGetUniformLocation(mProgram, "u_Matrix");

        textureId = OpenGLHelper.loadTexture(context, R.drawable.beauty);
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

        //frustumM(projectionMatrix,0, -aspectRatio, aspectRatio,1, -1,0.1f, 1f);


    }

    @Override
    public void onDrawFrame(GL10 gl) {


       /* GLES20.glGenFramebuffers(1, fFrame, 0);
        GLES20.glGenRenderbuffers(1, fRender, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, glSurfaceView.getWidth(), glSurfaceView.getHeight());
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, fRender[0]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, fRender[0]);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.d("缓冲区不完整", String.valueOf(GLES20.glGetError()));
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);*/



        /*GLES20.glEnable(GLES20.GL_STENCIL_TEST);
        GLES20.glStencilMask(0xFF);
        GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT);
        GLES20.glStencilFunc(GLES20.GL_ALWAYS, 1, 0xFF);
        GLES20.glStencilOp(GLES20.GL_REPLACE, GLES20.GL_REPLACE, GLES20.GL_REPLACE);*/


        GLES20.glGenFramebuffers(1, fFrame, 0);

        GLES20.glGenTextures(1, ftexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ftexture[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, glSurfaceView.getWidth(), glSurfaceView.getHeight(), 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, ftexture[0], 0);

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);


        //multiplyMM(tempMatrix,0,projectionMatrix,0,Gl2Utils.flip(Gl2Utils.getOriginalMatrix(),false,true),0);

        GLES20.glUniformMatrix4fv(u_matrix, 1, false, projectionMatrix, 0);
        glUniform1i(u_Text, 0);
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glUniform4fv(v_color, 1, color, 0);
        glEnableVertexAttribArray(a_textCoordinate);
        GLES20.glVertexAttribPointer(a_textCoordinate, 2, GLES20.GL_FLOAT, false, 0, Texturebuffer);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, ftexture[0]);

        float[] tempMatrix = new float[16];
        multiplyMM(tempMatrix,0,projectionMatrix,0,Gl2Utils.flip(Gl2Utils.getOriginalMatrix(),false,true),0);
        GLES20.glUniformMatrix4fv(u_matrix, 1, false, tempMatrix, 0);
        glUniform1i(u_Text, 0);
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glUniform4fv(v_color, 1, color, 0);

        Texturebuffer.clear();
        Texturebuffer.put(textureCoord).position(0);
        glEnableVertexAttribArray(a_textCoordinate);
        GLES20.glVertexAttribPointer(a_textCoordinate, 2, GLES20.GL_FLOAT, false, 0, Texturebuffer);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);



        /*GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, 0xFF);
        GLES20.glStencilMask(0x00);*/


        deleteEnvi();

    }


    private void deleteEnvi() {
        //GLES20.glDeleteRenderbuffers(1, fRender, 0);
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
    }

}
