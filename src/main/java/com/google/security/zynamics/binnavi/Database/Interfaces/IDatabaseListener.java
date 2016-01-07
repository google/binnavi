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
package com.google.security.zynamics.binnavi.Database.Interfaces;

import java.util.List;

import com.google.security.zynamics.binnavi.Database.LoadEvents;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;


/**
 * Interface for objects that want to be notified about changes in database objects.
 */
public interface IDatabaseListener {
  /**
   * Invoked after a module was added to the database.
   * 
   * @param database The database where the module was added.
   * @param module The module that was added to the database.
   */
  void addedModule(IDatabase database, INaviModule module);

  /**
   * Invoked after a project was added to the database.
   * 
   * @param database The database where the project was added.
   * @param project The project that was added to the database.
   */
  void addedProject(IDatabase database, INaviProject project);

  /**
   * Invoked after the auto connection state of the database changed.
   * 
   * @param database The database whose auto connection state changed.
   * @param autoConnect The new auto connection state.
   */
  void changedAutoConnect(IDatabase database, boolean autoConnect);

  /**
   * Invoked after the description of the database changed.
   * 
   * @param database The database whose description changed.
   * @param description The new description of the database.
   */
  void changedDescription(IDatabase database, String description);

  /**
   * Invoked after the driver of the database changed.
   * 
   * @param database The database whose driver changed.
   * @param driver The new driver of the database.
   */
  void changedDriver(IDatabase database, String driver);

  /**
   * Invoked after the host of the database changed.
   * 
   * @param database The database whose host changed.
   * @param host The new host of the database.
   */
  void changedHost(IDatabase database, String host);

  /**
   * Invoked after the identity of the current user has changed.
   * 
   * @param database The database where the identity changed.
   * @param identity The new identity of the user.
   */
  void changedIdentity(IDatabase database, String identity);

  /**
   * Invoked after the name of the database changed.
   * 
   * @param database The database whose name changed.
   * @param name The new name of the database.
   */
  void changedName(IDatabase database, String name);

  /**
   * Invoked after the password of the database changed.
   * 
   * @param database The database whose password changed.
   * @param password The new password of the database.
   */
  void changedPassword(IDatabase database, String password);

  /**
   * Invoked after the raw modules of a database were reloaded.
   * 
   * @param database Database whose raw modules were reloaded.
   * @param oldModules The previously loaded raw module.
   * @param newModules The now loaded raw modules.
   */
  void changedRawModules(IDatabase database, List<INaviRawModule> oldModules,
      List<INaviRawModule> newModules);

  /**
   * Invoked after the password saving state of the database changed.
   * 
   * @param database The database whose password saving state changed.
   * @param savePassword The new password saving state.
   */
  void changedSavePassword(IDatabase database, boolean savePassword);

  /**
   * Invoked after the user of the database changed.
   * 
   * @param database The database whose user changed.
   * @param user The new user of the database.
   */
  void changedUser(IDatabase database, String user);

  /**
   * Invoked after a database was closed.
   * 
   * @param database Database that was closed.
   */
  void closedDatabase(IDatabase database);

  /**
   * Invoked right before closing a database.
   * 
   * @param database The database to be closed.
   * 
   * @return True, to allow closing. False, to veto it.
   */
  boolean closingDatabase(IDatabase database);

  /**
   * Invoked after a module was deleted from the database.
   * 
   * @param database The database from which the module was deleted.
   * @param module The module that was deleted.
   */
  void deletedModule(IDatabase database, INaviModule module);

  /**
   * Invoked after a project was deleted from the database.
   * 
   * @param database The database from which the project was deleted.
   * @param project The project that was deleted.
   */
  void deletedProject(IDatabase database, INaviProject project);

  /**
   * Invoked after a raw module was deleted from the database.
   * 
   * @param database The database from which the raw module was deleted.
   * @param module The raw module that was deleted.
   */
  void deletedRawModule(IDatabase database, INaviRawModule module);

  /**
   * Invoked after the database was loaded.
   * 
   * @param database The database that was loaded.
   */
  void loadedDatabase(IDatabase database);

  /**
   * Invoked before a new load action starts.
   * 
   * @param event The load action that is about to start.
   * @param counter Number of the event.
   * 
   * @return True, to continue loading. False, to cancel loading.
   */
  boolean loading(LoadEvents event, int counter);

  /**
   * Invoked after the database was opened.
   * 
   * @param database The database that was opened.
   */
  void openedDatabase(IDatabase database);
}
