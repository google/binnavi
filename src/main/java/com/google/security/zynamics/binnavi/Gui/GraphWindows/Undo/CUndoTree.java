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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Undo;

import java.net.URL;

import javax.swing.JTree;

import com.google.security.zynamics.binnavi.Help.CHelpFunctions;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.Help.IHelpProvider;


/**
 * Tree that shows the selection history.
 */
public final class CUndoTree extends JTree implements IHelpProvider {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6738360704404921690L;

  /**
   * Creates a new UndoTree object.
   *
   * @param rootNode Root node of the tree.
   */
  public CUndoTree(final CSelectionHistoryTreeNode rootNode) {
    super(rootNode);
  }

  @Override
  public IHelpInformation getHelpInformation() {
    return new IHelpInformation() {
      @Override
      public String getText() {
        return "This tree is used to keep track of recent selection states. By clicking on the nodes of the tree you can return to earlier selection states.";
      }

      @Override
      public URL getUrl() {
        return CHelpFunctions.urlify(CHelpFunctions.MAIN_WINDOW_FILE);
      }
    };
  }
}
