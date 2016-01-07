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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CUndoFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Undo.CSelectionHistory;


/**
 * Action class for undoing a change in the selection state.
 */
public final class CActionUndoSelection extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -407405242270658638L;

  /**
   * Provides selection state histories.
   */
  private final CSelectionHistory m_history;

  /**
   * Creates a new action object.
   * 
   * @param history Provides selection state histories.
   */
  public CActionUndoSelection(final CSelectionHistory history) {
    super("Undo Last Selection");
    m_history = Preconditions.checkNotNull(history, "IE02832: history argument can not be null");
    putValue(ACCELERATOR_KEY, HotKeys.GRAPH_UNDO_SELECTION_HK.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CUndoFunctions.undoLastSelection(m_history);
  }
}
