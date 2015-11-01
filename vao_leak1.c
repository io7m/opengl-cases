#define GL_GLEXT_PROTOTYPES 1
#include <GL/glcorearb.h>

#include <GLFW/glfw3.h>

#include <assert.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

int
main(int argc, char *argv[])
{
  GLFWwindow* context;
  int leak = 0;

  leak = (argc > 1) && (strcmp(argv[1], "leak") == 0);

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

  glfwMakeContextCurrent(context);

  {
    GLuint aid;
    GLuint vao;
    GLuint vao_default;

    fprintf(stderr, "Vendor: %s\n", glGetString(GL_VENDOR));
    fprintf(stderr, "Version: %s\n", glGetString(GL_VERSION));

    {
      // Emulate JOGL's "default VAO"
      glGenVertexArrays(1, &vao_default);
      assert(glGetError() == GL_NO_ERROR);
      glBindVertexArray(vao_default);
      assert(glGetError() == GL_NO_ERROR);
    }

    for (int count = 0; count < 10; ++count) {
      fprintf(stderr, "Creating 100mb VBO\n");

      glGenBuffers(1, &aid);
      assert(glGetError() == GL_NO_ERROR);
      glBindBuffer(GL_ARRAY_BUFFER, aid);
      assert(glGetError() == GL_NO_ERROR);
      glBufferData(GL_ARRAY_BUFFER, 100000000L, NULL, GL_STATIC_DRAW);
      assert(glGetError() == GL_NO_ERROR);
      glBindBuffer(GL_ARRAY_BUFFER, 0);
      assert(glGetError() == GL_NO_ERROR);

      fprintf(stderr, "Created VBO %d\n", aid);

      fprintf(stderr, "Creating VAO\n");

      glGenVertexArrays(1, &vao);
      assert(glGetError() == GL_NO_ERROR);
      glBindVertexArray(vao);
      assert(glGetError() == GL_NO_ERROR);
      glBindBuffer(GL_ARRAY_BUFFER, aid);
      assert(glGetError() == GL_NO_ERROR);
      glEnableVertexAttribArray(0);
      assert(glGetError() == GL_NO_ERROR);
      glVertexAttribPointer(0, 1, GL_UNSIGNED_BYTE, 0, 0, (void*) 0L);
      assert(glGetError() == GL_NO_ERROR);
      glBindVertexArray(vao_default);
      assert(glGetError() == GL_NO_ERROR);

      fprintf(stderr, "Created VAO %d and associated VBO %d\n", vao, aid);
      fprintf(stderr, "Deleting VBO %d\n", aid);

      glDeleteBuffers(1, &aid);
      assert(glGetError() == GL_NO_ERROR);

      if (!leak) {
        fprintf(stderr, "Deleting VAO %d\n", vao);
        glDeleteVertexArrays(1, &vao);
        assert(glGetError() == GL_NO_ERROR);
      } else {
        fprintf(stderr, "Leaking VBO %d via VAO %d\n", aid, vao);
      }

      sleep(2L);
    }
  }

  glfwMakeContextCurrent(NULL);
  glfwTerminate();
  return 0;
}
