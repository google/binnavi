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
package com.google.security.zynamics.binnavi.ZyGraph;

import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyGraphListener;



/**
 * Listener interface to be implemented by objects that want to be notified about changes in graphs.
 */
public interface INaviGraphListener extends IZyGraphListener<NaviNode, NaviEdge> {
  /**
   * Invoked after a node was added to a graph.
   *
   * @param graph The graph where the node was added.
   * @param node The node added to the graph.
   */
  void addedNode(ZyGraph graph, NaviNode node);

  /**
   * Invoked after the underlying structures of a graph node changed. This typically happens if a
   * graph was saved as a new graph.
   *
   * @param graph Graph the node belongs to.
   * @param node The node whose model changed.
   */
  void changedModel(ZyGraph graph, NaviNode node);

  /**
   * Invoked after the view of a graph changed.
   *
   * @param oldView The previous view that backed the graph.
   * @param newView The new view that now backs the graph.
   */
  void changedView(INaviView oldView, INaviView newView);

  /**
   * Invoked after a node was tagged.
   *
   * @param view The view the tagged node belongs to.
   * @param node The node that was tagged.
   * @param tag The tag used to tag the node.
   */
  void taggedNode(INaviView view, INaviViewNode node, CTag tag);

  /**
   * Invoked after a node was untagged.
   *
   * @param view The view the untagged node belongs to.
   * @param node The node that was untagged.
   * @param tag The tag removed from the node.
   */
  void untaggedNode(INaviView view, INaviViewNode node, CTag tag);
}
