/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.TagContainer.Component;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag.Component.CChildTagsTable;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * This component is shown on the right side of the main window when a tag container node is
 * selected.
 */
public final class CTagContainerNodeComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -339329908645324706L;

  /**
   * Database that contains the tags to be shown.
   */
  private final IDatabase m_database;

  /**
   * Root tag to be shown in the component.
   */
  private final ITreeNode<CTag> m_tagTreeNode;

  /**
   * Table where the child tags of the root tag are shown.
   */
  private final CChildTagsTable m_childrenTagTable;

  /**
   * Border where information about the number of child tags is shown.
   */
  private final TitledBorder m_tableBorder;

  /**
   * Updates the GUI on relevant changes in the tag manager.
   */
  private final InternalTagManagerListener m_tagManagerListener = new InternalTagManagerListener();

  /**
   * Creates a new component object.
   * 
   * @param projectTree Project tree that is updated on certain events.
   * @param database Database that contains the tags to be shown.
   */
  public CTagContainerNodeComponent(final JTree projectTree, final IDatabase database) {
    super(new BorderLayout());

    Preconditions.checkNotNull(projectTree, "IE02003: Project tree argument can not be null");

    Preconditions.checkNotNull(database, "IE02004: Database argument can not be null");

    m_database = database;
    m_tagTreeNode = database.getContent().getViewTagManager().getRootTag();

    m_childrenTagTable = new CChildTagsTable(projectTree, m_tagTreeNode, database);

    m_tableBorder = new TitledBorder(getBorderTitle());

    m_database.getContent().getViewTagManager().addListener(m_tagManagerListener);

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(m_tableBorder);
    final JScrollPane scrollPane = new JScrollPane(m_childrenTagTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    add(panel, BorderLayout.CENTER);
  }

  /**
   * Creates the border text that shows the number of child tags.
   * 
   * @return The created border text.
   */
  private String getBorderTitle() {
    return String.format("%d %s", m_tagTreeNode.getChildren().size(), "Child Tags");
  }

  @Override
  public void dispose() {
    m_database.getContent().getViewTagManager().removeListener(m_tagManagerListener);

    m_childrenTagTable.dispose();
  }

  /**
   * Updates the GUI on relevant changes in the tag manager.
   */
  private class InternalTagManagerListener implements ITagManagerListener {
    @Override
    public void addedTag(final CTagManager manager, final ITreeNode<CTag> tag) {
      if (m_tagTreeNode.getObject().getId() == tag.getParent().getObject().getId()) {
        m_tableBorder.setTitle(getBorderTitle());
        updateUI();
      }
    }

    @Override
    public void deletedTag(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      // container node can't be deleted
    }

    @Override
    public void deletedTagSubtree(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      // container node subtree can't be deleted
    }

    @Override
    public void insertedTag(final CTagManager tagManager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      // container node can't insert nodes
    }
  }
}
