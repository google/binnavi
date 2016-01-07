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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;



/**
 * Abstract panel class for extending the bottom panel of graph windows.
 */
public abstract class CAbstractResultsPanel extends JPanel implements IResultsPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5563833680963943992L;

  /**
   * Listeners that are notified about changes in the results panel.
   */
  private final ListenerProvider<IResultsPanelListener> m_listeners =
      new ListenerProvider<IResultsPanelListener>();

  /**
   * Creates a new panel object.
   *
   * @param borderLayout Layout of the panel.
   */
  public CAbstractResultsPanel(final BorderLayout borderLayout) {
    super(borderLayout);
  }

  /**
   * Asks the results panel to show a specific panel.
   *
   * @param panel The component of the panel to show.
   */
  protected void notifyShow(final JComponent panel) {
    for (final IResultsPanelListener listener : m_listeners) {
      try {
        listener.show(panel);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void addListener(final IResultsPanelListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public final JComponent getComponent() {
    return this;
  }

  @Override
  public void removeListener(final IResultsPanelListener listener) {
    m_listeners.removeListener(listener);
  }
}
