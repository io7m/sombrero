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

package com.io7m.sombrero.jcpp;

import com.io7m.jnull.NullCheck;
import com.io7m.sombrero.core.SoShaderException;
import com.io7m.sombrero.core.SoShaderExceptionIO;
import com.io7m.sombrero.core.SoShaderFileReferenceType;
import com.io7m.sombrero.core.SoShaderPreprocessorCallbackErrorType;
import com.io7m.sombrero.core.SoShaderPreprocessorCallbackWarningType;
import com.io7m.sombrero.core.SoShaderPreprocessorConfig;
import com.io7m.sombrero.core.SoShaderPreprocessorType;
import com.io7m.sombrero.core.SoShaderResolverType;
import org.anarres.cpp.InputLexerSource;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.PreprocessorListener;
import org.anarres.cpp.Source;
import org.anarres.cpp.Token;
import org.anarres.cpp.VirtualFile;
import org.anarres.cpp.VirtualFileSystem;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * A JCPP-based preprocessor implementation.
 */

public final class SoShaderPreprocessorJCPP implements SoShaderPreprocessorType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SoShaderPreprocessorJCPP.class);
  }

  private final SoShaderPreprocessorConfig config;
  private final List<String> modules;
  private final ProcessorFilesystem filesystem;

  private SoShaderPreprocessorJCPP(
    final SoShaderPreprocessorConfig in_config)
  {
    this.config = NullCheck.notNull(in_config, "Config");
    this.modules = this.config.resolver().available().keySet()
      .stream().collect(Collectors.toList());
    this.filesystem = new ProcessorFilesystem();
  }

  /**
   * Create a new preprocessor.
   *
   * @param config The preprocessor configuration
   *
   * @return A new preprocessor
   */

  public static SoShaderPreprocessorType create(
    final SoShaderPreprocessorConfig config)
  {
    return new SoShaderPreprocessorJCPP(config);
  }

  @Override
  public List<String> preprocessFileWithCallbacks(
    final Map<String, String> defines,
    final String file,
    final SoShaderPreprocessorCallbackWarningType on_warning,
    final SoShaderPreprocessorCallbackErrorType on_error)
    throws SoShaderException
  {
    try (final Processor proc =
           new Processor(defines, file, on_warning, on_error)) {
      return proc.run();
    } catch (final IOException e) {
      throw new SoShaderExceptionIO(e);
    }
  }

  private final class ProcessorFilesystem implements VirtualFileSystem
  {
    ProcessorFilesystem()
    {

    }

    @Nonnull
    @Override
    public VirtualFile getFile(
      final @Nonnull String path)
    {
      LOG.trace("getFile: {}", path);
      return new ProcessorFile(path);
    }

    @Nonnull
    @Override
    public VirtualFile getFile(
      final @Nonnull String dir,
      final @Nonnull String name)
    {
      if (LOG.isTraceEnabled()) {
        LOG.trace("getFile: {} {}", dir, name);
      }

      final StringBuilder sb =
        new StringBuilder(dir.length() + name.length() + 1);

      final int ind = name.indexOf("/");
      if (ind != -1) {
        sb.append(name);
      } else {
        sb.append(dir);
        sb.append("/");
        sb.append(name);
      }
      final String target = sb.toString();

      if (LOG.isTraceEnabled()) {
        LOG.trace("transformed {} + {} -> {}", dir, name, target);
      }

      return new ProcessorFile(target);
    }

    private final class ProcessorFile implements VirtualFile
    {
      private final String file_name;

      private ProcessorFile(
        final String in_file_name)
      {
        this.file_name = NullCheck.notNull(in_file_name, "File name");
      }

      @Override
      public boolean isFile()
      {
        return true;
      }

      @Nonnull
      @Override
      public String getPath()
      {
        return this.file_name;
      }

      @Nonnull
      @Override
      public String getName()
      {
        return this.file_name;
      }

      @Override
      public VirtualFile getParentFile()
      {
        final int idx = this.file_name.lastIndexOf('/');
        if (idx < 1) {
          return null;
        }
        return new ProcessorFile(this.file_name.substring(0, idx));
      }

      @Nonnull
      @Override
      public VirtualFile getChildFile(
        final String name)
      {
        return new ProcessorFile(this.file_name + "/" + name);
      }

      @Nonnull
      @Override
      public Source getSource()
        throws IOException
      {
        final SoShaderResolverType res =
          SoShaderPreprocessorJCPP.this.config.resolver();

        try {
          final Optional<SoShaderFileReferenceType> ref_opt =
            res.resolve(this.file_name);

          if (ref_opt.isPresent()) {
            return new ProcessorSource(ref_opt.get().stream());
          }
        } catch (final SoShaderException e) {
          throw new IOException(e);
        }
        throw new NoSuchFileException(this.file_name);
      }

      private final class ProcessorSource extends InputLexerSource
      {
        ProcessorSource(
          final InputStream in_stream)
        {
          super(in_stream, StandardCharsets.UTF_8);
        }

        @Override
        public String getPath()
        {
          return ProcessorFile.this.file_name;
        }

        @Override
        public String getName()
        {
          return ProcessorFile.this.file_name;
        }
      }
    }
  }

  private final class Processor implements Closeable, PreprocessorListener
  {
    private final String file;
    private final Preprocessor pp;
    private final Map<String, String> defines;
    private final SoShaderPreprocessorCallbackWarningType on_warning;
    private final SoShaderPreprocessorCallbackErrorType on_error;

    Processor(
      final Map<String, String> in_defines,
      final String in_file,
      final SoShaderPreprocessorCallbackWarningType on_warning,
      final SoShaderPreprocessorCallbackErrorType on_error)
    {
      this.defines = NullCheck.notNull(in_defines, "Defines");
      this.file = NullCheck.notNull(in_file, "file");
      this.on_warning = NullCheck.notNull(on_warning, "on_warning");
      this.on_error = NullCheck.notNull(on_error, "on_error");
      this.pp = new Preprocessor();
    }

    @Override
    public void close()
      throws IOException
    {
      this.pp.close();
    }

    public List<String> run()
      throws SoShaderException
    {
      try {
        final SoShaderPreprocessorJCPP p = SoShaderPreprocessorJCPP.this;
        this.pp.setSystemIncludePath(p.modules);
        this.pp.setFileSystem(p.filesystem);
        this.pp.addInput(p.filesystem.getFile(this.file).getSource());
        this.pp.setListener(this);

        this.setupDefines();
        final int bsize = 2 << 14;
        try (final ByteArrayOutputStream bao =
               new ByteArrayOutputStream(bsize)) {
          this.processTokens(bao);
          return this.processLines(bao);
        }
      } catch (final IOException e) {
        throw new SoShaderExceptionIO(e);
      }
    }

    private void processTokens(
      final ByteArrayOutputStream bao)
      throws IOException
    {
      try (final PrintStream baos = new PrintStream(bao, false, "UTF-8")) {
        final OptionalInt version =
          SoShaderPreprocessorJCPP.this.config.version();
        if (version.isPresent()) {
          baos.printf(
            "#version %d core\n", Integer.valueOf(version.getAsInt()));
        }

        while (true) {
          final Token tok = this.pp.token();
          if (tok.getType() == Token.EOF) {
            break;
          }
          baos.print(tok.getText());
        }
        baos.flush();
      } catch (final LexerException e) {
        throw new IOException(e);
      }
    }

    private List<String> processLines(
      final ByteArrayOutputStream bao)
      throws IOException
    {
      try (final ByteArrayInputStream bai =
             new ByteArrayInputStream(bao.toByteArray())) {
        final List<String> lines =
          IOUtils.readLines(bai, StandardCharsets.UTF_8);
        return lines.stream()
          .filter(s -> !s.trim().isEmpty())
          .map(line -> line + "\n")
          .collect(Collectors.toList());
      }
    }

    private void setupDefines()
      throws SoShaderException
    {
      for (final String name : this.defines.keySet()) {
        final String value = this.defines.get(name);
        if (LOG.isTraceEnabled()) {
          LOG.trace("define {} {}", name, value);
        }
        try {
          this.pp.addMacro(name, value);
        } catch (final LexerException e) {
          throw new SoShaderException(e);
        }
      }
    }

    @Override
    public void handleWarning(
      final @Nonnull Source source,
      final int line,
      final int column,
      final @Nonnull String msg)
      throws LexerException
    {
      try {
        this.on_warning.onWarning(source.getPath(), line, column, msg);
      } catch (final SoShaderException e) {
        throw new LexerException(e);
      }
    }

    @Override
    public void handleError(
      final @Nonnull Source source,
      final int line,
      final int column,
      final @Nonnull String msg)
      throws LexerException
    {
      try {
        this.on_error.onError(source.getPath(), line, column, msg);
      } catch (final SoShaderException e) {
        throw new LexerException(e);
      }
    }

    @Override
    public void handleSourceChange(
      final @Nonnull Source source,
      final @Nonnull SourceChangeEvent event)
    {
      if (LOG.isTraceEnabled()) {
        LOG.trace("handleSourceChange: {} {}", source, event);
      }
    }
  }
}
