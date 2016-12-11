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

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.sombrero.core.SoShaderExceptionIO;
import com.io7m.sombrero.core.SoShaderFileReferenceType;
import com.io7m.sombrero.core.SoShaderModuleType;
import com.io7m.sombrero.core.SoShaderPreprocessorConfig;
import com.io7m.sombrero.core.SoShaderPreprocessorType;
import com.io7m.sombrero.core.SoShaderResolver;
import com.io7m.sombrero.core.SoShaderResolverType;
import org.apache.commons.io.IOUtils;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SoShaderPreprocessorContract
{
  @Rule public ExpectedException expected = ExpectedException.none();

  protected abstract SoShaderPreprocessorType create(
    SoShaderPreprocessorConfig config);

  @Test
  public final void testTrivial()
    throws Exception
  {
    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(330);
    final SoShaderPreprocessorConfig c = b.build();

    final SoShaderPreprocessorType preprocessor = this.create(c);
    final List<String> lines = preprocessor.preprocessFile(
      new HashMap<>(),
      "com.io7m.sombrero.example0/file0.h");

    Assert.assertEquals(2L, (long) lines.size());
    Assert.assertEquals("#version 330 core\n", lines.get(0));
    Assert.assertEquals("void file0();\n", lines.get(1));
  }

  @Test
  public final void testCrossModule()
    throws Exception
  {
    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(330);
    final SoShaderPreprocessorConfig c = b.build();

    final SoShaderPreprocessorType preprocessor = this.create(c);
    final List<String> lines =
      preprocessor.preprocessFile(
        new HashMap<>(),
        "com.io7m.sombrero.example0/cross_module.h");

    Assert.assertEquals(2L, (long) lines.size());
    Assert.assertEquals("#version 330 core\n", lines.get(0));
    Assert.assertEquals("void file0();\n", lines.get(1));
  }

  @Test
  public final void testSameModule()
    throws Exception
  {
    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(330);
    final SoShaderPreprocessorConfig c = b.build();

    final SoShaderPreprocessorType preprocessor = this.create(c);
    final List<String> lines =
      preprocessor.preprocessFile(
        new HashMap<>(),
        "com.io7m.sombrero.example0/same_module.h");

    Assert.assertEquals(2L, (long) lines.size());
    Assert.assertEquals("#version 330 core\n", lines.get(0));
    Assert.assertEquals("void file0();\n", lines.get(1));
  }

  @Test
  public final void testRelativeReject()
    throws Exception
  {
    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(330);
    final SoShaderPreprocessorConfig c = b.build();

    final SoShaderPreprocessorType preprocessor = this.create(c);

    this.expected.expect(SoShaderExceptionIO.class);
    this.expected.expectCause(IsInstanceOf.any(NoSuchFileException.class));
    preprocessor.preprocessFile(
      new HashMap<>(),
      "com.io7m.sombrero.example0/relative_reject.h");
  }

  @Test
  public final void testAbsoluteReject()
    throws Exception
  {
    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(330);
    final SoShaderPreprocessorConfig c = b.build();

    final SoShaderPreprocessorType preprocessor = this.create(c);

    this.expected.expect(SoShaderExceptionIO.class);
    this.expected.expectCause(IsInstanceOf.any(NoSuchFileException.class));
    preprocessor.preprocessFile(
      new HashMap<>(),
      "com.io7m.sombrero.example0/absolute_reject.h");
  }

  @Test
  public final void testAbsoluteQuoteReject()
    throws Exception
  {
    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(330);
    final SoShaderPreprocessorConfig c = b.build();

    final SoShaderPreprocessorType preprocessor = this.create(c);

    this.expected.expect(SoShaderExceptionIO.class);
    this.expected.expectCause(IsInstanceOf.any(NoSuchFileException.class));
    preprocessor.preprocessFile(
      new HashMap<>(),
      "com.io7m.sombrero.example0/absolute_quote_reject.h");
  }

  @Test
  public final void testTrivialDefines()
    throws Exception
  {
    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(330);
    final SoShaderPreprocessorConfig c = b.build();

    final SoShaderPreprocessorType preprocessor = this.create(c);

    final Map<String, String> defines = new HashMap<>();
    defines.put("DEFINE_0", "int0");
    defines.put("DEFINE_1", "int1");
    defines.put("DEFINE_2", "int2");

    final List<String> lines = preprocessor.preprocessFile(
      defines,
      "com.io7m.sombrero.example0/defines.h");

    Assert.assertEquals(4L, (long) lines.size());
    Assert.assertEquals("#version 330 core\n", lines.get(0));
    Assert.assertEquals("int int0;\n", lines.get(1));
    Assert.assertEquals("int int1;\n", lines.get(2));
    Assert.assertEquals("int int2;\n", lines.get(3));
  }

  @Test
  public final void testNoNewline()
    throws Exception
  {
    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(330);
    final SoShaderPreprocessorConfig c = b.build();

    final SoShaderPreprocessorType preprocessor = this.create(c);

    final Map<String, String> defines = new HashMap<>();
    final AtomicInteger warnings = new AtomicInteger(0);
    final List<String> lines = preprocessor.preprocessFileWithCallbacks(
      defines,
      "com.io7m.sombrero.example0/no_newline.h",
      (file, line, column, msg) -> {
        warnings.incrementAndGet();
      },
      (file, line, column, msg) -> {
        throw new UnreachableCodeException();
      });

    Assert.assertEquals(1L, (long) warnings.get());
  }

  @Test
  public final void testError()
    throws Exception
  {
    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(330);
    final SoShaderPreprocessorConfig c = b.build();

    final SoShaderPreprocessorType preprocessor = this.create(c);

    final Map<String, String> defines = new HashMap<>();
    final AtomicInteger errors = new AtomicInteger(0);
    final List<String> lines = preprocessor.preprocessFileWithCallbacks(
      defines,
      "com.io7m.sombrero.example0/errors.h",
      (file, line, column, msg) -> {
        throw new UnreachableCodeException();
      },
      (file, line, column, msg) -> {
        errors.incrementAndGet();
      });

    Assert.assertEquals(1L, (long) errors.get());
  }

  /**
   * <p>This checks that cross-module includes work correctly.</p>
   *
   * <p>com.io7m.sombrero.module1/module1.h includes
   * com.io7m.sombrero.module0/module0.h, which includes "included.h".</p>
   *
   * <p>If cross module includes work correctly, module0.h should get the
   * "included.h" file from its own module and not
   * com.io7m.sombrero.module1/included.h.</p>
   *
   * @throws Exception On errors
   */

  @Test
  public final void testCrossModuleCorrect()
    throws Exception
  {
    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(330);
    final SoShaderPreprocessorConfig c = b.build();

    final SoShaderPreprocessorType preprocessor = this.create(c);

    final Map<String, String> defines = new HashMap<>();

    final List<String> lines = preprocessor.preprocessFile(
      defines,
      "com.io7m.sombrero.module1/module1.h");

    Assert.assertEquals(2L, (long) lines.size());
    Assert.assertEquals("#version 330 core\n", lines.get(0));
    Assert.assertEquals("int ok;\n", lines.get(1));
  }
}
