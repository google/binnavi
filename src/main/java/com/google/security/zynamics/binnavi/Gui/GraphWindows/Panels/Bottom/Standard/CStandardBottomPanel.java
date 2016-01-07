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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Bottom.Standard;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CBottomPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.IResultsPanel;
import com.google.security.zynamics.zylib.types.lists.FilledList;



/**
 * Panel object shown at the bottom of graph views if the standard perspective is active.
 */
public final class CStandardBottomPanel extends CBottomPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8268542421191082895L;

  /**
   * Creates a new panel object.
   */
  public CStandardBottomPanel() {
    super(new FilledList<IResultsPanel>());
  }
}
