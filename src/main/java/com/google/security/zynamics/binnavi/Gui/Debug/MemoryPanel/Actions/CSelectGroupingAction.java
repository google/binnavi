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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action class that handles changes in data grouping.
 */
public final class CSelectGroupingAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8595296405237060303L;

  /**
   * New bytes per columns value.
   */
  private final int m_grouping;

  /**
   * The hex view where the data grouping setting is changed.
   */
  private final JHexView hexView;

  /**
   * Creates a new data grouping action.
   *
   * @param hexView Hex view where the data grouping setting is changed.
   * @param name Name of the action.
   * @param grouping Byte grouping value of the action.
   */
  public CSelectGroupingAction(final JHexView hexView, final String name, final int grouping) {
    super(name);

    this.hexView = Preconditions.checkNotNull(hexView, "IE01427: Hex view argument can't be null");
    m_grouping = grouping;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    hexView.setBytesPerColumn(m_grouping);
  }
}
