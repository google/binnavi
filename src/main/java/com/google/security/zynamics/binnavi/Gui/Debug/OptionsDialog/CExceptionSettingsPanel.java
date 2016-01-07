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
package com.google.security.zynamics.binnavi.Gui.Debug.OptionsDialog;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerExceptionHandlingAction;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Panel which contains all the exceptions settings of the debugger
 */
public class CExceptionSettingsPanel extends JPanel {
  private final Map<JComboBox<String>, DebuggerException> m_comboboxMap =
      new HashMap<JComboBox<String>, DebuggerException>();

  public CExceptionSettingsPanel(final DebuggerOptions options) {
    super(new BorderLayout());

    Preconditions.checkNotNull(options, "IE00267: Debugger options can not be null");

    final JPanel innerPanel = new JPanel(new BorderLayout());

    final JPanel componentPanel = new JPanel(new GridLayout(options.getExceptions().size(), 2));

    for (final DebuggerException exception : options.getExceptions()) {
      final JComboBox<String> comboBox = buildRow(componentPanel, exception);

      comboBox.addItemListener(new ComboboxItemListener());

      m_comboboxMap.put(comboBox, exception);
    }

    innerPanel.add(new JScrollPane(componentPanel), BorderLayout.NORTH);

    add(innerPanel);
  }

  /**
   * Construct a tuple of a label and a corresponding combobox allowing the user to control how the
   * debugger handles exceptions
   */
  private static JComboBox<String> buildRow(final JPanel componentPanel,
      final DebuggerException exception) {
    componentPanel.add(new JLabel(exception.getExceptionName()));

    final JComboBox<String> combobox = new JComboBox<String>();

    combobox.addItem(getString(DebuggerExceptionHandlingAction.Continue));
    combobox.addItem(getString(DebuggerExceptionHandlingAction.Halt));
    combobox.addItem(getString(DebuggerExceptionHandlingAction.Ignore));

    combobox.setSelectedIndex(exception.getExceptionAction().getValue());

    componentPanel.add(combobox);

    return combobox;
  }

  private static String getString(final DebuggerExceptionHandlingAction action) {
    switch (action) {
      case Continue:
        return "Continue";
      case Halt:
        return "Halt";
      case Ignore:
        return "Ignore";
      default:
        throw new IllegalStateException("IE02295: Unknown debug exception handling action");
    }
  }

  /**
   * Get list of exceptions which need special handling by the debugger
   */
  public Collection<DebuggerException> getExceptionSettings() {
    return m_comboboxMap.values();
  }

  private class ComboboxItemListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      @SuppressWarnings("unchecked")
      final JComboBox<String> combobox = (JComboBox<String>) event.getSource();

      final DebuggerException oldException = m_comboboxMap.get(combobox);
      final DebuggerExceptionHandlingAction exceptionAction =
          DebuggerExceptionHandlingAction.convertToHandlingAction(combobox.getSelectedIndex());

      m_comboboxMap.put(combobox, new DebuggerException(oldException.getExceptionName(),
          oldException.getExceptionCode(), exceptionAction));
    }
  }
}
