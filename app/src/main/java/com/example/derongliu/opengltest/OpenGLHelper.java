package com.example.derongliu.opengltest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.service.autofill.LuhnChecksumValidator;
import android.util.Log;
import android.view.VelocityTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glValidateProgram;
import static android.opengl.GLUtils.texImage2D;

public class OpenGLHelper {
    public static String readGlslFile(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static int compileShader(int type, String shaderCode) {
        final int shaderObject = glCreateShader(type);
        if (shaderObject == 0) {
            return 0;
        }
        glShaderSource(shaderObject, shaderCode);
        glCompileShader(shaderObject);
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObject, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            glDeleteShader(shaderObject);
            Log.d("compileProgramOpenGL", "failed");
            return 0;
        }
        return shaderObject;
    }

    public static int createProgram(int vertexId, int fragmentId) {
        final int programId = glCreateProgram();
        if (programId == 0) {
            Log.d("createProgramOpenGL", "failed");
            return 0;
        }
        glAttachShader(programId, vertexId);
        glAttachShader(programId, fragmentId);
        return programId;
    }

    public static void linkProgram(int programId) {
        glLinkProgram(programId);
        int[] linkStatus = new int[1];
        glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            glDeleteProgram(programId);
            Log.d("linkProgramOpenGL", "failed");
        }
        glValidateProgram(programId);
        final int[] validateStatus=new int[1];
        glGetProgramiv(programId,GL_VALIDATE_STATUS,validateStatus,0);
        if(validateStatus[0]==0){
            Log.d("validateProgramOpenGL", "failed");
            return;
        }
    }

    public static int loadTexture(Context context,int resourseId){
        final int[] textureId=new int[1];
        glGenTextures(1,textureId,1);
        if(textureId[0]==0){
            Log.d("textrueProgramOpenGL", "failed");
            return 0;
        }
        final BitmapFactory.Options options=new BitmapFactory.Options();
        options.inScaled=false;
        final Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(),R.drawable.air_hockey_surface,options);
        if(bitmap==null){
            Log.d("BitmapProgramOpenGL", "failed");
            return 0;
        }
        glBindTexture(GL_TEXTURE_2D,textureId[0]);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        texImage2D(GL_TEXTURE_2D,0,bitmap,0);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,0);
        return textureId[0];
    }
}
