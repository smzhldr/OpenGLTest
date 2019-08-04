//
// Created by Lvlingling on 2019-07-17.
//

using namespace std;

extern "C" {
#include <libavformat/avformat.h>

}


#ifndef OPENGLTEST_FFENCODER_H
#define OPENGLTEST_FFENCODER_H


class FFEncoder {

private:
    bool  isInited;
    AVFormatContext *fmt_ctx;
    AVOutputFormat *fmt;
    AVStream video_st;
    AVStream audio_st;
    AVCodec *video_codec ;
    AVCodec *audio_codec;
    bool have_video;
    bool have_audio;
    bool enableAudio;
    char *filePath;


public:
    bool initEncoder();

    bool addStream(AVStream *ost, AVFormatContext *oc, AVCodec **codec, enum AVCodecID codec_i);

    bool openAudio(AVCodec *codec,AVStream *ost, AVDictionary *opt_arg);

    AVFrame * allocAudioFrame(int channels, enum AVSampleFormat sample_fmt,uint64_t channel_layout, int sample_rate, int frame_size);

    bool openVideo(AVCodec *codec, AVStream *ost, AVDictionary *opt_arg);

    AVFrame * allocVideoFrame(enum AVPixelFormat pix_fmt, int width, int height);

    bool videoEncode(uint8_t *data);

    bool audioEncode(uint8_t *data, int len);

    bool stopEncode();

    void closeStream(AVFormatContext *oc,AVStream *ost);
};


#endif //OPENGLTEST_FFENCODER_H
