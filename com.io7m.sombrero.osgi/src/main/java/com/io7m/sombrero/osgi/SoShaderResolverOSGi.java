/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
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

package com.io7m.sombrero.osgi;

import com.io7m.sombrero.core.SoShaderException;
import com.io7m.sombrero.core.SoShaderExceptionBadPath;
import com.io7m.sombrero.core.SoShaderFileReferenceType;
import com.io7m.sombrero.core.SoShaderModuleProviderType;
import com.io7m.sombrero.core.SoShaderModuleType;
import com.io7m.sombrero.core.SoShaderResolverType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * An <tt>OSGI</tt> based implementation of the {@link SoShaderResolverType}.
 */

@Component
public final class SoShaderResolverOSGi implements SoShaderResolverType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SoShaderResolverOSGi.class);
  }

  private final Map<String, SoShaderModuleType> modules;
  private final Map<String, SoShaderModuleType> modules_view;

  /**
   * Construct a new resolver.
   */

  public SoShaderResolverOSGi()
  {
    this.modules = new ConcurrentSkipListMap<>();
    this.modules_view = Collections.unmodifiableMap(this.modules);
  }

  /**
   * Register a module provider.
   *
   * @param provider The module provider
   */

  @Reference(
    cardinality = ReferenceCardinality.MULTIPLE,
    policy = ReferencePolicy.DYNAMIC,
    policyOption = ReferencePolicyOption.GREEDY,
    unbind = "onModuleUnregister")
  public void onModuleRegister(
    final SoShaderModuleProviderType provider)
  {
    final Map<String, SoShaderModuleType> available = provider.available();
    for (final String name : available.keySet()) {
      final SoShaderModuleType module = available.get(name);
      if (this.modules.containsKey(name)) {
        LOG.warn("multiple modules with the same name: {}", name);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("registered module {} via provider {}", name, provider);
      }
      this.modules.put(name, module);
    }
  }

  /**
   * Unregister a module provider.
   *
   * @param provider The module provider
   */

  public void onModuleUnregister(
    final SoShaderModuleProviderType provider)
  {
    final Map<String, SoShaderModuleType> available = provider.available();
    for (final String name : available.keySet()) {
      final SoShaderModuleType module = available.get(name);

      if (LOG.isDebugEnabled()) {
        LOG.debug("unregistered module {} via provider {}", name, provider);
      }
      this.modules.remove(name, module);
    }
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

      final SoShaderModuleType m = this.modules.get(modu_name);
      if (m != null) {
        final String file_name = file.substring(ind);
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
    return this.modules_view;
  }
}
