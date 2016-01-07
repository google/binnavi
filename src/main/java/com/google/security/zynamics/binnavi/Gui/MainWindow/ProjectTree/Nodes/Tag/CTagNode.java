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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag;

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
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag.Component.CTagNodeComponent;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagListener;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.binnavi.Tagging.TagComparator;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

/**
 * Represents a single tag in the project tree.
 */
public final class CTagNode extends CProjectTreeNode<TreeNode<CTag>> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8765870129366710327L;

  /**
   * Icon of the node in the project tree.
   */
  private static final ImageIcon ICON_TAG = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/tag.png"));

  /**
   * Database where the represented tag is stored.
   */
  private final IDatabase m_database;

  /**
   * Tag represented by this node.
   */
  private final TreeNode<CTag> m_tag;

  /**
   * Updates the node on relevant changes in the tag manager.
   */
  private final InternalTagManagerListener m_tagManagerListener = new InternalTagManagerListener();

  /**
   * Updates the node on relevant changes in tags.
   */
  private final InternalTagListener m_tagListener = new InternalTagListener();

  /**
   * Creates a new tag node object.
   * 
   * @param projectTree Project tree of the main window.
   * @param database Database the represented tag belongs to.
   * @param tag Tag represented by this node.
   */
  public CTagNode(final JTree projectTree, final IDatabase database, final TreeNode<CTag> tag) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CTagNodeComponent(projectTree, database, tag);
      }
    }, new CTagNodeMenuBuilder(projectTree, database, tag), tag);

    m_database = Preconditions.checkNotNull(database, "IE01998: Database argument can NOT be null");
    m_tag = Preconditions.checkNotNull(tag, "IE02347: Tag argument can not be null");

    m_database.getContent().getViewTagManager().addListener(m_tagManagerListener);

    m_tag.getObject().addListener(m_tagListener);

    createChildren();
  }

  @Override
  protected void createChildren() {
    if (m_database.isLoaded()) {
      final List<ITreeNode<CTag>> children = m_tag.getChildren();

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
    m_tag.getObject().removeListener(m_tagListener);

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    // Do nothing
  }

  @Override
  public Icon getIcon() {
    return ICON_TAG;
  }

  /**
   * Returns the tag manager that provides the represented tag.
   * 
   * @return The tag manager.
   */
  public ITagManager getTagManager() {
    return m_database.getContent().getViewTagManager();
  }

  @Override
  public String toString() {
    return m_tag.getObject().getName() + " (" + m_tag.getChildren().size() + ")";
  }

  /**
   * Updates the node on relevant changes in tags.
   */
  private class InternalTagListener implements ITagListener {
    @Override
    public void changedDescription(final CTag tag, final String description) {
      getTreeModel().nodeChanged(CTagNode.this);
    }

    @Override
    public void changedName(final CTag tag, final String name) {
      getTreeModel().nodeChanged(CTagNode.this);
    }

    @Override
    public void deletedTag(final CTag tag) {
      // do nothing
    }
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
      if (tag.getParent() == m_tag) {
        add(new CTagNode(getProjectTree(), m_database, (TreeNode<CTag>) tag));

        getTreeModel().nodeStructureChanged(CTagNode.this);
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

        getTreeModel().nodeStructureChanged(CTagNode.this);
      }
    }

    @Override
    public void deletedTagSubtree(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      if (hasChild(tag)) {
        // Remove the node that represents the deleted project.
        for (int i = 0; i < getChildCount(); i++) {
          final CTagNode node = (CTagNode) getChildAt(i);

          if (node.getObject() == tag) {
            node.dispose();
            remove(node);
            break;
          }
        }
      }

      getTreeModel().nodeStructureChanged(CTagNode.this);
    }

    @Override
    public void insertedTag(final CTagManager tagManager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      if (tag.getParent() == m_tag) {
        final Set<CTagNode> toDelete = new HashSet<CTagNode>();

        for (int i = 0; i < getChildCount(); i++) {
          toDelete.add((CTagNode) getChildAt(i));
        }

        for (final CTagNode node : toDelete) {
          node.dispose();
          remove(node);
        }

        // Don't bother to recreate the children, the new inserted tag node will do that.

        add(new CTagNode(getProjectTree(), m_database, (TreeNode<CTag>) tag));

        getTreeModel().nodeStructureChanged(CTagNode.this);
      }
    }
  }
}
