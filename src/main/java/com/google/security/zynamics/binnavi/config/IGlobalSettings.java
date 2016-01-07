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

/**
 * Interface that provides global BinNavi settings.
 */
public interface IGlobalSettings extends IGraphColors, IDebugSettings {

  // Getters/setters for the various global settings.
  int getBottomPanelSize();
  void setBottomPanelSize(final int size);
  String getDefaultScriptingLanguage();
  void setDefaultScriptingLanguage(String language);
  String getIdaDirectory();
  void setIdaDirectory(String idaLocation);
  int getLeftPanelSize();
  void setLeftPanelSize(final int size);
  int getLogLevel();
  void setLogLevel(int level);
  int getRightPanelSize();
  void setRightPanelSize(final int size);
  boolean getShowExpiredInformation();
  void setShowExpiredInformation(final boolean show);
  boolean getShowExpiringReminder();
  void setShowExpiringInformation(final boolean show);
  String getSupportEmailAddress();
  void setSupportEmailAddress(String email);
}
