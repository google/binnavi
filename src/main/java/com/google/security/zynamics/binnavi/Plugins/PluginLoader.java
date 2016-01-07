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
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.IStandardDescriptionUpdater;
import com.google.security.zynamics.zylib.io.DirUtils;
import com.google.security.zynamics.zylib.io.IDirectoryTraversalCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that loads all plugins that can be found in the plugins directory.
 */
public final class PluginLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private PluginLoader() {}

  /**
   * Loads all plugin files of a given directory.
   *
   * @param pluginPath The path to the plugins directory.
   * @param pluginFiles The plugin files to load.
   * @param descriptionUpdater Receives updates about the load progress. This argument can be null.
   *
   * @return The result of the load process.
   */
  private static <T> LoadResult<T> loadPluginFiles(final String pluginPath,
      final Set<File> pluginFiles, final IStandardDescriptionUpdater descriptionUpdater) {
    final
        ArrayList<Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>, PluginStatus>>
        loadedPlugins = new ArrayList<>();
    final ArrayList<Pair<String, Throwable>> failedPlugins = new ArrayList<>();

    for (final File pluginFile : pluginFiles) {
      if (pluginFile.getName().endsWith(".jar")) {
        descriptionUpdater.next();
        descriptionUpdater.setDescription(
            String.format("Loading plugin JAR file '%s'", pluginFile.getName()));

        JarPluginLoader.processJarFile(pluginFile, loadedPlugins, failedPlugins);
      } else if (pluginFile.getName().endsWith(".class")) {
        descriptionUpdater.next();
        descriptionUpdater.setDescription(
            String.format("Loading plugin CLASS file '%s'", pluginFile.getName()));

        ClassPluginLoader.processClassFile(pluginPath, pluginFile, loadedPlugins, failedPlugins);
      }
    }

    return new LoadResult<T>(loadedPlugins, failedPlugins);
  }

  /**
   * Validates the loaded plugins and disables those which are invalid.
   *
   * @param result Result of previous load operation.
   *
   * @return New load result that includes validation state.
   */
  private static <T> LoadResult<T> validateLoadedPlugins(final LoadResult<T> result) {
    // Load all plugins
    final HashSet<Long> guids = new HashSet<>();

    final
        ArrayList<Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>, PluginStatus>>
        validatedPlugins = new ArrayList<>();

    // Check the validity of the loaded plugins and remove those
    // which have problems. This part enforces the existence
    // of a proper plugin name and a unique GUID.
    for (final Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>, PluginStatus>
        pluginPair : result.getLoadedPlugins()) {
      final com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T> plugin =
          pluginPair.first();

      final String name = plugin.getName();
      final long guid = plugin.getGuid();

      if ((name == null) && (guid == 0)) {
        validatedPlugins.add(new Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>,
            PluginStatus>(plugin, PluginStatus.InvalidNameGuid));
      } else if (name == null) {
        validatedPlugins.add(new Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>,
            PluginStatus>(plugin, PluginStatus.InvalidName));
      } else if (guid == 0) {
        validatedPlugins.add(new Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>,
            PluginStatus>(plugin, PluginStatus.InvalidGuid));
      } else if (guids.contains(guid)) {
        validatedPlugins.add(new Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>,
            PluginStatus>(plugin, PluginStatus.DuplicateGuid));
      } else {
        validatedPlugins.add(new Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>,
            PluginStatus>(plugin, PluginStatus.Valid));
      }

      guids.add(guid);
    }

    return new LoadResult<T>(validatedPlugins, result.getFailedPlugins());
  }

  /**
   * Collects all plugin files from the plugin directory.
   *
   * @param directory The plugin directory.
   *
   * @return The collected plugin files.
   */
  public static Set<File> collectPluginFiles(final String directory) {
    final Set<File> pluginFiles = new HashSet<File>();

    final File file = new File(directory);

    DirUtils.traverse(file, new IDirectoryTraversalCallback() {
      @Override
      public void entering(final File directory) {
        System.out.println(directory.getName());
        // Unused
      }

      @Override
      public void leaving(final File directory) {
        // Unused
      }

      @Override
      public void nextFile(final File pluginFile) {
        System.out.println(pluginFile.getName());
        if (pluginFile.getName().endsWith(".jar") || pluginFile.getName().endsWith(".class")) {
          pluginFiles.add(pluginFile);
        }
      }
    });

    return pluginFiles;
  }

  /**
   * Loads the plugins, validates them and initializes those that are valid.
   *
   * @param pluginPath The path to the plugins directory.
   * @param pluginFiles The plugin files to load.
   * @param descriptionUpdater Receives updates about the load progress. This argument can be null.
   *
   * @return The result of the load operation.
   */
  public static <T> LoadResult<T> loadPlugins(final String pluginPath, final Set<File> pluginFiles,
      final IStandardDescriptionUpdater descriptionUpdater) {
    Preconditions.checkNotNull(pluginFiles, "IE00832: Plugin files can't be null");

    descriptionUpdater.reset();
    descriptionUpdater.setMaximum(pluginFiles.size());
    final LoadResult<T> loadResult = loadPluginFiles(pluginPath, pluginFiles, descriptionUpdater);
    return validateLoadedPlugins(loadResult);
  }
}
