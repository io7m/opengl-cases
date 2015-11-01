#define GL_GLEXT_PROTOTYPES 1
#include <GL/glcorearb.h>

#include <GLFW/glfw3.h>

#include <assert.h>
#include <stdio.h>
#include <string.h>

int
main(void)
{
  GLFWwindow* context;

  if (!glfwInit()) {
    fprintf(stderr, "Unable to initialize\n");
    return -1;
  }

  glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
  glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
  glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
  glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

  context = glfwCreateWindow(320, 240, "Main", NULL, NULL);
  if (!context) {
    fprintf(stderr, "Unable to create context\n");
    glfwTerminate();
    return -1;
  }

  {
    GLuint bid;
    GLuint aid;

    glfwMakeContextCurrent(context);

    fprintf(stderr, "Vendor: %s\n", glGetString(GL_VENDOR));
    fprintf(stderr, "Version: %s\n", glGetString(GL_VERSION));

    glGetIntegerv(GL_ELEMENT_ARRAY_BUFFER_BINDING, &aid);
    assert(glGetError() == GL_NO_ERROR);
    assert(aid == 0);

    glGenBuffers(1, &bid);
    assert(glGetError() == GL_NO_ERROR);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bid);
    assert(glGetError() == GL_NO_ERROR);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, 100L, NULL, GL_STATIC_DRAW);
    assert(glGetError() == GL_NO_ERROR);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    assert(glGetError() == GL_NO_ERROR);

    glfwMakeContextCurrent(NULL);
  }

  glfwTerminate();
  return 0;
}
