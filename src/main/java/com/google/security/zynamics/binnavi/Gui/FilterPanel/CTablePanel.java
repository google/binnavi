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

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Use this base class for all tree components that display a table surrounded by a titled border.
 * 
 * @param <T> Type of the elements shown in the table.
 */
public class CTablePanel<T> extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 159443513599950639L;

  /**
   * The table that is shown in the panel.
   */
  private final IFilteredTable<T> m_table;

  /**
   * Panel that contains the panel input field.
   */
  private final CFilterPanel<T> m_filterPanel;

  /**
   * The border of the component.
   */
  private final TitledBorder m_titledBorder;

  /**
   * Listens on changes in the filter and updates the table accordingly.
   */
  private final InternalFilterPanelListener m_filterListener = new InternalFilterPanelListener();

  /**
   * Listeners that are notified about changes in the filter field.
   */
  private final ListenerProvider<IFilterFieldListener> m_listeners =
      new ListenerProvider<IFilterFieldListener>();

  private final IFilterFactory<T> m_filterFactory;

  /**
   * Creates a new component object.
   * 
   * @param table The table that is shown in the panel.
   * @param filterFactory Creates the filter.
   * @param filterHelp Provides context-sensitive information for the filter field.
   */
  public CTablePanel(final IFilteredTable<T> table, final IFilterFactory<T> filterFactory,
      final IHelpInformation filterHelp) {
    super(new BorderLayout());

    Preconditions.checkNotNull(table, "IE01841: Table argument can not be null");

    m_table = table;
    m_filterFactory = filterFactory;

    if (filterFactory == null) {
      m_filterPanel = null;
    } else {
      m_filterPanel = new CFilterPanel<T>(filterFactory, filterHelp);

      add(m_filterPanel, BorderLayout.NORTH);

      m_filterPanel.addListener(m_filterListener);
    }

    m_titledBorder = new TitledBorder("");
    setBorder(m_titledBorder);

    add(new JScrollPane(m_table.self()), BorderLayout.CENTER);
  }

  /**
   * This function can be overwritten by child classes to clean up their resources.
   */
  protected void disposeInternal() {
    // Empty default implementation
  }

  /**
   * Returns the filter field.
   * 
   * @return The filter field.
   */
  protected JTextField getFilterField() {
    return m_filterPanel.getFilterField();
  }

  /**
   * Updates the border text of the component.
   * 
   * @param text The new border text.
   */
  protected final void updateBorderText(final String text) {
    Preconditions.checkNotNull(text, "IE01842: Text argument can not be null");

    m_titledBorder.setTitle(text);

    updateUI();
  }

  /**
   * Adds a listener object that is notified about changes in the filter field.
   * 
   * @param listener The listener object to add.
   */
  public void addListener(final IFilterFieldListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public final void dispose() {
    if (m_filterFactory != null) {
      m_filterFactory.dispose();
    }

    if (m_filterPanel != null) {
      m_filterPanel.dispose();
      m_filterPanel.removeListener(m_filterListener);
    }

    m_table.dispose();

    disposeInternal();
  }

  /**
   * Returns the table that is shown in the component.
   * 
   * @return The table that is shown in the component.
   */
  public final IFilteredTable<T> getTable() {
    return m_table;
  }

  /**
   * Removes a listening listener object.
   * 
   * @param listener The listener object to remove.
   */
  public void removeListener(final IFilterFieldListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public final void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);

    m_table.setEnabled(enabled);

    if (m_filterPanel != null) {
      m_filterPanel.setEnabled(enabled);
    }
  }

  /**
   * Listens on changes in the filter and updates the table accordingly.
   */
  private class InternalFilterPanelListener implements IFilterPanelListener<T> {
    @Override
    public void changedFilter(final CFilterPanel<T> filterPanel, final IFilter<T> filter) {
      getTable().getTreeTableModel().setFilter(filter);
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      for (final IFilterFieldListener listener : m_listeners) {
        try {
          listener.mousePressed(event);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      for (final IFilterFieldListener listener : m_listeners) {
        try {
          listener.mouseReleased(event);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
