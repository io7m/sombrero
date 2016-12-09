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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The default implementation of the {@link SoShaderResolverType}.
 */

public final class SoShaderResolver implements SoShaderResolverType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SoShaderResolver.class);
  }

  private final Map<String, SoShaderModuleType> modules;

  private SoShaderResolver(
    final Map<String, SoShaderModuleType> in_modules)
  {
    this.modules =
      Collections.unmodifiableMap(NullCheck.notNull(in_modules, "modules"));
  }

  /**
   * <p>Create a new shader resolver.</p>
   *
   * <p>The resolver uses the {@link ServiceLoader} API to obtain all
   * available {@link SoShaderModuleProviderType} implementations and
   * registers all available modules.</p>
   *
   * @return A new shader resolver
   */

  public static SoShaderResolverType create()
  {
    final Map<String, SoShaderModuleType> modules = new HashMap<>();

    final ServiceLoader<SoShaderModuleProviderType> providers =
      ServiceLoader.load(SoShaderModuleProviderType.class);

    final Iterator<SoShaderModuleProviderType> iter = providers.iterator();
    while (iter.hasNext()) {
      final SoShaderModuleProviderType provider = iter.next();
      final Map<String, SoShaderModuleType> available = provider.available();

      for (final String name : available.keySet()) {
        final SoShaderModuleType module = available.get(name);
        if (modules.containsKey(name)) {
          LOG.warn("multiple modules with the same name: {}", name);
        }

        if (LOG.isDebugEnabled()) {
          LOG.debug("registered module {} via provider {}", name, provider);
        }
        modules.put(name, module);
      }
    }

    return new SoShaderResolver(modules);
  }

  @Override
  public Optional<SoShaderFileReferenceType> resolve(
    final String file)
    throws SoShaderException
  {
    LOG.debug("resolve: {}", file);

    final int ind = file.indexOf("/");
    if (ind != -1) {
      final String modu_name = file.substring(0, ind);
      final String file_name = file.substring(ind);

      if (this.modules.containsKey(modu_name)) {
        final SoShaderModuleType m = this.modules.get(modu_name);
        if (LOG.isDebugEnabled()) {
          LOG.debug("lookup [{}]: {}", modu_name, file_name);
        }
        return m.store().lookup(file_name);
      }

      LOG.debug("nonexistent module: {}", modu_name);
      return Optional.empty();
    }

    final StringBuilder sb = new StringBuilder(128);
    sb.append("Invalid path.");
    sb.append(System.lineSeparator());
    sb.append("  Expected: A path of the form module/file");
    sb.append(System.lineSeparator());
    sb.append("  Recevied: ");
    sb.append(file);
    sb.append(System.lineSeparator());
    throw new SoShaderExceptionBadPath(sb.toString());
  }

  @Override
  public Map<String, SoShaderModuleType> available()
  {
    return this.modules;
  }
}
