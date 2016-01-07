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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Project;

import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Root.CRootNodeMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Project.Component.CProjectViewsContainerComponent;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.CProjectListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.CViewFilter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * This node is the parent node of the view nodes of a project.
 */
public final class CProjectViewsContainerNode extends CProjectTreeNode<INaviProject> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2704981433389804914L;

  /**
   * Icon that is shown by the node.
   */
  private static final ImageIcon ICON_VIEW = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/view2.png"));

  /**
   * Project whose views are shown.
   */
  private final INaviProject m_project;

  /**
   * Keeps the node updated on changes in the project.
   */
  private final InternalProjectListener m_listener = new InternalProjectListener();

  /**
   * Creates a new project views container node.
   * 
   * @param projectTree The tree the node is added to.
   * @param project Project whose views are shown.
   * @param container View container that contains the views to be shown.
   */
  public CProjectViewsContainerNode(final JTree projectTree, final INaviProject project,
      final CProjectContainer container) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CProjectViewsContainerComponent(projectTree, project, container);
      }
    }, new CRootNodeMenuBuilder(projectTree), project);

    Preconditions.checkNotNull(project, "IE02051: Project can't be null");

    m_project = project;

    m_project.addListener(m_listener);

    createChildren();
  }

  @Override
  protected void createChildren() {
  }

  @Override
  public void dispose() {
    super.dispose();

    m_project.removeListener(m_listener);

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    // Nothing happens
  }

  @Override
  public Icon getIcon() {
    return ICON_VIEW;
  }

  @Override
  public String toString() {
    final List<INaviView> views = m_project.getContent().getViews();

    int count = CViewFilter.getCallgraphViewCount(views);
    count += CViewFilter.getFlowgraphViewCount(views);
    count += CViewFilter.getMixedgraphViewCount(views);

    return String.format("Project Views (%d)", count);
  }

  /**
   * Keeps the node updated on changes in the project.
   */
  private class InternalProjectListener extends CProjectListenerAdapter {
    @Override
    public void addedView(final INaviProject container, final INaviView view) {
      getTreeModel().nodeChanged(CProjectViewsContainerNode.this);
    }

    @Override
    public void deletedView(final INaviProject container, final INaviView view) {
      getTreeModel().nodeChanged(CProjectViewsContainerNode.this);
    }
  }
}
