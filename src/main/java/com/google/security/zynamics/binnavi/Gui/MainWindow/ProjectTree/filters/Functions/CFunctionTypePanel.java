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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Functions;



import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;



/**
 * Panel class for selecting functions according to their function type.
 */
public final class CFunctionTypePanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1503547368195398089L;

  /**
   * Checkbox for filtering normal functions.
   */
  private final JCheckBox m_normalFunctionCheckbox = new JCheckBox("", true);

  /**
   * Checkbox for filtering imported functions.
   */
  private final JCheckBox m_importedFunctionCheckbox = new JCheckBox("", true);

  /**
   * Checkbox for filtering library functions.
   */
  private final JCheckBox m_libraryFunctionCheckbox = new JCheckBox("", true);

  /**
   * Checkbox for filtering thunk functions.
   */
  private final JCheckBox m_thunkFunctionCheckbox = new JCheckBox("", true);

  /**
   * Checkbox for filtering thunk adjustor functions.
   */
  private final JCheckBox m_adjustorFunctionCheckbox = new JCheckBox("", true);

  /**
   * Listener that updates the filter on changes to the checkboxes.
   */
  private final InternalCheckboxListener m_checkBoxListener = new InternalCheckboxListener();

  /**
   * Listeners that are notified about changes in the dialog options.
   */
  private final ListenerProvider<IFilterDialogListener> m_listeners;

  /**
   * Creates a new function type panel object.
   * 
   * @param listeners Listeners that are notified about changes in the dialog options.
   */
  public CFunctionTypePanel(final ListenerProvider<IFilterDialogListener> listeners) {
    super(new BorderLayout());

    m_listeners = listeners;

    final JPanel innerPanel = new JPanel(new GridLayout(5, 1));

    innerPanel.add(buildRow("Normal functions", m_normalFunctionCheckbox));
    innerPanel.add(buildRow("Imported functions", m_importedFunctionCheckbox));
    innerPanel.add(buildRow("Library functions", m_libraryFunctionCheckbox));
    innerPanel.add(buildRow("Thunk functions", m_thunkFunctionCheckbox));
    innerPanel.add(buildRow("Thunk adjustor functions", m_adjustorFunctionCheckbox));

    add(innerPanel, BorderLayout.NORTH);

    setBorder(new TitledBorder("Function type"));
  }

  /**
   * Builds a checkbox row.
   * 
   * @param string String to show next to the checkbox.
   * @param checkBox Check box to add to the panel.
   * 
   * @return The created row.
   */
  private JPanel buildRow(final String string, final JCheckBox checkBox) {
    final JPanel panel = new JPanel(new BorderLayout());

    panel.add(new JLabel(string), BorderLayout.WEST);
    panel.add(checkBox, BorderLayout.EAST);
    checkBox.addItemListener(m_checkBoxListener);

    return panel;
  }

  /**
   * Returns whether adjustor functions should be shown.
   * 
   * @return True, if adjustor functions are shown. False, if not.
   */
  public boolean isShowAdjustorFunctions() {
    return m_adjustorFunctionCheckbox.isSelected();
  }

  /**
   * Returns whether imported functions should be shown.
   * 
   * @return True, if imported functions are shown. False, if not.
   */
  public boolean isShowImportedFunctions() {
    return m_importedFunctionCheckbox.isSelected();
  }

  /**
   * Returns whether library functions should be shown.
   * 
   * @return True, if library functions are shown. False, if not.
   */
  public boolean isShowLibraryFunctions() {
    return m_libraryFunctionCheckbox.isSelected();
  }

  /**
   * Returns whether normal functions should be shown.
   * 
   * @return True, if normal functions are shown. False, if not.
   */
  public boolean isShowNormalFunctions() {
    return m_normalFunctionCheckbox.isSelected();
  }

  /**
   * Returns whether thunk functions should be shown.
   * 
   * @return True, if thunk functions are shown. False, if not.
   */
  public boolean isShowThunkFunctions() {
    return m_thunkFunctionCheckbox.isSelected();
  }

  /**
   * Listener that updates the filter on changes to the checkboxes.
   */
  private class InternalCheckboxListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent arg0) {
      for (final IFilterDialogListener listener : m_listeners) {
        try {
          listener.updated();
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
