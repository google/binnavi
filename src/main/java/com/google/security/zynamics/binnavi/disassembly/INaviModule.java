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
import com.google.security.zynamics.binnavi.disassembly.Modules.IModuleListener;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IModule;

import java.util.List;

/**
 * Interface that represents modules.
 */
public interface INaviModule
extends IModule<INaviView, INaviFunction>, IDatabaseObject, IStaredItem {
  /**
   * Adds a listener to the module object that is notified when information about the module
   * changed.
   *
   * @param listener The listener to add.
   *
   * @throws IllegalArgumentException Thrown if the passed listener is null.
   * @throws IllegalArgumentException Thrown if the listener was already added before.
   */
  void addListener(final IModuleListener listener);

  /**
   * Closes the module.
   *
   * @return True, if the module was closed. False, otherwise.
   */
  boolean close();

  /**
   * Creates a new instruction in the module.
   *
   * @param address The address of the module.
   * @param mnemonic The mnemonic of the instruction.
   * @param operands The operands of the instruction.
   * @param data The binary data of the instruction.
   * @param architecture The architecture of the instruction.
   *
   * @return The created instruction.
   */
  INaviInstruction createInstruction(final IAddress address, final String mnemonic,
      final List<COperandTree> operands, final byte[] data, String architecture);

  /**
   * Creates a new operand in the module.
   *
   * @param node Root node of the operand.
   *
   * @return The created operand.
   */
  COperandTree createOperand(COperandTreeNode node);

  /**
   * Creates a new operand expression in the module.
   *
   * @param value Value of the operand expression.
   * @param expressionType Type of the operand expression.
   *
   * @return The created operand tree node.
   */
  COperandTreeNode createOperandExpression(String value, ExpressionType expressionType);

  /**
   * Returns the module configuration.
   *
   * @return The module configuration.
   */
  @Override
  INaviModuleConfiguration getConfiguration();

  /**
   * Returns the loaded module content.
   *
   * @return The module content.
   */
  @Override
  INaviModuleContent getContent();

  /**
   * Returns the number of custom non-native views in the module.
   *
   * @return The number of custom non-native views in the module.
   */
  int getCustomViewCount();

  /**
   * Returns the binary data of the module.
   *
   * @return The binary data of the module.
   */
  byte[] getData();

  /**
   * Returns the number of functions in this module. This number equals the number of native Flow
   * graph views.
   *
   * @return The number of functions in this module.
   */
  int getFunctionCount();

  TypeManager getTypeManager();

  /**
   * Returns the total number of views (both native and non-native) in the module.
   *
   * @return The total number of views in the module.
   */
  int getViewCount();

  /**
   * Returns all views of the module that contain the given addresses.
   *
   * @param addresses The addresses to search for.
   * @param all True, to return views that contain all addresses. False, to return views that
   *        contain any address.
   *
   * @return The views with the given addresses.
   *
   * @throws CouldntLoadDataException Thrown if the views could not be determined.
   */
  List<INaviView> getViewsWithAddresses(
      List<UnrelocatedAddress> addresses, boolean all) throws CouldntLoadDataException;

  /**
   * Initializes the raw module.
   *
   * @throws CouldntSaveDataException Thrown if the raw module could not be initialized.
   */
  void initialize() throws CouldntSaveDataException;

  /**
   * Returns whether the module is already initialized.
   *
   * @return True, if the module is initialized. False, otherwise.
   */
  boolean isInitialized();

  /**
   * Returns whether the module is currently being initialized.
   *
   * @return True, if the module is initializing. False, otherwise.
   */
  boolean isInitializing();

  /**
   * Returns a flag that says whether the module was loaded already.
   *
   * @return True, if the module is loaded. False, otherwise.
   */
  boolean isLoaded();

  /**
   * Returns whether the module is currently being loaded from the database.
   *
   * @return True, if is currently being loaded. False, otherwise.
   */
  boolean isLoading();

  /**
   * Loads the information of this module from the database. This includes the native Call graph
   * view and Flow graphs views as well as non-native views and raw data.
   *
   *  This function is guaranteed to be exception-safe. If an exception is thrown during the loading
   * process, the state of the module object remains unchanged.
   *
   * @throws CouldntLoadDataException Thrown if the module data could not be loaded from the
   *         database.
   * @throws LoadCancelledException Thrown if the user cancelled the load operation.
   */
  void load() throws CouldntLoadDataException, LoadCancelledException;

  /**
   * Loads the binary data of the module from the database.
   *
   * @throws CouldntLoadDataException Thrown if the binary data could not be loaded.
   */
  void loadData() throws CouldntLoadDataException;

  /**
   * Reads a single module setting from the database.
   *
   * @param key Key of the setting to read.
   *
   * @return Value of the setting.
   *
   * @throws CouldntLoadDataException Thrown if the setting value could not be loaded.
   */
  String readSetting(String key) throws CouldntLoadDataException;

  /**
   * Removes a module listener from the module object.
   *
   * @param listener The listener to remove.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalArgumentException Thrown if the listener is not listening on the module object.
   */
  void removeListener(IModuleListener listener);

  /**
   * Saves the binary of the module to the database.
   *
   * @throws CouldntSaveDataException Thrown if the binary data could not be saved.
   */
  void saveData() throws CouldntSaveDataException;

  /**
   * Changes the binary data of the module.
   *
   * @param data The new binary data.
   */
  void setData(byte[] data);

  /**
   * Sets the initialization state to initialized.
   */
  void setInitialized();

  /**
   * Writes a module setting to the database.
   *
   * @param key Key of the setting to write.
   * @param value Value of the setting to write.
   *
   * @throws CouldntSaveDataException Thrown if the setting could not be saved.
   */
  void writeSetting(String key, String value) throws CouldntSaveDataException;
}
