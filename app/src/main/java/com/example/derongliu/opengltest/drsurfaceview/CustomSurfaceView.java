package com.example.derongliu.opengltest.drsurfaceview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private GlThread glThread;

    public CustomSurfaceView(Context context) {
        super(context);
        init();
    }

    public CustomSurfaceView(Context context, AttributeSet attributes) {
        super(context, attributes);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }


    public void setRenderer(Renderer renderer) {
        glThread = new GlThread();
        glThread.setRenderer(renderer);
        glThread.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        glThread.setHolder(holder);
        glThread.isChanged = false;
        requestRender();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        glThread.isChanged = false;
        glThread.setSize(width, height);
        requestRender();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        glThread.onDestroy();
    }

    public void requestRender() {
        glThread.requestRenderer();
    }


    private static class GlThread extends Thread {

        boolean isCreate;
        boolean isChanged;

        private EglHelper eglHelper;
        private SurfaceHolder holder;
        private int width, height;
        private Renderer renderer;
        private final Object wait = new Object();

        void setSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        void setHolder(SurfaceHolder holder) {
            this.holder = holder;
        }

        void onDestroy() {
            if (eglHelper != null) {
                eglHelper.destroyGL();
            }
        }

        void setRenderer(Renderer renderer) {
            this.renderer = renderer;
        }

        void requestRenderer() {
            synchronized (wait) {
                wait.notifyAll();
            }
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                synchronized (wait) {
                    try {
                        wait.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //创建EGL环境
                if (eglHelper == null && holder != null) {
                    eglHelper = new EglHelper();
                    eglHelper.createGL(holder.getSurface());
                }

                if (!isCreate) {
                    if (renderer != null) {
                        renderer.onSurfaceCreated();
                        isCreate = true;
                    }
                }

                if (!isChanged) {
                    if (renderer != null) {
                        renderer.onSurfaceChanged(width, height);
                        isChanged = true;
                    }
                }

                if (renderer != null && isCreate && isChanged) {
                    renderer.onDraw();
                }

                if (eglHelper != null) {
                    eglHelper.swapBuffer();
                }

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface Renderer {
        void onSurfaceCreated();

        void onSurfaceChanged(int width, int height);

        void onDraw();
    }

}
