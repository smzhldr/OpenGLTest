package com.example.derongliu.opengltest;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.SurfaceView;

import java.io.IOException;
import java.lang.ref.SoftReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    SurfaceTexture surfaceTexture;
    GLSurfaceView glSurfaceView;
    Camera camera;

    public CameraRender(GLSurfaceView surfaceView,Camera camera){
        this.glSurfaceView= surfaceView;
        this.camera=camera;

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        int[] texture=new int[1];
        GLES20.glGenTextures(1,texture,0);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,0);
        surfaceTexture=new SurfaceTexture(texture[0]);

        surfaceTexture.setOnFrameAvailableListener(this);

        try {
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0,0,i,i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        if(surfaceTexture!=null){
            surfaceTexture.updateTexImage();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glSurfaceView.requestRender();
    }

    public void close(){
        camera.stopPreview();
        camera.release();
        camera=null;
    }
}
