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

import java.util.List;
import java.util.Map;

/**
 * The type of preprocessors.
 */

public interface SoShaderPreprocessorType
{
  /**
   * Preprocess a file.
   *
   * @param defines The set of preprocessor defines
   * @param file    The file
   *
   * @return The preprocessed file lines
   *
   * @throws SoShaderException On errors
   */

  default List<String> preprocessFile(
    final Map<String, String> defines,
    final String file)
    throws SoShaderException
  {
    return this.preprocessFileWithCallbacks(
      defines,
      file,
      (file_name, line, column, msg) -> {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("Warning: ");
        sb.append(file);
        sb.append(":");
        sb.append(line);
        sb.append(":");
        sb.append(column);
        sb.append(": ");
        sb.append(msg);
        sb.append(System.lineSeparator());
        sb.append("Note: treating warnings as fatal");
        sb.append(System.lineSeparator());
        throw new SoShaderException(sb.toString());
      },
      (file_name, line, column, msg) -> {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("Error: ");
        sb.append(file);
        sb.append(":");
        sb.append(line);
        sb.append(":");
        sb.append(column);
        sb.append(": ");
        sb.append(msg);
        sb.append(System.lineSeparator());
        throw new SoShaderException(sb.toString());
      });
  }

  /**
   * Preprocess a file.
   *
   * @param defines    The set of preprocessor defines
   * @param file       The file
   * @param on_warning Evaluated on warnings
   * @param on_error   Evaluated on errors
   *
   * @return The preprocessed file lines
   *
   * @throws SoShaderException On errors
   */

  List<String> preprocessFileWithCallbacks(
    final Map<String, String> defines,
    final String file,
    final SoShaderPreprocessorCallbackWarningType on_warning,
    final SoShaderPreprocessorCallbackErrorType on_error)
    throws SoShaderException;
}
