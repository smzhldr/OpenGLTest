//
// Created by derong.liu on 2019/3/14.
//

#include "com_example_derongliu_opengltest_ndk_bysurfaceview_NdkSurfaceView.h"
#include <android/native_window_jni.h> // requires ndk r5 or newer
#include "Renderer.h"

static ANativeWindow *window = 0;
static Renderer *renderer = 0;

JNIEXPORT void JNICALL
Java_com_example_derongliu_opengltest_ndk_bysurfaceview_NdkSurfaceView_onStart
        (JNIEnv *env, jobject jClass) {
    renderer = new Renderer();
    return;
}


JNIEXPORT void JNICALL
Java_com_example_derongliu_opengltest_ndk_bysurfaceview_NdkSurfaceView_onResume
        (JNIEnv *env, jobject jClass) {
    renderer->start();
    return;
}


JNIEXPORT void JNICALL
Java_com_example_derongliu_opengltest_ndk_bysurfaceview_NdkSurfaceView_onPause
        (JNIEnv *env, jobject jClass) {
    renderer->stop();
    return;
}


JNIEXPORT void JNICALL Java_com_example_derongliu_opengltest_ndk_bysurfaceview_NdkSurfaceView_onStop
        (JNIEnv *env, jobject jClass) {
    delete renderer;
    renderer = 0;
    return;
}


JNIEXPORT void JNICALL
Java_com_example_derongliu_opengltest_ndk_bysurfaceview_NdkSurfaceView_setSurface
        (JNIEnv *env, jobject jClass, jobject surface) {
    if (surface != 0) {
        window = ANativeWindow_fromSurface(env, surface);
        renderer->setWindow(window);
    } else {
        ANativeWindow_release(window);
    }
    return;
}

