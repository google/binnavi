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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;


/**
 * Interface for all objects that want to be database content objects.
 */
public interface IDatabaseContent {
  /**
   * Adds a new project to the database.
   * 
   * This function is guaranteed to be thread-safe. If the new project could not be saved to the
   * database, the state of the database object remains unchanged.
   * 
   * @param name The name of the new project.
   * 
   * @return The added project.
   * 
   * @throws IllegalArgumentException Thrown if the name of the new project is null.
   * @throws CouldntSaveDataException Thrown if the new project could not be saved to the database.
   */
  INaviProject addProject(String name) throws CouldntSaveDataException;

  /**
   * Removes a module from the database. All references to that module (for example from address
   * spaces that use this module) are deleted too.
   * 
   * @param module The module to delete.
   * 
   * @throws CouldntDeleteException Thrown if the module could not be deleted.
   */
  void delete(INaviModule module) throws CouldntDeleteException;

  /**
   * Removes a project from the databases. All references to that project are deleted too.
   * 
   * @param project The project to remove from the database.
   * 
   * @throws CouldntDeleteException Thrown if the project could not be deleted.
   */
  void delete(INaviProject project) throws CouldntDeleteException;

  /**
   * Removes a raw module from the database.
   * 
   * @param rawModule the raw module to delete.
   * 
   * @throws CouldntDeleteException Thrown if the raw module could not be deleted.
   */
  void delete(INaviRawModule rawModule) throws CouldntDeleteException;

  /**
   * Returns the debugger template manager of the database.
   * 
   * @return The debugger template manager of the database.
   */
  DebuggerTemplateManager getDebuggerTemplateManager();

  /**
   * Returns the module with the given ID.
   * 
   * @param moduleId The module ID to search for.
   * 
   * @return The module with the given ID.
   */
  INaviModule getModule(int moduleId);

  /**
   * Returns the list of modules that are stored in the database.
   * 
   * Note that the database must be loaded before this function is used.
   * 
   * @return The list of modules from the database.
   * 
   * @throws IllegalStateException Thrown if this function is called without loading the database
   *         before.
   */
  List<INaviModule> getModules();

  /**
   * Returns the tag manager used for node tagging.
   * 
   * @return The tag manager used for node tagging.
   */
  ITagManager getNodeTagManager();

  /**
   * Returns the list of projects that are stored in the database.
   * 
   * Note that the database must be loaded before this function is used.
   * 
   * @return The list of projects from the database.
   * 
   * @throws IllegalStateException Thrown if this function is called without loading the database
   *         before.
   */
  List<INaviProject> getProjects();

  /**
   * Returns the list of raw modules that are stored in the database.
   * 
   * Note that the database must be loaded before this function is used.
   * 
   * @return The list of raw modules from the database.
   * 
   * @throws IllegalStateException Thrown if this function is called without loading the database
   *         before.
   */
  List<INaviRawModule> getRawModules();

  /**
   * Returns the tag manager used for view tagging.
   * 
   * @return The tag manager used for view tagging.
   */
  ITagManager getViewTagManager();

  /**
   * Reloads the raw modules information from the database.
   * 
   * @throws CouldntLoadDataException Thrown if the data could not be loaded.
   */
  void refreshRawModules() throws CouldntLoadDataException;
}
