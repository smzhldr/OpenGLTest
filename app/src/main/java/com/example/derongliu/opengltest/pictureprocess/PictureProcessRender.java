package com.example.derongliu.opengltest.pictureprocess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.derongliu.opengltest.R;
import com.example.derongliu.opengltest.utils.OpenGLUtils;
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

public class PictureProcessRender implements GLSurfaceView.Renderer {

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 u_Matrix;" +
                    "attribute vec2 atextureCoordinate;" +

                    "varying vec2 aCoordinate;" +
                    "varying vec4 aPos;" +
                    "varying vec4 gPosition;" +

                    "void main() {" +
                    "  gl_Position = u_Matrix * vPosition;" +
                    "  aPos = vPosition;" +
                    "  aCoordinate = atextureCoordinate;" +
                    "  gPosition = u_Matrix * vPosition;" +

                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uTexture;" +

                    "uniform int vChangeType;" +
                    "uniform vec3 vChangeColor;" +
                    "uniform int vIsHalf;" +
                    "uniform float uXY;" +

                    "varying vec4 gPosition;" +
                    "varying vec2 aCoordinate;" +
                    "varying vec4 aPos;" +

                    "void modifyColor(vec4 color){" +
                    "color.r=max(min(color.r,1.0),0.0);" +
                    "color.g=max(min(color.g,1.0),0.0);" +
                    "color.b=max(min(color.b,1.0),0.0);" +
                    "color.a=max(min(color.a,1.0),0.0);" +
                    " }" +

                    "void main() {" +
                    "vec4 nColor = texture2D(uTexture,aCoordinate);" +
                    "if(aPos.x>0.0||vIsHalf==0){" +
                    "if(vChangeType==1){" +
                    "float c=nColor.r*vChangeColor.r+nColor.g*vChangeColor.g+nColor.b*vChangeColor.b;" +
                    "gl_FragColor=vec4(c,c,c,nColor.a);" +
                    "}" +
                    "else if(vChangeType==2){" +    //简单色彩处理，冷暖色调、增加亮度、降低亮度等
                    "vec4 deltaColor=nColor+vec4(vChangeColor,0.0);" +
                    "modifyColor(deltaColor);" +
                    "gl_FragColor=deltaColor;" +
                    "}" +
                    "else if(vChangeType==3){" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x-vChangeColor.r,aCoordinate.y-vChangeColor.r));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x-vChangeColor.r,aCoordinate.y+vChangeColor.r));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x+vChangeColor.r,aCoordinate.y-vChangeColor.r));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x+vChangeColor.r,aCoordinate.y+vChangeColor.r));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x-vChangeColor.g,aCoordinate.y-vChangeColor.g));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x-vChangeColor.g,aCoordinate.y+vChangeColor.g));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x+vChangeColor.g,aCoordinate.y-vChangeColor.g));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x+vChangeColor.g,aCoordinate.y+vChangeColor.g));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x-vChangeColor.b,aCoordinate.y-vChangeColor.b));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x-vChangeColor.b,aCoordinate.y+vChangeColor.b));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x+vChangeColor.b,aCoordinate.y-vChangeColor.b));" +
                    "nColor+=texture2D(uTexture,vec2(aCoordinate.x+vChangeColor.b,aCoordinate.y+vChangeColor.b));" +
                    "nColor/=13.0;" +
                    "gl_FragColor=nColor;" +
                    "}" +
                    "else if(vChangeType==4){" +  //放大镜效果
                    "float dis=distance(vec2(gPosition.x,gPosition.y/uXY),vec2(vChangeColor.r,vChangeColor.g));" +
                    "if(dis<vChangeColor.b){" +
                    "nColor=texture2D(uTexture,vec2(aCoordinate.x/2.0+0.25,aCoordinate.y/2.0+0.25));" +
                    "}" +
                    "gl_FragColor=nColor;" +
                    "}" +
                    "else{" +
                    "gl_FragColor=nColor;" +
                    "}" +
                    "}" +
                    "else{" +
                    "gl_FragColor=nColor;" +
                    "}" +
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

    private int mProgram;
    private int a_position;
    private int a_textCoordinate;
    private int u_Text;

    private int hChangeType;
    private int hChangeColor;
    private int hIsHalf;
    private int glHUxy;
    private float uXY;


    private int textureId;
    private int u_matrix;
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];


    public PictureProcessRender(Context context, GLSurfaceView glSurfaceView) {
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
        GLES20.glClearColor(1f, 1f, 1f, 1f);

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
        a_textCoordinate = GLES20.glGetAttribLocation(mProgram, "atextureCoordinate");
        u_matrix = glGetUniformLocation(mProgram, "u_Matrix");

        u_Text = GLES20.glGetUniformLocation(mProgram, "uTexture");
        hChangeType = GLES20.glGetUniformLocation(mProgram, "vChangeType");
        hChangeColor = GLES20.glGetUniformLocation(mProgram, "vChangeColor");
        hIsHalf = GLES20.glGetUniformLocation(mProgram, "vIsHalf");
        glHUxy = GLES20.glGetUniformLocation(mProgram, "uXY");


        textureId = OpenGLHelper.loadTexture(context, R.drawable.beauty3);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.beauty3, options);
        if (bitmap == null) {
            Log.d("BitmapProgramOpenGL", "failed");
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float sWH = w / (float) h;
        float sWidthHeight = width / (float) height;
        uXY = sWidthHeight;
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 7);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 7);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);


        float[] tempMatrix = new float[16];
        multiplyMM(tempMatrix, 0, mMVPMatrix, 0, OpenGLUtils.flip(OpenGLUtils.getOriginalMatrix(), false, true), 0);
        GLES20.glUniformMatrix4fv(u_matrix, 1, false, tempMatrix, 0);


        GLES20.glUniform1i(hChangeType, filter.getType());
        GLES20.glUniform3fv(hChangeColor, 1, filter.data(), 0);

        GLES20.glUniform1i(hIsHalf, isHalf ? 1 : 0);
        GLES20.glUniform1f(glHUxy, uXY);


        glUniform1i(u_Text, 0);
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        Texturebuffer.clear();
        Texturebuffer.put(textureCoord).position(0);
        glEnableVertexAttribArray(a_textCoordinate);
        GLES20.glVertexAttribPointer(a_textCoordinate, 2, GLES20.GL_FLOAT, false, 0, Texturebuffer);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);


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


}
