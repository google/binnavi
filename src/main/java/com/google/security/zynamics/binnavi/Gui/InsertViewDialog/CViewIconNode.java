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
package com.google.security.zynamics.binnavi.Gui.InsertViewDialog;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Class for view nodes of view selection dialogs.
 */
public final class CViewIconNode extends IconNode implements IViewSelectionTreeNode {

  /**
   * Icon used for native callgraphs.
   */
  private static final ImageIcon ICON_NATIVE_CALLGRAPHS =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/native_callgraph_view.png"));

  /**
   * Icon used for native flowgraphs.
   */
  private static final ImageIcon ICON_NATIVE_FUNCTION = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/native_flowgraph_views_container.png"));

  /**
   * Icon used or non-native callgraphs.
   */
  private static final ImageIcon ICON_CALLGRAPHS = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/global_callgraph_views_container.png"));

  /**
   * Icon used for non-native flowgraphs.
   */
  private static final ImageIcon ICON_FLOWGRAPH_VIEW = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/global_flowgraph_views_container.png"));

  /**
   * Icon used for mixed graphs.
   */
  private static final ImageIcon ICON_MIXED_VIEW = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/global_synthetic_views_container.png"));

  /**
   * The view that is represented by the node.
   */
  private final INaviView m_view;

  /**
   * Creates a new view selection node.
   *
   * @param view The view that is represented by the node.
   */
  public CViewIconNode(final INaviView view) {
    Preconditions.checkNotNull(view, "IE01823: View argument can not be null");

    m_view = view;
  }

  @Override
  public void doubleClicked() {
    // TODO: Load view on double click
  }

  @Override
  public Icon getIcon() {
    if (m_view.getType() == ViewType.Native) {
      switch (m_view.getGraphType()) {
        case CALLGRAPH:
          return ICON_NATIVE_CALLGRAPHS;
        case FLOWGRAPH:
          return ICON_NATIVE_FUNCTION;
        case MIXED_GRAPH:
          return ICON_MIXED_VIEW; // Do not remove this, add function node to native function view
                                  // and see
        default:
          throw new IllegalStateException("IE01156: Unknown graph type");
      }
    } else {
      switch (m_view.getGraphType()) {
        case CALLGRAPH:
          return ICON_CALLGRAPHS;
        case FLOWGRAPH:
          return ICON_FLOWGRAPH_VIEW;
        case MIXED_GRAPH:
          return ICON_MIXED_VIEW;
        default:
          throw new IllegalStateException("IE01157: Unknown graph type");
      }
    }
  }

  /**
   * Returns the view represented by this node.
   *
   * @return The view represented by this node.
   */
  public INaviView getView() {
    return m_view;
  }

  @Override
  public String toString() {
    return m_view.getName();
  }
}
