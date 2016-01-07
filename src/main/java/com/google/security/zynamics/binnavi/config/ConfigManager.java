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
package com.google.security.zynamics.binnavi.config;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.common.config.ConfigHelper;
import com.google.security.zynamics.common.config.TypedPropertiesWrapper;

import java.awt.Frame;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;

/**
 * A class that is used to read/write BinNavi configuration.
 */
public final class ConfigManager {

  private static final String DATABASES_PREFIX = "Databases.";
  private static final String DATABASES_COUNT = DATABASES_PREFIX + "Count";
  private static final int DATABASES_COUNT_DEFAULT = 0;

  /** This class is a singleton. */
  private static final ConfigManager instance = new ConfigManager(ConfigHelper.getConfigFileName(
      Constants.COMPANY_NAME, Constants.PROJECT_NAME, Constants.CONFIG_FILE_NAME));

  /**
   * Config file object that mirrors the XML file.
   */
  private final TypedPropertiesWrapper properties;

  private final List<DatabaseConfigItem> databases;
  private final GeneralSettingsConfigItem generalSettings;
  private final ColorsConfigItem colors;
  private final DebugColorsConfigItem debuggerColors;
  private final CallGraphSettingsConfigItem callGraphSettings;
  private final FlowGraphSettingsConfigItem flowGraphSettings;

  /**
   * Filename of the config file.
   */
  private final String filename;

  /**
   * Creates a new config file object.
   *
   * @param filename Filename of the config file.
   */
  private ConfigManager(final String filename) {
    this.filename = filename;
    properties = new TypedPropertiesWrapper(new Properties());
    databases = Lists.newArrayList();
    generalSettings = new GeneralSettingsConfigItem();
    colors = new ColorsConfigItem();
    debuggerColors = new DebugColorsConfigItem();
    callGraphSettings = new CallGraphSettingsConfigItem();
    flowGraphSettings = new FlowGraphSettingsConfigItem();
  }

  /**
   * Updates the graph settings stored in the configuration from given graph view settings.
   *
   * @param graphSettings Settings to be updated.
   * @param settings Source settings
   */
  private static void updateGraphSettings(
      final GraphSettingsConfigItem graphSettings, final ZyGraphViewSettings settings) {
    graphSettings.setAutomaticLayouting(settings.getLayoutSettings().getAutomaticLayouting());
    graphSettings.setProximityBrowsing(settings.getProximitySettings().getProximityBrowsing());
    graphSettings.setProximityBrowsingThreshold(
        settings.getProximitySettings().getProximityBrowsingActivationThreshold());
    graphSettings.setAutoLayoutDeactivationThreshold(
        settings.getLayoutSettings().getAutolayoutDeactivationThreshold());
    graphSettings.setLayoutCalculationThreshold(
        settings.getLayoutSettings().getLayoutCalculationTimeWarningThreshold());
    graphSettings.setVisibilityWarningThreshold(
        settings.getLayoutSettings().getVisibilityWarningTreshold());

    graphSettings.setHierarchicMinimumLayerDistance(
        settings.getLayoutSettings().getHierarchicalSettings().getMinimumLayerDistance());
    graphSettings.setHierarchicMinimumNodeDistance(
        settings.getLayoutSettings().getHierarchicalSettings().getMinimumNodeDistance());
    graphSettings.setHierarchicMinimumEdgeDistance(
        settings.getLayoutSettings().getHierarchicalSettings().getMinimumEdgeDistance());
    graphSettings.setHierarchicEdgeRoutingStyle(
        settings.getLayoutSettings().getHierarchicalSettings().getStyle().ordinal());
    graphSettings.setHierarchicOrientation(
        settings.getLayoutSettings().getHierarchicalSettings().getOrientation().ordinal());

    graphSettings.setOrthogonalMinimumNodeDistance(
        settings.getLayoutSettings().getOrthogonalSettings().getMinimumNodeDistance());
    graphSettings.setOrthogonalLayoutStyle(
        settings.getLayoutSettings().getOrthogonalSettings().getStyle().ordinal());
    graphSettings.setOrthogonalOrientation(
        settings.getLayoutSettings().getOrthogonalSettings().getOrientation().ordinal());

    graphSettings.setCircularMinimumNodeDistance(
        settings.getLayoutSettings().getCircularSettings().getMinimumNodeDistance());
    graphSettings.setCircularLayoutStyle(
        settings.getLayoutSettings().getCircularSettings().getStyle().ordinal());
    graphSettings.setDefaultGraphLayout(
        settings.getLayoutSettings().getDefaultGraphLayout().ordinal());

    graphSettings.setScrollSensitivity(settings.getMouseSettings().getScrollSensitivity());
    graphSettings.setZoomSensitivity(settings.getMouseSettings().getZoomSensitivity());

    graphSettings.setLayoutAnimation(settings.getLayoutSettings().getAnimateLayout());
    graphSettings.setAnimationSpeed(settings.getDisplaySettings().getAnimationSpeed());
    graphSettings.setGradientBackground(settings.getDisplaySettings().getGradientBackground());

    graphSettings.setFunctionNodeInformation(
        settings.getDisplaySettings().getFunctionNodeInformation());

    graphSettings.setAnimationSpeed(settings.getDisplaySettings().getAnimationSpeed());
    graphSettings.setMouseWheelAction(settings.getMouseSettings().getMouseWheelAction().ordinal());

    graphSettings.setProximityBrowsingChildren(
        settings.getProximitySettings().getProximityBrowsingChildren());
    graphSettings.setProximityBrowsingParents(
        settings.getProximitySettings().getProximityBrowsingParents());

    graphSettings.setEdgeHidingMode(settings.getEdgeSettings().getEdgeHidingMode().ordinal());
    graphSettings.setEdgeHidingThreshold(settings.getEdgeSettings().getEdgeHidingThreshold());
    graphSettings.setDrawBends(settings.getEdgeSettings().getDrawSelectedBends());
  }

  /**
   * Returns the only valid instance of the configuration file.
   *
   * @return The only valid instance of the configuration file.
   */
  public static ConfigManager instance() {
    return instance;
  }

  public List<DatabaseConfigItem> getDatabases() {
    return databases;
  }

  public GeneralSettingsConfigItem getGeneralSettings() {
    return generalSettings;
  }

  public ColorsConfigItem getColorSettings() {
    return colors;
  }

  public DebugColorsConfigItem getDebuggerColorSettings() {
    return debuggerColors;
  }

  public ZyGraphViewSettings getDefaultCallGraphSettings() {
    // Stupid hack: Don't remove without understanding what's going on here
    return new ZyGraphViewSettings(new ZyGraphViewSettings(callGraphSettings));
  }

  public ZyGraphViewSettings getDefaultFlowGraphSettings() {
    // Stupid hack: Don't remove without understanding what's going on here
    return new ZyGraphViewSettings(new ZyGraphViewSettings(flowGraphSettings));
  }

  /**
   * Reads the configuration file from disk.
   * @return True, if the configuration file existed. False, otherwise.
   *
   * @throws FileReadException Thrown if the file could not be read.
   */
  public boolean read() throws FileReadException {
    boolean notFound = false;
    try {
      properties.getProperties().loadFromXML(new FileInputStream(filename));
    } catch (final FileNotFoundException e) {
      notFound = true;
    } catch (final IOException e) {
      // Rethrow as FileReadException.
      throw new FileReadException(e);
    }

    generalSettings.load(properties);
    for (int i = 0; i < properties.getInteger(DATABASES_COUNT, DATABASES_COUNT_DEFAULT); i++) {
      final DatabaseConfigItem database = new DatabaseConfigItem();
      database.loadWithKeyPrefix(properties, DATABASES_PREFIX + i + ".");
      databases.add(database);
    }
    callGraphSettings.loadWithKeyPrefix(properties, "CallGraphSettings.");
    flowGraphSettings.loadWithKeyPrefix(properties, "FlowGraphSettings.");
    colors.loadWithKeyPrefix(properties, "Colors.");
    debuggerColors.loadWithKeyPrefix(properties, "Debugger.Colors.");

    if (notFound) {
      flowGraphSettings.setDefaultGraphLayout(1);
      flowGraphSettings.setProximityBrowsingThreshold(50);
      flowGraphSettings.setAutoLayoutDeactivationThreshold(70);
      flowGraphSettings.setLayoutCalculationThreshold(250);
      return false;
    }
    return true;
  }

  /**
   * Writes the configuration file to disk.
   *
   * @throws FileWriteException Thrown if the file could not be written.
   */
  private void write() throws FileWriteException {
    generalSettings.store(properties);
    properties.setInteger(DATABASES_COUNT, databases.size());
    for (int i = 0; i < databases.size(); i++) {
      databases.get(i).storeWithKeyPrefix(properties, DATABASES_PREFIX + i + ".");
    }
    callGraphSettings.storeWithKeyPrefix(properties, "CallGraphSettings.");
    flowGraphSettings.storeWithKeyPrefix(properties, "FlowGraphSettings.");
    colors.storeWithKeyPrefix(properties, "Colors.");
    debuggerColors.storeWithKeyPrefix(properties, "Debugger.Colors.");

    try {
      // Ensure that the configuration directory exists.
      final File configDir = new File(filename).getParentFile();
      if (configDir != null) configDir.mkdirs();
      properties.getProperties()
          .storeToXML(new FileOutputStream(filename), Constants.PROJECT_NAME_VERSION);
    } catch (final IOException e) {
      // Rethrow as FileWriteException.
      throw new FileWriteException(e);
    }
  }

  /**
   * Updates the default call graph settings in the configuration file from graph settings.
   *
   * @param settings The settings source.
   */
  public void updateCallgraphSettings(final ZyGraphViewSettings settings) {
    updateGraphSettings(callGraphSettings, settings);

    callGraphSettings.setMultipleEdgesAsOne(
        settings.getEdgeSettings().getDisplayMultipleEdgesAsOne());
  }

  /**
   * Updates the default flow graph settings in the configuration file from graph settings.
   *
   * @param settings The settings source.
   */
  public void updateFlowgraphSettings(final ZyGraphViewSettings settings) {
    updateGraphSettings(flowGraphSettings, settings);
  }

  /**
   * Stores all relevant settings in the config file object and saves the config file to the disk.
   *
   * @param parent Parent window used for dialogs.
   */
  public void saveSettings(final JFrame parent) {
    final Point location = parent.getLocation();

    final GeneralSettingsConfigItem.LastOpenWindowConfigItem window =
        generalSettings.getLastOpenWindow();
    window.setTop((int) location.getY());
    window.setLeft((int) location.getX());
    window.setHeight(parent.getHeight());
    window.setWidth(parent.getWidth());

    generalSettings.setMaximizeWindow((parent.getExtendedState() == Frame.ICONIFIED)
        || (parent.getExtendedState() == Frame.MAXIMIZED_BOTH));

    databases.clear();
    for (final IDatabase database : CDatabaseManager.instance()) {
      final DatabaseConfigItem databaseConfig = new DatabaseConfigItem();
      databaseConfig.setAutoConnect(database.getConfiguration().isAutoConnect());
      databaseConfig.setDescription(database.getConfiguration().getDescription());
      databaseConfig.setDriver(database.getConfiguration().getDriver());
      databaseConfig.setPassword(database.getConfiguration().isSavePassword()
          ? database.getConfiguration().getPassword() : "");
      databaseConfig.setSavePassword(database.getConfiguration().isSavePassword());
      databaseConfig.setHost(database.getConfiguration().getHost());
      databaseConfig.setName(database.getConfiguration().getName());
      databaseConfig.setUser(database.getConfiguration().getUser());
      databaseConfig.setIdentity(database.getConfiguration().getIdentity());
      databases.add(databaseConfig);
    }

    try {
      write();
    } catch (final FileWriteException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00150: " + "Could not write configuration file";
      final String innerDescription = CUtilityFunctions.createDescription(
          "The configuration file where the settings are stored could not be written.",
          new String[] {"There was a problem writing the file. Please see the stacktrace for more "
              + "information."},
          new String[] {"The active configuration was not saved and will be lost."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }
}
