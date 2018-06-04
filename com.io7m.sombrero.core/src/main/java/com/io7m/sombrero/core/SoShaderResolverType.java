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

import java.util.Map;
import java.util.Optional;

/**
 * A resolver for shader sources.
 */

public interface SoShaderResolverType
{
  /**
   * Resolve the file at {@code file}.
   *
   * @param file A file
   *
   * @return A reference to the file, if it exists
   *
   * @throws SoShaderException On errors
   */

  Optional<SoShaderFileReferenceType> resolve(
    String file)
    throws SoShaderException;

  /**
   * @return A map of the available modules
   */

  Map<String, SoShaderModuleType> available();
}