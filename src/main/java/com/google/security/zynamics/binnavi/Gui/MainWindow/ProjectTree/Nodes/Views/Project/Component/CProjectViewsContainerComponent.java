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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Project.Component;



import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JTree;
import javax.swing.border.TitledBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.CUserViewsComponent;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CProjectListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.CViewFilter;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.GraphType;

/**
 * Component that displays information about the non-native views of a project.
 */
public final class CProjectViewsContainerComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6539823583498119807L;

  /**
   * Project that contains the views to be displayed.
   */
  private final INaviProject m_project;

  /**
   * Border where the number of flowgraphs in the project is shown.
   */
  private final TitledBorder m_flowgraphBorder = new TitledBorder("");

  /**
   * Border where the number of callgraphs in the project is shown.
   */
  private final TitledBorder m_callgraphBorder = new TitledBorder("");

  /**
   * Border where the number of mixed graphs in the project is shown.
   */
  private final TitledBorder m_mixedgraphBorder = new TitledBorder("");

  /**
   * Updates the GUI on relevant changes in the project.
   */
  private final InternalProjectListener m_viewContainerListener = new InternalProjectListener();

  /**
   * Updates the GUI on relevant changes in the displayed views.
   */
  private final InternalViewListener m_viewListener = new InternalViewListener();

  /**
   * Creates a new component object.
   * 
   * @param projectTree Project tree that is updated on certain events.
   * @param project Project that contains the views to be displayed.
   * @param container Container that represents the context in which the module is displayed.
   */
  public CProjectViewsContainerComponent(final JTree projectTree, final INaviProject project,
      final IViewContainer container) {
    super(new BorderLayout());

    Preconditions.checkNotNull(projectTree, "IE02053: Project tree argument can not be null");
    Preconditions.checkNotNull(project, "IE02054: Project argument can not be null");
    Preconditions.checkNotNull(container, "IE02055: Container argument can not be null");

    m_project = project;

    add(new CUserViewsComponent(projectTree, container));

    updateBorderText();

    m_project.addListener(m_viewContainerListener);

    for (final INaviView view : project.getContent().getViews()) {
      view.addListener(m_viewListener);
    }
  }

  /**
   * Updates the border text that shows information about the project views.
   */
  private void updateBorderText() {
    final List<INaviView> views = m_project.getContent().getViews();
    m_callgraphBorder.setTitle(String.format("%d Call graph Views",
        CViewFilter.getCallgraphViewCount(views)));
    m_flowgraphBorder.setTitle(String.format("%d Flow graph Views",
        CViewFilter.getFlowgraphViewCount(views)));
    m_mixedgraphBorder.setTitle(String.format("%d Mixed graph Views",
        CViewFilter.getMixedgraphViewCount(views)));
    updateUI();
  }

  @Override
  public void dispose() {
    m_project.removeListener(m_viewContainerListener);

    if (m_project.isLoaded()) {
      for (final INaviView view : m_project.getContent().getViews()) {
        view.removeListener(m_viewListener);
      }
    }
  }

  /**
   * Updates the GUI on relevant changes in the project.
   */
  private class InternalProjectListener extends CProjectListenerAdapter {
    @Override
    public void addedView(final INaviProject container, final INaviView view) {
      updateBorderText();

      view.addListener(m_viewListener);
    }

    @Override
    public void deletedView(final INaviProject moduleContainer, final INaviView view) {
      updateBorderText();

      view.removeListener(m_viewListener);
    }

    @Override
    public void loadedProject(final CProject container) {
      updateBorderText();
    }
  }

  /**
   * Updates the GUI on relevant changes in the displayed views.
   */
  private class InternalViewListener extends CViewListenerAdapter {
    @Override
    public void changedGraphType(final INaviView view, final GraphType type, final GraphType oldType) {
      updateBorderText();
    }
  }
}
