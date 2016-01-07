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
package com.google.security.zynamics.zylib.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.resources.Constants;
import com.google.security.zynamics.zylib.system.SystemHelpers;

public class CFileChooser extends JFileChooser {
  private static final long serialVersionUID = -5907483378595594768L;

  private final List<CFileNameExtensionFilter> m_fileFilters =
      new ArrayList<CFileNameExtensionFilter>();

  private final JCheckBox m_checkBox = new JCheckBox();

  /**
   * Whether to ask for an existing file to be overwritten. Only makes sense in dialogs of type
   * <code>JFileChooser.SAVE_DIALOG</code> or <code>JFileChooser.CUSTOM_DIALOG</code>.
   * 
   * @see JFileChooser#setDialogType(int)
   */
  private boolean m_askFileOverwrite = false;

  public CFileChooser() {
    this("");
  }

  public CFileChooser(final FileExtension... extensions) {
    super();
    Preconditions.checkNotNull(extensions, "File extensions can't be null.");

    for (final FileExtension ext : extensions) {
      final CFileNameExtensionFilter fileFilter =
          new CFileNameExtensionFilter(Arrays.asList(ext.m_extensions), ext.m_description);

      m_fileFilters.add(fileFilter);
      addChoosableFileFilter(fileFilter);
    }
  }

  public CFileChooser(final List<String> fileExtensions, final String fileDescription) {
    super();

    Preconditions.checkNotNull(fileExtensions, "Error: File extensions can't be null.");
    Preconditions.checkNotNull(fileDescription, "Error: File descriptions can't be null.");

    addChoosableFileFilter(new CFileNameExtensionFilter(fileExtensions, fileDescription));
  }

  public CFileChooser(final String extension) {
    this(extension, "");
  }

  public CFileChooser(final String fileExtension, final String fileDescription) {
    super();

    Preconditions.checkNotNull(fileExtension, "Error: File extension can't be null.");
    Preconditions.checkNotNull(fileDescription, "Error: File description can't be null.");

    // Use the default "All Files" file filter if we were given no extension
    if (fileExtension.isEmpty()) {
      return;
    }

    addChoosableFileFilter(new FileFilter() {
      @Override
      public boolean accept(final File f) {
        if ("".equals(fileExtension)) {
          return f.isDirectory() || f.canExecute();
        }

        return f.isDirectory() || f.getName().toLowerCase().endsWith(fileExtension.toLowerCase())
            || fileExtension.equals("*");

      }

      @Override
      public String getDescription() {
        return fileDescription + " (*." + fileExtension + ")";
      }
    });
  }

  private static int showNativeFileDialog(final JFileChooser chooser) {
    final FileDialog result = new FileDialog((Frame) chooser.getParent());

    result.setDirectory(chooser.getCurrentDirectory().getPath());

    final File selected = chooser.getSelectedFile();
    result.setFile(selected == null ? "" : selected.getPath());
    result.setFilenameFilter(new FilenameFilter() {
      @Override
      public boolean accept(final File dir, final String name) {
        return chooser.getFileFilter().accept(new File(dir.getPath() + File.pathSeparator + name));
      }
    });

    if (chooser.getDialogType() == SAVE_DIALOG) {
      result.setMode(FileDialog.SAVE);
    } else {
      // The native dialog only support Open and Save
      result.setMode(FileDialog.LOAD);
    }

    if (chooser.getFileSelectionMode() == DIRECTORIES_ONLY) {
      System.setProperty("apple.awt.fileDialogForDirectories", "true");
    }

    // Display dialog
    result.setVisible(true);

    System.setProperty("apple.awt.fileDialogForDirectories", "false");
    if (result.getFile() == null) {
      return CANCEL_OPTION;
    }

    final String selectedDir = result.getDirectory();
    chooser
        .setSelectedFile(new File(FileUtils.ensureTrailingSlash(selectedDir) + result.getFile()));
    return APPROVE_OPTION;
  }

  @Override
  public void approveSelection() {
    if (m_askFileOverwrite && getSelectedFile().exists()) {
      if (CMessageBox.showYesNoQuestion(this, Constants.ASK_FILE_OVERWRITE) == JOptionPane.NO_OPTION) {
        return;
      }
    }
    super.approveSelection();
  }

  public boolean getAskFileOverwrite() {
    return m_askFileOverwrite;
  }

  public boolean isSelectedCheckBox() {
    return m_checkBox.isSelected();
  }

  public void setAskFileOverwrite(final boolean value) {
    m_askFileOverwrite = value;
  }

  public void setCheckBox(final String checkBoxText) {
    m_checkBox.setText(checkBoxText);
    m_checkBox.setBorder(new EmptyBorder(0, 0, 0, 0));

    final String approve = getApproveButtonText();
    final JButton approveButton =
        (JButton) GuiHelper.findComponentByPredicate(this, new GuiHelper.ComponentFilter() {
          @Override
          public boolean accept(final JComponent c) {
            if (!(c instanceof JButton)) {
              return false;
            }

            final String text = ((JButton) c).getText();
            if (text == null) {
              return approve == null;
            }
            if (approve == null) {
              return text == null;
            }
            return ((JButton) c).getText().equals(approve);
          }
        });

    JComponent parent = null;
    if (approveButton != null) {
      final Container approveParent = approveButton.getParent();
      if (approveParent instanceof JComponent) {
        parent = (JComponent) approveParent;
      }
    }

    if (parent == null) {
      // Fallback to using setAccessory() (ugly, but at least it works)
      setAccessory(m_checkBox);
      return;
    }

    // OS X is using SpringLayout, so we can just add the component and
    // are done.
    if (SystemHelpers.isRunningMacOSX()) {
      parent.add(m_checkBox, 0);
      return;
    }

    // Move the original buttons to a separate button panel while keeping
    // the original layout manager
    final JPanel buttonPanel = new JPanel(parent.getLayout());
    for (final Component c : parent.getComponents()) {
      buttonPanel.add(c);
    }

    // Set a new layout and add our checkbox
    parent.setLayout(new BorderLayout(0, 0));
    parent.add(m_checkBox, BorderLayout.LINE_START);
    parent.add(buttonPanel, BorderLayout.CENTER);
  }

  public void setFileFilter(final int index) {
    setFileFilter(m_fileFilters.get(index));
  }

  @Override
  public int showOpenDialog(final Component parent) throws HeadlessException {
    if (!SystemHelpers.isRunningMacOSX()) {
      return super.showOpenDialog(parent);
    }

    setDialogType(OPEN_DIALOG);
    return showNativeFileDialog(this);
  }

  private class CFileNameExtensionFilter extends FileFilter {
    private final List<String> m_fileExtensions;
    private final String m_fileDescription;

    public CFileNameExtensionFilter(final List<String> fileExtensions, final String fileDescription) {
      m_fileExtensions = fileExtensions;
      m_fileDescription = fileDescription;
    }

    @Override
    public boolean accept(final File f) {
      boolean accept = false;
      final String filenameLower = f.getName().toLowerCase();

      for (final String ext : m_fileExtensions) {
        accept = filenameLower.endsWith(ext.toLowerCase());
        if (accept) {
          break;
        }
      }

      return accept || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return m_fileDescription;
    }
  }

  public static class FileExtension {
    private final String m_description;
    private final String[] m_extensions;

    public FileExtension(final String desc, final String... extensions) {
      m_description = desc;
      m_extensions = extensions;
    }
  }
}
