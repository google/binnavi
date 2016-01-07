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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.Actions;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CShowViewFunctions;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.ZyZoomHelpers;
import com.google.security.zynamics.binnavi.ZyGraph.Painters.CrossReferencePainter;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

public class OpenInLastWindowAndZoomToAddressAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7167187997929184030L;

  /**
   * Parent window used for dialogs.
   */
  private final Window m_parent;

  /**
   * Context in which the views are opened.
   */
  private final IViewContainer m_container;

  /**
   * Views to be opened.
   */
  private final INaviView[] m_views;

  private final TypeInstanceReference reference;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param container Context in which the views are opened.
   * @param views Views to be opened.
   */
  public OpenInLastWindowAndZoomToAddressAction(final Window parent,
      final IViewContainer container, final INaviView[] views, final TypeInstanceReference reference) {
    super("Open in last window and zoom to address");

    m_parent = Preconditions.checkNotNull(parent, "IE02876: parent argument can not be null");
    m_container =
        Preconditions.checkNotNull(container, "IE02877: container argument can not be null");
    m_views = Preconditions.checkNotNull(views, "IE02878: views argument can not be null").clone();
    this.reference = reference;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final FutureCallback<Boolean> callBack = new FutureCallback<Boolean>() {

      @Override
      public void onFailure(final Throwable t) {
        CUtilityFunctions.logException(t);
      }

      @Override
      public void onSuccess(final Boolean result) {
        ZyGraph graph = null;
        final List<CGraphWindow> windows = CWindowManager.instance().getOpenWindows();
        for (final CGraphWindow graphContainer : windows) {
          for (final IGraphPanel window : graphContainer) {
            if (reference.getView().equals(window.getModel().getGraph().getRawView())) {
              graph = window.getModel().getGraph();
            }
          }
        }
        for (final NaviNode node : graph.getNodes()) {
          if (node.getRawNode() instanceof INaviCodeNode) {
            final INaviCodeNode codeNode = (INaviCodeNode) node.getRawNode();
            for (final INaviInstruction instruction : codeNode.getInstructions()) {
              if (instruction.getAddress().equals(reference.getAddress())) {
                ZyZoomHelpers.zoomToAddress(graph, reference.getAddress());
                CrossReferencePainter.paintCrossReference(node, codeNode, reference, instruction);
              }
            }
          }
        }
      }
    };

    CShowViewFunctions.showViewsAndPerformCallBack(m_parent, m_container, m_views, CWindowManager
        .instance().getLastWindow(), callBack);
  }
}
