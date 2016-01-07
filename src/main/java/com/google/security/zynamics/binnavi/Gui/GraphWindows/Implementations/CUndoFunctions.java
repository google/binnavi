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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Undo.CSelectionHistory;

/**
 * Helper classes to undo and redo graph selection states.
 */
public final class CUndoFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CUndoFunctions() {
  }

  /**
   * Redos a graph selection state.
   *
   * @param history The graph selection history that provides graph information.
   */
  public static void redoLastSelection(final CSelectionHistory history) {
    if (history.canRedo()) {
      history.redo();
    }
  }

  /**
   * Undos a graph selection state.
   *
   * @param history The graph selection history that provides graph information.
   */
  public static void undoLastSelection(final CSelectionHistory history) {
    if (history.canUndo()) {
      history.undo();
    }
  }
}
