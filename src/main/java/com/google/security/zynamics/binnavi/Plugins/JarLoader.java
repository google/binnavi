/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.Plugins;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.IStandardDescriptionUpdater;
import com.google.security.zynamics.zylib.io.DirUtils;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.io.IDirectoryTraversalCallback;
import com.google.security.zynamics.zylib.plugins.ClassPathHacker;

import java.io.File;
import java.util.Set;

/**
 * Used to load the JAR files from the additional JAR file directory. These JARs can then be used by
 * plugins.
 */
public final class JarLoader {
  /**
   * Location of the JAR files directory relative to the BinNavi root directory.
   */
  private static final String JAR_DIRECTORY = "jars";

  /**
   * You are not supposed to instantiate this class.
   */
  private JarLoader() {}

  /**
   * Finds the JAR files in the additional JAR file directory.
   *
   * @param rootPath The BinNavi root path.
   *
   * @return The list of JAR files found.
   */
  public static Set<File> collectJars(final String rootPath) {
    Preconditions.checkNotNull(rootPath, "IE00827: BinNavi root path can't be null");

    final Set<File> jarFiles = Sets.newHashSet();

    final File jarDirectory = new File(rootPath + File.separator + JAR_DIRECTORY);

    DirUtils.traverse(jarDirectory, new IDirectoryTraversalCallback() {
      @Override
      public void entering(final File directory) {
        // Not necessary
      }

      @Override
      public void leaving(final File directory) {
        // Not necessary
      }

      @Override
      public void nextFile(final File file) {
        if (!FileUtils.getFileExtension(file).equals("jar")) {
          return;
        }
        jarFiles.add(file);
      }
    });

    return jarFiles;
  }

  /**
   * Loads the given JAR files.
   *
   * @param jarFiles List of JAR files to load.
   * @param descriptionUpdater Description updater.
   */
  public static void loadJars(final Set<File> jarFiles,
      final IStandardDescriptionUpdater descriptionUpdater) {
    Preconditions.checkNotNull(jarFiles, "IE00700: JAR files argument can not be null");

    Preconditions.checkNotNull(descriptionUpdater,
        "IE00701: Description updater argument can not be null");

    descriptionUpdater.reset();
    descriptionUpdater.setMaximum(jarFiles.size());

    for (final File file : jarFiles) {
      descriptionUpdater.next();
      descriptionUpdater.setDescription(String.format("Loading JAR file %s", file.getName()));

      NaviLogger.info("Loading JAR file %s", file.getAbsolutePath());

      ClassPathHacker.addFile(file);
    }
  }
}
