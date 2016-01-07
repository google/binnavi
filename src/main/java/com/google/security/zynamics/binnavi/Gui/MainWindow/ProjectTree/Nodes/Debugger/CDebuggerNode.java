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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger.Component.CDebuggerNodeComponent;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateListener;

/**
 * Node that represents individual debuggers in the project tree.
 */
public final class CDebuggerNode extends CProjectTreeNode<DebuggerTemplate> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4377093985630890630L;

  /**
   * Icon used by this node.
   */
  private static final ImageIcon ICON_DEBUGGER = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/debugger3.png"));

  /**
   * Debugger represented by the node.
   */
  private final DebuggerTemplate m_debugger;

  /**
   * Updates the node on relevant changes in the debugger.
   */
  private final InternalDebuggerDescriptionListener m_listener =
      new InternalDebuggerDescriptionListener();

  /**
   * Creates a new node object.
   * 
   * @param projectTree Project tree of the main window.
   * @param parentNode Parent node of this node.
   * @param database Database the debugger belongs to.
   * @param debugger Debugger represented by the node.
   */
  public CDebuggerNode(final JTree projectTree, final DefaultMutableTreeNode parentNode,
      final IDatabase database, final DebuggerTemplate debugger) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CDebuggerNodeComponent(debugger);
      }
    }, new CDebuggerNodeMenuBuilder(projectTree, parentNode, null, database,
        new DebuggerTemplate[] {debugger}), debugger);

    Preconditions.checkNotNull(database, "IE01965: Database argument can not be null.");
    m_debugger = Preconditions.checkNotNull(debugger, "IE01966: Debugger argument can not be null");

    m_debugger.addListener(m_listener);
  }

  @Override
  protected void createChildren() {
    // no children
  }

  @Override
  public void dispose() {
    super.dispose();

    m_debugger.removeListener(m_listener);
  }

  @Override
  public void doubleClicked() {
    // Do nothing
  }

  @Override
  public Icon getIcon() {
    return ICON_DEBUGGER;
  }

  @Override
  public String toString() {
    return m_debugger.getName();
  }

  /**
   * Updates the node on relevant changes in the debugger.
   */
  private class InternalDebuggerDescriptionListener implements IDebuggerTemplateListener {
    @Override
    public void changedHost(final DebuggerTemplate debugger) {
      // do nothing
    }

    @Override
    public void changedName(final DebuggerTemplate debugger) {
      getTreeModel().nodeChanged(CDebuggerNode.this);
    }

    @Override
    public void changedPort(final DebuggerTemplate debugger) {
      // do nothing
    }
  }
}
