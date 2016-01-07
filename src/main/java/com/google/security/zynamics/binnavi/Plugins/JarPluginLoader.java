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

import com.google.security.zynamics.binnavi.API.plugins.IPluginServer;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.plugins.ClassPathHacker;
import com.google.security.zynamics.zylib.plugins.JarClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.Attributes;



/**
 * Contains code for loading plugins from JAR files.
 */
public final class JarPluginLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private JarPluginLoader() {
  }

  /**
   * Tries to find the main class name out of a JAR package.
   * 
   * @param file The JAR file whose main class is determined.
   * 
   * @return The name of the main class of the JAR file or null.
   * 
   * @throws IOException Thrown if any IO error occurs.
   */
  private static String getMainClassName(final File file) throws IOException {
    final URL jarUrl = new URL("jar", "", file.toURI().toURL() + "!/");
    final JarURLConnection urlConnection = (JarURLConnection) jarUrl.openConnection();
    final Attributes attr = urlConnection.getMainAttributes();
    return attr == null ? null : attr.getValue(Attributes.Name.MAIN_CLASS);
  }

  /**
   * Loads a plugin JAR file.
   * 
   * @param pluginFile The JAR file to load.
   * 
   * @return A list of all plugins that were found in the JAR file.
   * 
   * @throws ClassNotFoundException Thrown if the class could not be found by the JVM.
   * @throws InstantiationException Thrown if the class object could not be instantiated.
   * @throws IllegalAccessException ???
   * @throws IOException Thrown if any IO error occurs.
   */
  private static <T> Collection<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> loadPluginJar(final File pluginFile)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
    NaviLogger.info("Loading plugins from " + pluginFile.getAbsolutePath());

    final JarClassLoader jarLoader = new JarClassLoader(pluginFile.getAbsolutePath());

    final String mainClassName = getMainClassName(pluginFile);

    NaviLogger.info("Determined main class as " + mainClassName);

    if (mainClassName == null) {
      return new ArrayList<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>>();
    }

    /* Load the class from the .jar file and resolve it. */
    final Class<?> cls = jarLoader.loadClass(mainClassName, true);

    if (IPluginServer.class.isAssignableFrom(cls)) {
      ClassPathHacker.addFile(pluginFile);

      @SuppressWarnings("unchecked")
      final IPluginServer<T> server = (IPluginServer<T>) cls.newInstance();
      return server.getPlugins();
    }

    return new ArrayList<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>>();
  }

  /**
   * Processes a single JAR file that potentially contains plugins.
   * 
   * @param pluginFile The class file to load.
   * @param loadedPlugins If plugins are found and loaded, they are added to this list.
   * @param failedPlugins If plugins are found but fails to load, they are added to this list.
   */
  public static <T> void processJarFile(final File pluginFile,
      final List<Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>, PluginStatus>> loadedPlugins,
      final List<Pair<String, Throwable>> failedPlugins) {
    // ESCA-JAVA0166:
    try {
      final Collection<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> plugins = loadPluginJar(pluginFile);

      if (plugins != null) {
        for (final com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T> plugin : plugins) {
          loadedPlugins.add(new Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>, PluginStatus>(plugin,
              PluginStatus.Valid));
        }
      }

    } catch (final Exception e) {
      // Will be displayed later

      failedPlugins.add(new Pair<String, Throwable>(pluginFile.getAbsolutePath(), e));
    }
  }

}
