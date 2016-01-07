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
package com.google.security.zynamics.binnavi.Gui;

import com.google.security.zynamics.binnavi.config.ConfigManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * File chooser that automatically uses the last directory that was used in an arbitrary instance of
 * LastDirFileChooser before.
 * This class can be used as a drop-in replacement for JFileChooser.
 */
public class LastDirFileChooser extends JFileChooser {

  public LastDirFileChooser() {
    super(getLastDir());
    addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY,
        new DirectoryChangedListener());
  }

  private static String getLastDir() {
    return ConfigManager.instance().getGeneralSettings().getLastDirectory();
  }

  private static void setLastDir(File lastDir) {
    final String absolutePath = lastDir.getAbsolutePath();
    if (!absolutePath.isEmpty()) {
      ConfigManager.instance().getGeneralSettings().setLastDirectory(absolutePath);
    }
  }

  private class DirectoryChangedListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      setLastDir(getCurrentDirectory());
    }
  }
}
