/*
 * Copyright © 2015 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.opengl_cases;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.DebugGL3;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

public final class ShaderWrongAttributeType
{
  private ShaderWrongAttributeType()
  {
    throw new AssertionError("Unreachable code!");
  }

  public static void main(final String[] args)
    throws IOException
  {
    final GLProfile pro = GLProfile.get(GLProfile.GL3);
    final GLCapabilities caps = new GLCapabilities(pro);
    final GLDrawableFactory draw_fact = GLDrawableFactory.getFactory(pro);
    final GLOffscreenAutoDrawable drawable =
      draw_fact.createOffscreenAutoDrawable(null, caps, null, 640, 480);
    drawable.display();
    final GLContext context = drawable.getContext();
    context.makeCurrent();

    try {
      final GL3 g = new DebugGL3(context.getGL().getGL3());
      final IntBuffer cache = Buffers.newDirectIntBuffer(8);

      g.glGenBuffers(1, cache);
      final int eid = cache.get(0);
      g.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, eid);
      g.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, 100L, null, GL.GL_STATIC_DRAW);
      g.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

      g.glGenBuffers(1, cache);
      final int aid = cache.get(0);
      g.glBindBuffer(GL.GL_ARRAY_BUFFER, aid);
      g.glBufferData(GL.GL_ARRAY_BUFFER, 100L, null, GL.GL_STATIC_DRAW);
      g.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

      g.glGenVertexArrays(1, cache);
      final int vao = cache.get(0);
      g.glBindVertexArray(vao);
      g.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, eid);
      g.glBindBuffer(GL.GL_ARRAY_BUFFER, aid);
      g.glEnableVertexAttribArray(0);
      g.glVertexAttribIPointer(0, 1, GL.GL_UNSIGNED_BYTE, 1, 0L);
      g.glBindVertexArray(0);

      /**
       * An attribute of an integral type is bound using {@link
       * GL3#glVertexAttribIPointer(int, int, int, int, long)},
       * but the attribute in the shader is of a floating point type. The
       * OpenGL 3.3 specification says that executing the shader yields
       * undefined behaviour.
       */

      final Class<ShaderWrongAttributeType> c = ShaderWrongAttributeType.class;
      final int vsid = ShaderWrongAttributeType.compileVertexShader(
        g, cache, c, "/com/io7m/opengl_cases/integer0.vert");
      final int fsid = ShaderWrongAttributeType.compileFragmentShader(
        g, cache, c, vsid, "/com/io7m/opengl_cases/integer0.frag");
      final int pid =
        ShaderWrongAttributeType.compileProgram(g, cache, vsid, fsid);

      g.glBindVertexArray(vao);
      g.glUseProgram(pid);
      g.glDrawElements(GL.GL_POINTS, 100, GL.GL_UNSIGNED_BYTE, 0L);
      g.glUseProgram(0);
      g.glBindVertexArray(0);

    } finally {
      context.release();
      drawable.destroy();
    }
  }

  private static int compileProgram(
    final GL3 g,
    final IntBuffer cache,
    final int vsid,
    final int fsid)
  {
    final int pid;
    pid = g.glCreateProgram();
    g.glAttachShader(pid, vsid);
    g.glAttachShader(pid, fsid);
    g.glLinkProgram(pid);

    g.glGetProgramiv(pid, GL3.GL_LINK_STATUS, cache);
    final int status = cache.get(0);
    if (status == 0) {
      final ByteBuffer log_buffer = Buffers.newDirectByteBuffer(8192);
      final IntBuffer buffer_length = Buffers.newDirectIntBuffer(1);
      g.glGetShaderInfoLog(vsid, 8192, buffer_length, log_buffer);

      final byte[] raw = new byte[buffer_length.get(0)];
      log_buffer.get(raw);
      final String tt = new String(raw);
      throw new GLException("Program: " + tt);
    }
    return pid;
  }

  private static int compileFragmentShader(
    final GL3 g,
    final IntBuffer cache,
    final Class<ShaderWrongAttributeType> c,
    final int vsid,
    final String fs_name)
    throws IOException
  {
    final int fsid;
    try (final InputStream is = c.getResourceAsStream(fs_name)) {
      final List<String> lines = IOUtils.readLines(is);
      final IntBuffer lengths = Buffers.newDirectIntBuffer(lines.size());
      for (int index = 0; index < lines.size(); ++index) {
        lines.set(index, lines.get(index) + "\n");
        lengths.put(index, lines.get(index).length());
      }
      final String[] la = new String[lines.size()];
      lines.toArray(la);

      fsid = g.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
      g.glShaderSource(fsid, la.length, la, lengths);
      g.glCompileShader(fsid);

      g.glGetShaderiv(fsid, GL2ES2.GL_COMPILE_STATUS, cache);
      final int status = cache.get(0);
      if (status == 0) {
        final ByteBuffer log_buffer = Buffers.newDirectByteBuffer(8192);
        final IntBuffer buffer_length = Buffers.newDirectIntBuffer(1);
        g.glGetShaderInfoLog(vsid, 8192, buffer_length, log_buffer);

        final byte[] raw = new byte[buffer_length.get(0)];
        log_buffer.get(raw);
        final String tt = new String(raw);
        throw new GLException("Fragment shader: " + tt);
      }
    }
    return fsid;
  }

  private static int compileVertexShader(
    final GL3 g,
    final IntBuffer cache,
    final Class<ShaderWrongAttributeType> c,
    final String vs_name)
    throws IOException
  {
    final int vsid;
    try (final InputStream is = c.getResourceAsStream(vs_name)) {
      final List<String> lines = IOUtils.readLines(is);
      final IntBuffer lengths = Buffers.newDirectIntBuffer(lines.size());
      for (int index = 0; index < lines.size(); ++index) {
        lines.set(index, lines.get(index) + "\n");
        lengths.put(index, lines.get(index).length());
      }
      final String[] la = new String[lines.size()];
      lines.toArray(la);

      vsid = g.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
      g.glShaderSource(vsid, la.length, la, lengths);
      g.glCompileShader(vsid);

      g.glGetShaderiv(vsid, GL2ES2.GL_COMPILE_STATUS, cache);
      final int status = cache.get(0);
      if (status == 0) {
        final ByteBuffer log_buffer = Buffers.newDirectByteBuffer(8192);
        final IntBuffer buffer_length = Buffers.newDirectIntBuffer(1);
        g.glGetShaderInfoLog(vsid, 8192, buffer_length, log_buffer);

        final byte[] raw = new byte[buffer_length.get(0)];
        log_buffer.get(raw);
        final String tt = new String(raw);
        throw new GLException("Vertex shader: " + tt);
      }
    }
    return vsid;
  }
}
