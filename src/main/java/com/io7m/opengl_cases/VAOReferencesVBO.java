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

public final class VAOReferencesVBO
{
  private VAOReferencesVBO()
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
      final IntBuffer cache = Buffers.newDirectIntBuffer(8);

      for (int i = 0; i < 10; ++i) {

        cache.rewind();
        g.glGenBuffers(1, cache);
        final int aid = cache.get(0);
        g.glBindBuffer(GL.GL_ARRAY_BUFFER, aid);
        g.glBufferData(
          GL.GL_ARRAY_BUFFER, 100_000_000L, null, GL.GL_STATIC_DRAW);
        g.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

        System.out.printf("Allocated VBO %d\n", aid);

        cache.rewind();
        g.glGenVertexArrays(1, cache);
        final int vao = cache.get(0);
        g.glBindVertexArray(vao);
        g.glBindBuffer(GL.GL_ARRAY_BUFFER, aid);
        g.glEnableVertexAttribArray(0);
        g.glVertexAttribPointer(0, 1, GL.GL_UNSIGNED_BYTE, false, 0, 0L);
        g.glBindVertexArray(0);

        System.out.printf("Allocated VAO %d, associated VBO %d\n", vao, aid);
        System.out.printf("Deleting VBO %d\n", aid);

        cache.rewind();
        cache.put(0, aid);
        g.glDeleteBuffers(1, cache);

        if (leak) {
          System.out.printf("Leaking VBO %d via VAO %d\n", aid, vao);
        } else {
          System.out.printf("Deleting VAO %d\n", vao);
          cache.rewind();
          cache.put(0, vao);
          g.glDeleteVertexArrays(1, cache);
        }

        try {
          Thread.sleep(2000L);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }

    } finally {
      context.release();
      drawable.destroy();
    }
  }
}
