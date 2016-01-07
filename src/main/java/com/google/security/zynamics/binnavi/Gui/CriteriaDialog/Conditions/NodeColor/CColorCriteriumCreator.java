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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.NodeColor;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumCreator;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Factory class for creating color criterium objects.
 */
public final class CColorCriteriumCreator implements ICriteriumCreator {
  /**
   * The graph on which Select by Criteria is executed.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new creator object.
   *
   * @param graph The graph on which Select by Criteria is executed.
   */
  public CColorCriteriumCreator(final ZyGraph graph) {
    m_graph = graph;
  }

  @Override
  public ICriterium createCriterium() {
    return new CColorCriterium(m_graph);
  }

  @Override
  public String getCriteriumDescription() {
    return "Select Nodes by Color";
  }
}
