//
// Created by derong.liu on 2019-08-04.
//

#include "com_example_derongliu_ffmpeg_decoder_FFmpegDecoder.h"

#include <android/native_window_jni.h>


extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/imgutils.h"
#include <libswscale/swscale.h>
}

JNIEXPORT void JNICALL Java_com_example_derongliu_ffmpeg_decoder_FFmpegDecoder_playVideo
        (JNIEnv *env, jclass clazz, jstring path, jobject surface) {

    AVPacket *pAvPacket;
    AVFrame *pFrame, *pFrameRGB;
    AVCodecContext *pCodecContext;
    struct SwsContext *img_convert_context;
    AVFormatContext *pFormatContext;
    ANativeWindow *nativeWindow;
    ANativeWindow_Buffer window_buffer;
    uint8_t *p_out_buffer;

    int i;
    AVCodec *pCodec;
    char input_str[500] = {0};

    sprintf(input_str, "%s", env->GetStringUTFChars(path, nullptr));

    av_register_all();

    pFormatContext = avformat_alloc_context();
    if (avformat_open_input(&pFormatContext, input_str, nullptr, nullptr) != 0) {
        return;
    }

    if (avformat_find_stream_info(pFormatContext, nullptr) < 0) {
        return;
    }

    int videoIndex = -1;
    for (i = 0; i < pFormatContext->nb_streams; i++) {
        if (pFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoIndex = i;
            break;
        }
    }

    if (videoIndex == -1) {
        return;
    }

    pCodecContext = pFormatContext->streams[videoIndex]->codec;
    pCodec = avcodec_find_decoder(pCodecContext->codec_id);

    if (avcodec_open2(pCodecContext, pCodec, nullptr) < 0) {
        return;
    }

    nativeWindow = ANativeWindow_fromSurface(env, surface);
    if (nativeWindow == 0) {
        return;
    }

    int width = pCodecContext->width;
    int height = pCodecContext->height;

    pFrame = av_frame_alloc();
    pFrameRGB = av_frame_alloc();
    pAvPacket = (AVPacket *) av_malloc(sizeof(AVPacket));

    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, width, height, 1);
    p_out_buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));

    av_image_fill_arrays(pFrameRGB->data, pFrameRGB->linesize, p_out_buffer, AV_PIX_FMT_RGBA, width,
                         height, 1);
    img_convert_context = sws_getContext(width, height, pCodecContext->pix_fmt, width, height,
                                         AV_PIX_FMT_RGBA, SWS_BICUBIC,
                                         nullptr, nullptr, nullptr);

    if (0 >
        ANativeWindow_setBuffersGeometry(nativeWindow, width, height, WINDOW_FORMAT_RGBA_8888)) {
        ANativeWindow_release(nativeWindow);
        return;
    }

    while (av_read_frame(pFormatContext, pAvPacket) >= 0) {
        if (pAvPacket->stream_index == videoIndex) {
            int ret = avcodec_send_packet(pCodecContext, pAvPacket);
            if (ret < 0 && ret != AVERROR(EAGAIN) && ret != AVERROR_EOF) {
                return;
            }

            ret = avcodec_receive_frame(pCodecContext, pFrame);
            if (ret < 0 && ret != AVERROR_EOF) {
                return;
            }

            sws_scale(img_convert_context, (const uint8_t *const *) pFrame->data, pFrame->linesize,
                      0, pCodecContext->height, pFrameRGB->data, pFrameRGB->linesize);


            if (ANativeWindow_lock(nativeWindow, &window_buffer, nullptr) < 0) {
                printf("error");
            } else {
                uint8_t *dst = (uint8_t *) window_buffer.bits;
                for (int h = 0; h < height; h++) {
                    memcpy(dst + h * window_buffer.stride * 4,
                           p_out_buffer + h * pFrameRGB->linesize[0],
                           (size_t) pFrameRGB->linesize[0]);
                }
                ANativeWindow_unlockAndPost(nativeWindow);
            }
        }
        av_packet_unref(pAvPacket);
    }

    sws_freeContext(img_convert_context);
    av_free(pAvPacket);
    av_free(pFrameRGB);
    avcodec_close(pCodecContext);
    avformat_close_input(&pFormatContext);
}
