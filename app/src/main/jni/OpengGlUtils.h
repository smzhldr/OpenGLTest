//
// Created by derong.liu on 2019/3/21.
//

#ifndef OPENGLTEST_OPENGGLUTILS_H
#define OPENGLTEST_OPENGGLUTILS_H

#include <GLES2/gl2.h>

GLuint CompileShader(GLenum, const char *);

GLuint CreateProgram(const char *, const char *);

#endif //OPENGLTEST_OPENGGLUTILS_H
