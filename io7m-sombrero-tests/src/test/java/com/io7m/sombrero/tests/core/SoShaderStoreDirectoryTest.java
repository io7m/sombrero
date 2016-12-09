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

import com.io7m.sombrero.core.SoShaderFileReferenceType;
import com.io7m.sombrero.core.SoShaderStoreDirectory;
import com.io7m.sombrero.core.SoShaderStoreType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;

public final class SoShaderStoreDirectoryTest
{
  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void testLookupNonexistent()
    throws Exception
  {
    final Path base = Files.createTempDirectory("sombrero-");
    final SoShaderStoreType source = SoShaderStoreDirectory.create(base);
    Assert.assertEquals(Optional.empty(), source.lookup("/file.txt"));
  }

  @Test
  public void testLookupOutsideBase()
    throws Exception
  {
    final Path base = Files.createTempDirectory("sombrero-");
    final SoShaderStoreType source = SoShaderStoreDirectory.create(base);
    Assert.assertEquals(Optional.empty(), source.lookup("../../file.txt"));
  }

  @Test
  public void testLookupExists()
    throws Exception
  {
    final Path base = Files.createTempDirectory("sombrero-");

    try (final OutputStream os =
           Files.newOutputStream(base.resolve("file.txt"))) {
      os.write("Hello".getBytes(StandardCharsets.UTF_8));
    }

    final SoShaderStoreType source = SoShaderStoreDirectory.create(base);

    final Optional<SoShaderFileReferenceType> ref_opt = source.lookup("/file.txt");
    if (ref_opt.isPresent()) {
      final SoShaderFileReferenceType ref = ref_opt.get();
      try (final InputStream is = ref.stream()) {
        final byte[] b = new byte[5];
        final int r = is.read(b);
        Assert.assertEquals(5L, (long) r);
        Assert.assertEquals("Hello", new String(b, StandardCharsets.UTF_8));
      }
    } else {
      Assert.fail();
    }
  }
}
