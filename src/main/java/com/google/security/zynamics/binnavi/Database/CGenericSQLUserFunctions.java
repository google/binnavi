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
package com.google.security.zynamics.binnavi.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.Users.CUser;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;

public class CGenericSQLUserFunctions {
  /**
   * Adds a user to the users table in the database.
   * 
   * @param provider The provider used to access the database.
   * @param userName The user name which will be used to create the new user.
   * @return The user.
   * 
   * @throws CouldntSaveDataException if the user could not be saved to the database.
   */
  public static IUser addUser(final SQLProvider provider, final String userName)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00081: provider argument can not be null");
    Preconditions.checkNotNull(userName, "IE00087: userName argument can not be null");

    final Connection connection = provider.getConnection().getConnection();

    final String query =
        "INSERT INTO " + CTableNames.USER_TABLE
            + " VALUES (DEFAULT, ?, null, null) RETURNING user_id;";

    CUser user = null;

    try (PreparedStatement statement = connection.prepareStatement(query)) {
        statement.setString(1, userName);
        
        ResultSet resultSet = statement.executeQuery();
        
        while (resultSet.next()) {
          user = new CUser(resultSet.getInt(1), userName);
        }
     
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }

    return user;
  }

  /**
   * Deletes a user from the database.
   * 
   * @param provider The provider used to access the database.
   * @param user The user to be deleted.
   * 
   * @throws CouldntDeleteException if the user could not be deleted from the database.
   */
  public static void deleteUser(final SQLProvider provider, final IUser user)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(provider, "IE00088: provider argument can not be null");
    Preconditions.checkNotNull(user, "IE00106: user argument can not be null");

    final Connection connection = provider.getConnection().getConnection();

    final String query = "DELETE FROM " + CTableNames.USER_TABLE + " WHERE user_id = ?;";

    try (PreparedStatement statement = connection.prepareStatement(query)) {
     
        statement.setInt(1, user.getUserId());
        statement.execute();

    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * Edits a user name in the database of a user already saved.
   * 
   * @param provider The provider to access the database.
   * @param user The user which donates the id.
   * @param userName The user name for the user.
   * @return The new user.
   * 
   * @throws CouldntSaveDataException if the changes could not be saved in the database.
   */
  public static IUser editUserName(final SQLProvider provider, final IUser user,
      final String userName) throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00117: provider argument can not be null");
    Preconditions.checkNotNull(user, "IE00118: user argument can not be null");
    Preconditions.checkNotNull(userName, "IE00205: userName argument can not be null");

    final Connection connection = provider.getConnection().getConnection();

    final String query =
        "UPDATE " + CTableNames.USER_TABLE + " SET user_name = ? WHERE user_id = ?;";

    try (PreparedStatement statement = connection.prepareStatement(query)) {
      
        statement.setString(1, userName);
        statement.setInt(2, user.getUserId());
        statement.execute();

    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }

    return new CUser(user.getUserId(), userName);
  }

  /**
   * Loads the complete list of users known to the database.
   * 
   * @param provider The provider used to access the database.
   * 
   * @return A list of all users known to the database.
   * @throws CouldntLoadDataException
   */
  public static List<IUser> loadUsers(final SQLProvider provider) throws CouldntLoadDataException {
    Preconditions.checkNotNull(provider, "IE00206: provider argument can not be null");

    final CConnection connection = provider.getConnection();

    final String query = "SELECT user_id, user_name FROM " + CTableNames.USER_TABLE;

    final ArrayList<IUser> users = new ArrayList<>();

    try (ResultSet resultSet = connection.executeQuery(query, true)) {
    
      while (resultSet.next()) {
        final int userId = resultSet.getInt(1);
        final String userName = resultSet.getString(2);
        users.add(new CUser(userId, userName));
      }

    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }

    return users;
  }
}
