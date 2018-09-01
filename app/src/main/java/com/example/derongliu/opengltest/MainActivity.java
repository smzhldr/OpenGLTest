package com.example.derongliu.opengltest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.derongliu.opengltest.textrueviewcamera.CameraActivity;
import com.example.derongliu.opengltest.texture2dimage.Texture2DImageActivity;
import com.example.derongliu.opengltest.triangle.TriangleActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button camera, photo1,photo2;

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


    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.textrue_camera:
                intent.setClass(MainActivity.this,CameraActivity.class);
                break;
            case R.id.photo_rectangle:
                intent.setClass(MainActivity.this,Texture2DImageActivity.class);
                break;
            case R.id.photo_point:
                intent.setClass(MainActivity.this,TriangleActivity.class);
                break;
            default:
                break;
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
