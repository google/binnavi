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
package com.google.security.zynamics.binnavi.Gui.Tutorials;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Tutorials.CTutorial;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This dialog lists all available tutorials and the user can select the tutorial he wants to run.
 */
public final class CTutorialStartDialog extends JDialog {
  /**
   * The tutorial selected by the user.
   */
  private CTutorial selectedTutorial;

  /**
   * List box where all tutorials are listed.
   */
  private final JList<CTutorial> tutorialList;

  /**
   * Upon selecting a tutorial, the tutorial description is shown here.
   */
  private final JTextPane tutorialDescription = new JTextPane();

  /**
   * Creates a new tutorial dialog.
   *
   * @param owner Parent window of the dialog.
   * @param tutorials Tutorials the user can select from.
   */
  public CTutorialStartDialog(final JFrame owner, final List<CTutorial> tutorials) {
    super(owner, "Tutorial Selection", true);

    Preconditions.checkNotNull(owner, "IE02080: Owner argument can not be null");
    Preconditions.checkNotNull(tutorials, "IE02081: Tutorials argument can not be null");

    new CDialogEscaper(this);

    final JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(new TitledBorder("Please select the tutorial you want to run"));

    tutorialList = new JList<CTutorial>(tutorials.toArray(new CTutorial[tutorials.size()]));
    tutorialList.addListSelectionListener(new InternalSelectionListener());

    tutorialDescription.setBorder(new TitledBorder(""));
    tutorialDescription.setContentType("text/html");
    tutorialDescription.setEditable(false);

    if (!tutorials.isEmpty()) {
      tutorialList.setSelectedIndex(0);
    }

    final JSplitPane splitPane =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tutorialList),
            new JScrollPane(tutorialDescription));
    splitPane.setDividerLocation(200);

    panel.add(splitPane);

    final JPanel bottomPanel = new JPanel(new BorderLayout());

    final JPanel buttonPanel = new JPanel();

    final JButton closeButton = new JButton(new CancelAction());
    final JButton startButton = new JButton(new StartAction());

    buttonPanel.add(closeButton);
    buttonPanel.add(startButton);

    bottomPanel.add(buttonPanel, BorderLayout.EAST);

    add(panel);
    add(bottomPanel, BorderLayout.SOUTH);

    setSize(new Dimension(600, 400));

    GuiHelper.centerChildToParent(owner, this, true);
  }

  /**
   * Returns the tutorial selected by the user.
   *
   * @return The selected tutorial or null if no tutorial was selected.
   */
  public CTutorial getSelectedTutorial() {
    return selectedTutorial;
  }

  /**
   * Action handler for the Cancel button.
   */
  private class CancelAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 4681649702002708880L;

    /**
     * Creates a new action handler for the Cancel button.
     */
    private CancelAction() {
      super("Cancel");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      dispose();
    }
  }

  /**
   * Listener that updates the dialog on tutorial selection changes.
   */
  private class InternalSelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(final ListSelectionEvent event) {
      final CTutorial selectedTutorial = tutorialList.getSelectedValue();

      if (selectedTutorial != null) {
        tutorialDescription.setText(selectedTutorial.getDescription());
      }
    }
  }

  /**
   * Action handler for the Start button.
   */
  private class StartAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 5537321482800475692L;

    /**
     * Creates a new action handler for the Start button.
     */
    private StartAction() {
      super("Start");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      selectedTutorial = tutorialList.getSelectedValue();

      dispose();
    }
  }
}
