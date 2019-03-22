package com.example.derongliu.opengltest.ndk.bysurfaceview;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class NdkSurfaceViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NdkSurfaceView ndkSurfaceView = new NdkSurfaceView(this);
        setContentView(ndkSurfaceView);
    }
}
