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

// / Used to listen on databases.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * {@link Database} objects.
 */
public interface IDatabaseListener {

  // ! Signals a new module in the database.
  /**
   * Invoked after a new module was added to the database.
   *
   * @param database The database the module was added to.
   * @param module The module that was added to the database.
   */
  void addedModule(Database database, Module module);

  // ! Signals a new project in the database.
  /**
   * Invoked after a new project was added to the database.
   *
   * @param database The database the project was added to.
   * @param project The project that was added to the database.
   */
  void addedProject(Database database, Project project);

  // ! Signals an auto-connect settings change.
  /**
   * Invoked after the flag changes that determines whether a connection to the database is
   * established automatically when BinNavi starts.
   *
   * @param database The database whose auto-connection flag changed.
   * @param autoConnect The new value of the auto-connection flag.
   */
  void changedAutoConnect(Database database, boolean autoConnect);

  // ! Signals a new database description.
  /**
   * Invoked after the description string of the database changed.
   *
   * @param database The database whose description changed.
   * @param description The new description string of the database.
   */
  void changedDescription(Database database, String description);

  // ! Signals a new database driver.
  /**
   * Invoked after the driver string of the database changed.
   *
   * @param database The database whose driver string changed.
   * @param driver The new driver string of the database.
   */
  void changedDriver(Database database, String driver);

  // ! Signals a new database host.
  /**
   * Invoked after the host string of the database changed.
   *
   * @param database The database whose host string changed.
   * @param host The new host string of the database.
   */
  void changedHost(Database database, String host);

  // ! Signals a changed identity string
  /**
   * Invoked after the identity used for the current database has been changed.
   *
   * @param database The database where the identity changed.
   * @param identity The changed identity.
   */
  void changedIdentity(Database database, String identity);

  // ! Signals a new database name.
  /**
   * Invoked after the database name string of the database changed.
   *
   * @param database The database whose database name string changed.
   * @param name The new database name string of the database.
   */
  void changedName(Database database, String name);

  // ! Signals a new database password.
  /**
   * Invoked after the password that is used to connect to the database changed.
   *
   * @param database The database whose password changed.
   * @param password The new password string of the database.
   */
  void changedPassword(Database database, String password);

  // ! Signals a change in the save-password setting.
  /**
   * Invoked after the flag changes that determines whether the password of the database is saved in
   * the configuration file.
   *
   * @param database The database whose save-password flag changed.
   * @param savePassword The new value of the save-password flag.
   */
  void changedSavePassword(Database database, boolean savePassword);

  // ! Signals a new database user.
  /**
   * Invoked after the user that is used to connect to the database changed.
   *
   * @param database The database whose user string changed.
   * @param user The new user string of the database.
   */
  void changedUser(Database database, String user);

  // ! Signals that the database was closed.
  /**
   * Invoked after the connection to the database closed.
   *
   * @param database The database whose connection closed.
   */
  void closedDatabase(Database database);

  // ! Signals that the database is about to be closed.
  /**
   * Invoked right before a database is closed. The listening object has the opportunity to veto the
   * close process if it still needs to work with the database.
   *
   * @param database The database that is about to be closed.
   *
   * @return True, to indicate that the database can be closed. False, to veto the close process.
   */
  boolean closingDatabase(Database database);

  // ! Signals the deletion of a module from the database.
  /**
   * Invoked after a module was deleted from the database.
   *
   *  After this function was invoked, further usage of the deleted module or objects inside that
   * module lead to undefined behavior.
   *
   * @param database The database the module was deleted from.
   * @param module The module that was deleted from the database.
   */
  void deletedModule(Database database, Module module);

  // ! Signals the deletion of a project from the database.
  /**
   * Invoked after a project was deleted from the database.
   *
   *  After this function was invoked, further usage of the deleted project or objects inside that
   * project lead to undefined behavior.
   *
   * @param database The database the project was deleted from.
   * @param project The project that was deleted from the database.
   */
  void deletedProject(Database database, Project project);

  // ! Signals that data was loaded from the database.
  /**
   * Invoked after the data stored in the database was loaded.
   *
   *  After this function was invoked, database objects like the projects or modules stored in the
   * database can be accessed.
   *
   * @param database The database that was loaded.
   */
  void loadedDatabase(Database database);

  // ! Signals that a database connection was established.
  /**
   * Invoked after a connection to the database was established.
   *
   * @param database The database a connection was established to.
   */
  void openedDatabase(Database database);
}
