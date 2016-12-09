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

import com.io7m.sombrero.core.SoShaderExceptionIO;
import com.io7m.sombrero.core.SoShaderPreprocessorConfig;
import com.io7m.sombrero.core.SoShaderPreprocessorType;
import com.io7m.sombrero.core.SoShaderResolver;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SoShaderPreprocessorContract
{
  @Rule public ExpectedException expected = ExpectedException.none();

  protected abstract SoShaderPreprocessorType create(
    SoShaderPreprocessorConfig config);

  @Test
  public void testTrivial()
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
  public void testCrossModule()
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
  public void testSameModule()
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
  public void testRelativeReject()
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
  public void testAbsoluteReject()
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
  public void testAbsoluteQuoteReject()
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
  public void testTrivialDefines()
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
}
