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
import javax.swing.JTree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CProjectFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;

/**
 * Action that can be used to load projects.
 */
public final class CLoadProjectAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3164027319049009140L;

  /**
   * Tree to be updated after the projects were loaded.
   */
  private final JTree m_projectTree;

  /**
   * Projects to be loaded.
   */
  private final INaviProject[] m_projects;

  /**
   * Creates a new action object.
   * 
   * @param projectTree Tree to be updated after the address spaces were loaded.
   * @param projects Projects to be loaded.
   */
  public CLoadProjectAction(final JTree projectTree, final INaviProject[] projects) {
    super("Load Project");

    m_projectTree =
        Preconditions.checkNotNull(projectTree, "IE01904: Project tree argument can not be null");
    m_projects =
        Preconditions.checkNotNull(projects, "IE01905: Projects argument can't be null").clone();

    for (final INaviProject project : projects) {
      Preconditions.checkNotNull(project, "IE01906: Projects list contains a null-element");
    }

    putValue(ACCELERATOR_KEY, HotKeys.LOAD_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_LOAD_PROJECT".charAt(0));

  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CProjectFunctions.openProjects(m_projectTree, m_projects);
  }
}
