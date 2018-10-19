package com.example.derongliu.opengltest.pictureprocess;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import com.example.derongliu.opengltest.R;

import com.example.derongliu.opengltest.pictureprocess.PictureProcessRender.Filter;

public class PictureProcessActivity extends Activity {

    GLSurfaceView glSurfaceView;
    PictureProcessRender render;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pictureprocess);
        glSurfaceView = findViewById(R.id.glsurface);
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportES2 = configurationInfo.reqGlEsVersion >= 0X20000;
        if (supportES2) {
            glSurfaceView.setEGLContextClientVersion(2);
            render = new PictureProcessRender(this, glSurfaceView);
            render.setFilter(Filter.NONE);
            render.sethsHalf(false);
            glSurfaceView.setRenderer(render);
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        }
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "原图 ");
        menu.add(Menu.NONE, 1, 1, "黑白 ");
        menu.add(Menu.NONE, 2, 2, "冷色调 ");
        menu.add(Menu.NONE, 3, 3, "暖色调 ");
        menu.add(Menu.NONE, 4, 4, "模糊 ");
        menu.add(Menu.NONE, 5, 5, "放大镜 ");

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 0:
                render.setFilter(Filter.NONE);
                break;
            case 1:
                render.setFilter(Filter.GRAY);
                break;
            case 2:
                render.setFilter(Filter.COOL);
                break;
            case 3:
                render.setFilter(Filter.WARM);
                break;
            case 4:
                render.setFilter(Filter.BLUR);
                break;
            case 5:
                render.setFilter(Filter.MAGN);
                break;
        }
        glSurfaceView.requestRender();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
