package com.example.derongliu.opengltest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.derongliu.opengltest.camera.GLSurfaceCameraActivity;
import com.example.derongliu.opengltest.camera2.Camera2Activity;
import com.example.derongliu.opengltest.drsurfaceview.CustomGlSurfaceActivity;
import com.example.derongliu.opengltest.framebuffer.FBOActivity;
import com.example.derongliu.opengltest.gltriangle.GlTriangleActivity1;
import com.example.derongliu.opengltest.gltriangle.GlTriangleActivity2;
import com.example.derongliu.opengltest.lifangti.LifangtiActivity;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    private void initData() {
        itemNameList = new ArrayList<>();
        itemNameList.add("TextureView预览相机");
        itemNameList.add("Texture展示");
        itemNameList.add("Triangle绘图");
        itemNameList.add("三角形1");
        itemNameList.add("三角形2");
        itemNameList.add("FBO");
        itemNameList.add("立方体");
        itemNameList.add("图片滤镜");
        itemNameList.add("Camera预览");
        itemNameList.add("Camera2预览");
        itemNameList.add("DrSurfaceView");
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