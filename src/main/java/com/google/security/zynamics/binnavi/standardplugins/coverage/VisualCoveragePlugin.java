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
package com.google.security.zynamics.binnavi.standardplugins.coverage;

import com.google.security.zynamics.binnavi.API.gui.GraphFrame;
import com.google.security.zynamics.binnavi.API.plugins.IGraphMenuPlugin;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

/**
 * This plugin demonstrates how to use the BinNavi Plugin API to write a plugin that counts how
 * often the blocks of a graph are hit during a debugging session and paints the blocks in different
 * colors that reflect that hit count.
 */
public final class VisualCoveragePlugin implements IGraphMenuPlugin {
  /**
   * Keeps track of all created actions so that they can be cleaned up later.
   */
  private final List<VisualCoverageAction> createdActions = new ArrayList<VisualCoverageAction>();

  @Override
  public void closed(final GraphFrame graphFrame) {
    for (final VisualCoverageAction action : createdActions) {
      if (action.getFrame() == graphFrame) {
        action.dispose();
        createdActions.remove(action);
        return;
      }
    }
  }

  @Override
  public List<JComponent> extendPluginMenu(final GraphFrame graphFrame) {
    // Add a single additional menu to the Plugins menu of the graph window

    final List<JComponent> additionalMenus = new ArrayList<JComponent>();

    final VisualCoverageAction action = new VisualCoverageAction(graphFrame);

    createdActions.add(action);
    additionalMenus.add(new JMenuItem(action));

    return additionalMenus;
  }

  @Override
  public String getDescription() {
    return "This plugin can be used to create simple code coverage traces.";
  }

  @Override
  public long getGuid() {
    return 54235790543L;
  }

  @Override
  public String getName() {
    return "Visual Coverage";
  }

  @Override
  public void init(final IPluginInterface pluginInterface) {
    // Empty implementation
  }

  @Override
  public void unload() {
    // Free all allocated resources if the plugin is unloaded

    for (final VisualCoverageAction action : createdActions) {
      action.dispose();
    }

    createdActions.clear();
  }
}
