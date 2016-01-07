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
package com.google.security.zynamics.binnavi.Gui.plugins;

import com.google.security.zynamics.binnavi.Plugins.DisabledPluginReason;
import com.google.security.zynamics.binnavi.Plugins.IPluginRegistry;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.PluginConfigItem;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.JCheckedListbox;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

/**
 * List box in which the plugins can be selected for loading or skipping.
 *
 * @param <T> The interface which is used by plugins to interface with BinNavi.
 */
public final class PluginListBox<T> extends JCheckedListbox<PluginItem<T>> {

  /**
   * List model of the plugin list box.
   */
  private final DefaultListModel<PluginItem<T>> model;

  /**
   * Creates a new plugin list box.
   *
   * @param registry The plugin registry that provides the plugins to display.
   * @param configFile Provides information about which plugins to load and which plugins to skip.
   */
  public PluginListBox(
      final IPluginRegistry<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> registry,
      final ConfigManager configFile) {
    super(createPluginListModel(registry, configFile));
    model = (DefaultListModel<PluginItem<T>>) getModel();
    checkLoadedPlugins(configFile);
  }

  /**
   * Adds a plugin to the list model if the plugin identified by the GUID is loaded.
   *
   * @param registry The registry that provides all plugin information.
   * @param model The list model the plugin is added to.
   * @param guid The GUI to search for.
   * @param added All added plugins are added to this list too.
   * @param <T> The plugin interface which is used by plugins to interface with BinNavi.
   */
  private static <T> void addPluginIfLoaded(
      final IPluginRegistry<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> registry,
      final DefaultListModel<PluginItem<T>> model, final long guid,
      final List<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> added) {
    for (final com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T> plugin : registry) {
      if (guid == plugin.getGuid()) {
        model.addElement(new PluginItem<T>(plugin));
        added.add(plugin);
      }
    }
  }

  /**
   * Adds a plugin to the list model if the plugin identified by the GUID is not loaded.
   *
   * @param registry The registry that provides all plugin information.
   * @param model The list model the plugin is added to.
   * @param guid The GUI to search for.
   * @param added All added plugins are added to this list too.
   * @param <T> The plugin intertace which is used by plugins to interface with BinNavi.
   */
  private static <T> void addPluginIfUnloaded(
      final IPluginRegistry<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> registry,
      final DefaultListModel<PluginItem<T>> model, final long guid,
      final List<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> added) {
    for (final
        Pair<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>, DisabledPluginReason>
        pluginPair : registry.getDisabledPlugins()) {
      final com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T> plugin =
          pluginPair.first();

      // Do not replace this with added.contains(). Otherwise
      // all plugins need to implement equals(); that sucks
      // for scripted plugins.
      if (guid == plugin.getGuid()) {
        model.addElement(new PluginItem<T>(plugin));
        added.add(plugin);
      }
    }
  }

  /**
   * Adds all loaded plugins which are not mentioned in the configuration file to the list model.
   *
   * @param registry The registry that provides all plugin information.
   * @param model The list model the plugin is added to.
   * @param added All added plugins are added to this list too.
   * @param <T> The interface which is used by plugins to interface with BinNavi.
   */
  private static <T> void addUnmentionedPlugins(
      final IPluginRegistry<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> registry,
      final DefaultListModel<PluginItem<T>> model,
      final List<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> added) {
    for (final com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T> plugin : registry) {
      boolean contains = false;

      for (final com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T> plugin2 : added) {
        if (plugin == plugin2) {
          contains = true;
        }
      }

      if (!contains) {
        model.addElement(new PluginItem<T>(plugin));
      }
    }
  }

  /**
   * Creates the list model of the plugin list.
   *
   * @param registry The plugin registry from where the plugin information comes.
   * @param configFile Provides information about which plugins to load and which plugins to skip.
   * @param <T> The interface which is used by plugins to interface with
   *        com.google.security.zynamics.binnavi.
   * @return The list model created from the given arguments.
   */
  private static <T> DefaultListModel<PluginItem<T>> createPluginListModel(
      final IPluginRegistry<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> registry,
      final ConfigManager configFile) {
    final DefaultListModel<PluginItem<T>> model = new DefaultListModel<PluginItem<T>>();
    final ArrayList<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> added =
        new ArrayList<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>>();

    // The plugin order in the configuration file determines
    // the order in which the plugins are shown in the list
    // of the dialog. Only this way can the user decide
    // which plugins to load in which order.
    for (final PluginConfigItem plugin : configFile.getGeneralSettings().getPlugins()) {
      final long guid = plugin.getGUID();
      addPluginIfLoaded(registry, model, guid, added);
      addPluginIfUnloaded(registry, model, guid, added);
    }

    // Make sure to add the plugins that are not yet mentioned in the XML file
    addUnmentionedPlugins(registry, model, added);

    return model;
  }

  /**
   * Checks the plugins in the list box that were loaded. The plugins which were not loaded remain
   * unchecked.
   *
   * @param configFile Provides information about which plugins to load and which plugins to skip.
   */
  @SuppressWarnings("unchecked")
  private void checkLoadedPlugins(final ConfigManager configFile) {
    final ArrayList<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> checked =
        new ArrayList<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>>();

    for (int i = 0; i < model.getSize(); i++) {
      for (final PluginConfigItem plugin : configFile.getGeneralSettings().getPlugins()) {
        if (plugin.getGUID() == model.get(i).getObject().getGuid()) {
          setChecked(i, plugin.isLoad());
        }

        checked.add(model.get(i).getObject());
      }
    }

    // The plugins that are not mentioned in the XML file are loaded and checked by default.
    for (int i = configFile.getGeneralSettings().getPlugins().size(); i < model.getSize(); i++) {
      setChecked(i, true);
    }
  }

  /**
   * Switches two elements in the checked list box.
   *
   * @param first The index of the first element.
   * @param second The index of the second element.
   */
  public void switchElements(final int first, final int second) {
    // Switching two elements means switching the position
    // of the elements and switching the positions of the
    // checked boxes.

    final boolean oldFirst = isChecked(first);
    setChecked(first, isChecked(second));
    setChecked(second, oldFirst);

    final PluginItem<T> oldFirstObject = model.getElementAt(first);
    model.remove(first);
    model.insertElementAt(oldFirstObject, second);

    setSelectedIndex(second);
  }

}
