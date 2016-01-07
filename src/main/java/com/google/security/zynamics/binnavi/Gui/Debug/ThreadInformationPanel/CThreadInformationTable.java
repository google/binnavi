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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel;

import javax.swing.JTable;

/**
 * Table that displays thread information.
 */
public class CThreadInformationTable extends JTable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1437833381872939346L;

  /**
   * Model of the table.
   */
  private final CThreadInformationTableModel m_model = new CThreadInformationTableModel();

  /**
   * Used to render the table.
   */
  private final CThreadInformationTableRenderer m_renderer =
      new CThreadInformationTableRenderer(this);

  /**
   * Creates a new table object.
   */
  public CThreadInformationTable() {
    setModel(m_model);

    setDefaultRenderer(Object.class, m_renderer);
  }

  /**
   * Returns the table renderer.
   *
   * @return The table renderer.
   */
  public CThreadInformationTableRenderer getDefaultRenderer() {
    return m_renderer;
  }

  @Override
  public CThreadInformationTableModel getModel() {
    return m_model;
  }
}
