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
package com.google.security.zynamics.binnavi.Tagging;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.trees.BreadthFirstSorter;
import com.google.security.zynamics.zylib.types.trees.DepthFirstSorter;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

/**
 * Tag managers are used to manage tags of a given type. The tag manager can be used to create new
 * tags, to delete existing tags, etc.
 */
public final class CTagManager implements IDatabaseObject, ITagManager {
  /**
   * Tag tree that is managed by this tag manager.
   */
  private final Tree<CTag> m_tagTree;

  /**
   * Type of all tags that are managed by this tag manager.
   */
  private final TagType m_type;

  /**
   * SQL provider that is used to write changes in the tree to the database.
   */
  private final SQLProvider m_provider;

  /**
   * List of listeners that are notified about changes in the tag manager.
   */
  private final ListenerProvider<ITagManagerListener> m_listeners =
      new ListenerProvider<ITagManagerListener>();

  /**
   * Creates a new tag manager object.
   * 
   * @param tagTree Tag tree that is managed by this tag manager.
   * @param type Type of all tags that are managed by this tag manager.
   * @param provider SQL provider that is used to write changes in the tree to the database.
   */
  public CTagManager(final Tree<CTag> tagTree, final TagType type, final SQLProvider provider) {
    m_tagTree = Preconditions.checkNotNull(tagTree, "IE00853: Tag tree argument can't be null");
    m_type = Preconditions.checkNotNull(type, "IE00854: Type argument can't be null");
    m_provider = Preconditions.checkNotNull(provider, "IE00855: Provider argument can't be null");
  }


  /**
   * Connects a parent node with a child node.
   * 
   * @param parent The parent node.
   * @param child The child node.
   */
  private static void link(final ITreeNode<CTag> parent, final ITreeNode<CTag> child) {
    child.setParent(parent);
    parent.addChild(child);
  }

  /**
   * Unlinks two tag nodes.
   * 
   * @param parent The parent node to unlink.
   * @param child The child node to unlink.
   */
  private static void unlink(final ITreeNode<CTag> parent, final ITreeNode<CTag> child) {
    child.setParent(null);
    parent.removeChild(child);
  }

  /**
   * Checks whether a given tag is managed by this tag manager.
   * 
   * @param tag The tag to be checked.
   * 
   * @return True, if the tag is managed by this tag. False, otherwise.
   */
  private boolean hasTag(final ITreeNode<CTag> tag) {
    return (tag == getRootTag()) || BreadthFirstSorter.getSortedList(getRootTag()).contains(tag);
  }

  /**
   * Makes sure that a tag is a valid tag in the context of this tag manager.
   * 
   * @param tag The tag to check.
   */
  private void validateTag(final ITreeNode<CTag> tag) {
    Preconditions.checkNotNull(tag, "IE00859: Tag argument can't be null");
    Preconditions.checkNotNull(tag.getObject(), "IE00860: Tag object can't be null");
    Preconditions.checkArgument(tag.getObject().getType() == m_type,
        "IE00861: Tag has an incorrect type");
    Preconditions.checkArgument(hasTag(tag), "IE00862: Tag is not managed by this manager");
  }

  @Override
  public void addListener(final ITagManagerListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public ITreeNode<CTag> addTag(final ITreeNode<CTag> parent, final String name)
      throws CouldntSaveDataException {
    validateTag(parent);

    Preconditions.checkNotNull(name, "IE00863: Name argument can't be null");

    final CTag tag = m_provider.createTag(parent.getObject(), name, "", m_type);

    final TreeNode<CTag> child = new TreeNode<CTag>(tag);

    link(parent, child);

    for (final ITagManagerListener listener : m_listeners) {
      // ESCA-JAVA0166:
      try {
        listener.addedTag(this, child);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return child;
  }

  @Override
  public void deleteTag(final ITreeNode<CTag> tag) throws CouldntDeleteException {
    validateTag(tag);
    Preconditions.checkArgument(tag != getRootTag(), "IE00864: Can not delete the root tag");

    m_provider.deleteTag(tag);

    // Connect the parent of the deleted tag with the
    // children of the deleted tag
    final ITreeNode<CTag> parent = tag.getParent();

    for (final ITreeNode<CTag> child : tag.getChildren()) {
      link(parent, child);
    }

    // Remove the old connections to the child.
    // NOTE: The old parent connection is overwritten
    // by link() above.
    parent.removeChild(tag);
    tag.setParent(null);

    tag.getObject().deleteTag();

    for (final ITagManagerListener listener : m_listeners) {
      try {
        listener.deletedTag(this, parent, tag);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void deleteTagSubTree(final ITreeNode<CTag> tag) throws CouldntDeleteException {
    validateTag(tag);
    Preconditions.checkArgument((tag != getRootTag()), "IE00865: Can not delete the root tag");

    final List<? extends ITreeNode<CTag>> children = tag.getChildren();

    if (children.isEmpty()) {
      deleteTag(tag);

      return;
    }

    m_provider.deleteTagSubtree(tag);

    for (final ITreeNode<CTag> child : children) {
      for (final ITreeNode<CTag> c : DepthFirstSorter.getSortedList(child)) {
        c.getParent().removeChild(c);
        c.getObject().deleteTag();
      }

      child.getParent().removeChild(child);
      child.getObject().deleteTag();
    }

    tag.getParent().removeChild(tag);
    tag.getObject().deleteTag();

    for (final ITagManagerListener listener : m_listeners) {
      // ESCA-JAVA0166:
      try {
        listener.deletedTagSubtree(this, tag.getParent(), tag);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public ITreeNode<CTag> getRootTag() {
    return m_tagTree.getRootNode();
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    return provider.inSameDatabase(m_provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return provider.equals(m_provider);
  }

  @Override
  public ITreeNode<CTag> insertTag(final ITreeNode<CTag> parent, final String name)
      throws CouldntSaveDataException {
    validateTag(parent);

    Preconditions.checkNotNull(name, "IE00867: Name argument can't be null");

    final CTag tag = m_provider.insertTag(parent, name, "", m_type);

    final List<? extends ITreeNode<CTag>> children = parent.getChildren();

    final ITreeNode<CTag> tagTreeNode = new TreeNode<CTag>(tag);

    link(parent, tagTreeNode);

    for (final ITreeNode<CTag> child : children) {
      parent.removeChild(child);
      link(tagTreeNode, child);
    }

    for (final ITagManagerListener listener : m_listeners) {
      try {
        listener.insertedTag(this, parent, tagTreeNode);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return tagTreeNode;
  }

  @Override
  public void moveTag(final ITreeNode<CTag> parent, final ITreeNode<CTag> child)
      throws CouldntSaveDataException {
    m_provider.moveTag(parent, child, m_type);

    final ITreeNode<CTag> childParent = child.getParent();

    if (childParent != null) {
      for (final ITreeNode<CTag> childChild : child.getChildren()) {
        unlink(child, childChild);
        link(childParent, childChild);
      }

      childParent.removeChild(child);

      for (final ITagManagerListener listener : m_listeners) {
        try {
          listener.deletedTag(this, parent, child);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    link(parent, child);

    for (final ITagManagerListener listener : m_listeners) {
      try {
        listener.addedTag(this, child);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void removeListener(final ITagManagerListener listener) {
    m_listeners.removeListener(listener);
  }
}
