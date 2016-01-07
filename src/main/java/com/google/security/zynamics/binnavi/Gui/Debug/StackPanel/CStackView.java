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
package com.google.security.zynamics.binnavi.Gui.Debug.StackPanel;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.zylib.gui.JStackView.JStackView;

/**
 * Combines a stack view with a synchronizer that keeps the stack view synchronized to a debug GUI
 * perspective.
 */
public final class CStackView extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3557489956883932908L;

  /**
   * Control where the stack data is shown.
   */
  private final JStackView m_stackView;

  /**
   * Model that provides the stack data to be displayed.
   */
  private final CStackMemoryProvider m_model = new CStackMemoryProvider();

  /**
   * Synchronizes the data shown in the stack view with the underlying data.
   */
  private final CStackViewSynchronizer m_synchronizer;

  /**
   * Creates a new stack panel.
   *
   * @param debugPerspectiveModel Describes the active debugger GUI options.
   */
  public CStackView(final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01503: Debug perspective model argument can not be null");

    setBorder(new TitledBorder("Stack"));

    m_model.setDebugger(debugPerspectiveModel.getCurrentSelectedDebugger());

    m_stackView = new JStackView(m_model);
    m_stackView.addMouseListener(new InternalMouseListener());

    add(m_stackView);

    m_synchronizer = new CStackViewSynchronizer(m_stackView, m_model, debugPerspectiveModel);
  }

  /**
   * Shows the stack window context menu.
   *
   * @param event Mouse event that describes the click on the stack window.
   */
  private void showPopupMenu(final MouseEvent event) {
    final JPopupMenu menu = new CStackViewMenu(m_stackView, m_model, event.getPoint());

    menu.show(this, event.getX(), event.getY());
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_synchronizer.dispose();
  }

  /**
   * Returns the model that provides the stack data to be displayed.
   *
   * @return The model that provides the stack data to be displayed.
   */
  public CStackMemoryProvider getStackProvider() {
    return m_model;
  }

  /**
   * Handles clicks on the stack view.
   */
  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }
  }
}
