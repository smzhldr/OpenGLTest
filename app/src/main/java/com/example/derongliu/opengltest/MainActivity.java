package com.example.derongliu.opengltest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.derongliu.mediacodec.encoder.MediaEncodeActivity;
import com.example.derongliu.opengltest.camera.GLSurfaceCameraActivity;
import com.example.derongliu.opengltest.camera2.Camera2Activity;
import com.example.derongliu.opengltest.customsurfaceview.CustomGlSurfaceActivity;
import com.example.derongliu.ffmpeg.FFmpegActivity;
import com.example.derongliu.opengltest.framebuffer.FBOActivity;
import com.example.derongliu.opengltest.gltriangle.GlTriangleActivity1;
import com.example.derongliu.opengltest.gltriangle.GlTriangleActivity2;
import com.example.derongliu.opengltest.lifangti.LifangtiActivity;
import com.example.derongliu.mediacodec.MediaActivity;
import com.example.derongliu.opengltest.ndk.byglsurfaceview.NdkGlActivity;
import com.example.derongliu.opengltest.ndk.bysurfaceview.NdkSurfaceViewActivity;
import com.example.derongliu.opengltest.pictureprocess.PictureProcessActivity;
import com.example.derongliu.opengltest.textrueviewcamera.CameraActivity;
import com.example.derongliu.opengltest.texture2dimage.Texture2DImageActivity;
import com.example.derongliu.opengltest.triangle.TriangleActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements OnItemClickListener {

    private List<String> itemNameList;
    private List<Class> classList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.main_recyclerview);
        initData();
        initClass();
        MainAdapter adapter = new MainAdapter(itemNameList);
        adapter.setListener(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3,LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

    }


    private void initData() {
        itemNameList = new ArrayList<>();
        itemNameList.add("TV_Preview");
        itemNameList.add("Texture_Pic");
        itemNameList.add("Rectangle");
        itemNameList.add("Trag1");
        itemNameList.add("Trag_Matrix");
        itemNameList.add("FBO");
        itemNameList.add("Cube");
        itemNameList.add("Filters");
        itemNameList.add("Camera");
        itemNameList.add("Camera2");
        itemNameList.add("SV_Egl_Thread");
        itemNameList.add("Ndk_GSV");
        itemNameList.add("Ndk_SV_Thread");
        itemNameList.add("MediaCodec_Libs");
        itemNameList.add("FFmpeg Test");
        itemNameList.add("M_encode");
    }

    private void initClass() {
        classList = new ArrayList<>();
        classList.add(CameraActivity.class);
        classList.add(Texture2DImageActivity.class);
        classList.add(TriangleActivity.class);
        classList.add(GlTriangleActivity1.class);
        classList.add(GlTriangleActivity2.class);
        classList.add(FBOActivity.class);
        classList.add(LifangtiActivity.class);
        classList.add(PictureProcessActivity.class);
        classList.add(GLSurfaceCameraActivity.class);
        classList.add(Camera2Activity.class);
        classList.add(CustomGlSurfaceActivity.class);
        classList.add(NdkGlActivity.class);
        classList.add(NdkSurfaceViewActivity.class);
        classList.add(MediaActivity.class);
        classList.add(FFmpegActivity.class);
        classList.add(MediaEncodeActivity.class);
    }


    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(MainActivity.this, classList.get(position));
        startActivity(intent);

    }


    private class MainAdapter extends RecyclerView.Adapter<MainViewHolder> {
        List list;
        private OnItemClickListener listener;

        MainAdapter(List list) {
            this.list = list;
        }

        @Override
        public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item, parent, false);
            return new MainViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MainViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            holder.textView.setText(list.get(position).toString());
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        void setListener(OnItemClickListener listener) {
            this.listener = listener;
        }
    }


    private static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        MainViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.item_textView);
        }
    }

}