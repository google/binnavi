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
package com.google.security.zynamics.binnavi.Gui.Users;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProviderListener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUserManagerListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The user manager is the central source for all things which are related to the user management in
 * BinNavi It provides the information about which users are known in a specific database and
 * regulates the access to the resources. User management as currently implemented in BinNavi does
 * only provide support to avoid concurrent modification problems when multiple instances access the
 * same database. This feature does not provide any security and will probably never will.
 */

public class CUserManager {
  /**
   * Keeps track of the user managers for the individual databases.
   */
  private static Map<SQLProvider, CUserManager> managers = new HashMap<SQLProvider, CUserManager>();

  /**
   * Objects that want to be notified about changes regarding users.
   */
  private final ListenerProvider<IUserManagerListener> listeners =
      new ListenerProvider<IUserManagerListener>();

  /**
   * The set of users known to the manager.
   */
  private final Set<IUser> users = new HashSet<IUser>();

  /**
   * The currently active user.
   */
  private IUser activeUser = null;

  /**
   * Database for which the user manager was created.
   */
  private final SQLProvider provider;

  /**
   * Listener which gets informed about changes in the {@link SQLProvider provider}. Used here to
   * make sure we clean up the static references to the {@link SQLProvider provider} on close.
   */
  private final SQLProviderListener providerListener = new InternalSQLProviderListener();

  /**
   * Creates a new user manager object.
   *
   * @param provider Database for which the user manager was created.
   */
  private CUserManager(final SQLProvider provider, final List<IUser> users) {
    this.provider =
        Preconditions.checkNotNull(provider, "IE02716: provider argument can not be null.");
    this.users.addAll(users);
    this.provider.addListener(providerListener);
  }

  /**
   * Returns the user manager for a database.
   *
   * @param provider The provider which is used to access the database.
   *
   * @return The user manager associated with the given database.
   */
  public static synchronized CUserManager get(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "IE02717: provider argument can not be null.");

    if (!managers.containsKey(provider)) {
      try {
        managers.put(provider, new CUserManager(provider, provider.loadUsers()));
      } catch (final CouldntLoadDataException exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return managers.get(provider);
  }

  private void close() {
    managers.remove(provider);
    provider.removeListener(providerListener);
  }

  /**
   * Synchronizes the internal list of known users with the list of known users in the database.
   */
  private synchronized void syncUsers() {
    try {
      users.clear();
      users.addAll(provider.loadUsers());
    } catch (final CouldntLoadDataException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  /**
   * Adds a listener object that is notified about changes to users and changes in the user
   * management.
   *
   * @param listener The listener object to add.
   */
  public synchronized void addListener(final IUserManagerListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Adds a user to the user management and saves the information to the database
   *
   * @param userName The name of the user to be added to user management.
   *
   * @return The user.
   * @throws CouldntSaveDataException
   */
  public synchronized IUser addUser(final String userName) throws CouldntSaveDataException {
    Preconditions.checkNotNull(userName, "IE02718: user name argument can not be null.");

    if (containsUserName(userName)) {
      throw new IllegalStateException("IE02719: User is already known to user management.");
    }

    final IUser user = provider.addUser(userName);
    users.add(user);

    for (final IUserManagerListener listener : listeners) {
      try {
        listener.addedUser(user);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return user;
  }

  /**
   * Checks the stored user names against a provided user name.
   *
   * @param userName The user name to be checked.
   *
   * @return true if the user names already contain the given user name.
   */
  public synchronized boolean containsUserName(final String userName) {
    Preconditions.checkNotNull(userName, "IE02720: userName argument can not be null");

    syncUsers();

    for (final IUser storedUser : users) {
      if (storedUser.getUserName().equalsIgnoreCase(userName)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Removes a user from user management and deletes him from the database.
   *
   * @param user
   * @throws CouldntDeleteException
   */
  public synchronized void deleteUser(final IUser user) throws CouldntDeleteException {
    Preconditions.checkNotNull(user, "IE02721: user argument can not be null");

    if (!users.contains(user)) {
      throw new IllegalStateException("IE02722: User is not known to the user management.");
    }

    provider.deleteUser(user);

    for (final IUserManagerListener listener : listeners) {
      try {
        listener.deletedUser(user);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Edits a users user name to something else.
   *
   * @param user The user where the name should be changed.
   * @param userName The user name to change the name of the user to.
   *
   * @return The new user.
   * @throws CouldntSaveDataException
   */
  public synchronized IUser editUserName(final IUser user, final String userName)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(user, "IE02723: user argument can not be null");
    Preconditions.checkNotNull(userName, "IE02724: userName argument can not be null");

    if (!users.contains(user)) {
      throw new IllegalStateException("IE02725: User is not known to the user management.");
    }
    if (containsUserName(userName)) {
      throw new IllegalStateException("IE02726: User name is already in use by another user.");
    }

    final IUser newUser = provider.editUserName(user, userName);

    for (final IUserManagerListener listener : listeners) {
      try {
        listener.editedUser(newUser);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return newUser;
  }

  /**
   * Returns the currently active user.
   *
   * @return The user which is currently active.
   */
  public synchronized IUser getCurrentActiveUser() {
    if (activeUser == null) {
      throw new IllegalStateException("IE02727: Current active user is not set.");
    }
    return activeUser;
  }

  /**
   * Returns the currently known listeners.
   *
   * @return The currently known listeners.
   */
  public synchronized Iterator<IUserManagerListener> getListeners() {
    return listeners.iterator();
  }

  /**
   * Returns a user be reference of id.
   *
   * @param userId The user id of the user to search for.
   *
   * @return The user id found null otherwise.
   */
  public synchronized IUser getUserById(final int userId) {
    Preconditions.checkArgument(userId >= 0, "Error: User id must be a positive number");

    for (final IUser storedUser : users) {
      if (storedUser.getUserId() == userId) {
        return storedUser;
      }
    }

    // if it is not found locally it might be present in the database therefore sync after the local
    // users have been checked.
    syncUsers();

    for (final IUser storedUser : users) {
      if (storedUser.getUserId() == userId) {
        return storedUser;
      }
    }

    return null;
  }

  /**
   * Returns a user by reference of name.
   *
   * @param userName The user name of the user to search for.
   *
   * @return The user if found null otherwise.
   */
  public synchronized IUser getUserByUserName(final String userName) {
    Preconditions.checkNotNull(userName, "IE02728: userName argument can not be null");

    for (final IUser storedUser : users) {
      if (storedUser.getUserName().equalsIgnoreCase(userName)) {
        return storedUser;
      }
    }

    // if it is not found locally it might be present in the database therefore sync after the local
    // users have been checked.
    syncUsers();

    for (final IUser storedUser : users) {
      if (storedUser.getUserName().equalsIgnoreCase(userName)) {
        return storedUser;
      }
    }


    return null;
  }

  public synchronized boolean isOwner(final IComment comment) {
    return getCurrentActiveUser().getUserId() == comment.getUser().getUserId();
  }

  /**
   * Remove a listener object from the user management
   *
   * @param listener the listener to be removed.
   */
  public synchronized void removeListener(final IUserManagerListener listener) {
    listeners.removeListener(listener);
  }

  /**
   * Sets the current active user.
   *
   * @param user The user which will be set active.
   */
  public synchronized void setCurrentActiveUser(final IUser user) {
    Preconditions.checkNotNull(user, "IE02729: user argument can not be null.");

    if (users.contains(user)) {
      activeUser = user;
    } else {
      throw new IllegalStateException(
          "Error: User to be set active is not known to user management.");
    }
  }

  /**
   * Internal listener class to be informed about provider changes.
   */
  private class InternalSQLProviderListener implements SQLProviderListener {

    @Override
    public void providerClosing(SQLProvider provider) {
      if (CUserManager.this.provider.equals(provider)) {
        CUserManager.this.close();
      }
    }
  }
}
