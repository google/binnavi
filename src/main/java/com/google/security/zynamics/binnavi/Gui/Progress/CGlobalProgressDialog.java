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
package com.google.security.zynamics.binnavi.Gui.Progress;



import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.security.zynamics.binnavi.Gui.CIconInitializer;
import com.google.security.zynamics.zylib.gui.GuiHelper;



/**
 * Progress dialog that shows all active progress operations.
 */
public final class CGlobalProgressDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8241186630373844927L;

  /**
   * Panel that shows the individual progress dialogs.
   */
  private final JPanel m_progressPanel = new JPanel(new GridBagLayout());

  /**
   * Keeps track of new and finished progress operations.
   */
  private final IGlobalProgressManagerListener m_listener = new IGlobalProgressManagerListener() {
    @Override
    public void added(final IProgressOperation operation) {
      add(operation);
    }

    @Override
    public void removed(final IProgressOperation operation) {
      remove(operation);
    }
  };

  /**
   * Counts how many progress operations were already shown (for layouting reasons).
   */
  private int counter = 0;

  /**
   * Creates a new dialog object.
   */
  public CGlobalProgressDialog() {
    super((Window) null, "Ongoing operations");

    CIconInitializer.initializeWindowIcons(this);

    setLayout(new BorderLayout());

    final JPanel innerPanel = new JPanel(new BorderLayout());
    final JPanel upperPanel = new JPanel(new BorderLayout());

    upperPanel.add(m_progressPanel, BorderLayout.NORTH);
    innerPanel.add(new JScrollPane(upperPanel));
    add(innerPanel);
    final JPanel lowerPanel = new JPanel(new BorderLayout());
    lowerPanel.add(new JButton(new CHideAction()), BorderLayout.EAST);
    add(lowerPanel, BorderLayout.SOUTH);
    setSize(400, 300);
    GuiHelper.centerOnScreen(this);
    setDefaultCloseOperation(HIDE_ON_CLOSE);

    for (final IProgressOperation operation : CGlobalProgressManager.instance().getOperations()) {
      add(operation);
    }

    setFocusable(false);

    CGlobalProgressManager.instance().addListener(m_listener);
  }

  /**
   * Updates the dialog with a new operation.
   * 
   * @param operation The new operation.
   */
  private void add(final IProgressOperation operation) {
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridy = counter++;
    constraints.gridx = 0;
    constraints.weightx = 1;
    constraints.weighty = 1;
    m_progressPanel.add(operation.getProgressPanel(), constraints);

    // new SwingInvoker() {
    // @Override
    // protected void operation() {
    // m_progressPanel.updateUI();
    // }
    // }.invokeLater();
  }

  /**
   * Updates the dialog after an operation that was removed.
   * 
   * @param operation The removed operation.
   */
  private void remove(final IProgressOperation operation) {
    m_progressPanel.remove(operation.getProgressPanel());

    // new SwingInvoker() {
    // @Override
    // protected void operation() {
    // m_progressPanel.updateUI();
    // }
    // }.invokeLater();

    if (m_progressPanel.getComponentCount() == 0) {
      setVisible(false);
    }
  }

  /**
   * Action class for handling clicks on the Hide button.
   */
  private final class CHideAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 20541718054084944L;

    /**
     * Creates a new action object.
     */
    public CHideAction() {
      super("Hide");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      setVisible(false);
    }
  }
}
