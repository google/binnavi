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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

// / Keeps track of known tags.
/**
 * The tag manager is used to manage the tags that can be used to tag either views or nodes in
 * views.
 */
public final class TagManager {

  /**
   * The internal tag manager objects that backs the API tag manager object.
   */
  private final ITagManager m_manager;

  /**
   * Root tag managed by the tag manager.
   */
  private final Tag m_root;

  /**
   * Keeps track of all internal tag objects and their corresponding API objects.
   */
  private final Map<ITreeNode<CTag>, Tag> m_allTags = new HashMap<ITreeNode<CTag>, Tag>();

  /**
   * Listener that forwards events of the internal tag manager to the API tag manager.
   */
  private final InternalListener m_listener = new InternalListener();

  /**
   * Listeners that are notified about changes in the API tag manager object.
   */
  private final ListenerProvider<ITagManagerListener> m_listeners =
      new ListenerProvider<ITagManagerListener>();

  // / @cond INTERNAL
  /**
   * Creates a new API tag manager object.
   * 
   * @param manager The wrapped internal tag manager object.
   */
  // / @endcond
  public TagManager(final ITagManager manager) {
    m_manager = manager;

    m_root = clone(manager.getRootTag(), null);

    manager.addListener(m_listener);
  }

  /**
   * Converts an internal tag tree into an API tag tree.
   * 
   * @param currentNode The internal tag tree node to convert.
   * @param parentExpression Parent of the converted API tag tree node.
   * 
   * @return The converted API tag tree node.
   */
  private Tag clone(final ITreeNode<CTag> currentNode, final Tag parentExpression) {
    final Tag childExpression = new Tag(currentNode);

    m_allTags.put(currentNode, childExpression);

    if (parentExpression != null) {
      Tag.link(parentExpression, childExpression);
    }

    for (final ITreeNode<CTag> child : currentNode.getChildren()) {
      clone(child, childExpression);
    }

    return childExpression;
  }

  /**
   * Searches for an API tag that wraps a given internal tag.
   * 
   * @param tag The internal tag to search for.
   * @param apiTag Tag to search through.
   * 
   * @return The found API tag or null.
   */
  private Tag findTag(final CTag tag, final Tag apiTag) {
    if (tag == apiTag.getNative().getObject()) {
      return apiTag;
    }

    for (final Tag child : apiTag.getChildren()) {
      final Tag foundTag = findTag(tag, child);

      if (foundTag != null) {
        return foundTag;
      }
    }

    return null;
  }

  // ! Adds a tag manager listener.
  /**
   * Adds an object that is notified about changes in the tag manager.
   * 
   * @param listener The listener object that is notified about changes in the tag manager.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the tag
   *         manager.
   */
  public void addListener(final ITagManagerListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Adds a new tag.
  /**
   * Adds a new tag as the child tag of a given parent tag.
   * 
   * @param parent The parent tag of the new tag. If this argument is null, a new root tag is
   *        created.
   * @param name The name of the new tag.
   * 
   * @return The created tag.
   * 
   * @throws IllegalArgumentException Thrown if the name argument is null or if the parent tag is
   *         not managed by this tag manager.
   * @throws CouldntSaveDataException Thrown if the tag could not be created.
   */
  public Tag addTag(final Tag parent, final String name) throws CouldntSaveDataException {
    try {
      if (parent == null) {
        return ObjectFinders.getObject(m_manager.addTag(m_root.getNative(), name), m_allTags.values());
      } else {
        return ObjectFinders.getObject(m_manager.addTag(parent.getNative(), name), m_allTags.values());
      }
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  // ! Deletes a tag.
  /**
   * Permanently deletes an existing tag.
   * 
   * @param tag The tag to delete.
   * 
   * @throws CouldntDeleteException Thrown if the tag could not be deleted.
   */
  public void deleteTag(final Tag tag) throws CouldntDeleteException {
    Preconditions.checkNotNull(tag, "Error: Tag argument can not be null");

    try {
      m_manager.deleteTag(tag.getNative());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException e) {
      throw new CouldntDeleteException(e);
    }
  }

  // ! The managed root tags.
  /**
   * Returns the root tags that are managed by this tag manager.
   * 
   * @return A list of root tags.
   */
  public List<Tag> getRootTags() {
    return m_root.getChildren();
  }

  // / @cond INTERNAL
  /**
   * Returns the API tag that wraps a given internal tag.
   * 
   * @param tag The internal tag to search for.
   * 
   * @return The corresponding API tag.
   */
  // / @endcond
  public Tag getTag(final CTag tag) {
    // TODO (timkornau): This needs to be improved

    return findTag(tag, m_root);
  }

  // ! Inserts a new tag.
  /**
   * Inserts a new tag between a given parent tag and the children of that parent tag.
   * 
   * @param parent The parent tag of the new tag. If this argument is null, a new root tag is
   *        created.
   * @param name The name of the new tag.
   * 
   * @return The created tag.
   * 
   * @throws IllegalArgumentException Thrown if the name argument is null or if the parent tag is
   *         not managed by this tag manager.
   * @throws CouldntSaveDataException Thrown if the tag could not be created.
   */
  public Tag insertTag(final Tag parent, final String name) throws CouldntSaveDataException {
    try {
      if (parent == null) {
        return ObjectFinders.getObject(m_manager.insertTag(m_root.getNative(), name), m_allTags.values());
      } else {
        return ObjectFinders.getObject(m_manager.insertTag(parent.getNative(), name), m_allTags.values());
      }
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  // ! Removes a tag manager listener.
  /**
   * Removes a listener object from the tag manager.
   * 
   * @param listener The listener object to remove from the tag manager.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the tag
   *         manager.
   */
  public void removeListener(final ITagManagerListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Printable representation of the tag manager.
  /**
   * Returns the string representation of the tag manager.
   * 
   * @return The string representation of the tag manager.
   */
  @Override
  public String toString() {
    return "Tag Manager";
  }

  /**
   * Listener that forwards events of the internal tag manager to the API tag manager.
   */
  private class InternalListener implements com.google.security.zynamics.binnavi.Tagging.ITagManagerListener {
    /**
     * Removes a tag and all of its children.
     * 
     * @param tag The tag to remove.
     */
    private void removeTree(final ITreeNode<CTag> tag) {
      m_allTags.remove(tag);

      for (final ITreeNode<CTag> child : tag.getChildren()) {
        removeTree(child);
      }
    }

    @Override
    public void addedTag(final CTagManager manager, final ITreeNode<CTag> tag) {
      final Tag parentTag = m_allTags.get(tag.getParent());

      final Tag newTag = new Tag(tag);

      Tag.link(parentTag, newTag);

      m_allTags.put(tag, newTag);

      for (final ITagManagerListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.addedTag(TagManager.this, newTag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedTag(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      final Tag deletedTag = m_allTags.get(tag);

      final Tag parentTag = deletedTag.getParent();
      final List<Tag> children = deletedTag.getChildren();

      Tag.unlink(parentTag, deletedTag);

      for (final Tag child : children) {
        Tag.unlink(deletedTag, child);
        Tag.link(parentTag, child);
      }

      m_allTags.remove(tag);

      for (final ITagManagerListener listener : m_listeners) {
        try {
          listener.deletedTag(TagManager.this, deletedTag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedTagSubtree(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      final Tag deletedTag = m_allTags.get(tag);

      final Tag parentTag = deletedTag.getParent();

      Tag.unlink(parentTag, deletedTag);

      removeTree(tag);

      for (final ITagManagerListener listener : m_listeners) {
        try {
          listener.deletedTagTree(TagManager.this, deletedTag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void insertedTag(final CTagManager tagManager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      final Tag parentTag = m_allTags.get(tag.getParent());

      final Tag newTag = new Tag(tag);

      for (final Tag child : parentTag.getChildren()) {
        Tag.unlink(parentTag, child);
        Tag.link(newTag, child);
      }

      Tag.link(parentTag, newTag);

      m_allTags.put(tag, newTag);

      for (final ITagManagerListener listener : m_listeners) {
        try {
          listener.insertedTag(TagManager.this, parentTag, newTag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
