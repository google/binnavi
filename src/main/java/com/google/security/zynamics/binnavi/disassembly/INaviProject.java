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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

import java.util.List;

/**
 * Interface to be implemented by all classes that want to serve as BinNavi projects.
 */
public interface INaviProject extends IDatabaseObject {
  /**
   * Adds a listener that is notified about changes in the project.
   *
   * @param listener The listener object to add.
   */
  void addListener(IProjectListener listener);

  /**
   * Closes the project.
   *
   * @return True, if the project was closed. False, if the close operation was vetoed.
   */
  boolean close();

  /**
   * Returns the number of address spaces in the project.
   *
   * @return The number of address spaces in the project.
   */
  int getAddressSpaceCount();

  /**
   * Returns the project configuration object.
   *
   * @return The project configuration object.
   */
  CProjectConfiguration getConfiguration();

  /**
   * Returns the project content object.
   *
   * @return The project content object.
   */
  CProjectContent getContent();

  /**
   * Returns all views of the project that contain given addresses.
   *
   * @param addresses The list of addresses to search for.
   * @param all If true, views that contain all addresses from the list are returnd. If false, views
   *        that contain any of the addresses from the list are returned.
   *
   * @return A list of views that contain the given addresses.
   *
   * @throws CouldntLoadDataException
   */
  List<INaviView> getViewsWithAddresses(List<UnrelocatedAddress> addresses, boolean all)
      throws CouldntLoadDataException;

  /**
   * Returns whether the project was already loaded.
   *
   * @return True, if the project was loaded. False, otherwise.
   */
  boolean isLoaded();

  /**
   * Returns whether the project is currently being loaded from the database.
   *
   * @return True, if is currently being loaded. False, otherwise.
   */
  boolean isLoading();

  /**
   * Loads the whole project. This includes the recursive loading of all address spaces and modules
   * in the project.
   *
   *  This function is guaranteed to be exception-safe. Should any part of the loading process go
   * wrong, the state of the project object is not modified.
   *
   * @throws CouldntLoadDataException Thrown if the project could not be loaded.
   * @throws LoadCancelledException Thrown if the user cancelled project loading.
   */
  void load() throws CouldntLoadDataException, LoadCancelledException;

  /**
   * Reads a project setting from the database.
   *
   * @param key The key of the setting.
   *
   * @return The value of the setting.
   *
   * @throws CouldntLoadDataException Thrown if the setting could not be loaded.
   */
  String readSetting(String key) throws CouldntLoadDataException;

  /**
   * Removes a project listener object from the list of listeners that are notified about changes in
   * the project.
   *
   * @param listener The listener object to remove.
   */
  void removeListener(IProjectListener listener);

  /**
   * Writes a project setting to the database.
   *
   * @param key The key of the setting to write.
   * @param value The value of the setting to write.
   *
   * @throws CouldntSaveDataException Thrown if the setting could not be written to the database.
   */
  void writeSetting(String key, String value) throws CouldntSaveDataException;
}
