//
// Created by derong.liu on 2019/6/12.
//
#include "com_example_derongliu_opengltest_ffmpeg_FFmpegUtils.h"
#include <jni.h>
#include <stdlib.h>
#include <stdbool.h>
#include <stdio.h>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavfilter/avfilter.h"

}

JNIEXPORT jstring JNICALL Java_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_getFFmpegInfo
        (JNIEnv *env, jclass jObject) {
    char info[40000] = {0};
    av_register_all();
    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            sprintf(info, "%s[Dec]", info);
        } else {
            sprintf(info, "%s[Enc]", info);
        }
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s[Video]", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s[Audio]", info);
                break;
            default:
                sprintf(info, "%s[Other]", info);
                break;
        }
        sprintf(info, "%s[%10s]\n", info, c_temp->name);
        c_temp = c_temp->next;
    }
    return env->NewStringUTF(info);
}
