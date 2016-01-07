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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag.Component;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag.Component.Help.CChildTagsTableHelp;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import javax.swing.JPopupMenu;
import javax.swing.JTree;



/**
 * Table in which information about child tags of a tag is shown.
 */
public final class CChildTagsTable extends CAbstractTreeTable<CTag> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8866373714772413510L;

  /**
   * Creates a new table object.
   * 
   * @param projectTree Project tree of the main window.
   * @param tag Tag whose information is shown in the table.
   * @param database Database where the tag is stored.
   */
  public CChildTagsTable(final JTree projectTree, final ITreeNode<CTag> tag,
      final IDatabase database) {
    super(projectTree, new CChildTagsModel(database, tag), new CChildTagsTableHelp());
  }

  @Override
  protected JPopupMenu getPopupMenu(final int x, final int y, final int row) {
    return null;
  }

  @Override
  protected void handleDoubleClick(final int row) {
    // Nothing happens when tags are double-clicked
  }
}
