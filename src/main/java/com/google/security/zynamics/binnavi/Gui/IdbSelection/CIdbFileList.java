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
package com.google.security.zynamics.binnavi.Gui.IdbSelection;

import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.io.File;

import javax.swing.AbstractListModel;
import javax.swing.JList;


/**
 * List component for displaying selected IDB files.
 */
public final class CIdbFileList extends JList<File> {
  /**
   * Model of the IDB selection list.
   */
  private final CIdbListModel m_listModel = new CIdbListModel();

  /**
   * Creates a new list object.
   */
  public CIdbFileList() {
    setModel(m_listModel);
  }

  /**
   * Adds a new file to the list.
   *
   * @param file The file to add.
   */
  public void addFile(final File file) {
    m_listModel.add(file);
  }

  /**
   * Returns a file at the given index.
   *
   * @param index The index of the file.
   *
   * @return The file with the given index.
   */
  public File getFileAt(final int index) {
    return m_listModel.getElementAt(index);
  }

  /**
   * Returns the number of files in the list.
   *
   * @return The number of files in the list.
   */
  public int getFileCount() {
    return m_listModel.getSize();
  }

  /**
   * Removes a file from the list.
   *
   * @param file The file to remove.
   */
  public void removeFile(final File file) {
    m_listModel.remove(file);
  }

  /**
   * List model of the IDB file list.
   */
  private static class CIdbListModel extends AbstractListModel<File> {
    /**
     * Files shown in the list.
     */
    private final IFilledList<File> m_files = new FilledList<File>();

    /**
     * Adds a new file to the list.
     *
     * @param file The file to add.
     */
    public void add(final File file) {
      if (m_files.contains(file)) {
        return;
      }

      m_files.add(file);

      fireIntervalAdded(file, m_files.size() - 1, m_files.size() - 1);
    }

    @Override
    public File getElementAt(final int index) {
      return m_files.get(index);
    }

    @Override
    public int getSize() {
      return m_files.size();
    }

    /**
     * Removes a file from the list.
     *
     * @param file The file to remove.
     */
    public void remove(final File file) {
      if (m_files.remove(file)) {
        fireIntervalRemoved(file, 0, m_files.size());
      }
    }
  }

}
