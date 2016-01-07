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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;

/**
 * Shows a combined status bar that summarizes all active progress operations.
 */
public class CProgressStatusBar extends JPanel {

  /**
   * The progress panel.
   */
  private final CProgressPanel m_progressPanel = new CProgressPanel(null, true, false, false);

  /**
   * Updates the progress panel on changes to ongoing operations.
   */
  private final IGlobalProgressManagerListener m_listener = new IGlobalProgressManagerListener() {
    @Override
    public void added(final IProgressOperation operation) {
      process();
    }

    @Override
    public void removed(final IProgressOperation operation) {
      process();
    }
  };

  /**
   * Progress dialog shown when the user double-clicks the status bar.
   */
  private static CGlobalProgressDialog m_dialog = new CGlobalProgressDialog();

  /**
   * Creates a new status bar object.
   */
  public CProgressStatusBar() {
    setLayout(new BorderLayout());

    add(m_progressPanel);
    process();

    setPreferredSize(new Dimension(200, 30));

    CGlobalProgressManager.instance().addListener(m_listener);

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent event) {
        if ((event.getClickCount() == 2) && SwingUtilities.isLeftMouseButton(event)) {
          getDialog().setVisible(true);
        }
      }
    });
  }

  /**
   * Lazily returns the instance of the global progress dialog.
   * 
   * @return The instance of the global progress dialog.
   */
  private CGlobalProgressDialog getDialog() {
    return m_dialog;
  }

  /**
   * Processes a new ongoing operation.
   */
  private synchronized void process() {
    updateDisplayString();
  }

  /**
   * Updates the string to display in the status bar.
   */
  private void updateDisplayString() {
    final List<IProgressOperation> operations = CGlobalProgressManager.instance().getOperations();

    if (operations.isEmpty()) {
      m_progressPanel.setProgressText("");
      this.setVisible(false);
    } else {
      final IProgressOperation firstOperation = operations.get(0);

      final String displayString =
          firstOperation.getDescription()
              + (operations.size() == 1 ? "" : " ( +" + (operations.size() - 1) + ")");

      m_progressPanel.setProgressText(displayString);
      this.setVisible(true);
    }
  }
}
