//
// Created by derong.liu on 2019/6/15.
//

#ifndef OPENGLTEST_UTILS_H
#define OPENGLTEST_UTILS_H


#include <jni.h>

class utils {
public:
    static char *Jstring2CStr(JNIEnv *env, jstring jstr);
};


#endif //OPENGLTEST_UTILS_H
