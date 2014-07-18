/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SquidTestUtils {

  /**
   * Use squid to scan directories.
   */
  public static void scanDirectories(JavaSquid squid, Collection<File> sourceDirectories, Collection<File> bytecodeFilesOrDirectories) {
    List<InputFile> sourceFiles = Lists.newArrayList();
    for (File dir : sourceDirectories) {
      for(File source: FileUtils.listFiles(dir, new String[]{"java"}, true)) {
        InputFile javaFile = new DefaultInputFile(source.getPath()).setLanguage("java").setFile(source);
        sourceFiles.add(javaFile);
      }
    }
    squid.scan(sourceFiles, Collections.<InputFile>emptyList(), bytecodeFilesOrDirectories);
  }

}
