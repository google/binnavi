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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.DebuggerContainer;



import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger.CDebuggerNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.DebuggerContainer.Component.CDebuggerContainerNodeComponent;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateManagerListener;

/**
 * Project tree node that represents a debugger container.
 */
public final class CDebuggerContainerNode extends CProjectTreeNode<Object> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7942293319476781440L;

  /**
   * Icon used for this tree node.
   */
  private static final ImageIcon ICON_DEBUGGER_CONTAINER = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/debugger_container2.png"));

  /**
   * Database the debuggers belong to.
   */
  private final IDatabase m_database;

  /**
   * Updates the node on relevant changes in the debugger template manager.
   */
  private final InternalDebuggerTemplateManagerListener m_debuggerManagerListener =
      new InternalDebuggerTemplateManagerListener();

  /**
   * Creates a new node object.
   * 
   * @param projectTree The project tree of the main window.
   * @param database Database the debuggers belong to.
   */
  public CDebuggerContainerNode(final JTree projectTree, final IDatabase database) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CDebuggerContainerNodeComponent(projectTree, database);
      }
    }, new CDebuggerContainerNodeMenuBuilder(projectTree, database));

    m_database = Preconditions.checkNotNull(database, "IE01968: Database can't be null");

    m_database.getContent().getDebuggerTemplateManager().addListener(m_debuggerManagerListener);

    createChildren();
  }

  @Override
  protected void createChildren() {
    if (m_database.isLoaded()) {
      for (final DebuggerTemplate debugger : m_database.getContent().getDebuggerTemplateManager()) {
        add(new CDebuggerNode(getProjectTree(), this, m_database, debugger));
      }
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    final DebuggerTemplateManager debuggerManager =
        m_database.getContent().getDebuggerTemplateManager();
    debuggerManager.removeListener(m_debuggerManagerListener);

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    // Do nothing
  }

  @Override
  public Icon getIcon() {
    return ICON_DEBUGGER_CONTAINER;

  }

  @Override
  public String toString() {
    return "Debuggers" + " ("
        + m_database.getContent().getDebuggerTemplateManager().debuggerCount() + ")";

  }

  /**
   * Updates the node on relevant changes in the debugger template manager.
   */
  private class InternalDebuggerTemplateManagerListener implements IDebuggerTemplateManagerListener {
    @Override
    public void addedDebugger(final DebuggerTemplateManager manager,
        final DebuggerTemplate debugger) {
      add(new CDebuggerNode(getProjectTree(), CDebuggerContainerNode.this, m_database, debugger));

      getTreeModel().nodeStructureChanged(CDebuggerContainerNode.this);
    }

    @Override
    public void removedDebugger(final DebuggerTemplateManager manager,
        final DebuggerTemplate debugger) {
      for (int i = 0; i < getChildCount(); i++) {
        final CDebuggerNode node = (CDebuggerNode) getChildAt(i);

        if (node.getObject() == debugger) {
          node.dispose();
          remove(node);
          break;
        }
      }

      getTreeModel().nodeStructureChanged(CDebuggerContainerNode.this);
    }
  }

}
