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
package com.google.security.zynamics.binnavi.ZyGraph.Updaters.CodeNodes;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.ZyGraph.Updaters.INodeUpdater;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Factory class that is used to create and initialize code node updaters.
 */
public final class CCodeNodeUpdaterCreator implements INodeUpdater {
  @Override
  public void visit(final CGraphModel model, final NaviNode node) {
    if (node.getRawNode() instanceof INaviCodeNode) {
      node.getRealizer().setUpdater(new CCodeNodeUpdater(model.getGraph(), node,
          (INaviCodeNode) node.getRawNode(), model.getDebuggerProvider()));
    }
  }
}
