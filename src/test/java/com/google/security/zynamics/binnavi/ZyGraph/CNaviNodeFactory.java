/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.ZyGraph;

import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyTextNodeBuilder;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.CTextNodeFactory;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNormalNodeRealizer;

import y.view.Graph2D;

public class CNaviNodeFactory {
  public static NaviNode get() {
    final Graph2D graph = new Graph2D();

    final CTextNode node = CTextNodeFactory.get();

    final ZyLabelContent content = ZyTextNodeBuilder.buildContent(node);

    final IZyNodeRealizer realizer = new ZyNormalNodeRealizer<NaviNode>(content);

    return new NaviNode(graph.createNode(), realizer, node);
  }
}
