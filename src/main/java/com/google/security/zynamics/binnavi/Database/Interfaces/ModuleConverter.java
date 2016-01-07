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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleInitializeReporter;

/**
 * Interface that must be implemented by classes that want to convert a raw module in a database
 * into a BinNavi module.
 */
public interface ModuleConverter {
  /**
   * Creates a module from a raw module.
   * 
   * @param provider The SQL provider used to access the database.
   * @param rawModule The raw module that is converted into a BinNavi module.
   * 
   * @return The created BinNavi module.
   * 
   * @throws CouldntLoadDataException Thrown if the converted module could not be loaded.
   * @throws CouldntSaveDataException Thrown if the raw module could not be converted.
   */
  CModule createModule(SQLProvider provider, INaviRawModule rawModule)
      throws CouldntLoadDataException, CouldntSaveDataException;

  /**
   * Initializes a module.
   * 
   * @param provider The SQL provider used to access the database.
   * @param module The module to initialize.
   * @param reporter Reports progress.
   * 
   * @throws CouldntSaveDataException Thrown if the module could not be initialized.
   */
  void initializeModule(SQLProvider provider, INaviModule module,
      CModuleInitializeReporter reporter) throws CouldntSaveDataException;
}
