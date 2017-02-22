/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
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

package com.io7m.sombrero.tests.core;

import com.io7m.sombrero.core.SoShaderExceptionBadPath;
import com.io7m.sombrero.core.SoShaderFileReferenceType;
import com.io7m.sombrero.core.SoShaderModuleType;
import com.io7m.sombrero.core.SoShaderResolverType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public abstract class SoShaderModuleResolverContract
{
  @Rule public ExpectedException expected = ExpectedException.none();

  protected abstract SoShaderResolverType create();

  @Test
  public final void testResolver()
    throws Exception
  {
    final SoShaderResolverType r = this.create();
    final Map<String, SoShaderModuleType> m = r.available();

    Assert.assertEquals(4L, (long) m.size());
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.example0"));
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.example1"));
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.module0"));
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.module1"));

    final Optional<SoShaderFileReferenceType> ref_opt = r.resolve(
      "com.io7m.sombrero.example0/example.txt");
    Assert.assertTrue(ref_opt.isPresent());
    final SoShaderFileReferenceType ref = ref_opt.get();

    try (final InputStream stream = ref.stream()) {
      final byte[] b = new byte[6];
      final int read = stream.read(b);
      Assert.assertEquals(6L, (long) read);
      Assert.assertEquals("Hello.", new String(b, StandardCharsets.UTF_8));
    }
  }

  @Test
  public final void testResolverNonexistentModule()
    throws Exception
  {
    final SoShaderResolverType r = this.create();
    final Map<String, SoShaderModuleType> m = r.available();

    Assert.assertEquals(4L, (long) m.size());
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.example0"));
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.example1"));
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.module0"));
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.module1"));

    final Optional<SoShaderFileReferenceType> ref_opt = r.resolve(
      "com.io7m.nonexistent/example.txt");
    Assert.assertFalse(ref_opt.isPresent());
  }

  @Test
  public final void testResolverNoModule()
    throws Exception
  {
    final SoShaderResolverType r = this.create();
    final Map<String, SoShaderModuleType> m = r.available();

    Assert.assertEquals(4L, (long) m.size());
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.example0"));
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.example1"));
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.module0"));
    Assert.assertTrue(m.containsKey("com.io7m.sombrero.module1"));

    this.expected.expect(SoShaderExceptionBadPath.class);
    r.resolve("example.txt");
  }
}
