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
package com.google.security.zynamics.binnavi.Gui.GraphWindows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.CLimitedInputPane;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * This dialog is used to edit the name and description of a view.
 * 
 * TODO: Generalize this class because not only views are edited by it.
 */
public final class CViewCommentDialog extends JDialog implements ActionListener {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1008750634672304176L;

  /**
   * The text field where the user can enter a new name for the view.
   */
  private final JTextField m_nameField;

  /**
   * The text field where the user can enter a new description for the view.
   */
  private final CLimitedInputPane m_descriptionField;

  /**
   * Flag that indicates whether the dialog was closed through the OK button (false) or through
   * other means (true).
   */
  private boolean m_wasCancelled = true;

  /**
   * Creates a new dialog object.
   * 
   * @param parent Parent window of the dialog.
   * @param title Title of the dialog.
   * @param name Name of the element to edit.
   * @param description Comment to edit.
   */
  public CViewCommentDialog(final Window parent, final String title, final String name,
      final String description) {
    super(parent, title, ModalityType.APPLICATION_MODAL);

    Preconditions.checkNotNull(parent, "IE01632: Parent argument can't be null");
    Preconditions.checkNotNull(name, "IE01633: Name argument can't be null");

    new CDialogEscaper(this);

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));

    final JPanel upperPanel = new JPanel(new BorderLayout());
    final JPanel namePanel = new JPanel(new BorderLayout());
    final JLabel nameLabel = new JLabel("Name");

    nameLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
    namePanel.add(nameLabel, BorderLayout.WEST);
    m_nameField = new JTextField(name);
    namePanel.add(m_nameField, BorderLayout.CENTER);
    namePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    upperPanel.add(namePanel, BorderLayout.NORTH);

    m_descriptionField = new CLimitedInputPane(description == null ? "" : description);
    m_descriptionField.setEditable(true);
    m_descriptionField.setBorder(new LineBorder(Color.BLACK));

    final JScrollPane descriptionPane = new JScrollPane(m_descriptionField);
    descriptionPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    upperPanel.add(descriptionPane, BorderLayout.CENTER);
    panel.add(upperPanel, BorderLayout.CENTER);
    final CPanelTwoButtons okcancel = new CPanelTwoButtons(this, "OK", "Cancel");
    panel.add(okcancel, BorderLayout.SOUTH);
    getContentPane().add(panel, BorderLayout.CENTER);
    setSize(350, 350);
    GuiHelper.centerChildToParent(parent, this, true);
    final InputMap windowImap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    windowImap.put(HotKeys.APPLY_HK.getKeyStroke(), "APPLY");
    getRootPane().getActionMap().put("APPLY", CActionProxy.proxy(new ApplyAction()));

  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (event.getActionCommand().equals("OK")) {
      m_wasCancelled = false;
    }

    dispose();
  }

  /**
   * Returns the new comment entered by the user.
   * 
   * @return The new comment entered by the user.
   */
  public String getComment() {
    return m_descriptionField.getText();
  }

  @Override
  public String getName() {
    return m_nameField.getText();
  }

  /**
   * Returns a flag that describes how the dialog was closed.
   * 
   * @return True, to indicate the dialog was cancelled. False, otherwise.
   */
  public boolean wasCancelled() {
    return m_wasCancelled;
  }

  /**
   * Action object for handling clicks on the OK button.
   */
  private class ApplyAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 2803007873766570860L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      m_wasCancelled = false;
      dispose();
    }
  }
}
