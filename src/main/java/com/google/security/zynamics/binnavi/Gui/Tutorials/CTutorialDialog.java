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



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.CIconInitializer;
import com.google.security.zynamics.binnavi.Tutorials.CTutorial;
import com.google.security.zynamics.binnavi.Tutorials.ITutorialListener;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * Dialog where tutorials are run. In this dialog the individual tutorial steps are shown and the
 * user can control the progress of the tutorials.
 */
public final class CTutorialDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5087316538644576623L;

  /**
   * The only valid instance of the tutorial dialog class.
   */
  private static CTutorialDialog m_instance = new CTutorialDialog();

  /**
   * Message shown when the user executes a wrong action.
   */
  private static final String WRONG_ACTION_STRING = "Wrong action executed (%d)";

  /**
   * Message that shows the progress of the tutorial.
   */
  private static final String TOPIC_STRING = "Current Tutorial: %s (Step %d of %d)";

  /**
   * Text field where the step description is shown.
   */
  private final JTextPane m_descriptionField = new JTextPane();

  /**
   * Listener that updates the dialog on changes in the current tutorial.
   */
  private final InternalTutorialListener m_listener = new InternalTutorialListener();

  /**
   * The currently active tutorial.
   */
  private CTutorial m_currentTutorial = null;

  /**
   * Label where the title of the tutorial is shown.
   */
  private final JLabel m_topicLabel = new JLabel();

  /**
   * Field where warning messages are shown.
   */
  private final JTextField m_warningLabel = new JTextField("XXXXXXXX");

  /**
   * Button used to advance the active tutorial to the next step (if available).
   */
  private final JButton m_nextButton = new JButton(new NextAction());

  /**
   * The skip button allows the user to skip to the next tutorial step without performing the
   * required actions.
   */
  private final JButton m_skipButton = new JButton(new SkipAction());

  /**
   * Creates a new tutorial dialog.
   */
  private CTutorialDialog() {
    super((Window) null, "BinNavi Tutorial");

    CIconInitializer.initializeWindowIcons(this);

    setLayout(new BorderLayout());

    m_topicLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

    m_descriptionField.setContentType("text/html");
    m_descriptionField.setEditable(false);
    m_descriptionField.setBorder(new TitledBorder(""));

    final JPanel topPanel = new JPanel();

    topPanel.add(m_topicLabel);

    add(m_topicLabel, BorderLayout.NORTH);

    add(new JScrollPane(m_descriptionField));

    final JPanel bottomPanel = new JPanel(new BorderLayout());

    final JButton cancelButton = new JButton(new QuitAction());

    final JPanel leftPanel = new JPanel(new BorderLayout());

    m_warningLabel.setEditable(false);
    m_warningLabel.setForeground(Color.RED);
    m_warningLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

    leftPanel.add(cancelButton, BorderLayout.WEST);
    leftPanel.add(m_warningLabel);

    final JPanel buttonsPanel = new JPanel(new GridLayout(0, 2));
    buttonsPanel.add(m_skipButton);
    buttonsPanel.add(m_nextButton);

    bottomPanel.add(leftPanel, BorderLayout.WEST);
    bottomPanel.add(buttonsPanel, BorderLayout.EAST);

    add(bottomPanel, BorderLayout.SOUTH);

    setSize(550, 300);

    GuiHelper.centerOnScreen(this);
    setLocation(getLocation().x + 200, getLocation().y - 200);

    setAlwaysOnTop(true);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new InternalWindowListener());
  }

  /**
   * Returns the globally valid instance of the tutorial dialog.
   * 
   * @return The globally valid instance of the tutorial dialog.
   */
  public static CTutorialDialog instance() {
    return m_instance;
  }

  /**
   * Closes the dialog after asking the user if he wants to cancel the current tutorial.
   */
  private void quit() {
    if (JOptionPane.YES_OPTION == CMessageBox.showYesNoCancelQuestion(this,
        "Do you really want to quit the current tutorial?")) {
      m_currentTutorial.removeListener(m_listener);
      m_currentTutorial = null;

      m_warningLabel.setText("");
      updateNextButton();
      m_skipButton.setEnabled(true);

      m_descriptionField.setText("");

      setVisible(false);
    }
  }

  /**
   * Updates the Next button depending on the state of the current tutorial.
   */
  private void updateNextButton() {
    m_nextButton.setEnabled((m_currentTutorial != null)
        && (m_currentTutorial.getStepCounter() != m_currentTutorial.getStepCount())
        && m_currentTutorial.getCurrentStep().canNext());
  }

  /**
   * Returns the currently active tutorial.
   * 
   * @return The currently active tutorial or null if no tutorial is active.
   */
  public CTutorial getCurrentTutorial() {
    return m_currentTutorial;
  }

  /**
   * Starts a given tutorial.
   * 
   * @param tutorial The tutorial to start.
   */
  public void start(final CTutorial tutorial) {
    Preconditions.checkNotNull(tutorial, "IE01296: Tutorial argument can not be null");

    tutorial.addListener(m_listener);

    tutorial.start();
  }

  /**
   * Shows a warning that a wrong action was executed by the user.
   * 
   * @param actionId Identifier of the wrong action.
   */
  public void wrongAction(final long actionId) {
    m_warningLabel.setText(String.format(WRONG_ACTION_STRING, actionId));
    toFront();
  }

  /**
   * Listener that updates the dialog on changes in the current tutorial.
   */
  private class InternalTutorialListener implements ITutorialListener {
    @Override
    public void changedStep(final CTutorial tutorial) {
      m_warningLabel.setText("");
      updateNextButton();

      m_topicLabel.setText(String.format(TOPIC_STRING, tutorial.getName(),
          tutorial.getStepCounter() + 1, tutorial.getStepCount()));

      m_descriptionField.setText(tutorial.getCurrentStep().getDescription());
      m_descriptionField.setCaretPosition(0);

      toFront();
    }

    @Override
    public void finished(final CTutorial tutorial) {
      m_warningLabel.setText("");
      updateNextButton();
      m_skipButton.setEnabled(false);

      m_descriptionField.setText("Tutorial finished");

      toFront();
    }

    @Override
    public void started(final CTutorial tutorial) {
      m_warningLabel.setText("");
      m_currentTutorial = tutorial;

      m_topicLabel.setText(String.format(TOPIC_STRING, tutorial.getName(),
          tutorial.getStepCounter() + 1, tutorial.getStepCount()));
      updateNextButton();

      if (!isVisible()) {
        setVisible(true);
      }

      m_descriptionField.setText(tutorial.getCurrentStep().getDescription());

      toFront();
    }
  }

  /**
   * Window listener for modified window closing behaviour.
   */
  private class InternalWindowListener extends WindowAdapter {
    @Override
    public void windowClosing(final WindowEvent event) {
      quit();
    }
  }

  /**
   * Action controller of the Next button.
   */
  private class NextAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 7665614446808231684L;

    /**
     * Creates a new action controller for the Next button.
     */
    private NextAction() {
      super("Next", new ImageIcon(CMain.class.getResource("data/arrow_right.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      m_currentTutorial.next();
    }
  }

  /**
   * Action controller for the Quit button.
   */
  private class QuitAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 5896923932054029970L;

    /**
     * Creates a new action controller for the Quit button.
     */
    private QuitAction() {
      super("Quit", new ImageIcon(CMain.class.getResource("data/cancel.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      quit();
    }
  }

  /**
   * Action controller for the Skip button.
   */
  private class SkipAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -623733076208212254L;

    /**
     * Creates a new action controller for the Next button.
     */
    private SkipAction() {
      super("Skip Step", new ImageIcon(CMain.class.getResource("data/arrow_right.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      m_currentTutorial.next();
    }
  }
}
