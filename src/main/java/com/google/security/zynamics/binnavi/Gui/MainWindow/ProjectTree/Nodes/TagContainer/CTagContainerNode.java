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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.TagContainer;



import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag.CTagNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.TagContainer.Component.CTagContainerNodeComponent;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.binnavi.Tagging.TagComparator;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

/**
 * Node that represents tag containers in the project tree.
 */
public final class CTagContainerNode extends CProjectTreeNode<Object> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4190505746650765116L;

  /**
   * Icon used by this node in the project tree.
   */
  private static final ImageIcon ICON_DEBUGGER_CONTAINER = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/tag_container.png"));

  /**
   * Database the tags are stored in.
   */
  private final IDatabase m_database;

  /**
   * Root view tag stored in the database.
   */
  private final ITreeNode<CTag> m_rootTag;

  /**
   * Updates the node on relevant changes in the tag manager.
   */
  private final InternalTagManagerListener m_tagManagerListener = new InternalTagManagerListener();

  /**
   * Creates a new container node.
   * 
   * @param projectTree Project tree of the main window.
   * @param database Database the tags are stored in.
   */
  public CTagContainerNode(final JTree projectTree, final IDatabase database) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CTagContainerNodeComponent(projectTree, database);
      }
    }, new CTagContainerNodeMenuBuilder(projectTree, database), 0); // TagId of RootNode

    Preconditions.checkNotNull(database, "IE02002: Database can't be null");

    m_database = database;
    m_rootTag = m_database.getContent().getViewTagManager().getRootTag();

    m_database.getContent().getViewTagManager().addListener(m_tagManagerListener);

    createChildren();
  }

  @Override
  protected void createChildren() {
    if (m_database.isLoaded()) {
      final List<? extends ITreeNode<CTag>> children = m_rootTag.getChildren();

      Collections.sort(children, new TagComparator());

      for (final ITreeNode<CTag> tag : children) {
        add(new CTagNode(getProjectTree(), m_database, (TreeNode<CTag>) tag));
      }
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    m_database.getContent().getViewTagManager().removeListener(m_tagManagerListener);

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    // Do nothing
  }

  @Override
  public Icon getIcon() {
    return ICON_DEBUGGER_CONTAINER;
  }

  @Override
  public String toString() {
    return "View Tags" + " (" + m_rootTag.getChildren().size() + ")";
  }

  /**
   * Updates the node on relevant changes in the tag manager.
   */
  private class InternalTagManagerListener implements ITagManagerListener {
    /**
     * Finds out whether this node has a child node that represents a given tag.
     * 
     * @param tag The tag to search for.
     * 
     * @return True, if such a child node exists. False, otherwise.
     */
    private boolean hasChild(final ITreeNode<CTag> tag) {
      for (int i = 0; i < getChildCount(); i++) {
        final CTagNode node = (CTagNode) getChildAt(i);

        if (node.getObject().getObject() == tag.getObject()) {
          return true;
        }
      }

      return false;
    }

    @Override
    public void addedTag(final CTagManager manager, final ITreeNode<CTag> tag) {
      if (tag.getParent() == m_rootTag) {
        add(new CTagNode(getProjectTree(), m_database, (TreeNode<CTag>) tag));

        getTreeModel().nodeStructureChanged(CTagContainerNode.this);
      }
    }

    @Override
    public void deletedTag(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      if (hasChild(tag)) {
        final Set<CTagNode> toDelete = new HashSet<CTagNode>();

        // Remove all children
        for (int i = 0; i < getChildCount(); i++) {
          final CTagNode node = (CTagNode) getChildAt(i);
          toDelete.add(node);
        }

        for (final CTagNode node : toDelete) {
          node.dispose();
          remove(node);
        }

        // Recreate all children
        createChildren();

        getTreeModel().nodeStructureChanged(CTagContainerNode.this);
      }
    }

    @Override
    public void deletedTagSubtree(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      if (hasChild(tag)) {
        // Remove the node that represents the deleted project; child nodes are removed
        // automatically.

        for (int i = 0; i < getChildCount(); i++) {
          final CTagNode node = (CTagNode) getChildAt(i);

          if (node.getObject() == tag) {
            node.dispose();
            remove(node);
            break;
          }
        }

        getTreeModel().nodeStructureChanged(CTagContainerNode.this);
      }
    }

    @Override
    public void insertedTag(final CTagManager tagManager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      // container node can't insert nodes
    }
  }

}
