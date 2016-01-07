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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Represents a single tag that is stored in the database and can be used to type either nodes or
 * views depending on the type of the tag.
 */
public final class CTag implements IDatabaseObject {
  /**
   * The ID of the tag object as it is stored in the database.
   */
  private final int m_id;

  /**
   * The name of the tag object.
   */
  private String m_name;

  /**
   * The description of the tag object.
   */
  private String m_description;

  /**
   * The type of the tag object.
   */
  private final TagType m_type;

  /**
   * The SQL provider that is used to store changes in the tag to the database.
   */
  private final SQLProvider m_sql;

  /**
   * List of listeners that are notified about changes in the tag.
   */
  private final ListenerProvider<ITagListener> m_listeners = new ListenerProvider<ITagListener>();

  /**
   * Creates a new tag object.
   * 
   * @param tagId The ID of the tag object as it is stored in the database. Note that ID = 0 is
   *        reserved for the root node.
   * @param name The name of the tag object.
   * @param description The description of the tag object.
   * @param type The type of the tag object.
   * @param sqlProvider The SQL provider that is used to store changes in the tag to the database.
   */
  public CTag(final int tagId, final String name, final String description, final TagType type,
      final SQLProvider sqlProvider) {

    Preconditions.checkArgument(tagId >= 0, "IE00845: Tag IDs must not be negative");
    m_name = Preconditions.checkNotNull(name, "IE00846: Name argument can't be null");
    m_description =
        Preconditions.checkNotNull(description, "IE00847: Description argument can't be null");
    m_type = Preconditions.checkNotNull(type, "IE00848: Type argument can't be null");
    m_sql = Preconditions.checkNotNull(sqlProvider, "IE00849: SQL Provider argument can't be null");

    m_id = tagId;
  }

  /**
   * Adds a listener object that is notified about changes in the tag.
   * 
   * @param listener The listener object that is added to the tag.
   */
  public void addListener(final ITagListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Deletes a tag from the database.
   */
  public void deleteTag() {
    for (final ITagListener listener : m_listeners) {
      // ESCA-JAVA0166:
      try {
        listener.deletedTag(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public boolean equals(final Object rhs) {
    if (!(rhs instanceof CTag)) {
      return false;
    }

    return m_id == ((CTag) rhs).m_id;
  }

  /**
   * Returns the description of the tag.
   * 
   * @return The description of the tag.
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * Returns the ID of the tag.
   * 
   * @return The ID of the tag.
   */
  public int getId() {
    return m_id;
  }

  /**
   * Returns the name of the tag.
   * 
   * @return The name of the tag.
   */
  public String getName() {
    return m_name;
  }

  /**
   * Returns the type of the tag.
   * 
   * @return The type of the tag.
   */
  public TagType getType() {
    return m_type;
  }

  @Override
  public int hashCode() {
    return m_id;
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject object) {
    Preconditions.checkNotNull(object, "IE00850: Object argument can't be null");

    return object.inSameDatabase(m_sql);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return m_sql == provider;
  }

  /**
   * Removes a listener object from the tag.
   * 
   * @param listener The listener object to remove.
   */
  public void removeListener(final ITagListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Changes the description of the tag.
   * 
   * @param description The new description of the tag.
   * 
   * @throws CouldntSaveDataException Thrown if the new description could not be stored in the
   *         database.
   */
  public void setDescription(final String description) throws CouldntSaveDataException {
    Preconditions.checkNotNull(description, "IE00851: Description argument can't be null");

    if (description.equals(m_description)) {
      return;
    }

    m_sql.setDescription(this, description);

    m_description = description;

    for (final ITagListener listener : m_listeners) {
      try {
        listener.changedDescription(this, description);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Changes the name of the tag.
   * 
   * @param name The new name of the tag.
   * 
   * @throws CouldntSaveDataException Thrown if the new name could not be stored in the database.
   */
  public void setName(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00852: Name argument can't be null");

    if (name.equals(m_name)) {
      return;
    }

    m_sql.setName(this, name);

    m_name = name;

    for (final ITagListener listener : m_listeners) {
      try {
        listener.changedName(this, name);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  @Override
  public String toString() {
    return String.format("Tag '%s'", getName());
  }
}
