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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Views;



import com.google.security.zynamics.binnavi.Gui.CIconInitializer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Functions.IFilterDialogListener;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;



/**
 * Dialog for filtering views.
 */
public final class CViewFilterDialog extends JDialog {
  /**
   * Panel for filtering views by their type.
   */
  private CViewTypePanel m_typePanel;

  /**
   * Listeners that are notified about changes in the dialog options.
   */
  private final ListenerProvider<IFilterDialogListener> m_listeners =
      new ListenerProvider<IFilterDialogListener>();

  /**
   * Creates a new view filter dialog.
   */
  public CViewFilterDialog(final ITagManager viewTagManager, final ITagManager nodeTagManager) {
    super((Window) null, "View filter");

    new CDialogEscaper(this);
    CIconInitializer.initializeWindowIcons(this);

    setLayout(new BorderLayout());

    final JPanel innerPanel = new JPanel(new GridLayout(1, 3));

    innerPanel.add(m_typePanel = new CViewTypePanel(m_listeners));

    add(innerPanel);

    pack();

    addWindowFocusListener(new WindowAdapter() {
      @Override
      public void windowLostFocus(final WindowEvent event) {
        for (final IFilterDialogListener listener : m_listeners) {
          m_listeners.removeListener(listener);
        }

        dispose();
      }
    });
  }

  /**
   * Adds a listener that is notified about changes in the dialog.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IFilterDialogListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the view type panel.
   *
   * @return The view type panel.
   */
  public CViewTypePanel getViewTypePanel() {
    return m_typePanel;
  }

  /**
   * Removes a previously added listener object.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final IFilterDialogListener listener) {
    m_listeners.removeListener(listener);
  }
}
