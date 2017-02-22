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
import com.io7m.sombrero.core.SoShaderResolverType;
import com.io7m.sombrero.osgi.SoShaderResolverOSGi;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public final class SoShaderModuleResolverOSGiTest extends
  SoShaderModuleResolverContract
{
  @Override
  protected SoShaderResolverType create()
  {
    final SoShaderResolverOSGi r = new SoShaderResolverOSGi();
    r.onModuleRegister(new ExampleModuleProvider());
    return r;
  }

  @Test
  public void testUnregister()
    throws Exception
  {
    final ExampleModuleProvider provider = new ExampleModuleProvider();

    final SoShaderResolverOSGi r = new SoShaderResolverOSGi();
    r.onModuleRegister(provider);

    {
      final Optional<SoShaderFileReferenceType> ref_opt =
        r.resolve("com.io7m.sombrero.example0/example.txt");
      Assert.assertTrue(ref_opt.isPresent());
    }

    r.onModuleUnregister(provider);

    {
      final Optional<SoShaderFileReferenceType> ref_opt =
        r.resolve("com.io7m.sombrero.example0/example.txt");
      Assert.assertFalse(ref_opt.isPresent());
    }
  }
}
