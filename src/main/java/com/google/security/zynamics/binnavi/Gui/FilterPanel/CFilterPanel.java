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
package com.google.security.zynamics.binnavi.Gui.FilterPanel;



import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Help.CHelpTextField;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.antlr.runtime.RecognitionException;



/**
 * Panel that shows a text field that can be used to filter the rows of a table.
 * 
 * @param <T> Type of the elements shown in the table.
 */
public final class CFilterPanel<T> extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7546909182146443491L;

  /**
   * Input field where the user enters filter expressions.
   */
  private final JTextField m_inputField;

  /**
   * Listeners that are notified about changes in the filter.
   */
  private final ListenerProvider<IFilterPanelListener<T>> m_listeners =
      new ListenerProvider<IFilterPanelListener<T>>();

  /**
   * Creates the filter from the user input.
   */
  private final IFilterFactory<T> m_filterFactory;

  /**
   * Forwards clicks on the filter panel.
   */
  private final MouseListener m_internalMouseListener = new InternalMouseListener();

  /**
   * Listener that updates the filter panel on changes in the filter component.
   */
  private final IFilterComponentListener m_filterComponentListener =
      new InternalFilterComponentListener();

  /**
   * Creates a new panel object.
   * 
   * @param filterFactory Creates the filter from the user input.
   * @param filterHelp Provides context-sensitive information for the filter field.
   */
  public CFilterPanel(final IFilterFactory<T> filterFactory, final IHelpInformation filterHelp) {
    super(new BorderLayout());

    m_filterFactory = filterFactory;

    m_inputField = new CHelpTextField(filterHelp);

    m_inputField.addMouseListener(m_internalMouseListener);

    add(m_inputField);

    m_inputField.getDocument().addDocumentListener(new InternalDocumentListener());

    final IFilterComponent<T> filterComponent = filterFactory.getFilterComponent();

    if (filterComponent != null) {
      add(filterComponent.getComponent(), BorderLayout.WEST);

      filterComponent.addListener(m_filterComponentListener);
    }

    setBorder(new TitledBorder("Filter"));
  }

  /**
   * Updates the active filter according to the current user input.
   */
  private void updateFilter() {
    IFilter<T> filter;

    try {
      filter = m_filterFactory.createFilter(m_inputField.getText());

      m_inputField.setBackground(Color.WHITE);
    } catch (final RecognitionException e) {
      m_inputField.setBackground(Color.RED.brighter().brighter());

      return;
    }

    for (final IFilterPanelListener<T> listener : m_listeners) {
      try {
        listener.changedFilter(this, filter);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Adds a listener that is notified about changes in the filter.
   * 
   * @param listener The listener object.
   */
  public void addListener(final IFilterPanelListener<T> listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    final IFilterComponent<T> filterComponent = m_filterFactory.getFilterComponent();

    if (filterComponent != null) {
      filterComponent.removeListener(m_filterComponentListener);
    }
  }

  /**
   * Returns the filter field.
   * 
   * @return The filter field.
   */
  public JTextField getFilterField() {
    return m_inputField;
  }

  /**
   * Removes a listener object that was previously notified about changes in the filter.
   * 
   * @param listener The listener object to remove.
   */
  public void removeListener(final IFilterPanelListener<T> listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void setEnabled(final boolean enabled) {
    m_inputField.setEnabled(enabled);
  }

  /**
   * Updates the filter when the user enters something into the input field.
   */
  private class InternalDocumentListener implements DocumentListener {
    @Override
    public void changedUpdate(final DocumentEvent event) {
      updateFilter();
    }

    @Override
    public void insertUpdate(final DocumentEvent event) {
      updateFilter();
    }

    @Override
    public void removeUpdate(final DocumentEvent event) {
      updateFilter();
    }
  }

  /**
   * Listener that updates the filter panel on changes in the filter component.
   */
  private class InternalFilterComponentListener implements IFilterComponentListener {
    @Override
    public void updated() {
      updateFilter();
    }
  }

  /**
   * Forwards clicks on the filter panel.
   */
  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(final MouseEvent event) {
      for (final IFilterPanelListener<T> listener : m_listeners) {
        try {
          listener.mousePressed(event);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      for (final IFilterPanelListener<T> listener : m_listeners) {
        try {
          listener.mouseReleased(event);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
