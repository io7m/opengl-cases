#version 330 core

layout(location = 0) in float a;

void
main ()
{
  gl_Position = vec4(float(a));
}
