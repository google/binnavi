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
package com.google.security.zynamics.binnavi.standardplugins.criterium;

import com.google.security.zynamics.binnavi.API.disassembly.ViewNode;
import com.google.security.zynamics.binnavi.API.helpers.GraphAlgorithms;
import com.google.security.zynamics.binnavi.API.plugins.ICriteriaSelectionPlugin;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.binnavi.yfileswrap.API.disassembly.View2D;

import javax.swing.JPanel;

/**
 * This example plugin demonstrates how to extend the Select by Criteria dialog in graph windows.
 * The specific criterium implemented by the plugin is to select all nodes inside loops.
 */
public class LoopSelectionCriteriumPlugin implements IPlugin<IPluginInterface>, ICriteriaSelectionPlugin {
  @Override
  public ICriterium getCriterium(final View2D view2D) {
    return new LoopCriterium();
  }

  @Override
  public String getCriteriumDescription() {
    return "Select Nodes in loops";
  }

  @Override
  public String getDescription() {
    return "Extends the Select by Criteria dialog to offer the option to select all nodes that are inside loops.";
  }

  @Override
  public long getGuid() {
    return 423749054235790534L;
  }

  @Override
  public String getName() {
    return "Loop Selection Plugin";
  }

  @Override
  public void init(final IPluginInterface pluginInterface) {
  }

  @Override
  public void unload() {
  }

  /**
   * Fixes the user configuration input of LoopCriterium objects. Since the loop criterium can not
   * be configured, this object is stateless. For criteria that can be configured, this object would
   * be passed all the necessary configuration values in the constructor.
   */
  private static class EmptyFixedCriterium implements IFixedCriterium {
    @Override
    public boolean matches(final ViewNode node) {
      return GraphAlgorithms.getSuccessors(node).contains(node);
    }
  }

  /**
   * Criterium class that describes individual criterium nodes in the Select by Criteria tree.
   */
  private static class LoopCriterium implements ICriterium {
    /**
     * Further configuration is not required => Show a simple standard panel.
     */
    private final JPanel m_panel = new JPanel();

    @Override
    public String getCriteriumDescription() {
      return "Select Nodes in loops";
    }

    @Override
    public JPanel getCriteriumPanel() {
      return m_panel;
    }

    @Override
    public IFixedCriterium getFixedCriterium() {
      return new EmptyFixedCriterium();
    }

    @Override
    public String getFormulaString() {
      return "in loop";
    }

    @Override
    public boolean matches(final ViewNode node) {
      return GraphAlgorithms.getSuccessors(node).contains(node);
    }
  }
}
