//
// Created by derong.liu on 2019/3/19.
//

#include <stdint.h>
#include <unistd.h>
#include <android/native_window.h>
#include "Renderer.h"
#include "com_example_derongliu_opengltest_ndk_byglsurfaceview_NdkGlHelper.h"
#include "OpengGlUtils.h"


GLuint program1;
GLuint glAttrPosition1;

const char *vsCode1 = "attribute vec4 position;\n"
        "void main()\n"
        "{\n"
        "gl_Position=position;\n"
        "}\n";

const char *fsCode1 = "void main()\n"
        "{\n"
        "gl_FragColor=vec4(1.0,0.0,0.0,1.0);\n"
        "}\n";
float cube1[] = {
        0.0f, 0.5f,
        -0.5f, -0.5f,
        0.5f, -0.5f
};


Renderer::Renderer() : _msg(MSG_NONE), _display(0), _surface(0), _context(0) {
    pthread_mutex_init(&_mutex, 0);
    return;
}

Renderer::~Renderer() {
    pthread_mutex_destroy(&_mutex);
    return;
}

void Renderer::start() {
    pthread_create(&_thread_id, 0, threadStartCallback, this);
    return;
}

void Renderer::stop() {
    pthread_mutex_lock(&_mutex);
    _msg = MSG_RENDERER_LOOP_EXIT;
    pthread_mutex_unlock(&_mutex);
    pthread_join(_thread_id, 0);
    return;
}

void Renderer::setWindow(ANativeWindow *window) {
    pthread_mutex_lock(&_mutex);
    _msg = MSG_WINDOW_SET;
    _window = window;
    pthread_mutex_unlock(&_mutex);
    return;
}

void Renderer::renderLoop() {
    bool renderingEnable = true;
    while (renderingEnable) {
        pthread_mutex_lock(&_mutex);
        switch (_msg) {
            case MSG_WINDOW_SET:
                initialize();
                break;
            case MSG_RENDERER_LOOP_EXIT:
                renderingEnable = false;
                destroy();
                break;
            default:
                break;
        }
        _msg = MSG_NONE;
        if (_display) {
            drawFrame();
            if (!eglSwapBuffers(_display, _surface)) {

            }
        }
        pthread_mutex_unlock(&_mutex);
    }
    return;
}

bool Renderer::initialize() {

    GLint majorVersion;
    GLint minorVersion;
    EGLint width;
    EGLint height;

    _display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (EGL_NO_DISPLAY == _display) {
        return -1;
    }

    if (!eglInitialize(_display, &majorVersion, &minorVersion)) {
        return -1;
    }

    EGLint config_attrs[] = {
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_NONE
    };

    int num_configs = 0;
    EGLConfig eglConfig;
    if (!eglChooseConfig(_display, config_attrs, &eglConfig, 1, &num_configs)) {
        return -1;
    }

    _surface = eglCreateWindowSurface(_display, eglConfig, _window, NULL);
    if (EGL_NO_SURFACE == _surface) {
        return -1;
    }

    if (!eglQuerySurface(_display, _surface, EGL_WIDTH, &width) ||
        !eglQuerySurface(_display, _surface, EGL_HEIGHT, &height)) {
        return -1;
    }

    EGLint context_attrs[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL_NONE
    };

    _context = eglCreateContext(_display, eglConfig, EGL_NO_CONTEXT, context_attrs);
    if (EGL_NO_CONTEXT == _context) {
        return -1;
    }

    if (!eglMakeCurrent(_display, _surface, _surface, _context)) {
        return -1;
    }

    return true;
}

void Renderer::destroy() {
    eglMakeCurrent(_display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    eglDestroyContext(_display, _context);
    eglDestroySurface(_display, _surface);
    eglTerminate(_display);
    _display = EGL_NO_DISPLAY;
    _surface = EGL_NO_SURFACE;
    _context = EGL_NO_CONTEXT;
    return;
}

void Renderer::drawFrame() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glClearColor(0, 0, 1, 0);
    program1 = CreateProgram(vsCode1, fsCode1);
    glAttrPosition1 = glGetAttribLocation(program1, "position");
    glUseProgram(program1);
    glEnableVertexAttribArray(glAttrPosition1);
    glVertexAttribPointer(glAttrPosition1,2,GL_FLOAT,GL_FALSE, 0,cube1);
    glDrawArrays(GL_TRIANGLES,0,3);
    glDisableVertexAttribArray(glAttrPosition1);
}

void *Renderer::threadStartCallback(void *myself) {
    Renderer *renderer = (Renderer *) myself;
    renderer->renderLoop();
    pthread_exit(0);
}

