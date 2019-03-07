//
// Created by derong.liu on 2019/3/5.
//
#include "com_example_derongliu_opengltest_ndk_NdkGlHelper.h"

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


JNIEXPORT void JNICALL Java_com_example_derongliu_opengltest_ndk_NdkGlHelper_onSurfaceCreated
        (JNIEnv *env, jclass jObject) {
    glClearColor(0.0, 0.0, 0.0, 0.0);
    program = CreateProgram(vsCode, fsCode);
    glAttrPosition = glGetAttribLocation(program, "position");
}


JNIEXPORT void JNICALL Java_com_example_derongliu_opengltest_ndk_NdkGlHelper_onSurfaceChanged
        (JNIEnv *env, jclass jObject, jint width, jint height) {
    glViewport(0, 0, width, height);
}

JNIEXPORT void JNICALL Java_com_example_derongliu_opengltest_ndk_NdkGlHelper_onDrawFrame
        (JNIEnv *env, jclass jObject) {
    glClear(GL_COLOR_BUFFER_BIT);
    glUseProgram(program);
    glEnableVertexAttribArray(glAttrPosition);
    glVertexAttribPointer(glAttrPosition,2,GL_FLOAT,GL_FALSE, 0,cube);
    glDrawArrays(GL_TRIANGLES,0,3);
    glDisableVertexAttribArray(glAttrPosition);
}


GLuint CompileShader(GLenum shaderType, const char *code) {
    if (code == NULL) {
        //info("compile shader error,code is null");
        return 0;
    }
    GLuint shader = glCreateShader(shaderType);
    glShaderSource(shader, 1, &code, NULL);
    glCompileShader(shader);
    //check error
    GLint compileResult = 0;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compileResult);
    if (compileResult == GL_FALSE) {
        //info("compile shader error with code : %s", code);
        char szLogBuffer[1024] = {0};
        GLsizei realLogLen = 0;
        glGetShaderInfoLog(shader, 1024, &realLogLen, szLogBuffer);
        //info("error log : %s", szLogBuffer);
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

GLuint CreateProgram(const char *vsCode, const char *fsCode) {
    GLuint program = glCreateProgram();
    GLuint vsShader = CompileShader(GL_VERTEX_SHADER, vsCode);
    if (vsShader == 0) {
        //info("compile vs shader fail");
        return 0;
    }
    GLuint fsShader = CompileShader(GL_FRAGMENT_SHADER, fsCode);
    if (fsShader == 0) {
        //info("compile fs shader fail");
        return 0;
    }
    glAttachShader(program, vsShader);
    glAttachShader(program, fsShader);
    glLinkProgram(program);
    //check error
    glDetachShader(program, vsShader);
    glDetachShader(program, fsShader);
    glDeleteShader(vsShader);
    glDeleteShader(fsShader);

    GLint linkResult = 0;
    glGetProgramiv(program, GL_LINK_STATUS, &linkResult);
    if (linkResult == GL_FALSE) {
        //info("link program error ");
        char szLogBuffer[1024];
        GLsizei realLogLen = 0;
        glGetShaderInfoLog(program, 1024, &realLogLen, szLogBuffer);
        //info("error log : %s", szLogBuffer);
        glDeleteProgram(program);
        return 0;
    }
    return program;
}


