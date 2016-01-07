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
import com.google.security.zynamics.common.config.AbstractConfigItem;
import com.google.security.zynamics.common.config.TypedPropertiesWrapper;

import java.util.List;

public class GeneralSettingsConfigItem extends AbstractConfigItem {
  static final String PROPERTY_PREFIX = "GeneralSettings.";

  private static final String IDA_DIRECTORY = PROPERTY_PREFIX + "IdaDirectory";
  private static final String IDA_DIRECTORY_DEFAULT = "";
  private String idaDirectory = IDA_DIRECTORY_DEFAULT;

  private static final String DEFAULT_EXPORTER = PROPERTY_PREFIX + "DefaultExporter";
  private static final int DEFAULT_EXPORTER_DEFAULT = 0;
  private int defaultExporter = DEFAULT_EXPORTER_DEFAULT;

  private static final String SUPPORT_EMAIL_ADDRESS = PROPERTY_PREFIX + "SupportEmailAddress";
  private static final String SUPPORT_EMAIL_ADDRESS_DEFAULT = "";
  private String supportEmailAddress = SUPPORT_EMAIL_ADDRESS_DEFAULT;

  private static final String SHOW_EXPIRING_INFORMATION =
      PROPERTY_PREFIX + "ShowExpiringInformation";
  private static final boolean SHOW_EXPIRING_INFORMATION_DEFAULT = true;
  private Boolean showExpiringInformation = SHOW_EXPIRING_INFORMATION_DEFAULT;

  private static final String SHOW_EXPIRED_INFORMATION =
      PROPERTY_PREFIX + "ShowExpiredInformation";
  private static final boolean SHOW_EXPIRED_INFORMATION_DEFAULT = true;
  private Boolean showExpiredInformation = SHOW_EXPIRED_INFORMATION_DEFAULT;

  private static final String MAXIMIZE_WINDOW = PROPERTY_PREFIX + "MaximizeWindow";
  private static final boolean MAXIMIZE_WINDOW_DEFAULT = true;
  private Boolean maximizeWindow = MAXIMIZE_WINDOW_DEFAULT;

  private static final String LOG_LEVEL = PROPERTY_PREFIX + "LogLevel";
  private static final int LOG_LEVEL_DEFAULT = 0;
  private int logLevel = LOG_LEVEL_DEFAULT;

  private static final String DEFAULT_SCRIPTING_LANGUAGE =
      PROPERTY_PREFIX + "DefaultScriptingLanguage";
  private static final String DEFAULT_SCRIPTING_LANGUAGE_DEFAULT = "";
  private String defaultScriptingLanguage = DEFAULT_SCRIPTING_LANGUAGE_DEFAULT;

  private static final String PLUGINS_PREFIX = PROPERTY_PREFIX + "Plugins.";
  private static final String PLUGINS_COUNT = PLUGINS_PREFIX + "Count";
  private static final int PLUGINS_COUNT_DEFAULT = 0;

  // The last directory that was used in a file chooser dialog in BinNavi.
  private static final String LAST_DIRECTORY = PROPERTY_PREFIX + "LastDirectory";
  private static final String LAST_DIRECTORY_DEFAULT = "";
  private static String lastDirectory = LAST_DIRECTORY_DEFAULT;
  
  private List<String> idbDirectories;
  private LastOpenWindowConfigItem lastOpenWindow;
  private GeneralSettingsConfigItem.GraphWindowConfigItem graphWindow;
  private List<PluginConfigItem> plugins;

  public GeneralSettingsConfigItem() {
    idbDirectories = Lists.newArrayList();
    lastOpenWindow = new LastOpenWindowConfigItem();
    graphWindow = new GraphWindowConfigItem();
    plugins = Lists.newArrayList();
  }

  @Override
  public void load(final TypedPropertiesWrapper properties) {
    idaDirectory = properties.getString(IDA_DIRECTORY, IDA_DIRECTORY_DEFAULT);
    defaultExporter = properties.getInteger(DEFAULT_EXPORTER, DEFAULT_EXPORTER_DEFAULT);
    supportEmailAddress =
        properties.getString(SUPPORT_EMAIL_ADDRESS, SUPPORT_EMAIL_ADDRESS_DEFAULT);
    showExpiringInformation =
        properties.getBoolean(SHOW_EXPIRING_INFORMATION, SHOW_EXPIRING_INFORMATION_DEFAULT);
    showExpiredInformation =
        properties.getBoolean(SHOW_EXPIRED_INFORMATION, SHOW_EXPIRED_INFORMATION_DEFAULT);
    maximizeWindow = properties.getBoolean(MAXIMIZE_WINDOW, MAXIMIZE_WINDOW_DEFAULT);
    logLevel = properties.getInteger(LOG_LEVEL, LOG_LEVEL_DEFAULT);
    defaultScriptingLanguage =
        properties.getString(DEFAULT_SCRIPTING_LANGUAGE, DEFAULT_SCRIPTING_LANGUAGE_DEFAULT);
    lastOpenWindow.load(properties);
    graphWindow.load(properties);
    lastDirectory = properties.getString(LAST_DIRECTORY, LAST_DIRECTORY_DEFAULT);
    
    for (int i = 0; i < properties.getInteger(PLUGINS_COUNT, PLUGINS_COUNT_DEFAULT); i++) {
      final PluginConfigItem plugin = new PluginConfigItem();
      plugin.loadWithKeyPrefix(properties, PLUGINS_PREFIX + i + ".");
      plugins.add(plugin);
    }
  }

  @Override
  public void store(final TypedPropertiesWrapper properties) {
    properties.setString(IDA_DIRECTORY, idaDirectory);
    properties.setInteger(DEFAULT_EXPORTER, defaultExporter);
    properties.setString(SUPPORT_EMAIL_ADDRESS, supportEmailAddress);
    properties.setBoolean(SHOW_EXPIRING_INFORMATION, showExpiringInformation);
    properties.setBoolean(SHOW_EXPIRED_INFORMATION, showExpiredInformation);
    properties.setBoolean(MAXIMIZE_WINDOW, maximizeWindow);
    properties.setInteger(LOG_LEVEL, logLevel);
    if (defaultScriptingLanguage != null) {
      properties.setString(DEFAULT_SCRIPTING_LANGUAGE, defaultScriptingLanguage);
    }
    lastOpenWindow.store(properties);
    graphWindow.store(properties);
    properties.setString(LAST_DIRECTORY, lastDirectory);
    
    properties.setInteger(PLUGINS_COUNT, plugins.size());
    for (int i = 0; i < plugins.size(); i++) {
      plugins.get(i).storeWithKeyPrefix(properties, PLUGINS_PREFIX + i + ".");
    }
  }

  public String getIdaDirectory() {
    return idaDirectory;
  }

  public void setIdaDirectory(final String value) {
    idaDirectory = value;
  }

  public List<String> getIdbDirectories() {
    return idbDirectories;
  }

  public Integer getDefaultExporter() {
    return defaultExporter;
  }

  public void setDefaultExporter(final Integer value) {
    this.defaultExporter = value;
  }

  public String getSupportEmailAddress() {
    return supportEmailAddress;
  }

  public void setSupportEmailAddress(final String value) {
    this.supportEmailAddress = value;
  }

  public Boolean isShowExpiringInformation() {
    return showExpiringInformation;
  }

  public void setShowExpiringInformation(final Boolean value) {
    this.showExpiringInformation = value;
  }

  public Boolean isShowExpiredInformation() {
    return showExpiredInformation;
  }

  public void setShowExpiredInformation(final Boolean value) {
    this.showExpiredInformation = value;
  }

  public Boolean isMaximizeWindow() {
    return maximizeWindow;
  }

  public void setMaximizeWindow(final Boolean value) {
    this.maximizeWindow = value;
  }

  public Integer getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(final Integer value) {
    this.logLevel = value;
  }

  public String getDefaultScriptingLanguage() {
    return defaultScriptingLanguage;
  }

  public void setDefaultScriptingLanguage(final String value) {
    this.defaultScriptingLanguage = value;
  }

  public LastOpenWindowConfigItem getLastOpenWindow() {
    return lastOpenWindow;
  }

  public GraphWindowConfigItem getGraphWindow() {
    return graphWindow;
  }

  public List<PluginConfigItem> getPlugins() {
    return plugins;
  }

  public void setLastDirectory(String directory) {
    lastDirectory = directory;
  }
  
  public String getLastDirectory() {
    return lastDirectory;  
  }
  
  public class LastOpenWindowConfigItem extends AbstractConfigItem {
    private static final String TOP = "LastOpenWindow.Top";
    private static final int TOP_DEFAULT = 0;
    private int top = TOP_DEFAULT;

    private static final String LEFT = "LastOpenWindow.Left";
    private static final int LEFT_DEFAULT = 0;
    private int left = LEFT_DEFAULT;

    private static final String HEIGHT = "LastOpenWindow.Height";
    private static final int HEIGHT_DEFAULT = 600;
    private int height = HEIGHT_DEFAULT;

    private static final String WIDTH = "LastOpenWindow.Width";
    private static final int WIDTH_DEFAULT = 800;
    private int width = WIDTH_DEFAULT;

    @Override
    public void load(final TypedPropertiesWrapper properties) {
      top = properties.getInteger(TOP, TOP_DEFAULT);
      left = properties.getInteger(LEFT, LEFT_DEFAULT);
      height = properties.getInteger(HEIGHT, HEIGHT_DEFAULT);
      width = properties.getInteger(WIDTH, WIDTH_DEFAULT);
    }

    @Override
    public void store(final TypedPropertiesWrapper properties) {
      properties.setInteger(TOP, top);
      properties.setInteger(LEFT, left);
      properties.setInteger(HEIGHT, height);
      properties.setInteger(WIDTH, width);
    }

    public int getTop() {
      return top;
    }

    public void setTop(final int value) {
      this.top = value;
    }

    public int getLeft() {
      return left;
    }

    public void setLeft(final int value) {
      this.left = value;
    }

    public int getHeight() {
      return height;
    }

    public void setHeight(final int value) {
      this.height = value;
    }

    public int getWidth() {
      return width;
    }

    public void setWidth(final int value) {
      this.width = value;
    }
  }

  public static class GraphWindowConfigItem {
    private static final String SIZE_LEFT_PANEL = "GraphWindow.SizeLeftPanel";
    private static final int SIZE_LEFT_PANEL_DEFAULT = -1;
    private int sizeLeftPanel;

    private static final String SIZE_RIGHT_PANEL = "GraphWindow.SizeRightPanel";
    private static final int SIZE_RIGHT_PANEL_DEFAULT = -1;
    private int sizeRightPanel = SIZE_RIGHT_PANEL_DEFAULT;

    private static final String SIZE_BOTTOM_PANEL = "GraphWindow.SizeBottomPanel";
    private static final int SIZE_BOTTOM_PANEL_DEFAULT = -1;
    private int sizeBottomPanel = SIZE_BOTTOM_PANEL_DEFAULT;

    public void load(final TypedPropertiesWrapper properties) {
      sizeLeftPanel = properties.getInteger(SIZE_LEFT_PANEL, SIZE_LEFT_PANEL_DEFAULT);
      sizeRightPanel = properties.getInteger(SIZE_RIGHT_PANEL, SIZE_RIGHT_PANEL_DEFAULT);
      sizeBottomPanel = properties.getInteger(SIZE_BOTTOM_PANEL, SIZE_BOTTOM_PANEL_DEFAULT);
    }

    public void store(final TypedPropertiesWrapper properties) {
      properties.setInteger(SIZE_LEFT_PANEL, sizeLeftPanel);
      properties.setInteger(SIZE_RIGHT_PANEL, sizeRightPanel);
      properties.setInteger(SIZE_BOTTOM_PANEL, sizeBottomPanel);
    }

    public int getSizeLeftPanel() {
      return sizeLeftPanel;
    }

    public void setSizeLeftPanel(final int value) {
      this.sizeLeftPanel = value;
    }

    public int getSizeRightPanel() {
      return sizeRightPanel;
    }

    public void setSizeRightPanel(final int value) {
      this.sizeRightPanel = value;
    }

    public int getSizeBottomPanel() {
      return sizeBottomPanel;
    }

    public void setSizeBottomPanel(final int value) {
      this.sizeBottomPanel = value;
    }
  }
}