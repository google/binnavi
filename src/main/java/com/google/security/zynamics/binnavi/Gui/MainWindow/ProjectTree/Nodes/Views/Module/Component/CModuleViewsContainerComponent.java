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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component;

import java.awt.BorderLayout;

import javax.swing.JTree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.CUserViewsComponent;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Component that displays information about the non-native views of a module.
 */
public final class CModuleViewsContainerComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6803579984362240312L;

  /**
   * Component where the non-native flow graph views are shown.
   */
  private final CUserViewsComponent m_flowComponent;

  /**
   * Creates a new component object.
   * 
   * @param projectTree Project tree that is updated on certain events.
   * @param container Container that represents the context in which the module is displayed.
   */
  public CModuleViewsContainerComponent(final JTree projectTree, final IViewContainer container) {
    super(new BorderLayout());

    Preconditions.checkNotNull(projectTree, "IE02045: Project tree argument can not be null");
    Preconditions.checkNotNull(container, "IE02048: Container argument can not be null");

    m_flowComponent = new CUserViewsComponent(projectTree, container);

    add(m_flowComponent);
  }

  /**
   * Frees all allocated resources.
   */
  @Override
  public void dispose() {
    m_flowComponent.dispose();
  }
}
