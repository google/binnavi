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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.viewReferences;

import com.google.security.zynamics.binnavi.Help.CHelpFunctions;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.Help.IHelpProvider;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.jtree.IconNodeRenderer;

import java.net.URL;

import javax.swing.JTree;

/**
 * Tree that displays local and global variables referenced by a view.
 */
public final class ViewReferencesTable extends JTree implements IHelpProvider {

  /**
   * Creates a new variables tree object.
   *
   * @param model Model of the tree.
   */
  public ViewReferencesTable(final ViewReferencesTableModel model) {
    super(model);
    setRootVisible(false);
    final IconNodeRenderer renderer = new IconNodeRenderer();
    renderer.setFont(GuiHelper.MONOSPACED_FONT);
    setCellRenderer(renderer);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    ((ViewReferencesTableModel) getModel()).dispose();
  }

  @Override
  public IHelpInformation getHelpInformation() {
    return new IHelpInformation() {
      @Override
      public String getText() {
        return "This field shows both the local and global variables referenced by the view.\n\n"
            + "To rename variables, right-click on the variables.\n"
            + "To highlight variable access in the code, select the variables.\n"
            + "To see the references and quickly navigate between them, "
            + "double-click on the variable names.";
      }

      @Override
      public URL getUrl() {
        return CHelpFunctions.urlify(CHelpFunctions.MAIN_WINDOW_FILE);
      }
    };
  }
}
