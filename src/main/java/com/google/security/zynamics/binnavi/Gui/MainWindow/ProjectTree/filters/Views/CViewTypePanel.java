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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Views;



import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Functions.IFilterDialogListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;



public class CViewTypePanel extends JPanel {
  /**
	 * 
	 */
  private static final long serialVersionUID = -7710323137862784506L;

  /**
   * Checkbox for filtering flow graph views.
   */
  private final JCheckBox m_flowgraphViewsCheckbox = new JCheckBox("", true);

  /**
   * Checkbox for filtering call graph views.
   */
  private final JCheckBox m_callgraphViewsCheckbox = new JCheckBox("", true);

  /**
   * Checkbox for filtering mixed views.
   */
  private final JCheckBox m_mixedViewsCheckbox = new JCheckBox("", true);

  /**
   * Listener that updates the filter on changes to the checkboxes.
   */
  private final InternalCheckboxListener m_checkBoxListener = new InternalCheckboxListener();

  /**
   * Listeners that are notified about changes in the dialog options.
   */
  private final ListenerProvider<IFilterDialogListener> m_listeners;

  public CViewTypePanel(final ListenerProvider<IFilterDialogListener> listeners) {
    super(new BorderLayout());

    m_listeners = listeners;

    final JPanel innerPanel = new JPanel(new GridLayout(3, 1));

    innerPanel.add(buildRow("Flow graph views", m_flowgraphViewsCheckbox));
    innerPanel.add(buildRow("Call graph views", m_callgraphViewsCheckbox));
    innerPanel.add(buildRow("Mixed views", m_mixedViewsCheckbox));

    add(innerPanel, BorderLayout.NORTH);

    setBorder(new TitledBorder("View type"));
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
   * Returns whether call graph views should be shown.
   * 
   * @return True, if call graph views are shown. False, if not.
   */
  public boolean isShowCallgraphViews() {
    return m_callgraphViewsCheckbox.isSelected();
  }

  /**
   * Returns whether flow graph views should be shown.
   * 
   * @return True, if flow graph views are shown. False, if not.
   */
  public boolean isShowFlowgraphViews() {
    return m_flowgraphViewsCheckbox.isSelected();
  }

  /**
   * Returns whether mixed views should be shown.
   * 
   * @return True, if mixed views are shown. False, if not.
   */
  public boolean isShowMixedViews() {
    return m_mixedViewsCheckbox.isSelected();
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
