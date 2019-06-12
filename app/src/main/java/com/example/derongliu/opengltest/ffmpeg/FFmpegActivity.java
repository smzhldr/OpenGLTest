package com.example.derongliu.opengltest.ffmpeg;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.derongliu.opengltest.R;

public class FFmpegActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg);
        TextView textView = findViewById(R.id.id_ffmpeg_tv);
        textView.setText(FFmpegUtils.getFFmpegInfo());
    }
}
