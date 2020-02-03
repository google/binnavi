// Copyright 2011-2016 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.binnavi.Gui.IdbSelection;

import com.google.security.zynamics.binnavi.Gui.IdaSelectionDialog.CBinExportInstallationChecker;
import com.google.security.zynamics.binnavi.Gui.IdaSelectionDialog.InstallationState;
import com.google.security.zynamics.binnavi.Importers.CIdbFileFilter;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Dialog used to select one or more IDB files for importing.
 */
public final class CIdbSelectionDialog extends JDialog {
  /**
   * Used to select between the last N directories from which IDB files were imported.
   */
  private final JComboBox<?> m_previousDirectoryBox;

  /**
   * Used to select the exporter.
   */
  private final JComboBox<?> m_exportersBox;

  /**
   * Slider for selecting the number of parallel imports.
   */
  private final JSlider m_threadsSlider;

  /**
   * Files selected by the user for importing.
   */
  private final IFilledList<File> m_selectedFiles = new FilledList<>();

  /**
   * Used to navigate through the available drives.
   */
  private final JFileChooser m_chooser;

  /**
   * List where all selected IDB files are shown.
   */
  private final CIdbFileList m_selectionList = new CIdbFileList();

  /**
   * Handles clicks on the Import and Cancel buttons.
   */
  private ExporterSelection m_exporter;

  /**
   * Creates a new IDB selection dialog.
   *
   * @param parent Parent window of the dialog.
   * @param previousDirectories List of directories from which IDB files were previously selected.
   */
  private CIdbSelectionDialog(final JFrame parent, final List<String> previousDirectories) {
    super(parent, "Select IDB files for importing", true);

    new CDialogEscaper(this);

    final JPanel upperPanel = new JPanel(new GridBagLayout());

    final GridBagConstraints constraints = new GridBagConstraints();

    final JLabel availableExportersLabel = new JLabel("Available Exporters" + ":");
    availableExportersLabel.setBorder(new EmptyBorder(10, 5, 10, 5));

    constraints.anchor = GridBagConstraints.FIRST_LINE_START;
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 1;
    constraints.weighty = 1;
    upperPanel.add(availableExportersLabel, constraints);

    m_exportersBox = new JComboBox<Object>(new Object[] {"BinExport IDA plugin"});
    m_exportersBox.setBorder(new EmptyBorder(10, 5, 10, 5));

    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.weightx = 5;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    upperPanel.add(m_exportersBox, constraints);

    final JLabel previousDirectoriesLabel = new JLabel("Previous directories" + ":");
    previousDirectoriesLabel.setBorder(new EmptyBorder(10, 5, 10, 5));

    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.NONE;
    upperPanel.add(previousDirectoriesLabel, constraints);

    m_previousDirectoryBox = new CPreviousDirectoriesBox(previousDirectories);
    m_previousDirectoryBox.setBorder(new EmptyBorder(10, 5, 10, 5));

    m_previousDirectoryBox.addItemListener(new InternalItemListener());

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.weightx = 5;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    upperPanel.add(m_previousDirectoryBox, constraints);

    // ---

    final JLabel parallelImportsLabel = new JLabel("Number of parallel imports" + ":");
    parallelImportsLabel.setBorder(new EmptyBorder(10, 5, 10, 5));

    constraints.anchor = GridBagConstraints.FIRST_LINE_START;
    constraints.gridx = 0;
    constraints.gridy = 2;
    constraints.weightx = 1;
    constraints.weighty = 1;
    upperPanel.add(parallelImportsLabel, constraints);

    final JPanel innerPanel = new JPanel(new BorderLayout());

    m_threadsSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 3);
    m_threadsSlider.setPaintLabels(true);
    m_threadsSlider.setPaintTicks(true);
    m_threadsSlider.setPaintTrack(true);
    m_threadsSlider.setMajorTickSpacing(1);
    m_threadsSlider.setSnapToTicks(true);
    m_threadsSlider.setBorder(new EmptyBorder(10, 5, 10, 5));

    innerPanel.add(m_threadsSlider);

    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.weightx = 5;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    upperPanel.add(innerPanel, constraints);

    final JPanel leftMiddlePanel = new JPanel(new BorderLayout());

    /**
     * Workaround for null pointer exception in file chooser as reported in the following bug case:
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6973685
     */
    m_chooser = new JFileChooser() {
      /**
       * Used for serialization.
       */
      private static final long serialVersionUID = 4054469348978937674L;

      @Override
      public void approveSelection() {
        if (!updateSelectedExporter()) {
          return;
        }

        m_selectedFiles.add(m_chooser.getSelectedFile());

        CIdbSelectionDialog.this.dispose();
      }

      @Override
      public void cancelSelection() {
        CIdbSelectionDialog.this.dispose();
      }
    };

    m_chooser.setControlButtonsAreShown(false);
    m_chooser.setFileFilter(new CIdbFileFilter());
    m_chooser.setMultiSelectionEnabled(true);

    if (!previousDirectories.isEmpty()) {
      final File file = new File(previousDirectories.get(0));

      if (file.exists() && file.isDirectory()) {
        m_chooser.setCurrentDirectory(file);
      }
    }

    leftMiddlePanel.add(m_chooser);
    leftMiddlePanel.setBorder(new TitledBorder("Select IDB Files"));

    final JPanel rightPanel = new JPanel(new BorderLayout());

    final JPanel centerButtonPanel = new JPanel();
    centerButtonPanel.setLayout(new BoxLayout(centerButtonPanel, BoxLayout.LINE_AXIS));
    final JPanel innerCenterButtonPanel = new JPanel();
    innerCenterButtonPanel.setLayout(new BoxLayout(innerCenterButtonPanel, BoxLayout.PAGE_AXIS));

    centerButtonPanel.add(innerCenterButtonPanel);

    final JButton addButton = new JButton(new CAddFilesAction());
    final JButton removeButton = new JButton(new CRemoveFilesAction());

    innerCenterButtonPanel.add(addButton);
    innerCenterButtonPanel.add(Box.createVerticalStrut(15));
    innerCenterButtonPanel.add(removeButton);
    innerCenterButtonPanel.setBorder(new EmptyBorder(0, 0, 0, 5));

    rightPanel.add(centerButtonPanel, BorderLayout.WEST);

    final JPanel listPanel = new JPanel(new BorderLayout());
    listPanel.add(new JScrollPane(m_selectionList));

    listPanel.setBorder(new TitledBorder("Selected IDB Files"));

    rightPanel.add(listPanel);
    rightPanel.setBorder(new EmptyBorder(0, 5, 0, 5));

    final JPanel middlePanel = new JPanel(new BorderLayout());
    middlePanel.add(leftMiddlePanel, BorderLayout.WEST);
    middlePanel.add(rightPanel);

    final JPanel lowerPanel = new JPanel(new BorderLayout());

    final CPanelTwoButtons buttonPanel =
        new CPanelTwoButtons(new InternalActionListener(), "Cancel", "Import");

    lowerPanel.add(buttonPanel, BorderLayout.EAST);

    add(upperPanel, BorderLayout.NORTH);
    add(middlePanel, BorderLayout.CENTER);
    add(lowerPanel, BorderLayout.SOUTH);

    setSize(800, 600);
  }

  private static boolean checkExporterInstall(final Container parent) {
    final File directory =
        new File(ConfigManager.instance().getGeneralSettings().getIdaDirectory());

    // ATTENTION: I am doing this check for null because of Case 2453 (Path to IDA Pro is not saved
    // after initial set up)
    // If the path passed to the above File constructor is invalid, the behavior of getParentFile
    // seems to be
    // different on Linux and Windows. On Linux, the directory object is null while on Windows it is
    // non-null
    // and just the same file that was passed to the File constructor.
    if ((directory == null) || !directory.exists()) {
      CMessageBox.showError(parent,
          "The selected exporter is not installed properly. Please configure the exporter in the IDA Pro configuration dialog.");

      return false;
    }

    final InstallationState cppState = CBinExportInstallationChecker.getState(directory);

    if (cppState != InstallationState.Installed) {
      CMessageBox.showError(parent,
          "The selected exporter is not installed properly. Please configure the exporter in the IDA Pro configuration dialog.");

      return false;
    }

    return true;
  }

  /**
   * Shows an IDB selection dialog.
   *
   * @param parent Parent window of the dialog.
   * @param previousDirectories List of directories from which IDB files were previously selected.
   *
   * @return The dialog object.
   */
  public static CIdbSelectionDialog show(final JFrame parent,
      final List<String> previousDirectories) {
    if (!checkExporterInstall(parent)) {
      return null;
    }

    final CIdbSelectionDialog dialog = new CIdbSelectionDialog(parent, previousDirectories);

    if (parent != null) {
      GuiHelper.centerChildToParent(parent, dialog, true);
    }

    dialog.setVisible(true);

    return dialog;
  }

  /**
   * Updates the selected exporter depending on the state of the selection box.
   *
   * @return True, if the exporter selection was changed. False, otherwise.
   */
  private boolean updateSelectedExporter() {
    m_exporter = ExporterSelection.BinExport;

    return checkExporterInstall(getParent());
  }

  /**
   * Returns the number of parallel imports selected by the user.
   *
   * @return The number of parallel imports selected by the user.
   */
  public int getNumberOfParallelImports() {
    return m_threadsSlider.getValue();
  }

  /**
   * Returns the selected exporter.
   *
   * @return The selected exporter.
   */
  public ExporterSelection getSelectedExporter() {
    return m_exporter;
  }

  /**
   * Returns the IDB files selected by the user.
   *
   * @return The IDB files selected by the user.
   */
  public List<File> getSelectedFiles() {
    return new FilledList<File>(m_selectedFiles);
  }

  /**
   * Action class for handling clicks on the >> button.
   */
  private class CAddFilesAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 1057262675119145707L;

    /**
     * Creates a new action object.
     */
    public CAddFilesAction() {
      super(">>");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      for (final File file : m_chooser.getSelectedFiles()) {
        m_selectionList.addFile(file);
      }
    }
  }

  /**
   * Action class for handling clicks on the << button.
   */
  private class CRemoveFilesAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -9004931928537737608L;

    /**
     * Creates a new action object.
     */
    public CRemoveFilesAction() {
      super("<<");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      final int[] indices = m_selectionList.getSelectedIndices();

      final File[] files = new File[indices.length];

      for (int i = 0; i < indices.length; i++) {
        files[i] = m_selectionList.getFileAt(indices[i]);
      }

      for (final File file : files) {
        m_selectionList.removeFile(file);
      }
    }
  }

  /**
   * Listens for clicks on the buttons.
   */
  private class InternalActionListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("Import")) {
        if (!updateSelectedExporter()) {
          return;
        }

        if (m_selectionList.getFileCount() == 0) {
          for (final File file : m_chooser.getSelectedFiles()) {
            m_selectedFiles.add(file);
          }
        } else {
          for (int i = 0; i < m_selectionList.getFileCount(); i++) {
            m_selectedFiles.add(m_selectionList.getFileAt(i));
          }
        }
      }

      dispose();
    }
  }

  /**
   * Switches the current directory when the user selects from the previous directories combo box.
   */
  private class InternalItemListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      final File file = new File(event.getItem().toString());

      if (file.exists() && file.isDirectory()) {
        m_chooser.setCurrentDirectory(file);
      }
    }
  }
}
