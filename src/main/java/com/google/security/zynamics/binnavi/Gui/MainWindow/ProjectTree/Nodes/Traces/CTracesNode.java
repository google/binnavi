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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Traces;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Root.CRootNodeMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Traces.Component.CTracesNodeComponent;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceManagerListener;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Project tree node that can be selected to show the traces of a module or project.
 */
public final class CTracesNode extends CProjectTreeNode<Object> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7710391862165745578L;

  /**
   * Icon shown by the node in the project tree.
   */
  private static final ImageIcon ICON_TRACES = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/debug_traces.png"));

  /**
   * View container that provides the trace information shown in the component.
   */
  private final IViewContainer m_viewContainer;

  /**
   * Listener that listens on relevant trace events.
   */
  private final ITraceManagerListener m_listener = new InternalTraceListener();

  /**
   * Creates a new node object.
   * 
   * @param tree Project tree of the main window.
   * @param container Container whose traces are represented by the node.
   */
  public CTracesNode(final JTree tree, final IViewContainer container) {
    super(tree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CTracesNodeComponent(tree, container);
      }
    }, new CRootNodeMenuBuilder(tree));

    Preconditions.checkNotNull(container, "IE02006: Container argument can not be null");

    m_viewContainer = container;

    container.getTraceProvider().addListener(m_listener);
  }

  @Override
  protected void createChildren() {
    // No children
  }

  @Override
  public void dispose() {
    super.dispose();

    m_viewContainer.getTraceProvider().removeListener(m_listener);
  }

  @Override
  public void doubleClicked() {
    // Double clicking on the traces node does not have an associated action.
  }

  @Override
  public Icon getIcon() {
    return ICON_TRACES;
  }

  @Override
  public String toString() {
    return String.format("Debug Traces (%d)", m_viewContainer.getTraceProvider()
        .getNumberOfTraceLists());
  }

  /**
   * Listener that listens on relevant trace events.
   */
  private class InternalTraceListener implements ITraceManagerListener {
    @Override
    public void addedTrace(final TraceList trace) {
      getTreeModel().nodeChanged(CTracesNode.this);
    }

    @Override
    public void loaded() {
      // The load state of traces are not displayed
    }

    @Override
    public void removedTrace(final TraceList trace) {
      getTreeModel().nodeChanged(CTracesNode.this);
    }
  }
}
