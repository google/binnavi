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
package com.google.security.zynamics.binnavi.ZyGraph.Implementations;

import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.ProximityNodeFunctions;

import java.awt.Window;
import java.util.List;



/**
 * This class is used to handle all kinds of functions related to proximity nodes.
 */
public final class CProximityFunctions
    extends ProximityNodeFunctions<INaviViewNode, NaviNode, ZyGraph> {
  @Override
  protected void showNodes(
      final Window parent, final ZyGraph graph, final List<NaviNode> nodes, final boolean toShow) // NO_UCD
  {
    CGraphFunctions.showNodes(parent, graph, nodes, toShow);
  }
}
