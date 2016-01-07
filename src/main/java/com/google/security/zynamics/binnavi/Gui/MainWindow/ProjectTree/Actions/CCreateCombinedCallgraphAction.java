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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions;



import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CAddressSpaceFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;


/**
 * Action class for combining the callgraphs of the modules of an address space.
 */
public final class CCreateCombinedCallgraphAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8340580137660093660L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_frame;

  /**
   * Context in which the created view is opened.
   */
  private final IViewContainer m_container;

  /**
   * Project where the combined Call graph view is created.
   */
  private final INaviProject m_project;

  /**
   * Address space whose Call graphs are combined.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * Creates a new action object.
   * 
   * @param frame Parent window used for dialogs.
   * @param container Context in which the created view is opened.
   * @param project Project where the combined Call graph view is created.
   * @param addressSpace Address space whose Call graphs are combined.
   */
  public CCreateCombinedCallgraphAction(final JFrame frame, final IViewContainer container,
      final INaviProject project, final INaviAddressSpace addressSpace) {
    super("Create combined callgraph");

    m_frame = Preconditions.checkNotNull(frame, "Error: frame argument can not be null");
    m_container =
        Preconditions.checkNotNull(container, "Error: container argument can not be null");
    m_project = Preconditions.checkNotNull(project, "Error: project argument can not be null");
    m_addressSpace =
        Preconditions.checkNotNull(addressSpace, "Error: addressSpace argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CAddressSpaceFunctions.createCombinedCallgraph(m_frame, m_container, m_project, m_addressSpace);
  }
}
