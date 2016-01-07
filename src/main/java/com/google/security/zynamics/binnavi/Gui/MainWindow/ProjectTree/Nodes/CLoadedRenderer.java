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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes;

import java.awt.Font;

import javax.swing.JTable;

/**
 * Table renderer that highlights loaded elements differently than loaded elements.
 */
public abstract class CLoadedRenderer extends CProjectTreeTableRenderer {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4771062426457978117L;

  /**
   * Default font used to print text.
   */
  private final Font NORMAL_FONT = new Font(this.getFont().getFontName(), Font.PLAIN, 12);

  /**
   * Font used to print loaded elements.
   */
  private final Font NORMAL_BOLD_FONT = new Font(this.getFont().getFontName(), Font.BOLD, 12);

  /**
   * Determines whether the element in a given row is loaded or not.
   * 
   * @param row The row index of the element.
   * 
   * @return True, if the element shown in the row is loaded. False, otherwise.
   */
  public abstract boolean isLoaded(int row);

  @Override
  public void postProcess(final JTable table, final Object value, final boolean isSelected,
      final boolean hasFocus, final int row, final int column) {
    if (isLoaded(row)) {
      setFont(NORMAL_BOLD_FONT);
    } else {
      setFont(NORMAL_FONT);
    }
  }
}
