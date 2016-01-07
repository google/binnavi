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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton.Actions.CAskRefreshAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton.Actions.CRefreshAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;

/**
 * Encapsulates a memory refresh button with a synchronizer that keeps the refresh button
 * synchronized with the state of a debug GUI perspective.
 */
public final class CMemoryRefreshButtonPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2126975710360682580L;

  /**
   * The refresh button is used to refresh the visible memory.
   */
  private final CMemoryRefreshButton m_refreshButton = new CMemoryRefreshButton();

  /**
   * Synchronizes the refresh button with a debug GUI perspective.
   */
  private final CMemoryRefreshButtonSynchronizer m_buttonSynchronizer;

  /**
   * Creates a new memory refresh panel.
   *
   * @param parent Parent window of the panel.
   * @param debugPerspectiveModel Describes a debug GUI perspective.
   * @param rangeProvider Provides the ranges refreshed by the refresh button.
   * @param stackRangeProvider Provides information about the stack memory to refresh.
   */
  public CMemoryRefreshButtonPanel(final JFrame parent,
      final CDebugPerspectiveModel debugPerspectiveModel, final IRefreshRangeProvider rangeProvider,
      final IRefreshRangeProvider stackRangeProvider) {
    super(new BorderLayout());

    Preconditions.checkNotNull(parent, "IE01439: Parent can not be null");

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01440: Debug perspective model can not be null");

    Preconditions.checkNotNull(rangeProvider, "IE01441: Range provider argument can not be null");

    add(m_refreshButton);

    final Action defaultAction =
        new CRefreshAction(parent, debugPerspectiveModel, rangeProvider, stackRangeProvider);
    final Action askAction = new CAskRefreshAction(parent, debugPerspectiveModel);

    m_buttonSynchronizer = new CMemoryRefreshButtonSynchronizer(
        m_refreshButton, debugPerspectiveModel, defaultAction, askAction);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_buttonSynchronizer.dispose();
  }
}
