//
// Created by derong.liu on 2019/6/15.
//

#include <stdint.h>


#ifndef OPENGLTEST_DECODER_H
#define OPENGLTEST_DECODER_H


#include "Codec.h"

class Decoder : public Codec{
private:
    size_t frameSize;
    long bitRate;
    int sampleRate;
    int audioFormat;
    int channelCount;
    size_t bytesPerSample;

public:
    int start();
    int input(uint8_t *data);
    int output(uint8_t *data);
    void set(int key,int value);
    int get(int key);
    int stop();
};


#endif //OPENGLTEST_DECODER_H
