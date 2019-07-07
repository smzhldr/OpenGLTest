//
// Created by derong.liu on 2019/6/12.
//
#include "com_example_derongliu_ffmpeg_FFmpegUtils.h"
#include <jni.h>
#include "AACDecoder.h"
#include "Codec.h"
#include "utils.h"
#include "H264Decoder.h"

Codec *codec;

JNIEXPORT jstring JNICALL
Java_com_example_derongliu_ffmpeg_FFmpegUtils_getInfo(JNIEnv *env, jclass obj) {
    return env->NewStringUTF(Codec::getInfo(0));
}


JNIEXPORT void JNICALL
Java_com_example_derongliu_ffmpeg_FFmpegUtils_init(JNIEnv *env, jclass obj) {
    Codec::init();
}

JNIEXPORT jint JNICALL
Java_com_example_derongliu_ffmpeg_FFmpegUtils_start(JNIEnv *env, jobject obj,
                                                               jint flag) {
    if (flag == 0) {
        codec = new Decoder();
    } else {
        codec = new H264Decoder();
    }
    return codec->start();
}

JNIEXPORT jint JNICALL
Java_com_example_derongliu_ffmpeg_FFmpegUtils_input(JNIEnv *env, jobject obj,
                                                               jbyteArray data) {
    return codec->input((uint8_t *) env->GetByteArrayElements(data, JNI_FALSE));
}

JNIEXPORT jint JNICALL
Java_com_example_derongliu_ffmpeg_FFmpegUtils_output(JNIEnv *env, jobject obj,
                                                                jbyteArray data) {
    return codec->output((uint8_t *) env->GetByteArrayElements(data, JNI_FALSE));
}

JNIEXPORT jint JNICALL
Java_com_example_derongliu_ffmpeg_FFmpegUtils_stop(JNIEnv *env, jobject obj) {
    return codec->stop();
}

JNIEXPORT void JNICALL
Java_com_example_derongliu_ffmpeg_FFmpegUtils_set(JNIEnv *env, jobject obj, jint key,
                                                             jint value) {

}

JNIEXPORT jint JNICALL
Java_com_example_derongliu_ffmpeg_FFmpegUtils_get(JNIEnv *env, jobject obj, jint key) {
    return codec->get(key);
}

JNIEXPORT void JNICALL
Java_com_example_derongliu_ffmpeg_FFmpegUtils_release(JNIEnv *env, jobject obj) {
    Codec::release();
}
