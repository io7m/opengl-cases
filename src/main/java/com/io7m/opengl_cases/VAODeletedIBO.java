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
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;

import java.nio.IntBuffer;

public final class VAODeletedIBO
{
  private VAODeletedIBO()
  {
    throw new AssertionError("Unreachable code!");
  }

  public static void main(final String[] args)
  {
    final boolean leak = args.length > 0 && "leak".equals(args[0]);

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
      final IntBuffer cache = Buffers.newDirectIntBuffer(1);

      final int vao0;
      final int i0;

      cache.rewind();
      g.glGenBuffers(1, cache);
      i0 = cache.get(0);

      cache.rewind();
      g.glGenVertexArrays(1, cache);
      vao0 = cache.get(0);
      g.glBindVertexArray(vao0);
      g.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, i0);
      g.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, 100L, null, GL.GL_STATIC_DRAW);
      g.glBindVertexArray(0);

      {
        g.glGetIntegerv(GL.GL_ELEMENT_ARRAY_BUFFER_BINDING, cache);
        final int cid = cache.get(0);
        System.out.printf("ibo binding after vertex array: %d\n", cid);
        assert (cid == 0);
      }

      g.glBindVertexArray(vao0);
      {
        g.glGetIntegerv(GL.GL_ELEMENT_ARRAY_BUFFER_BINDING, cache);
        final int cid = cache.get(0);
        System.out.printf("ibo binding after vertex array: %d\n", cid);
        assert (cid == i0);
      }

      /**
       * OpenGL 3.3, section 2.9, text page 40:
       *
       * "If a buffer object is deleted while it is bound, all bindings to
       * that object in the current context (i.e. in the thread that called
       * DeleteBuffers) are reset to zero."
       */

      cache.rewind();
      cache.put(0, i0);
      g.glDeleteBuffers(1, cache);
      {
        g.glGetIntegerv(GL.GL_ELEMENT_ARRAY_BUFFER_BINDING, cache);
        final int cid = cache.get(0);
        System.out.printf("ibo binding after buffer deletion: %d\n", cid);
        assert (cid == 0);
      }
      g.glBindVertexArray(0);

    } finally {
      context.release();
      drawable.destroy();
    }
  }
}
