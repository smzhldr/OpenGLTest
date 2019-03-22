//
// Created by derong.liu on 2019/3/20.
//
#include "OpengGlUtils.h"
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

