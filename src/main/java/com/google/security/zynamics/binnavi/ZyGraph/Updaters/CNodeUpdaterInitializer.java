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
package com.google.security.zynamics.binnavi.ZyGraph.Updaters;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * Initializes node updaters
 */
public final class CNodeUpdaterInitializer {
  /**
   * You are not supposed to instantiate this.
   */
  private CNodeUpdaterInitializer() {
  }

  /**
   * Adds node updaters to all nodes of a graph.
   *
   * @param model The model that describes the graph to be initialized.
   */
  public static void addUpdaters(final CGraphModel model) {
    Preconditions.checkNotNull(model, "IE02240: Model argument can not be null");

    model.getGraph().iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        addUpdaters(model, node);

        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * Adds node updaters to a single node.
   *
   * @param model Describes the graph the node belongs to.
   * @param node The node to be initialized.
   */
  public static void addUpdaters(final CGraphModel model, final NaviNode node) {
    Preconditions.checkNotNull(model, "IE02236: Model argument can not be null");

    Preconditions.checkNotNull(node, "IE02237: Node argument can not be null");

    for (final INodeUpdater updater : CNodeUpdaterFactory.getUpdaters()) {
      updater.visit(model, node);
    }
  }
}
