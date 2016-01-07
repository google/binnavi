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
package com.google.security.zynamics.binnavi.Gui.plugins.output;



import com.google.security.zynamics.binnavi.Gui.plugins.output.implementations.CLogConsoleFunctions;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Global plugin output dialog where plugins can quickly write to.
 */
public final class CPluginOutputDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -313664677958811619L;

  /**
   * Globally valid instance of the plugin output dialog.
   */
  private static CPluginOutputDialog m_instance = new CPluginOutputDialog();

  /**
   * Text field where the plugin ouput is shown.
   */
  private final JTextArea m_textArea = new JTextArea();

  /**
   * Creates a new plugin output dialog.
   */
  private CPluginOutputDialog() {
    super((JFrame) null, "Global Plugin Log");

    new CDialogEscaper(this);

    setLayout(new BorderLayout());

    m_textArea.setEditable(false);
    m_textArea.setFont(GuiHelper.MONOSPACED_FONT);

    add(new JScrollPane(m_textArea));

    final JPanel bottomPanel = new JPanel(new BorderLayout());

    bottomPanel.add(new JButton(new ClearAction()), BorderLayout.WEST);
    bottomPanel.add(new JButton(new CloseAction()), BorderLayout.EAST);

    add(bottomPanel, BorderLayout.SOUTH);

    setAlwaysOnTop(true);

    setJMenuBar(new CLogConsoleMenuBar());

    m_textArea.addMouseListener(new InternalMouseListener());

    setSize(600, 400);
  }

  /**
   * Returns the only valid instance of the plugin output dialog.
   *
   * @return The only valid instance of the plugin output dialog.
   */
  public static CPluginOutputDialog instance() {
    return m_instance;
  }

  /**
   * Clears the output area.
   */
  public void clear() {
    m_textArea.setText("");
  }

  /**
   * Appends a new message to the output area.
   *
   * @param message The message to add.
   */
  public void log(final String message) {
    m_textArea.append(message);
    m_textArea.setCaretPosition(m_textArea.getText().length());
  }

  /**
   * Brings the output dialog to the front.
   */
  public void showDialog() {
    setVisible(true);
    toFront();
  }

  /**
   * Action handler for the Clear button.
   */
  private class ClearAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -7767044892455183230L;

    /**
     * Creates a new action handler object.
     */
    private ClearAction() {
      super("Clear");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      m_textArea.setText("");
    }
  }

  /**
   * Menu bar of the log console window.
   */
  private class CLogConsoleMenuBar extends JMenuBar {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 1484024003232886495L;

    /**
     * Creates a new log console menu bar object.
     */
    public CLogConsoleMenuBar() {
      final JMenu menu = new JMenu("Log");
      menu.setMnemonic("LogMenuMnemonic".charAt(0));

      menu.add(new JMenuItem(new CSaveToFileAction()));

      add(menu);
    }
  }

  /**
   * Action handler for the Close button.
   */
  private class CloseAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -687912619884442777L;

    /**
     * Creates a new action handler object.
     */
    private CloseAction() {
      super("Close");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      setVisible(false);
    }
  }

  /**
   * Action class for saving the content of the log to a file.
   */
  private class CSaveToFileAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 4433760167850896301L;

    /**
     * Creates a new action object.
     */
    public CSaveToFileAction() {
      super("Save to file");

      putValue(MNEMONIC_KEY, (int) "SaveLogToFileMnemonic".charAt(0));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      CLogConsoleFunctions.save(CPluginOutputDialog.this, m_textArea.getText());
    }
  }

  /**
   * Keeps track of clicks on the plugin output dialog.
   */
  private class InternalMouseListener extends MouseAdapter {
    /**
     * Shows a popup menu depending on the mouse event.
     *
     * @param event The mouse event.
     */
    private void maybeShowPopup(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        final JPopupMenu menu = new JPopupMenu();

        menu.add(new CopyLogAction(m_textArea));

        menu.show(event.getComponent(), event.getX(), event.getY());
      }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      maybeShowPopup(event);
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      maybeShowPopup(event);
    }
  }
}
