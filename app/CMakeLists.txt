# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        ndklib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/jni/ndkGlHelper.cpp
        src/main/jni/NdkSurfaceView.cpp
        src/main/jni/Renderer.cpp
        src/main/jni/OpenGlUtils.cpp
        src/main/jni/ffmpeg/ffmpegJni.cpp
        src/main/jni/ffmpeg/AACDecoder.cpp
        src/main/jni/ffmpeg/Codec.cpp
        src/main/jni/ffmpeg/utils.cpp
        src/main/jni/ffmpeg/H264Decoder.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)


# 设置cpp目录路径
set(CPP_DIR ${CMAKE_SOURCE_DIR}/src/main/jni)

# 设置jniLibs目录路径
set(LIBS_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)

# 引用源码 ../代表上级目录
include_directories(${CPP_DIR}/include)
include_directories(${CPP_DIR}/ffmpeg)


# 用于各种类型声音、图像编解码

add_library( # 库名称
        avcodec

        # 动态库，生成so文件
        SHARED

        # 表示该库是引用的不是生成的
        IMPORTED)

# 引用库文件
set_target_properties( # 库名称
        avcodec

        # 库的路径
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/armeabi/libavcodec-57.so)

# 用于各种音视频封装格式的生成和解析，读取音视频帧等功能
add_library(avformat
        SHARED
        IMPORTED)

set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/armeabi/libavformat-57.so)

# 包含一些公共的工具函数
add_library(avutil
        SHARED
        IMPORTED)

set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/armeabi/libavutil-55.so)

# 提供了各种音视频过滤器
add_library(avfilter
        SHARED
        IMPORTED)

set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/armeabi/libavfilter-6.so)

# 用于音频重采样，采样格式转换和混合
add_library(swresample
        SHARED
        IMPORTED)

set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/armeabi/libswresample-2.so)

# 用于视频场景比例缩放、色彩映射转换
add_library(swscale
        SHARED
        IMPORTED)

set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/armeabi/libswscale-4.so)


add_library(device
        SHARED
        IMPORTED)

set_target_properties(device
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/armeabi/libavdevice-57.so)


add_library(postproc
        SHARED
        IMPORTED)

set_target_properties(postproc
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/armeabi/libpostproc-54.so)



target_link_libraries( # Specifies the target library.
        ndklib
        -ljnigraphics
        -landroid
        GLESv2
        EGL

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}

        avcodec
        avformat
        avutil
        avfilter
        swresample
        swscale
        device
        postproc)