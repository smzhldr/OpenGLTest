package com.example.derongliu.opengltest;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.derongliu.opengltest.camera.GLSurfaceCameraActivity;
import com.example.derongliu.opengltest.camera2.Camera2Activity;
import com.example.derongliu.opengltest.gltriangle.GlTriangleActivity1;
import com.example.derongliu.opengltest.gltriangle.GlTriangleActivity2;
import com.example.derongliu.opengltest.framebuffer.FBOActivity;
import com.example.derongliu.opengltest.lifangti.LifangtiActivity;
import com.example.derongliu.opengltest.lifangti.LifangtiRender;
import com.example.derongliu.opengltest.pictureprocess.PictureProcessActivity;
import com.example.derongliu.opengltest.textrueviewcamera.CameraActivity;
import com.example.derongliu.opengltest.texture2dimage.Texture2DImageActivity;
import com.example.derongliu.opengltest.triangle.TriangleActivity;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button camera, photo1, photo2, trangle1, triangle2, steciltest, xingti,
            Lifangti, pictureProcess, glcamera, camera2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camera = findViewById(R.id.textrue_camera);
        camera.setOnClickListener(this);

        photo1 = findViewById(R.id.photo_rectangle);
        photo1.setOnClickListener(this);

        photo2 = findViewById(R.id.photo_point);
        photo2.setOnClickListener(this);

        xingti = findViewById(R.id.gl_xingti);
        xingti.setOnClickListener(this);

        trangle1 = findViewById(R.id.gl_sanjiaoxing1);
        trangle1.setOnClickListener(this);

        triangle2 = findViewById(R.id.gl_sanjiaoxing2);
        triangle2.setOnClickListener(this);


        steciltest = findViewById(R.id.gl_stencil_test);
        steciltest.setOnClickListener(this);

        Lifangti = findViewById(R.id.lifangti);
        Lifangti.setOnClickListener(this);

        pictureProcess = findViewById(R.id.pictureProcess);
        pictureProcess.setOnClickListener(this);

        glcamera = findViewById(R.id.glcamera);
        glcamera.setOnClickListener(this);

        camera2 = findViewById(R.id.glcamera2);
        camera2.setOnClickListener(this);


        if (!EasyPermissions.hasPermissions(this, new String[]{Manifest.permission.CAMERA})) {
            EasyPermissions.requestPermissions(this, "拍照需要摄像头权限", 0, new String[]{Manifest.permission.CAMERA});
        }

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.textrue_camera:
                intent.setClass(MainActivity.this, CameraActivity.class);
                break;
            case R.id.photo_rectangle:
                intent.setClass(MainActivity.this, Texture2DImageActivity.class);
                break;
            case R.id.photo_point:
                intent.setClass(MainActivity.this, TriangleActivity.class);
                break;
            case R.id.gl_xingti:
                break;
            case R.id.gl_sanjiaoxing1:
                intent.setClass(MainActivity.this, GlTriangleActivity1.class);
                break;
            case R.id.gl_sanjiaoxing2:
                intent.setClass(MainActivity.this, GlTriangleActivity2.class);
                break;
            case R.id.gl_stencil_test:
                intent.setClass(MainActivity.this, FBOActivity.class);
                break;
            case R.id.lifangti:
                intent.setClass(MainActivity.this, LifangtiActivity.class);
                break;
            case R.id.pictureProcess:
                intent.setClass(MainActivity.this, PictureProcessActivity.class);
                break;
            case R.id.glcamera:
                intent.setClass(MainActivity.this, GLSurfaceCameraActivity.class);
                break;
            case R.id.glcamera2:
                intent.setClass(MainActivity.this, Camera2Activity.class);
                break;
            default:
                break;
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
