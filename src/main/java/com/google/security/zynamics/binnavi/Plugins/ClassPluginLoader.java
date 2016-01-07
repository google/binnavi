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



import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.plugins.FileClassLoader;

import java.io.File;
import java.util.List;



/**
 * Contains code for loading plugins from CLASS files.
 */
public final class ClassPluginLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private ClassPluginLoader() {
  }

  /**
   * Loads a single class file.
   * 
   * @param file The class file to load.
   * @param pluginDirectory The plugin base directory.
   * 
   * @return If the class contains an IPlugin object, a new instance of that object. Null,
   *         otherwise.
   * 
   * @throws ClassNotFoundException Thrown if the class could not be found by the JVM.
   * @throws InstantiationException Thrown if the class object could not be instantiated.
   * @throws IllegalAccessException ???
   */
  @SuppressWarnings("unchecked")
  private static <T> com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T> loadClassFile(final File file,
      final String pluginDirectory) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    NaviLogger.info("Loading class file %s", file.getAbsolutePath());

    final FileClassLoader loader =
        new FileClassLoader(file.getParentFile().getPath() + System.getProperty("file.separator"));

    // At this point we are trying to determine the fully qualified name of the class
    // depending on what sub-directory of the plugin directory it resides in.
    final String relativePath =
        pluginDirectory.length() == file.getParentFile().getPath().length() ? "" : file
            .getParentFile().getPath().substring(pluginDirectory.length());
    final String classPrefix = relativePath.replaceAll("[/\\\\]", ".");

    final Class<?> cls =
        loader.loadClass(("".equals(classPrefix) ? "" : classPrefix + ".")
            + FileUtils.getFileBasename(file));

    if (com.google.security.zynamics.binnavi.api2.plugins.IPlugin.class.isAssignableFrom(cls)) {
      return (com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>) cls.newInstance();
    } else {
      return null;
    }
  }

  /**
   * Processes a single class file that potentially contains a plugin.
   * 
   * @param directory The plugin base directory.
   * @param pluginFile The class file to load.
   * @param loadedPlugins If the plugin is loaded, the plugin is added to this list.
   * @param failedPlugins If the plugin fails to load, the plugin is added to this list.
   */
  public static <T> void processClassFile(final String directory, final File pluginFile,
      final List<Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>, PluginStatus>> loadedPlugins,
      final List<Pair<String, Throwable>> failedPlugins) {
    // ESCA-JAVA0166:
    try {
      final com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T> plugin = loadClassFile(pluginFile, directory);

      if (plugin == null) {
        return;
      }

      loadedPlugins.add(new Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>, PluginStatus>(plugin,
          PluginStatus.Valid));
    } catch (final Exception e) {
      // Will be displayed later

      failedPlugins.add(new Pair<String, Throwable>(pluginFile.getAbsolutePath(), e));
    }
  }
}
