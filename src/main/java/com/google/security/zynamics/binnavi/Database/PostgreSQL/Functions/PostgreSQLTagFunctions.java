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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.zylib.strings.Commafier;
import com.google.security.zynamics.zylib.types.trees.DepthFirstSorter;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

public final class PostgreSQLTagFunctions {
  /**
   * You are not supposed to instantiate this class
   */
  private PostgreSQLTagFunctions() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Checks arguments for validity.
   * 
   * @param provider The provider argument to check.
   * @param tag The tag argument to check.
   * @param type The type argument to check.
   */
  private static void checkArguments(final AbstractSQLProvider provider, final CTag tag,
      final TagType type) {
    checkArguments(provider, tag);
    Preconditions.checkNotNull(type, "IE00549: Type argument can not be null");
    Preconditions.checkArgument(tag.getType() == type,
        "IE00550: Type of the node is different from the type of the given node");
  }

  /**
   * Checks arguments for validity.
   * 
   * @param provider The provider argument to check.
   * @param tag The tag argument to check.
   */
  private static void checkArguments(final AbstractSQLProvider provider, final ITreeNode<CTag> tag) {
    Preconditions.checkNotNull(provider, "IE00551: Provider argument can not be null");
    Preconditions.checkNotNull(tag, "IE00552: Tag argument can not be null");
    Preconditions.checkArgument(tag.getObject().inSameDatabase(provider),
        "IE00553: Tag is not part of this database");
  }

  /**
   * Checks arguments for validity.
   * 
   * @param provider The provider argument to check.
   * @param tag The tag argument to check.
   */
  static void checkArguments(final AbstractSQLProvider provider, final CTag tag) {
    Preconditions.checkNotNull(provider, "IE00546: Provider argument can not be null");
    Preconditions.checkNotNull(tag, "IE00547: Tag argument can not be null");
    Preconditions.checkArgument(tag.inSameDatabase(provider),
        "IE00548: Tag is not part of this database");
  }

  /**
   * Checks arguments for validity.
   * 
   * @param provider The provider argument to check.
   * @param tag The tag argument to check.
   * @param type The type argument to check.
   */
  static void checkArguments(final AbstractSQLProvider provider, final ITreeNode<CTag> tag,
      final TagType type) {
    checkArguments(provider, tag);
    Preconditions.checkNotNull(type, "IE00554: Type argument can not be null");
    Preconditions.checkArgument(tag.getObject().getType() == type,
        "IE00555: Type of the node is different from the type of the given node");
  }

  /**
   * Creates a new tag in the database.
   * 
   * @param provider The connection to the database.
   * @param parent The parent tag of the tag.
   * @param name The name of the new tag.
   * @param description The description of the new tag.
   * @param type The type of the new tag.
   * 
   * @return The new tag.
   * 
   * @throws CouldntSaveDataException Thrown if creating the tag failed.
   */
  public static CTag createTag(final AbstractSQLProvider provider, final CTag parent,
      final String name, final String description, final TagType type)
      throws CouldntSaveDataException {
    checkArguments(provider, parent, type);
    Preconditions.checkNotNull(name, "IE00556: Name argument can not be null");
    Preconditions.checkNotNull(description, "IE00557: Description argument can not be null");

    final CConnection connection = provider.getConnection();

    final String query =
        "insert into " + CTableNames.TAGS_TABLE
            + "(parent_id, name, description, type) values(?, ?, ?, ?::tag_type) returning id";

    try (PreparedStatement statement =
            connection.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)) {
      
        if (parent.getId() == 0) {
          statement.setNull(1, Types.INTEGER);
        } else {
          statement.setInt(1, parent.getId());
        }

        statement.setString(2, name);
        statement.setString(3, description);
        statement.setString(4, tagToString(type));

        Integer id = null;
        try (ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            if (resultSet.isFirst()) {
              id = resultSet.getInt(1);
            }
          }
        }
        if (id != null) {
          return new CTag(id, name, description, type, provider);
        } else {
          throw new IllegalStateException("IE02141: Error id can not be null");
        }
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Deletes a tag from the database.
   * 
   * @param provider The connection to the database.
   * @param tag The tag to delete.
   * 
   * @throws CouldntDeleteException Thrown if the tag could not be deleted.
   */
  public static void deleteTag(final AbstractSQLProvider provider, final ITreeNode<CTag> tag)
      throws CouldntDeleteException {
    checkArguments(provider, tag);
    Preconditions.checkNotNull(tag.getParent(), "IE00558: Can not delete the root tag");
    final CConnection connection = provider.getConnection();

    try {
      final ITreeNode<CTag> parent = tag.getParent();
      final String parentId =
          parent.getObject().getId() == 0 ? "null" : String.valueOf(parent.getObject().getId());
      final String query_1 =
          String.format("UPDATE %s SET parent_id = %s WHERE parent_id = ?", CTableNames.TAGS_TABLE,
              parentId);

      try (PreparedStatement statement_1 = connection.getConnection().prepareStatement(query_1)) {
        statement_1.setInt(1, tag.getObject().getId());
        statement_1.executeUpdate();
      }

      final String query_2 = String.format("DELETE FROM %s WHERE id = ?", CTableNames.TAGS_TABLE);

      try (PreparedStatement statement_2 = connection.getConnection().prepareStatement(query_2)) {
        statement_2.setInt(1, tag.getObject().getId());
        statement_2.executeUpdate();
      }

    } catch (final SQLException e) {
      throw new CouldntDeleteException(e);
    }
  }

  /**
   * Deletes a tag and all of its subtrees.
   * 
   * @param provider The connection to the database.
   * @param tag The root tag of the subtree to delete.
   * 
   * @throws CouldntDeleteException Thrown if the subtree could not be deleted.
   */
  public static void deleteTagSubtree(final AbstractSQLProvider provider, final ITreeNode<CTag> tag)
      throws CouldntDeleteException {
    checkArguments(provider, tag);
    Preconditions.checkNotNull(tag.getParent(), "IE00559: Can not delete the root tag");
    final CConnection connection = provider.getConnection();

    try {
      final StringBuilder query =
          new StringBuilder(String.format("delete from %s where id = %d", CTableNames.TAGS_TABLE,
              tag.getObject().getId()));

      final List<Integer> idsToDelete = new ArrayList<>();

      for (final ITreeNode<CTag> child : DepthFirstSorter.getSortedList(tag)) {
        idsToDelete.add(child.getObject().getId());

        query.append(" or id = " + tag.getObject().getId());
      }

      connection.executeUpdate(query.toString(), true);
    } catch (final SQLException e) {
      throw new CouldntDeleteException(e);
    }
  }

  /**
   * Inserts a new tag into a tag hierarchy.
   * 
   * @param provider The connection to the database.
   * @param parent Parent tag under which the new tag is inserted.
   * @param name The name of the new tag.
   * @param description The description of the new tag.
   * @param type The type of the new tag.
   * 
   * @return The new tag.
   * 
   * @throws CouldntSaveDataException Thrown if creating the tag failed.
   */
  public static CTag insertTag(final AbstractSQLProvider provider, final ITreeNode<CTag> parent,
      final String name, final String description, final TagType type)
      throws CouldntSaveDataException {
    checkArguments(provider, parent, type);
    Preconditions.checkNotNull(name, "IE00563: Name argument can not be null");
    Preconditions.checkNotNull(description, "IE00564: Description argument can not be null");

    final CConnection connection = provider.getConnection();
    
    final String query =
          String.format("update %s set parent_id = ? where parent_id = ? and id <> ?",
              CTableNames.TAGS_TABLE);
              
    try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {
      final CTag tag = createTag(provider, parent.getObject(), name, description, type);

        statement.setInt(1, tag.getId());
        statement.setInt(2, parent.getObject().getId());
        statement.setInt(3, tag.getId());
        statement.executeUpdate();

      return tag;

    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }

  }

  /**
   * Moves a tag.
   * 
   * @param provider Connection to the database.
   * @param newParentNode The new parent node of the tag.
   * @param movedNode The tag to move.
   * 
   * @throws CouldntSaveDataException Thrown if the tag could not be moved.
   */
  public static void moveTag(final AbstractSQLProvider provider,
      final ITreeNode<CTag> newParentNode, final ITreeNode<CTag> movedNode, final TagType type)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE02083: Provider argument can not be null");
    Preconditions.checkNotNull(newParentNode, "IE02190: Parent argument can not be null");
    Preconditions.checkNotNull(movedNode, "IE02191: Child argument can not be null");

    final List<Integer> childIds = new ArrayList<>();

    for (final ITreeNode<CTag> childChild : movedNode.getChildren()) {
      childIds.add(childChild.getObject().getId());
    }
    
    
    try {
      final String childParentId =
          movedNode.getParent().getObject().getId() == 0 ? "null" : String.valueOf(movedNode
              .getParent().getObject().getId());

      if (!childIds.isEmpty()) {
        // Connect the parent of the child tag with the children of the child tag
        provider.getConnection().executeUpdate(
            "update " + CTableNames.TAGS_TABLE + " set parent_id = " + childParentId
                + " where id in (" + Commafier.commafy(childIds) + ") and type = '"
                + tagToString(type) + "'", true);
      }

      // Connect the parent tag with the child tag
      provider.getConnection().executeUpdate(
          "update " + CTableNames.TAGS_TABLE + " set parent_id = "
              + newParentNode.getObject().getId() + " where id = " + movedNode.getObject().getId()
              + " and type = '" + tagToString(type) + "'", true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Changes the description of a tag.
   * 
   * @param provider The connection to the database.
   * @param tag The tag whose description is changed.
   * @param description The new description of the tag.
   * 
   * @throws CouldntSaveDataException Thrown if changing the tag description failed.
   */
  public static void setDescription(final AbstractSQLProvider provider, final CTag tag,
      final String description) throws CouldntSaveDataException {
    checkArguments(provider, tag);

    Preconditions.checkNotNull(description, "IE00713: Description argument can not be null");

    final CConnection connection = provider.getConnection();
    
    final String query = "update " + CTableNames.TAGS_TABLE + " set description = ? where id = ?";
    try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {
      
        statement.setString(1, description);
        statement.setInt(2, tag.getId());
        statement.executeUpdate();
      
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Changes the name of a tag.
   * 
   * @param provider The connection to the database.
   * @param tag The tag whose name is changed.
   * @param name The new name of the tag.
   * 
   * @throws CouldntSaveDataException Thrown if changing the tag name failed.
   */
  public static void setName(final AbstractSQLProvider provider, final CTag tag, final String name)
      throws CouldntSaveDataException {
    checkArguments(provider, tag);

    Preconditions.checkNotNull(name, "IE00565: Name argument can not be null");

    final CConnection connection = provider.getConnection();

    final String query = "update " + CTableNames.TAGS_TABLE + " set name = ? where id = ?";
    try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {
      
        statement.setString(1, name);
        statement.setInt(2, tag.getId());
        statement.executeUpdate();
        
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Turns a tag type object into a string that can be written to the database.
   * 
   * @param type The tag type object.
   * 
   * @return The string that represents the tag type object.
   */
  public static String tagToString(final TagType type) {
    if (type == TagType.VIEW_TAG) {
      return "view_tag";
    } else if (type == TagType.NODE_TAG) {
      return "node_tag";
    }

    throw new IllegalArgumentException("IE00566: Unknown tag type");
  }

}
