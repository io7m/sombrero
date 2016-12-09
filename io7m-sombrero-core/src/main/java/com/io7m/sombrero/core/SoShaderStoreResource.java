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

package com.io7m.sombrero.core;

import com.io7m.jnull.NullCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

/**
 * A shader source based on Java resources. Resources will be resolved
 * relative to a given class.
 */

public final class SoShaderStoreResource implements SoShaderStoreType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SoShaderStoreResource.class);
  }

  private final String base;
  private final Class<?> clazz;

  private SoShaderStoreResource(
    final String in_base,
    final Class<?> in_clazz)
  {
    this.base = NullCheck.notNull(in_base, "Base");
    this.clazz = NullCheck.notNull(in_clazz, "Clazz");
  }

  /**
   * Construct a new shader store.
   *
   * @param base The base directory
   * @param c    The class that will be used to resolve resources
   *
   * @return A shader store
   */

  public static SoShaderStoreType create(
    final String base,
    final Class<?> c)
  {
    return new SoShaderStoreResource(base, c);
  }

  @Override
  public Optional<SoShaderFileReferenceType> lookup(
    final String name)
    throws SoShaderException
  {
    NullCheck.notNull(name, "name");

    final String target = this.base + "/" + name;

    if (LOG.isDebugEnabled()) {
      LOG.debug("open: {}:{}", this.clazz, target);
    }

    final URL url = this.clazz.getResource(target);
    if (url != null) {
      return Optional.of(new Reference(url));
    }

    return Optional.empty();
  }

  private final class Reference implements SoShaderFileReferenceType
  {
    private final URL url;

    Reference(final URL in_url)
    {
      this.url = NullCheck.notNull(in_url, "url");
    }

    @Override
    public InputStream stream()
      throws IOException
    {
      return this.url.openStream();
    }
  }
}
