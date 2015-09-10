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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.ViewType;

/**
 * Contains PostgreSQL queries for working with views.
 */
public final class PostgreSQLViewFunctions {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLViewFunctions() {
    // You are not supposed to instantiate this class
  }

  /**
   * Checks arguments for validity.
   * 
   * @param provider The provider argument to validate.
   * @param view The view argument to validate.
   */
  private static void checkArguments(final AbstractSQLProvider provider, final INaviView view) {
    Preconditions.checkNotNull(provider, "IE00611: Provider argument can not be null");
    Preconditions.checkNotNull(view, "IE00612: View argument can not be null");
    Preconditions.checkArgument(view.inSameDatabase(provider),
        "IE00613: View is not part of this database");
  }

  /**
   * Deletes a view from the database.
   * 
   * Note that only non-native views can be deleted from the database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param view View to delete from the database.
   * 
   * @throws CouldntDeleteException Thrown if the view could not be deleted.
   */
  public static void deleteView(final AbstractSQLProvider provider, final INaviView view)
      throws CouldntDeleteException {
    checkArguments(provider, view);
    Preconditions.checkArgument(view.getType() != ViewType.Native,
        "IE00614: Native views can not be deleted");

    final CConnection connection = provider.getConnection();

    NaviLogger.info("Deleting view %s", view.getName());

    final int viewId = view.getConfiguration().getId();

    if (!(viewId == -1)) {
      try {
        PostgreSQLHelpers.beginTransaction(connection);
        PostgreSQLHelpers.deleteById(connection, CTableNames.VIEWS_TABLE, viewId);
        PostgreSQLHelpers.deleteByColumnValue(connection, "" + CTableNames.NODES_TABLE + "",
            "view_id", viewId);
        PostgreSQLHelpers.deleteByColumnValue(connection, "" + CTableNames.TRACES_TABLE + "",
            "view_id", viewId);
        PostgreSQLHelpers.endTransaction(connection);
      } catch (final SQLException exception) {
        throw new CouldntDeleteException(exception);
      }
    }
  }

  /**
   * Returns the derived views of the given view.
   * 
   * @param provider Provides the connection to the database.
   * @param view The view whose derived views are returned.
   * 
   * @return The derived views.
   * 
   * @throws CouldntLoadDataException Thrown if the derived views could not be determined.
   */
  public static List<INaviView> getDerivedViews(final AbstractSQLProvider provider,
      final INaviView view) throws CouldntLoadDataException {
    checkArguments(provider, view);

    final List<INaviView> views = new ArrayList<INaviView>();

    // currently project views can not have derived views.
    if (view.getConfiguration().getModule() == null) {
      return views;
    }

    final String query = "SELECT * FROM get_derived_views(?)";
    try (PreparedStatement statement =
          provider.getConnection().getConnection().prepareStatement(query)) {
   
      // TODO(timkornau): this should be changed to the ViewManager.
      final List<INaviView> moduleViews =
          view.getConfiguration().getModule().getContent().getViewContainer().getViews();

      statement.setInt(1, view.getConfiguration().getId());
      
      final ResultSet resultSet = statement.executeQuery();
      if (resultSet == null) {
        return views;
      }

        while (resultSet.next()) {
          final int viewId = resultSet.getInt(1);

          if (viewId != view.getConfiguration().getId()) {
            for (final INaviView moduleView : moduleViews) {
              if (moduleView.getConfiguration().getId() == viewId) {
                views.add(moduleView);
                break;
              }
            }
          }
        }

      return views;
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }
  }

  /**
   * Returns the modification date of a view.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param view The view whose modification date is determined.
   * 
   * @return The modification date of the view.
   * 
   * @throws CouldntLoadDataException Thrown if the modification date of the view could not be
   *         determined.
   */
  public static Date getModificationDate(final AbstractSQLProvider provider, final INaviView view)
      throws CouldntLoadDataException {
    checkArguments(provider, view);
    final CConnection connection = provider.getConnection();
    return PostgreSQLHelpers.getModificationDate(connection, CTableNames.VIEWS_TABLE, view
        .getConfiguration().getId());
  }

  /**
   * Loads the settings of a view from the database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param view The view whose settings are loaded.
   * 
   * @return The settings map of the view.
   * 
   * @throws CouldntLoadDataException Thrown if the settings could not be loaded.
   */
  public static Map<String, String> loadSettings(final AbstractSQLProvider provider,
      final CView view) throws CouldntLoadDataException {
    checkArguments(provider, view);

    final CConnection connection = provider.getConnection();

    final String query =
        "SELECT name, value FROM " + CTableNames.VIEW_SETTINGS_TABLE + " WHERE view_id = "
            + view.getConfiguration().getId();

    try (ResultSet resultSet = connection.executeQuery(query, true)) {

      final HashMap<String, String> settings = new HashMap<>();

        while (resultSet.next()) {
          settings.put(PostgreSQLHelpers.readString(resultSet, "name"),
              PostgreSQLHelpers.readString(resultSet, "value"));
        }

        return settings;

    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }

  /**
   * Stores the settings map of a view to the database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param view The view whose settings are stored.
   * @param settings The settings map to store to the database.
   * 
   * @throws CouldntSaveDataException Thrown if the view settings could not be stored in the
   *         database.
   */
  public static void saveSettings(final AbstractSQLProvider provider, final CView view,
      final Map<String, String> settings) throws CouldntSaveDataException {
    checkArguments(provider, view);
    Preconditions.checkNotNull(settings, "IE02414: settings argument can not be null");

    if (settings.isEmpty()) {
      return;
    }

    final CConnection connection = provider.getConnection();

    final StringBuilder deleteQuery =
        new StringBuilder("DELETE FROM " + CTableNames.VIEW_SETTINGS_TABLE + " WHERE ");
    final StringBuilder insertQuery =
        new StringBuilder("INSERT INTO " + CTableNames.VIEW_SETTINGS_TABLE + " VALUES");

    boolean first = true;

    for (final Map.Entry<String, String> pair : settings.entrySet()) {
      final String value = pair.getValue();
      final String key = pair.getKey();
      if ((value == null) || (key == null)) {
        continue;
      } else {
        if (!first) {
          deleteQuery.append("OR");
          insertQuery.append(',');
        }
        deleteQuery.append(" (view_id = " + view.getConfiguration().getId() + " AND name = '" + key
            + "') ");
        insertQuery.append(" (" + view.getConfiguration().getId() + ", '" + key + "', '" + value
            + "' ) ");
      }

      first = false;
    }

    try {
      connection.executeUpdate(deleteQuery.toString(), true);
      connection.executeUpdate(insertQuery.toString(), true);
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException("E00115: Could not update settings in "
          + CTableNames.VIEW_SETTINGS_TABLE);
    }
  }

  /**
   * Changes the description of the view.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param view The view whose description is changed.
   * @param description The new description of the view.
   * 
   * @throws CouldntSaveDataException Thrown if the description of the view could not be changed.
   */
  public static void setDescription(final AbstractSQLProvider provider, final INaviView view,
      final String description) throws CouldntSaveDataException {
    checkArguments(provider, view);
    Preconditions.checkNotNull(description, "IE00714: Description argument can not be null");
    PostgreSQLHelpers.setDescription(provider.getConnection(), view.getConfiguration().getId(),
        description, CTableNames.VIEWS_TABLE);
  }

  /**
   * Changes the name of the view.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param view The view whose description is changed.
   * @param name The new name of the view.
   * 
   * @throws CouldntSaveDataException Thrown if the description of the view could not be changed.
   */
  public static void setName(final AbstractSQLProvider provider, final INaviView view,
      final String name) throws CouldntSaveDataException {
    checkArguments(provider, view);
    Preconditions.checkNotNull(name, "IE00716: Name argument can not be null");
    PostgreSQLHelpers.setName(provider.getConnection(), view.getConfiguration().getId(), name,
        CTableNames.VIEWS_TABLE);
  }

  /**
   * Stars a view.
   * 
   * @param provider Provides the connection to the database.
   * @param view The view to star.
   * @param isStared True, to star the view. False, to unstar it.
   * 
   * @throws CouldntSaveDataException Thrown if the the star state of the module could not be
   *         updated.
   */
  public static void starView(final AbstractSQLProvider provider, final INaviView view,
      final boolean isStared) throws CouldntSaveDataException {
    checkArguments(provider, view);

    try {
      provider.getConnection().executeUpdate(
          "UPDATE " + CTableNames.VIEWS_TABLE + " SET stared = " + isStared + " WHERE id = "
              + view.getConfiguration().getId(), true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Tags a view.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param view The view to tag.
   * @param tag The tag to tag the view.
   * 
   * @throws CouldntSaveDataException Thrown if the view could not be tagged.
   */
  public static void tagView(final AbstractSQLProvider provider, final INaviView view,
      final CTag tag) throws CouldntSaveDataException {
    checkArguments(provider, view);

    Preconditions.checkNotNull(tag, "IE00615: Tag argument can not be null");
    Preconditions.checkArgument(tag.inSameDatabase(provider),
        "IE00616: Tag is not part of this database");

    final String query =
        String.format("insert into %s(view_id, tag_id) values(%d, %d)",
            CTableNames.TAGGED_VIEWS_TABLE, view.getConfiguration().getId(), tag.getId());

    final CConnection connection = provider.getConnection();

    try {
      connection.executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Removes a tag from a view.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param view The view from which the tag is removed.
   * @param tag The tag to be removed from the view.
   * 
   * @throws CouldntSaveDataException Thrown if the tag could not be removed from the view.
   */
  public static void untagView(final AbstractSQLProvider provider, final INaviView view,
      final CTag tag) throws CouldntSaveDataException {
    checkArguments(provider, view);

    Preconditions.checkNotNull(tag, "IE00617: Tag argument can not be null");
    Preconditions.checkArgument(tag.inSameDatabase(provider),
        "IE00618: Tag is not part of this database");

    final String query =
        String.format("delete from %s where view_id = %d and tag_id = %d",
            CTableNames.TAGGED_VIEWS_TABLE, view.getConfiguration().getId(), tag.getId());

    try {
      provider.getConnection().executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }
}
