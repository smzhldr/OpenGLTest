//
// Created by derong.liu on 2019/3/5.
//
#include "com_example_derongliu_opengltest_ndk_byglsurfaceview_NdkGlHelper.h"
#include "OpengGlUtils.h"

GLuint program;
GLuint glAttrPosition;

const char *vsCode = "attribute vec4 position;\n"
                     "void main()\n"
                     "{\n"
                     "gl_Position=position;\n"
                     "}\n";

const char *fsCode = "void main()\n"
                     "{\n"
                     "gl_FragColor=vec4(1.0,0.0,0.0,1.0);\n"
                     "}\n";
float cube[] = {
        0.0f, 0.5f,
        -0.5f, -0.5f,
        0.5f, -0.5f
};


JNIEXPORT void JNICALL JNICALL Java_com_example_derongliu_opengltest_ndk_byglsurfaceview_NdkGlHelper_onSurfaceCreated
        (JNIEnv *env, jclass jObject) {
    glClearColor(0.0, 0.0, 0.0, 0.0);
    program = CreateProgram(vsCode, fsCode);
    glAttrPosition = glGetAttribLocation(program, "position");
}


JNIEXPORT void JNICALL Java_com_example_derongliu_opengltest_ndk_byglsurfaceview_NdkGlHelper_onSurfaceChanged
        (JNIEnv *env, jclass jObject, jint width, jint height) {
    glViewport(0, 0, width, height);
}

JNIEXPORT void JNICALL Java_com_example_derongliu_opengltest_ndk_byglsurfaceview_NdkGlHelper_onDrawFrame
        (JNIEnv *env, jclass jObject) {
    glClear(GL_COLOR_BUFFER_BIT);
    glUseProgram(program);
    glEnableVertexAttribArray(glAttrPosition);
    glVertexAttribPointer(glAttrPosition,2,GL_FLOAT,GL_FALSE, 0,cube);
    glDrawArrays(GL_TRIANGLES,0,3);
    glDisableVertexAttribArray(glAttrPosition);
}
