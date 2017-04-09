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
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * A shader source based on Java resources. Resources will be resolved
 * relative to a given class.
 */

public final class SoShaderStoreResource implements SoShaderStoreType
{
  private static final Logger LOG;
  private static final Pattern SLASHES;

  static {
    LOG = LoggerFactory.getLogger(SoShaderStoreResource.class);
    SLASHES = Pattern.compile("/+");
  }

  private final String base;
  private final Function<String, URL> loader;

  private SoShaderStoreResource(
    final String in_base,
    final Function<String, URL> in_loader)
  {
    this.base = NullCheck.notNull(in_base, "Base");
    this.loader = NullCheck.notNull(in_loader, "Loader");
  }

  /**
   * Construct a new shader store.
   *
   * @param base The base directory
   * @param c    A function that will be used to resolve resources. This is
   *             intended to be a method reference to {@link
   *             Class#getResource(String)}.
   *
   * @return A shader store
   */

  public static SoShaderStoreType create(
    final String base,
    final Function<String, URL> c)
  {
    return new SoShaderStoreResource(base, c);
  }

  @Override
  public Optional<SoShaderFileReferenceType> lookup(
    final String name)
    throws SoShaderException
  {
    NullCheck.notNull(name, "name");

    final String target =
      SLASHES.matcher((this.base + "/" + name)).replaceAll("/");

    if (LOG.isDebugEnabled()) {
      LOG.debug("open: {} {}", this.loader, target);
    }

    final URL url = this.loader.apply(target);
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
