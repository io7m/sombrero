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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * A shader store based on a directory. Requests for files are restricted to
 * descendants of the base directory.
 */

public final class SoShaderStoreDirectory implements SoShaderStoreType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SoShaderStoreDirectory.class);
  }

  private final Path base;

  private SoShaderStoreDirectory(
    final Path in_base)
  {
    this.base = NullCheck.notNull(in_base, "Base");
  }

  /**
   * Construct a new shader store.
   *
   * @param base The base directory
   *
   * @return A shader store
   */

  public static SoShaderStoreType create(final Path base)
  {
    return new SoShaderStoreDirectory(base);
  }

  @Override
  public Optional<SoShaderFileReferenceType> lookup(
    final String name)
    throws SoShaderException
  {
    NullCheck.notNull(name, "name");

    final Path target =
      Paths.get(this.base.toString() + "/" + name).normalize();

    LOG.debug("open: {}", target);
    if (target.startsWith(this.base)) {
      if (Files.exists(target)) {
        return Optional.of(new Reference(target));
      }
      return Optional.empty();
    }

    if (LOG.isWarnEnabled()) {
      LOG.warn(
        "Refusing to allow access out of the base directory (base {}, request {}, actual {})",
        this.base,
        name,
        target);
    }

    return Optional.empty();
  }

  private static final class Reference implements SoShaderFileReferenceType
  {
    private final Path path;

    private Reference(final Path target)
    {
      this.path = NullCheck.notNull(target, "Target");
    }

    @Override
    public InputStream stream()
      throws IOException
    {
      return Files.newInputStream(this.path);
    }
  }
}
