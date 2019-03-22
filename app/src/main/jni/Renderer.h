//
// Created by derong.liu on 2019/3/19.
//

#ifndef OPENGLTEST_RENDERER_H
#define OPENGLTEST_RENDERER_H

#include <pthread.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>


class Renderer {

public:
    Renderer();

    virtual ~Renderer();

    void start();

    void stop();

    void setWindow(ANativeWindow *window);

private:
    enum RendererThreadMessage {
        MSG_NONE = 0,
        MSG_WINDOW_SET,
        MSG_RENDERER_LOOP_EXIT
    };

    pthread_t _thread_id;
    pthread_mutex_t _mutex;
    enum RendererThreadMessage _msg;

    ANativeWindow *_window;
    EGLDisplay _display;
    EGLSurface _surface;
    EGLContext _context;

    void renderLoop();

    bool initialize();

    void destroy();

    void drawFrame();

    static void *threadStartCallback(void *myself);

};


#endif //OPENGLTEST_RENDERER_H
