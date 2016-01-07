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

import java.util.ArrayList;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

// / Represents a single view tag or node tag.
/**
 * A tag can be used to categorize views or nodes.
 */
public final class Tag implements ApiObject<ITreeNode<CTag>> {
  /**
   * The internal tag object that backs the API tag object.
   */
  private final ITreeNode<CTag> m_tag;

  /**
   * Parent tag of the tag.
   */
  private Tag m_parent;

  /**
   * Child tags of the tag.
   */
  private final List<Tag> m_children = new ArrayList<Tag>();

  /**
   * Listener that forwards changes in the internal tag object to the API tag object.
   */
  private final InternalListener m_listener = new InternalListener();

  /**
   * Listeners that are notified about changes in the API tag object.
   */
  private final ListenerProvider<ITagListener> m_listeners = new ListenerProvider<ITagListener>();

  /**
   * Creates a new API tag object backed by an internal tag object.
   * 
   * @param tag The internal tag object that backs the API tag object.
   */
  Tag(final ITreeNode<CTag> tag) {
    m_tag = Preconditions.checkNotNull(tag, "Error: Tag argument can not be null");

    m_tag.getObject().addListener(m_listener);
  }

  /**
   * Links two tag objects.
   * 
   * @param parent The parent tag object that is set as the parent of the child.
   * @param child The child tag object that is added to the child list of the parent.
   */
  static void link(final Tag parent, final Tag child) {
    Preconditions.checkNotNull(parent, "Error: Parent can't be null");
    Preconditions.checkNotNull(child, "Error: Child can't be null");
    parent.m_children.add(child);
    child.m_parent = parent;
  }

  /**
   * Unlinks two tag objects.
   * 
   * @param parent The parent tag.
   * @param child The child tag that is removed from the child list of the parent.
   */
  static void unlink(final Tag parent, final Tag child) {
    Preconditions.checkNotNull(parent, "Error: Parent can't be null");
    Preconditions.checkNotNull(child, "Error: Child can't be null");
    parent.m_children.remove(child);
    child.m_parent = null;
  }

  @Override
  public ITreeNode<CTag> getNative() {
    return m_tag;
  }

  // ! Adds a tag listener.
  /**
   * Adds an object that is notified about changes in the tag.
   * 
   * @param listener The listener object that is notified about changes in the tag.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the tag.
   */
  public void addListener(final ITagListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Child tags of the tag.
  /**
   * Returns the children of the tag.
   * 
   * @return A list of child tags.
   */
  public List<Tag> getChildren() {
    return new ArrayList<Tag>(m_children);
  }

  // ! Description of the tag.
  /**
   * Returns the description of the tag.
   * 
   * @return The description of the tag.
   */
  public String getDescription() {
    return m_tag.getObject().getDescription();
  }

  // ! Name of the tag.
  /**
   * Returns the name of the tag.
   * 
   * @return The name of the tag.
   */
  public String getName() {
    return m_tag.getObject().getName();
  }

  // ! Parent tag of the tag.
  /**
   * Returns the parent tag of the tag.
   * 
   * @return The parent tag of the tag.
   */
  public Tag getParent() {
    return m_parent;
  }

  // ! Type of the tag.
  /**
   * Returns the type of the tag.
   * 
   * @return The type of the tag.
   */
  public TagType getType() {
    return TagType.convert(m_tag.getObject().getType());
  }

  // ! Removes a tag listener.
  /**
   * Removes a listener object from the tag.
   * 
   * @param listener The listener object to remove from the tag.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the tag.
   */
  public void removeListener(final ITagListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Changes the tag description.
  /**
   * Changes the description of the tag.
   * 
   * @param description The new description of the tag.
   * 
   * @throws CouldntSaveDataException Thrown if the description of the tag could not be changed.
   */
  public void setDescription(final String description) throws CouldntSaveDataException {
    try {
      m_tag.getObject().setDescription(description);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the tag name.
  /**
   * Changes the name of the tag.
   * 
   * @param name The new description of the tag.
   * 
   * @throws CouldntSaveDataException Thrown if the name of the tag could not be changed.
   */
  public void setName(final String name) throws CouldntSaveDataException {
    try {
      m_tag.getObject().setName(name);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Printable representation of the tag.
  /**
   * Returns a string representation of the tag.
   * 
   * @return A string representation of the tag.
   */
  @Override
  public String toString() {
    return String.format("Tag '%s'", getName());
  }

  /**
   * Listener that forwards changes in the internal tag object to the API tag object.
   */
  private class InternalListener implements com.google.security.zynamics.binnavi.Tagging.ITagListener {
    @Override
    public void changedDescription(final CTag tag, final String description) {
      for (final ITagListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.changedDescription(Tag.this, description);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedName(final CTag tag, final String name) {
      for (final ITagListener listener : m_listeners) {
        try {
          listener.changedName(Tag.this, name);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedTag(final CTag tag) {
      // TODO (timkornau): forward this functionality to the API.
    }
  }
}
