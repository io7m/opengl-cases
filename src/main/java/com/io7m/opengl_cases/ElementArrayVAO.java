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
import com.jogamp.opengl.TraceGL3;

import java.nio.IntBuffer;

public final class ElementArrayVAO
{
  private ElementArrayVAO()
  {
    throw new AssertionError("Unreachable code!");
  }

  public static void main(final String[] args)
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
      final GL3 g =
        new DebugGL3(context.getGL().getGL3());
      final IntBuffer cache = Buffers.newDirectIntBuffer(8);

      {
        g.glGetIntegerv(GL.GL_ELEMENT_ARRAY_BUFFER_BINDING, cache);
        final int cid = cache.get(0);
        System.out.printf("initial binding: %d\n", cid);
        assert (cid == 0);
      }

      g.glGenBuffers(1, cache);
      final int bid = cache.get(0);
      System.out.printf("new buffer: %d\n", bid);
      g.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, bid);
      g.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, 100L, null, GL.GL_STATIC_DRAW);
      g.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

      {
        g.glGetIntegerv(GL.GL_ELEMENT_ARRAY_BUFFER_BINDING, cache);
        final int cid = cache.get(0);
        System.out.printf("binding prior to vertex array: %d\n", cid);
        assert (cid == 0);
      }

      g.glGenVertexArrays(1, cache);
      final int vao = cache.get(0);
      g.glBindVertexArray(vao);
      g.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, bid);

      {
        g.glGetIntegerv(GL.GL_ELEMENT_ARRAY_BUFFER_BINDING, cache);
        final int cid = cache.get(0);
        System.out.printf("binding in vertex array: %d\n", cid);
        assert (cid == bid);
      }

      g.glBindVertexArray(0);

      {
        g.glGetIntegerv(GL.GL_ELEMENT_ARRAY_BUFFER_BINDING, cache);
        final int cid = cache.get(0);
        System.out.printf("binding after vertex array: %d\n", cid);
        assert (cid == 0);
      }

      g.glBindVertexArray(vao);
      {
        g.glGetIntegerv(GL.GL_ELEMENT_ARRAY_BUFFER_BINDING, cache);
        final int cid = cache.get(0);
        System.out.printf("binding in vertex array: %d\n", cid);
        assert (cid == bid);
      }
      g.glBindVertexArray(0);

    } finally {
      context.release();
      drawable.destroy();
    }
  }
}
