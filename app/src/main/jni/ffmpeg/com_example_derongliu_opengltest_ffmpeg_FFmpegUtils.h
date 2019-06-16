/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_example_derongliu_opengltest_ffmpeg_FFmpegUtils */

#ifndef _Included_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
#define _Included_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
#ifdef __cplusplus
extern "C" {
#endif
#undef com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_WIDTH
#define com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_WIDTH 4097L
#undef com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_HEIGHT
#define com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_HEIGHT 4098L
#undef com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_BIT_RATE
#define com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_BIT_RATE 8193L
#undef com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_SAMPLE_RATE
#define com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_SAMPLE_RATE 8194L
#undef com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_AUDIO_FORMAT
#define com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_AUDIO_FORMAT 8195L
#undef com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_CHANNEL_COUNT
#define com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_CHANNEL_COUNT 8196L
#undef com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_FRAME_SIZE
#define com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_KEY_FRAME_SIZE 8197L
#undef com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_EOF
#define com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_EOF -541478725L
/*
 * Class:     com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
 * Method:    getInfo
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_getInfo
  (JNIEnv *, jclass);

/*
 * Class:     com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_init
  (JNIEnv *, jclass);

/*
 * Class:     com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
 * Method:    start
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_start
  (JNIEnv *, jobject,jint flag);

/*
 * Class:     com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
 * Method:    input
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_input
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
 * Method:    output
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_output
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
 * Method:    stop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_stop
  (JNIEnv *, jobject);

/*
 * Class:     com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
 * Method:    set
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_set
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
 * Method:    get
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_get
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_example_derongliu_opengltest_ffmpeg_FFmpegUtils
 * Method:    release
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_example_derongliu_opengltest_ffmpeg_FFmpegUtils_release
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
