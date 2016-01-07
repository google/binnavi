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

import java.util.Date;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IModuleConfiguration;



/**
 * Interface for all classes that want to provide module configuration.
 */
public interface INaviModuleConfiguration extends IModuleConfiguration {
  /**
   * Returns the date when the module was added to the database.
   * 
   * @return The data when the module was added to the database. This value is guaranteed to be
   *         non-null.
   */
  Date getCreationDate();

  /**
   * Returns the debugger object that can be used to debug the module. There is not necessarily a
   * debugger configured for a module, so this object can be null.
   * 
   * @return The debugger object of the address space.
   */
  IDebugger getDebugger();

  /**
   * Returns the last debugger template that was used to create a debugger object. There is not
   * necessarily a debugger object configured for a module, so this object can be null.
   * 
   * @return The last debugger template that was used to create a debugger for the module.
   */
  DebuggerTemplate getDebuggerTemplate();

  /**
   * Returns the comment that is associated with the module.
   * 
   * @return The comment associated with the module. This value is guaranteed to be non-null.
   */
  String getDescription();

  /**
   * Returns the file base of the module.
   * 
   * @return The file base of the module.
   */
  @Override
  IAddress getFileBase();

  /**
   * Returns the ID of the module as it can be found in the database.
   * 
   * @return The ID of the module.
   */
  int getId();

  /**
   * Returns the image base of the module.
   * 
   * @return The image base of the module.
   */
  IAddress getImageBase();

  /**
   * Returns the MD5 hash of the module's input file.
   * 
   * @return The MD5 hash of the module's input file. This value is guaranteed to be non-null.
   */
  String getMD5();

  /**
   * Returns the modification date of the module.
   * 
   * @return The modification date of the module.
   */
  Date getModificationDate();

  /**
   * Returns the name of the module.
   * 
   * @return The name of the module.
   */
  @Override
  String getName();

  /**
   * Returns the raw module that backs the module.
   * 
   * @return The raw module that backs the module or null.
   */
  INaviRawModule getRawModule();

  /**
   * Returns the SHA1 hash of the module's input file.
   * 
   * @return The SHA1 hash of the module's input file. This value is guaranteed to be non-null.
   */
  String getSha1();

  /**
   * Returns whether the module is stared or not.
   * 
   * @return True, if the module is stared. False, otherwise.
   */
  boolean isStared();

  /**
   * Changes the debugger template of the module.
   * 
   * @param template The new debugger template of the module.
   * @throws CouldntSaveDataException Thrown if the new debugger template could not be saved.
   */
  void setDebuggerTemplate(final DebuggerTemplate template) throws CouldntSaveDataException;

  /**
   * Changes the description of the module.
   * 
   * @param description The new module description.
   * 
   * @throws CouldntSaveDataException Thrown if the new description could not be saved.
   * @throws IllegalArgumentException Thrown if the new module description is null.
   */
  void setDescription(final String description) throws CouldntSaveDataException;

  /**
   * Sets the file base of the module.
   * 
   * @param fileBase The new file base of the module.
   * 
   * @throws CouldntSaveDataException Thrown if the file base could not be saved to the database.
   */
  void setFileBase(final IAddress fileBase) throws CouldntSaveDataException;

  /**
   * Sets the image base of the module.
   * 
   * @param imageBase The new image base of the module.
   * 
   * @throws CouldntSaveDataException Thrown if the image base could not be saved to the database.
   */
  void setImageBase(final IAddress imageBase) throws CouldntSaveDataException;

  /**
   * Changes the name of the module.
   * 
   * @param name The new module name.
   * 
   * @throws CouldntSaveDataException Thrown if the new name could not be saved.
   * @throws IllegalArgumentException Thrown if the new module name is null.
   */
  void setName(final String name) throws CouldntSaveDataException;

  /**
   * Changes the star state of a module.
   * 
   * @param stared The new star state.
   * 
   * @throws CouldntSaveDataException Thrown if the star state could not saved.
   */
  void setStared(boolean stared) throws CouldntSaveDataException;

  /**
   * Updates the modification date of the module.
   */
  void updateModificationDate();
}
